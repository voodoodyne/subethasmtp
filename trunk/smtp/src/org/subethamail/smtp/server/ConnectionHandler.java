package org.subethamail.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.server.io.CRLFTerminatedReader;
import org.subethamail.smtp.server.io.LastActiveInputStream;

/**
 * The thread that handles a connection. This class
 * passes most of it's responsibilities off to the
 * CommandHandler.
 * 
 * @author Jon Stevens
 */
public class ConnectionHandler extends Thread implements ConnectionContext
{
	private static Log log = LogFactory.getLog(ConnectionHandler.class);

	private SMTPServer server;
	private Session session;

	private InputStream input;
	private OutputStream output;

	private CRLFTerminatedReader reader;
	private PrintWriter writer;

	private Socket socket;

	private long startTime;
	private long lastActiveTime;
	
	public ConnectionHandler(SMTPServer server, Socket socket)
		throws IOException
	{
		super(server.getConnectionGroup(), ConnectionHandler.class.getName());
		this.server = server;

		setSocket(socket);

		this.startTime = System.currentTimeMillis();
		this.lastActiveTime = this.startTime;
		
	}
	
	public Session getSession()
	{
		return this.session;
	}
	
	public ConnectionHandler getConnection()
	{
		return this;
	}
	
	public SMTPServer getServer()
	{
		return this.server;
	}

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

	public void run()
	{
		if (log.isDebugEnabled())
			log.debug("SMTP connection count: " + this.server.getNumberOfConnections());

		this.session = new Session();
		try
		{
			if (this.server.hasTooManyConnections())
			{
				log.debug("SMTP Too many connections!");

				this.sendResponse("554 Transaction failed. Too many connections.");
				return;
			}

			this.sendResponse("220 " + this.server.getHostName() + " ESMTP " + this.server.getName());

			while (session.isActive())
			{
				try
				{
					String line = this.reader.readLine();
					if (line == null)
					{
						log.debug("no more lines from client");
						break;
					}
					this.server.getCommandHandler().handleCommand(this, line);
					lastActiveTime = System.currentTimeMillis();
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
			closeConnection();
		}
	}

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
				closeSocket();
			}
		}
		catch (IOException e)
		{
			log.debug(e);
		}
	}

	public void setSocket(Socket socket) throws IOException
	{
		this.socket = socket;
		this.input = new LastActiveInputStream(this.socket.getInputStream(), this);
		this.output = this.socket.getOutputStream();
		this.reader = new CRLFTerminatedReader(this.input);
		this.writer = new PrintWriter(this.output);
	}

	public Socket getSocket()
	{
		return this.socket;
	}

	private void closeSocket() throws IOException
	{
		if (this.socket != null && this.socket.isBound() && !this.socket.isClosed())
			this.socket.close();
	}

	public InputStream getInput()
	{
		return this.input;
	}

	public OutputStream getOutput()
	{
		return this.output;
	}

	public void sendResponse(String response) throws IOException
	{
		this.writer.print(response + "\r\n");
		this.writer.flush();
	}
	
	public long getStartTime()
	{
		return this.startTime;
	}

	public long getLastActiveTime()
	{
		return this.lastActiveTime;
	}

	public void refreshLastActiveTime()
	{
		this.lastActiveTime = System.currentTimeMillis();
	}
}
