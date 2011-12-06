package org.subethamail.smtp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.concurrent.GuardedBy;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SMTPServer
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
	private ExecutorService executorService;

	private CommandHandler commandHandler;

	/** The thread listening on the server socket. */
	@GuardedBy("this")
	private ServerThread serverThread;

	/**
	 * True if this SMTPServer was started. It remains true even if the
	 * SMTPServer has been stopped since.
	 **/
	@GuardedBy("this")
	private boolean started = false;

	/** If true, TLS is enabled */
	private boolean enableTLS = false;
	/** If true, TLS is not announced; ignored if enableTLS=false */
	private boolean hideTLS = false;
	/** If true, a TLS handshake is required; ignored if enableTLS=false */
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
	 * Simple constructor.
	 */
	public SMTPServer(MessageHandlerFactory handlerFactory)
	{
		this(handlerFactory, null, null);
	}

	/**
	 * Constructor with {@link AuthenticationHandlerFactory}.
	 */
	public SMTPServer(MessageHandlerFactory handlerFactory, AuthenticationHandlerFactory authHandlerFact)
	{
	    this(handlerFactory, authHandlerFact, null);
	}

	/**
	 * Complex constructor.
	 * 
	 * @param authHandlerFact
	 *            the {@link AuthenticationHandlerFactory} which performs
	 *            authentication in the SMTP AUTH command. If null,
	 *            authentication is not supported. Note that setting an
	 *            authentication handler does not enforce authentication, it
	 *            only makes authentication possible. Enforcing authentication
	 *            is the responsibility of the client application, which usually
	 *            enforces it only selectively. Use
	 *            {@link Session#isAuthenticated} to check whether the client
	 *            was authenticated in the session.
	 * @param executorService
	 *            the ExecutorService which will handle client connections, one
	 *            task per connection. The SMTPServer will shut down this
	 *            ExecutorService when the SMTPServer itself stops. If null, a
	 *            default one is created by {@link
	 *            Executors.newCachedThreadPool()}.
	 */
	public SMTPServer(MessageHandlerFactory msgHandlerFact, AuthenticationHandlerFactory authHandlerFact, ExecutorService executorService)
	{
		this.messageHandlerFactory = msgHandlerFact;
		this.authenticationHandlerFactory = authHandlerFact;

		if (executorService != null) {
			this.executorService = executorService;
		} else {
			this.executorService = Executors.newCachedThreadPool();
		}

		try
		{
			this.hostName = InetAddress.getLocalHost().getCanonicalHostName();
		}
		catch (UnknownHostException e)
		{
			this.hostName = UNKNOWN_HOSTNAME;
		}

		this.commandHandler = new CommandHandler();
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
	 * @return the ExecutorService handling client connections
	 */
	public ExecutorService getExecutorService() {
		return executorService;
	}

	/**
	 * Is the server running after start() has been called?
	 */
	public synchronized boolean isRunning()
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

		if (this.started)
			throw new IllegalStateException(
					"SMTPServer can only be started once");

		// Create our server socket here.
		ServerSocket serverSocket;
		try
		{
			serverSocket = this.createServerSocket();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		this.serverThread = new ServerThread(this, serverSocket);
		this.serverThread.start();
		this.started = true;
	}

	/**
	 * Shut things down gracefully.
	 */
	public synchronized void stop()
	{
		if (log.isInfoEnabled())
			log.info("SMTP server {} stopping", getDisplayableLocalSocketAddress());
		if (this.serverThread == null)
			return;

		this.serverThread.shutdown();
		this.serverThread = null;
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
		serverSocket.bind(isa, this.backlog);

		if (this.port == 0)
		{
			this.port = serverSocket.getLocalPort();
		}

		return serverSocket;
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

	public String getDisplayableLocalSocketAddress()
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
	public int getMaxConnections()
	{
		return this.maxConnections;
	}

	/**
	 * Set's the maximum number of connections this server instance will accept.
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

	/**
	 * If set to true, TLS will be supported.
	 * <p>
	 * The minimal JSSE configuration necessary for a working TLS support on
	 * Oracle JRE 6:
	 * <ul>
	 * <li>javax.net.ssl.keyStore system property must refer to a file
	 * containing a JKS keystore with the private key.
	 * <li>javax.net.ssl.keyStorePassword system property must specify the
	 * keystore password.
	 * </ul>
	 * <p>
	 * Up to SubEthaSMTP 3.1.5 the default was true, i.e. TLS was enabled.
	 * 
	 * @see <a
	 *      href="http://blog.jteam.nl/2009/11/10/securing-connections-with-tls/">Securing
	 *      Connections with TLS</a>
	 */
	public void setEnableTLS(boolean enableTLS)
	{
		this.enableTLS = enableTLS;
	}

	/** */
	public boolean getEnableTLS()
	{
		return enableTLS;
	}

	/**
	 * @deprecated use {@link #enableTLS}
	 */
	@Deprecated
	public boolean getDisableTLS()
	{
		return !this.enableTLS;
	}

	/**
	 * @deprecated use {@link #setEnableTLS(boolean)}
	 */
	@Deprecated
	public void setDisableTLS(boolean value)
	{
		this.enableTLS = !value;
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

	/**
	 * @param maxMessageSize
	 *            the maxMessageSize to set
	 */
	public void setMaxMessageSize(int maxMessageSize)
	{
		this.maxMessageSize = maxMessageSize;
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

}
