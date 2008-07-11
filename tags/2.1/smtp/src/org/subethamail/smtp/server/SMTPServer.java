package org.subethamail.smtp.server;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.integration.jmx.IoServiceManager;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.Version;

/**
 * Main SMTPServer class.  Construct this object, set the
 * hostName, port, and bind address if you wish to override the 
 * defaults, and call start(). 
 * 
 * This class starts opens a <a href="http://mina.apache.org/">Mina</a> 
 * based listener and creates a new
 * instance of the ConnectionHandler class when a new connection
 * comes in.  The ConnectionHandler then parses the incoming SMTP
 * stream and hands off the processing to the CommandHandler which
 * will execute the appropriate SMTP command class.
 *  
 * There are two ways of using this server.  The first is to
 * construct with a MessageHandlerFactory.  This provides the
 * lowest-level and most flexible access.  The second way is
 * to construct with a collection of MessageListeners.  This
 * is a higher, and sometimes more convenient level of abstraction.
 * 
 * In neither case is the SMTP server (this library) responsible
 * for deciding what recipients to accept or what to do with the
 * incoming data.  That is left to you.
 * 
 * @author Jon Stevens
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jeff Schnitzer
 * 
 * This file has been used and differs from the original
 * by the use of MINA NIO framework.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SMTPServer
{
	private static Logger log = LoggerFactory.getLogger(SMTPServer.class);

	public final static Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

	/**
	 * default to all interfaces
	 */
	private InetAddress bindAddress = null;

	/**
	 * default to 25
	 */
	private int port = 25;

	/**
	 * defaults to a lookup of the local address
	 */
	private String hostName;

	/**
	 * defaults to 5000
	 */
	private int backlog = 5000;
	
	private MessageHandlerFactory messageHandlerFactory;
	private CommandHandler commandHandler;
	private SocketAcceptor acceptor;
	private ExecutorService executor;
	private ExecutorService acceptorThreadPool;
	private SocketAcceptorConfig config;
	private IoServiceManager serviceManager;
	private ObjectName jmxName;
	private ConnectionHandler handler;
	private SMTPCodecDecoder codecDecoder;
	private boolean go = false;

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
	 * 4 megs by default. The server will buffer incoming messages to disk
	 * when they hit this limit in the DATA received.
	 */
	protected final static int DEFAULT_DATA_DEFERRED_SIZE = 1024*1024*4;
	
	private int dataDeferredSize = DEFAULT_DATA_DEFERRED_SIZE;
	
	private boolean announceTls = true;

	/**
	 * The primary constructor.
	 */
	public SMTPServer(MessageHandlerFactory handlerFactory)
	{
		this.messageHandlerFactory = handlerFactory;

		try
		{
			this.hostName = InetAddress.getLocalHost().getCanonicalHostName();
		}
		catch (UnknownHostException e)
		{
			this.hostName = "localhost";
		}

		this.commandHandler = new CommandHandler();
		initService();
	}

	/**
	 * A convenience constructor that splits the smtp data among multiple listeners
	 * (and multiple recipients).
	 */
	public SMTPServer(Collection<MessageListener> listeners)
	{
		this(new MessageListenerAdapter(listeners));
	}

	/**
	 * Starts the JMX service with a polling interval default of 1000ms.
	 * 
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws NotCompliantMBeanException
	 */
	public void startJMXService()
		throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
	{
		startJMXService(1000);
	}

	/**
	 * Start the JMX service.
	 * 
	 * @param pollingInterval
	 * @throws InstanceAlreadyExistsException
	 * @throws MBeanRegistrationException
	 * @throws NotCompliantMBeanException
	 */
	public void startJMXService(int pollingInterval)
		throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
	{
		serviceManager.startCollectingStats(pollingInterval);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		mbs.registerMBean(serviceManager, jmxName);
	}

	/**
	 * Stop the JMX service.
	 * 
	 * @throws InstanceNotFoundException
	 * @throws MBeanRegistrationException
	 */
	public void stopJMXService() throws InstanceNotFoundException, MBeanRegistrationException
	{
		serviceManager.stopCollectingStats();
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		mbs.unregisterMBean(jmxName);
	}

	/**
	 * Initializes the runtime service.
	 */
	private void initService()
	{
		try
		{
			ByteBuffer.setUseDirectBuffers(false);
			ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

			acceptorThreadPool = Executors.newCachedThreadPool();
			acceptor =
				new SocketAcceptor(Runtime.getRuntime().availableProcessors() + 1, acceptorThreadPool);

			// JMX instrumentation
			serviceManager = new IoServiceManager(acceptor);
			jmxName = new ObjectName("subethasmtp.mina.server:type=IoServiceManager");

			config = new SocketAcceptorConfig();
			config.setThreadModel(ThreadModel.MANUAL);
			((SocketAcceptorConfig)config).setReuseAddress(true);

			DefaultIoFilterChainBuilder chain = config.getFilterChain();

			if (log.isTraceEnabled())
				chain.addLast("logger", new LoggingFilter());

			SMTPCodecFactory codecFactory = new SMTPCodecFactory(DEFAULT_CHARSET, getDataDeferredSize());
			codecDecoder = (SMTPCodecDecoder) codecFactory.getDecoder();
			chain.addLast("codec", new ProtocolCodecFilter(codecFactory));

			executor = Executors.newCachedThreadPool(new ThreadFactory() {
				int sequence;
				
				public Thread newThread(Runnable r) 
				{					
					sequence += 1;
					return new Thread(r, "SubEthaSMTP Thread "+sequence);
				}			
			});
			
			chain.addLast("threadPool", new ExecutorFilter(executor));
			
			handler = new ConnectionHandler(this);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Call this method to get things rolling after instantiating the
	 * SMTPServer.
	 */
	public synchronized void start()
	{
		if (go == true)
			throw new RuntimeException("SMTPServer is already started.");
		
		InetSocketAddress isa;

		if (this.bindAddress == null)
		{
			isa = new InetSocketAddress(this.port);
		}
		else
		{
			isa = new InetSocketAddress(this.bindAddress, this.port);
		}

		((SocketAcceptorConfig)config).setBacklog(getBacklog());

		try
		{
			acceptor.bind(isa, handler, config);
			go = true;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Shut things down gracefully.
	 */
	public synchronized void stop()
	{
		try
		{
			log.info("SMTP Server socket shut down.");
			try { acceptor.unbindAll(); } catch (Exception e) { }
			try { executor.shutdown(); } catch (Exception e) { }
			try { acceptorThreadPool.shutdown(); } catch (Exception e) { }
		}
		finally
		{
			go = false;
		}
	}

	/**
	 * Tells the server to announce the TLS support. Defaults to true. 
	 */
	public void setAnnounceTLS(boolean announceTls)
	{
		this.announceTls = announceTls;
	}
	
	/**
	 * @return true if server is allowed to announce TLS support.
	 */
	public boolean announceTLS()
	{
		return announceTls;
	}

	/** 
	 * @return the host name that will be reported to SMTP clients 
	 */
	public String getHostName()
	{
		if (this.hostName == null)
			return "localhost";
		else
			return this.hostName;
	}

	/** 
	 * The host name that will be reported to SMTP clients 
	 */
	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	/** null means all interfaces */
	public InetAddress getBindAddress()
	{
		return this.bindAddress;
	}

	/**
	 * null means all interfaces
	 */
	public void setBindAddress(InetAddress bindAddress)
	{
		this.bindAddress = bindAddress;
	}

	/**
	 * get the port the server is running on.
	 */
	public int getPort()
	{
		return this.port;
	}

	/**
	 * set the port the server is running on.
	 * @param port
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * Is the server running after start() has been called?
	 */
	public synchronized boolean isRunning()
	{
		return this.go;
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
	 * The name of the server software.
	 */
	public String getName()
	{
		return "SubEthaSMTP";
	}

	/**
	 * The name + version of the server software.
	 */
	public String getNameVersion()
	{
		return getName() + " " + Version.getSpecification();
	}

	/**
	 * All smtp data is eventually routed through the handlers.
	 */
	public MessageHandlerFactory getMessageHandlerFactory()
	{
		return this.messageHandlerFactory;
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

	/**
	 * Number of connections in the handler.
	 */
	public int getNumberOfConnections()
	{
		return handler.getNumberOfConnections();
	}

	/**
	 * Are we over the maximum amount of connections ?
	 */
	public boolean hasTooManyConnections()
	{
		return (this.maxConnections > -1 && 
				getNumberOfConnections() >= this.maxConnections);
	}

	/**
	 * What is the maximum amount of connections?
	 */
	public int getMaxConnections()
	{
		return this.maxConnections;
	}

	/**
	 * Set's the maximum number of connections this server instance will
	 * accept. If set to -1 then limit is ignored.
	 * 
	 * @param maxConnections
	 */
	public void setMaxConnections(int maxConnections)
	{
		this.maxConnections = maxConnections;
	}

	/**
	 * What is the connection timeout?
	 */
	public int getConnectionTimeout()
	{
		return this.connectionTimeout;
	}

	/**
	 * Set the connection timeout.
	 */
	public void setConnectionTimeout(int connectionTimeout)
	{
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * What is the maximum number of recipients for a single message ?
	 */
	public int getMaxRecipients()
	{
		return this.maxRecipients;
	}

	/**
	 * Set the maximum number of recipients for a single message.
	 * If set to -1 then limit is ignored.
	 */
	public void setMaxRecipients(int maxRecipients)
	{
		this.maxRecipients = maxRecipients;
	}

	/**
	 * Get the maximum size in bytes of a single message before it is 
	 * dumped to a temporary file.
	 */	
	public int getDataDeferredSize() 
	{
		return dataDeferredSize;
	}

	/**
	 * Set the maximum size in bytes of a single message before it is 
	 * dumped to a temporary file. Argument must be a positive power 
	 * of two in order to follow the expanding algorithm of 
	 * {@link org.apache.mina.common.ByteBuffer} to prevent unnecessary
	 * memory consumption.
	 */	
	public void setDataDeferredSize(int dataDeferredSize) 
	{
		if (isPowerOfTwo(dataDeferredSize))
		{
			this.dataDeferredSize = dataDeferredSize;
			if (codecDecoder != null)
				codecDecoder.setDataDeferredSize(dataDeferredSize);
		}
		else
			throw new IllegalArgumentException(
					"Argument dataDeferredSize must be a positive power of two");
	}
	
	/**
	 * Sets the receive buffer size.
	 */
	public void setReceiveBufferSize(int receiveBufferSize)
	{
		handler.setReceiveBufferSize(receiveBufferSize);
	}	
	
	/**
	 * Demonstration : if x is a power of 2, it can't share any bit with x-1. So 
	 * x & (x-1) should be equal to 0. To get rid of negative values, we check
	 * that x is higher than 1 (0 and 1 being of course unacceptable values 
	 * for a buffer length). 
	 * 
	 * @param x the number to test
	 * @return true if x is a power of two
	 */
	protected boolean isPowerOfTwo(int x)
	{
		return (x > 1) && (x & (x-1)) == 0;
	}
}