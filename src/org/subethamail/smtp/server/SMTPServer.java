package org.subethamail.smtp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.Version;

/**
 * Main SMTPServer class.  Construct this object, set the
 * hostName, port, and bind address if you wish to override the
 * defaults, and call start().
 *
 * This class starts opens a ServerSocket and creates a new
 * instance of the ConnectionHandler class when a new connection
 * comes in.  The ConnectionHandler then parses the incoming SMTP
 * stream and hands off the processing to the CommandHandler which
 * will execute the appropriate SMTP command class.
 *
 * This class also manages a watchdog thread which will timeout
 * stale connections.
 *
 * To use this class, construct a server with your implementation
 * of the MessageHandlerFactory.  This provides low-level callbacks
 * at various phases of the SMTP exchange.  For a higher-level
 * but more limited interface, you can pass in a
 * org.subethamail.smtp.helper.SimpleMessageListenerAdapter.
 *
 * By default, no authentication methods are offered.  To use
 * authentication, set an AuthenticationHandlerFactory.
 *
 * @author Jon Stevens
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jeff Schnitzer
 */
public class SMTPServer implements Runnable
{
	private final static Logger log = LoggerFactory.getLogger(SMTPServer.class);

	/** Hostname used if we can't find one */
	private final static String UNKNOWN_HOSTNAME = "localhost";

	private InetAddress bindAddress = null;	// default to all interfaces
	private int port = 25;	// default to 25
	private String hostName;	// defaults to a lookup of the local address
	private int backlog = 50;
	private String softwareName = "SubEthaSMTP " + Version.getSpecification();

	private MessageHandlerFactory messageHandlerFactory;
	private AuthenticationHandlerFactory authenticationHandlerFactory;

	private CommandHandler commandHandler;

	private ServerSocket serverSocket;
	private boolean shuttingDown;

	private Thread serverThread;

	private ThreadGroup sessionGroup;

	/** 
	 * TLS
	 *  By default, TLS is supported (and announced) but not required.
	 *  To override default behavior, only one of these need be specified:
	 *   To disable TLS, set disableTLS=true; 
	 *   To allow TLS but not announce it, set hideTLS=true; 
	 *   To require TLS, set requireTLS=true.
	 */
	/** If true, TLS is disabled */
	private boolean disableTLS = false;
	/** If true, TLS is not announced; implied if disableTLS=true */
	private boolean hideTLS = false;
	/** If true, a TLS handshake is required; ignored if disableTLS=true */
	private boolean requireTLS = false;
	/** If true, no Received headers will be inserted */
	private boolean disableReceivedHeaders = false;

	/**
	 * set a hard limit on the maximum number of connections this server will accept
	 * once we reach this limit, the server will gracefully reject new connections.
	 * Default is 1000.
	 */
	private int maxConnections = 1000;

	/**
	 * The timeout for waiting for data on a connection is one minute: 1000 * 60 * 1
	 */
	private int connectionTimeout = 1000 * 60 * 1;

	/**
	 * The maximal number of recipients that this server accepts per message delivery request.
	 */
	private int maxRecipients = 1000;

	/**
	 * The maximum size of a message that the server will accept. This value is advertised
	 * during the EHLO phase if it is larger than 0. If the message size specified by the client
	 * during the MAIL phase, the message will be rejected at that time. (RFC 1870)
	 * Default is 0.  Note this doesn't actually enforce any limits on the message being
	 * read; you must do that yourself when reading data.
	 */
	private int maxMessageSize = 0;

	/**
	 * The primary constructor.
	 */
	public SMTPServer(MessageHandlerFactory handlerFactory)
	{
		this(handlerFactory, null);
	}

	/**
	 * The primary constructor.
	 */
	public SMTPServer(MessageHandlerFactory msgHandlerFact, AuthenticationHandlerFactory authHandlerFact)
	{
		this.messageHandlerFactory = msgHandlerFact;
		this.authenticationHandlerFactory = authHandlerFact;

		try
		{
			this.hostName = InetAddress.getLocalHost().getCanonicalHostName();
		}
		catch (UnknownHostException e)
		{
			this.hostName = UNKNOWN_HOSTNAME;
		}

		this.commandHandler = new CommandHandler();

		this.sessionGroup = new ThreadGroup(SMTPServer.class.getName() + " Session Group");
	}

	/** @return the host name that will be reported to SMTP clients */
	public String getHostName()
	{
		if (this.hostName == null)
			return UNKNOWN_HOSTNAME;
		else
			return this.hostName;
	}

	/** The host name that will be reported to SMTP clients */
	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	/** null means all interfaces */
	public InetAddress getBindAddress()
	{
		return this.bindAddress;
	}

	/** null means all interfaces */
	public void setBindAddress(InetAddress bindAddress)
	{
		this.bindAddress = bindAddress;
	}

	/** */
	public int getPort()
	{
		return this.port;
	}

	/** */
	public void setPort(int port)
	{
		this.port = port;
	}

	/** 
	 * The string reported to the public as the software running here.  Defaults
	 * to SubEthaSTP and the version number. 
	 */
	public String getSoftwareName()
	{
		return this.softwareName;
	}

	/** 
	 * Changes the publicly reported software information. 
	 */
	public void setSoftwareName(String value)
	{
		this.softwareName = value;
	}

	/**
	 * Is the server running after start() has been called?
	 */
	public boolean isRunning()
	{
		return this.serverThread != null;
	}

	/**
	 * The backlog is the Socket backlog.
	 *
	 * The backlog argument must be a positive value greater than 0.
	 * If the value passed if equal or less than 0, then the default value will be assumed.
	 *
	 * @return the backlog
	 */
	public int getBacklog()
	{
		return this.backlog;
	}

	/**
	 * The backlog is the Socket backlog.
	 *
	 * The backlog argument must be a positive value greater than 0.
	 * If the value passed if equal or less than 0, then the default value will be assumed.
	 */
	public void setBacklog(int backlog)
	{
		this.backlog = backlog;
	}

	/**
	 * Call this method to get things rolling after instantiating the
	 * SMTPServer.
	 */
	public synchronized void start()
	{
		if (log.isInfoEnabled())
			log.info("SMTP server {} starting", getDisplayableLocalSocketAddress());

		if (this.serverThread != null)
			throw new IllegalStateException("SMTPServer already started");

		// Create our server socket here.
		try
		{
			this.serverSocket = this.createServerSocket();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		this.serverThread = new Thread(this, SMTPServer.class.getName());

		this.shuttingDown = false;

		// Now this.run() will be called
		this.serverThread.start();
	}

	/**
	 * Shut things down gracefully.
	 */
	public synchronized void stop()
	{
		if (log.isInfoEnabled())
			log.info("SMTP server {} stopping", getDisplayableLocalSocketAddress());

		// First make sure we aren't accepting any new connections
		this.stopServerThread();

		// Shut down any open connections.
		this.stopAllSessions();
	}

	/**
	 * Grabs all instances of Sessions and attempts to close the
	 * socket if it is still open.  Note this must be called after
	 * the main server socket is shut down, otherwise new sessions
	 * could be created while we are shutting them down.
	 */
	protected void stopAllSessions()
	{
		Thread[] groupThreads = new Thread[this.getSessionGroup().activeCount()];

		this.getSessionGroup().enumerate(groupThreads);
		for (Thread thread : groupThreads)
		{
			if (thread instanceof Session)
			{
				Session handler = (Session)thread;
				handler.quit();
			}
		}
	}

	/**
	 * Override this method if you want to create your own server sockets.
	 * You must return a bound ServerSocket instance
	 *
	 * @throws IOException
	 */
	protected ServerSocket createServerSocket() throws IOException
	{
		InetSocketAddress isa;

		if (this.bindAddress == null)
		{
			isa = new InetSocketAddress(this.port);
		}
		else
		{
			isa = new InetSocketAddress(this.bindAddress, this.port);
		}

		ServerSocket serverSocket = new ServerSocket();
		// http://java.sun.com/j2se/1.5.0/docs/api/java/net/ServerSocket.html#setReuseAddress(boolean)
		serverSocket.setReuseAddress(true);
		serverSocket.bind(isa, this.backlog);

		if (this.port == 0)
		{
			this.port = serverSocket.getLocalPort();
		}

		return serverSocket;
	}

	/**
	 * Closes the serverSocket in an orderly way
	 */
	protected void closeServerSocket()
	{
		try
		{
			if ((this.serverSocket != null) && !this.serverSocket.isClosed())
				this.serverSocket.close();

			log.debug("SMTP Server socket shut down");
		}
		catch (IOException e)
		{
			log.error("Failed to close server socket.", e);
		}

		this.serverSocket = null;
	}

	/**
	 * Create a SSL socket that wraps the existing socket. This method
	 * is called after the client issued the STARTTLS command.
	 * <p>
	 * Subclasses may override this method to configure the key stores, enabled protocols/
	 * cipher suites, enforce client authentication, etc.
	 *
	 * @param socket the existing socket as created by {@link #createServerSocket()} (not null)
	 * @return a SSLSocket
	 * @throws IOException when creating the socket failed
	 */
	public SSLSocket createSSLSocket(Socket socket) throws IOException
	{
		SSLSocketFactory sf = ((SSLSocketFactory) SSLSocketFactory.getDefault());
		InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
		SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(), socket.getPort(), true));

		// we are a server
		s.setUseClientMode(false);

		// allow all supported cipher suites
		s.setEnabledCipherSuites(s.getSupportedCipherSuites());

		return s;
	}

	/**
	 * Shuts down the server thread and the associated server socket in an orderly fashion.
	 */
	protected void stopServerThread()
	{
		this.shuttingDown = true;
		this.closeServerSocket();

		this.serverThread = null;
	}

	/**
	 * This method is called by this thread when it starts up.  To safely cause this to
	 * exit, call stopServerThread().
	 */
	public void run()
	{
		if (log.isInfoEnabled())
		{
			MDC.put("smtpServerLocalSocketAddress", getDisplayableLocalSocketAddress());
			log.info("SMTP server {} started", getDisplayableLocalSocketAddress());
		}
		
		while (!this.shuttingDown)
		{
			// This deals with a race condition; a start followed by a quick stop
			// that clears the serversocket in the key moments between the while loop
			// check and the accept() call.  In that case, the socket will be invalid,
			// but the accept() will throw an exception and we will be fine.
			ServerSocket server;
			synchronized(this) { server = this.serverSocket; }

			if (server != null)
			{
				try
				{
					Session sess = new Session(this, server.accept());
					sess.start();
				}
				catch (IOException ioe)
				{
					if (!this.shuttingDown)
						log.error("Error accepting connections", ioe);
				}
			}
		}

		// Normally we get here because we're shutting down and we've close()ed the
		// serverSocket.  If some other IOException brought us here, let's make sure
		// thing is shut down properly.
		this.closeServerSocket();

		this.serverSocket = null;
		this.serverThread = null;

		if (log.isInfoEnabled())
		{
			log.info("SMTP server {} stopped", getDisplayableLocalSocketAddress());
			MDC.remove("smtpServerLocalSocketAddress");
		}
	}

	private String getDisplayableLocalSocketAddress()
	{
		return (this.bindAddress == null ? "*" : this.bindAddress) + ":" + this.port;
	}

	/**
	 * @return the factory for message handlers, cannot be null
	 */
	public MessageHandlerFactory getMessageHandlerFactory()
	{
		return this.messageHandlerFactory;
	}

	/** */
	public void setMessageHandlerFactory(MessageHandlerFactory fact)
	{
		this.messageHandlerFactory = fact;
	}

	/**
	 * @return the factory for auth handlers, or null if no such factory has been set.
	 */
	public AuthenticationHandlerFactory getAuthenticationHandlerFactory()
	{
		return this.authenticationHandlerFactory;
	}

	/** */
	public void setAuthenticationHandlerFactory(AuthenticationHandlerFactory fact)
	{
		this.authenticationHandlerFactory = fact;
	}

	/**
	 * The CommandHandler manages handling the SMTP commands
	 * such as QUIT, MAIL, RCPT, DATA, etc.
	 *
	 * @return An instance of CommandHandler
	 */
	public CommandHandler getCommandHandler()
	{
		return this.commandHandler;
	}

	/** */
	protected ThreadGroup getSessionGroup()
	{
		return this.sessionGroup;
	}

	/** */
	public int getNumberOfConnections()
	{
		return this.sessionGroup.activeCount();
	}

	/** */
	public boolean hasTooManyConnections()
	{
		if (this.maxConnections < 0)
			return false;
		else
			return (this.getNumberOfConnections() >= this.maxConnections);
	}

	/** */
	public int getMaxConnections()
	{
		return this.maxConnections;
	}

	/**
	 * Set's the maximum number of connections this server instance will
	 * accept. A value of -1 means "unlimited".
	 *
	 * @param maxConnections
	 */
	public void setMaxConnections(int maxConnections)
	{
		if (this.isRunning())
			throw new RuntimeException("Server is already running. It isn't possible to set the maxConnections. Please stop the server first.");

		this.maxConnections = maxConnections;
	}

	/** */
	public int getConnectionTimeout()
	{
		return this.connectionTimeout;
	}

	/**
	 * Set the number of milliseconds that the server will wait for
	 * client input.  Sometime after this period expires, an client will
	 * be rejected and the connection closed.
	 */
	public void setConnectionTimeout(int connectionTimeout)
	{
		this.connectionTimeout = connectionTimeout;
	}

	public int getMaxRecipients()
	{
		return this.maxRecipients;
	}

	/**
	 * Set the maximum number of recipients allowed for each message.
	 * A value of -1 means "unlimited".
	 */
	public void setMaxRecipients(int maxRecipients)
	{
		this.maxRecipients = maxRecipients;
	}

	/** */
	public boolean getDisableTLS()
	{
		return this.disableTLS;
	}

	/**
	 * If set to true, TLS will not be announced or supported.
	 * Default is false.
	 */
	public void setDisableTLS(boolean value)
	{
		this.disableTLS = value;
	}

	/** */
	public boolean getHideTLS()
	{
		return this.hideTLS;
	}

	/**
	 * If set to true, TLS will not be advertised in the EHLO string.
	 * Default is false; true implied when disableTLS=true.
	 */
	public void setHideTLS(boolean value)
	{
		this.hideTLS = value;
	}

	/** */
	public boolean getRequireTLS()
	{
		return this.requireTLS;
	}

	/**
	 * @param requireTLS true to require a TLS handshake, 
	 *   false to allow operation with or without TLS.
	 *   Default is false; ignored when disableTLS=true.
	 */
	public void setRequireTLS(boolean requireTLS)
	{
		this.requireTLS = requireTLS;
	}

	/**
	 * @return the maxMessageSize
	 */
	public int getMaxMessageSize()
	{
		return maxMessageSize;
	}

	/** */
	public boolean getDisableReceivedHeaders()
	{
		return disableReceivedHeaders;
	}

	/**
	 * @param disableReceivedHeaders
	 *            false to include Received headers. Default is false.
	 */
	public void setDisableReceivedHeaders(boolean disableReceivedHeaders)
	{
		this.disableReceivedHeaders = disableReceivedHeaders;
	}

	/**
	 * @param maxMessageSize the maxMessageSize to set
	 */
	public void setMaxMessageSize(int maxMessageSize)
	{
		this.maxMessageSize = maxMessageSize;
	}

}
