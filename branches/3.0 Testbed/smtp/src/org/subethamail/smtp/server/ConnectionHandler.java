package org.subethamail.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.server.io.CRLFTerminatedReader;
import org.subethamail.smtp.server.io.LastActiveInputStream;

/**
 * The thread that handles a connection. This class
 * passes most of it's responsibilities off to the
 * CommandHandler.
 * 
 * @author Jon Stevens
 */
public class ConnectionHandler extends Thread implements MessageContext
{
	private static Log log = LogFactory.getLog(ConnectionHandler.class);

	/** A link to our parent server */
	private SMTPServer server;

	/** When this goes true, the thread shuts down */
	private boolean shutdown = false;

	/** I/O to the client */
	private Socket socket;
	private CRLFTerminatedReader reader;
	private PrintWriter writer;

	/** Can be used to check for staleness */
	private LastActiveInputStream input;

	/** Might exist if the client has successfully authenticated */
	private AuthenticationHandler authenticationHandler;
	
	/** Might exist if the client is giving us a message */
	private MessageHandler messageHandler;

	/** Some state information */
	private boolean hasSeenHelo = false;
	private boolean hasMailFrom = false;
	private int recipientCount = 0;

	/**
	 * Creates (but does not start) the thread object.
	 * 
	 * @param server a link to our parent
	 * @param socket is the socket to the client
	 * @throws IOException
	 */
	public ConnectionHandler(SMTPServer server, Socket socket)
		throws IOException
	{
		super(server.getConnectionGroup(), ConnectionHandler.class.getName());
		
		this.server = server;

		this.setSocket(socket);
	}

	/**
	 * @return a reference to the master server object
	 */
	public SMTPServer getServer()
	{
		return this.server;
	}

	/**
	 * Called by the watchdog to shut down this session. 
	 */
	public void timeout() throws IOException
	{
		try
		{
			this.sendResponse("421 Timeout waiting for data from client.");
		}
		finally
		{
			closeConnection();
		}
	}

	/**
	 * The thread for each session runs on this and shuts down when the shutdown member goes true.
	 */
	public void run()
	{
		if (log.isDebugEnabled())
			log.debug("SMTP connection count: " + this.server.getNumberOfConnections());

		this.messageHandler = this.server.getMessageHandlerFactory().create(this);
		try
		{
			if (this.server.hasTooManyConnections())
			{
				log.debug("SMTP Too many connections!");

				this.sendResponse("554 Transaction failed. Too many connections.");
				return;
			}

			this.sendResponse("220 " + this.server.getHostName() + " ESMTP " + this.server.getName());

			while (!this.shutdown)
			{
				try
				{
					String line = this.reader.readLine();
					if (line == null)
					{
						log.debug("no more lines from client");
						break;
					}

					if (log.isDebugEnabled())
						log.debug("Client: " + line);

					this.server.getCommandHandler().handleCommand(this, line);
				}
				catch (CRLFTerminatedReader.TerminationException te)
				{
					String msg = "501 Syntax error at character position "
						+ te.position()
						+ ". CR and LF must be CRLF paired.  See RFC 2821 #2.7.1.";

					log.debug(msg);
					this.sendResponse(msg);

					// if people are screwing with things, close connection
					break;
				}
				catch (CRLFTerminatedReader.MaxLineLengthException mlle)
				{
					String msg = "501 " + mlle.getMessage();

					log.debug(msg);
					this.sendResponse(msg);

					// if people are screwing with things, close connection
					break;
				}
			}
		}
		catch (IOException e1)
		{
			try
			{
				// primarily if things fail during the MessageListener.deliver(), then try
				// to send a temporary failure back so that the server will try to resend 
				// the message later.
				this.sendResponse("450 Problem attempting to execute commands. Please try again later.");
			}
			catch (IOException e)
			{
			}
			
			if (log.isDebugEnabled())
				log.debug(e1);
		}
		finally
		{
			this.closeConnection();
		}
	}

	/** 
	 * Close reader, writer, and socket, logging exceptions but otherwise ignoring them
	 */
	private void closeConnection()
	{
		try
		{
			try
			{
				this.writer.close();
				this.input.close();
			}
			finally
			{
				this.closeSocket();
			}
		}
		catch (IOException e)
		{
			log.info(e);
		}
	}

	/**
	 * Initializes our reader, writer, and the i/o filter chains based on
	 * the specified socket.  This is called internally when we startup
	 * and when (if) SSL is started.
	 */
	public void setSocket(Socket socket) throws IOException
	{
		this.socket = socket;
		this.input = new LastActiveInputStream(this.socket.getInputStream());
		this.reader = new CRLFTerminatedReader(this.input);
		this.writer = new PrintWriter(this.socket.getOutputStream());
	}

	/**
	 * @return the current socket to the client
	 */
	public Socket getSocket()
	{
		return this.socket;
	}

	/** Close the client socket if it is open */
	public void closeSocket() throws IOException
	{
		if (this.socket != null && this.socket.isBound() && !this.socket.isClosed())
			this.socket.close();
	}

	/**
	 * @return the raw input stream from the client
	 */
	public InputStream getRawInput()
	{
		return this.input;
	}
	
	/**
	 * @return the cooked CRLF-terminated reader from the client
	 */
	public CRLFTerminatedReader getReader()
	{
		return this.reader;
	}

	/** Sends the response to the client */
	public void sendResponse(String response) throws IOException
	{
		if (log.isDebugEnabled())
			log.debug("Server: " + response);

		this.writer.print(response + "\r\n");
		this.writer.flush();
	}
	
	/* (non-Javadoc)
	 * @see org.subethamail.smtp.SMTPContext#getRemoteAddress()
	 */
	public SocketAddress getRemoteAddress()
	{
		return this.socket.getRemoteSocketAddress();
	}

	/* (non-Javadoc)
	 * @see org.subethamail.smtp.MessageContext#getSMTPServer()
	 */
	public SMTPServer getSMTPServer()
	{
		return this.server;
	}
	
	/**
	 * @return the current message handler
	 */
	public MessageHandler getMessageHandler()
	{
		return this.messageHandler;
	}

	/** Simple state */
	public boolean getHasMailFrom()
	{
		return this.hasMailFrom;
	}

	public void setHasMailFrom(boolean value)
	{
		this.hasMailFrom = value;
	}

	public boolean getHasSeenHelo()
	{
		return this.hasSeenHelo;
	}

	public void setHasSeenHelo(boolean hasSeenHelo)
	{
		this.hasSeenHelo = hasSeenHelo;
	}

	public void addRecipient()
	{
		this.recipientCount++;
	}
	
	public int getRecipientCount()
	{
		return this.recipientCount;
	}
	
	public boolean isAuthenticated()
	{
		return this.authenticationHandler != null;
	}
	
	public AuthenticationHandler getAuthenticationHandler()
	{
		return this.authenticationHandler;
	}
	
	/**
	 * This is called by the AuthCommand when a session is successfully authenticated.  The
	 * handler will be an object created by the AuthenticationHandlerFactory.
	 */
	public void setAuthenticationHandler(AuthenticationHandler handler)
	{
		this.authenticationHandler = handler;
	}
	
	/**
	 * Some state is associated with each particular message (senders, recipients, the message handler).
	 * Some state is not; seeing hello, TLS, authentication.
	 */
	public void resetMessageState()
	{
		this.messageHandler = this.server.getMessageHandlerFactory().create(this);
		this.hasMailFrom = false;
		this.recipientCount = 0;
	}
}
