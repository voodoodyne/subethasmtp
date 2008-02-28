package org.subethamail.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;

import org.apache.mina.common.BufferDataException;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.SSLFilter.SSLFilterMessage;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.auth.Credential;
import org.subethamail.smtp.command.AuthCommand;
import org.subethamail.smtp.command.DataEndCommand;
import org.subethamail.smtp.command.EhloCommand;
import org.subethamail.smtp.command.HelloCommand;
import org.subethamail.smtp.command.NoopCommand;
import org.subethamail.smtp.command.QuitCommand;
import org.subethamail.smtp.command.ResetCommand;

/**
 * The IoHandler that handles a connection. This class
 * passes most of it's responsibilities off to the
 * CommandHandler.
 * 
 * @author Jon Stevens
 * 
 * This file has been used and differs from the original
 * by the use of MINA NIO framework.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class ConnectionHandler extends IoHandlerAdapter
{
	public class Context implements ConnectionContext, MessageContext
	{
		private SMTPServer server;

		private Session sessionCtx;

		private IoSession session;
		
		private Credential credential;
		
		private InputStream inputStream;

		public Context(SMTPServer server, IoSession session)
		{
			this.server = server;
			this.session = session;
			sessionCtx = new Session(this.server.getMessageHandlerFactory().create(this));
		}

		public InputStream getInputStream() 
		{
			return inputStream;
		}

		public void setInputStream(InputStream inputStream) 
		{
			this.inputStream = inputStream;
		}

		public Session getSession()
		{
			return sessionCtx;
		}

		public void sendResponse(String response) throws IOException
		{
			ConnectionHandler.sendResponse(session, response);
		}

		public SocketAddress getRemoteAddress()
		{
			return session.getRemoteAddress();
		}

		public SMTPServer getSMTPServer()
		{
			return server;
		}

		public IoSession getIOSession()
		{
			return session;
		}

		public Credential getCredential() 
		{
			return credential;
		}

		public void setCredential(Credential credential) 
		{
			this.credential = credential;
		}
	}

	// Session objects
	protected final static String CONTEXT_ATTRIBUTE = ConnectionHandler.class.getName() + ".ctx";

	private static Logger log = LoggerFactory.getLogger(ConnectionHandler.class);

	private SMTPServer server;

	private int numberOfConnections;
	
	private int receiveBufferSize = 128;

	public ConnectionHandler(SMTPServer server)
	{
		this.server = server;
	}

	/** */
	private synchronized void updateNumberOfConnections(int newValue)
	{
		numberOfConnections = newValue;
		if (log.isDebugEnabled())
			log.debug("Active connections = " + numberOfConnections);
	}

	/**
	 * @return The number of open connections
	 */
	public int getNumberOfConnections()
	{
		return numberOfConnections;
	}

	/**
	 * Sets the receive buffer size.
	 */
	public void setReceiveBufferSize(int receiveBufferSize)
	{
		this.receiveBufferSize=receiveBufferSize;
	}
	
	/** */
	public void sessionCreated(IoSession session)
	{
		updateNumberOfConnections(numberOfConnections + 1);

		if (session.getTransportType() == TransportType.SOCKET)
		{
			((SocketSessionConfig)session.getConfig()).setReceiveBufferSize(receiveBufferSize);
			((SocketSessionConfig)session.getConfig()).setSendBufferSize(64);
		}

		session.setIdleTime(IdleStatus.READER_IDLE, server.getConnectionTimeout() / 1000);

		// We're going to use SSL negotiation notification.
		session.setAttribute(SSLFilter.USE_NOTIFICATION);

		// Init protocol internals
		if (log.isDebugEnabled())
			log.debug("SMTP connection count: " + this.server.getNumberOfConnections());

		Context minaCtx = new Context(server, session);
		session.setAttribute(CONTEXT_ATTRIBUTE, minaCtx);

		try
		{
			if (this.server.hasTooManyConnections())
			{
				log.debug("SMTP Too many connections!");

				sendResponse(session, "554 Transaction failed. Too many connections.");
			}

			sendResponse(session, "220 " + this.server.getHostName() + " ESMTP " + this.server.getName());
		}
		catch (IOException e1)
		{
			try
			{
				// primarily if things fail during the MessageListener.deliver(), then try
				// to send a temporary failure back so that the server will try to resend 
				// the message later.
				sendResponse(session, "450 Problem when connecting. Please try again later.");
			}
			catch (IOException e)
			{
			}
			if (log.isDebugEnabled())
				log.debug("Error on session creation", e1);

			session.close();
		}
	}

	/**
	 * Session closed.
	 */
	public void sessionClosed(IoSession session) throws Exception
	{
		updateNumberOfConnections(numberOfConnections - 1);
	}

	/**
	 * Sends a response telling that the session is idle and closes it.
	 */
	public void sessionIdle(IoSession session, IdleStatus status)
	{
		try
		{
			sendResponse(session, "421 Timeout waiting for data from client.");
		}
		catch (IOException ioex)
		{
		}
		finally
		{
			session.close();
		}
	}

	/** */
	public void exceptionCaught(IoSession session, Throwable cause)
	{
		if (log.isDebugEnabled())
			log.debug("Exception occured :", cause);

		try
		{
			if (cause instanceof BufferDataException)
			{
				sendResponse(session, "501 " + cause.getMessage());
			}
			else
			{
				// primarily if things fail during the MessageListener.deliver(), then try
				// to send a temporary failure back so that the server will try to resend 
				// the message later.
				sendResponse(session, "450 Problem attempting to execute commands. Please try again later.");
			}
		}
		catch (IOException e)
		{
		}
		finally
		{
			session.close();
		}
	}

	/** */
	public void messageReceived(IoSession session, Object message) throws Exception
	{
		if (message == null)
		{
			if (log.isDebugEnabled())
				log.debug("no more lines from client");
			return;
		}
		
		if (message instanceof SSLFilterMessage)
		{
			if (log.isDebugEnabled())
				log.debug("SSL FILTER message -> " + message);
			return;
		}

		Context minaCtx = (Context) session.getAttribute(CONTEXT_ATTRIBUTE);

		if (message instanceof InputStream)
		{
			minaCtx.setInputStream((InputStream) message);
			new DataEndCommand().execute(null, minaCtx);
		}
		else
		{
			String line = (String) message;
			
			if (log.isDebugEnabled())
				log.debug("C: " + line);
			
            if (minaCtx.getSession().isAuthenticating())
            	this.server.getCommandHandler().handleAuthChallenge(minaCtx, line);
            else
            if (!minaCtx.getSession().isAuthenticated() 
            		&& !minaCtx.getSession().getMessageHandler().getAuthenticationMechanisms().isEmpty())
            {
            	// Per RFC 2554
            	Command cmd = this.server.getCommandHandler().getCommandFromString(line);
            	
            	if (cmd != null && (cmd instanceof AuthCommand || cmd instanceof EhloCommand || cmd instanceof HelloCommand ||
            			   cmd instanceof NoopCommand || cmd instanceof ResetCommand || cmd instanceof QuitCommand))
            		this.server.getCommandHandler().handleCommand(minaCtx, line);
            	else
            		sendResponse(session, "530 Authentication required");
            }
            else
            	this.server.getCommandHandler().handleCommand(minaCtx, line);
		}
	}

	/** */
	public static void sendResponse(IoSession session, String response) throws IOException
	{
		if (log.isDebugEnabled())
			log.debug("S: " + response);

		if (response == null)
			return;

		session.write(response);
	}
}