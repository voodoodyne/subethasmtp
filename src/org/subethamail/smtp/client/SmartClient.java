package org.subethamail.smtp.client;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A somewhat smarter abstraction of an SMTP client which doesn't require knowing
 * anything about the nitty gritty of SMTP.
 *
 * @author Jeff Schnitzer
 */
public class SmartClient extends SMTPClient
{
	/** */
	private static Logger log = LoggerFactory.getLogger(SmartClient.class);

	/** */
	boolean sentFrom;
	int recipientCount;
	/** The host name which is sent in the HELO and EHLO commands */
	private String heloHost;

	/**
	 * True if the server sent a 421
	 * "Service not available, closing transmission channel" response. In this
	 * case the QUIT command should not be sent.
	 */
	private boolean serverClosingTransmissionChannel = false;

	/**
	 * Creates an unconnected client.
	 */
	public SmartClient()
	{
		// nothing to do
	}
	
	/**
	 * Connects to the specified server and issues the initial HELO command.
	 * 
	 * @throws UnknownHostException if problem looking up hostname
	 * @throws SMTPException if problem reported by the server
	 * @throws IOException if problem communicating with host
	 */
	public SmartClient(String host, int port, String myHost) throws UnknownHostException, IOException, SMTPException
	{
		this(host, port, null, myHost);
	}

	/**
	 * Connects to the specified server and issues the initial HELO command.
	 * 
	 * @throws UnknownHostException if problem looking up hostname
	 * @throws SMTPException if problem reported by the server
	 * @throws IOException if problem communicating with host
	 */
	public SmartClient(String host, int port, SocketAddress bindpoint, String myHost) throws UnknownHostException,
			IOException, SMTPException
	{
		this.setBindpoint(bindpoint);
		this.setHeloHost(myHost);
		this.connect(host, port);
	}

	/**
	 * Connects to the specified server and issues the initial HELO command. It
	 * gracefully closes the connection if it could be established but
	 * subsequently it fails or if the server does not accept messages.
	 */
	@Override
	public void connect(String host, int port) throws IOException, SMTPException
	{
		if (heloHost == null)
			throw new IllegalStateException("Helo host must be specified before connectiong");

		super.connect(host, port);
		try
		{
			this.receiveAndCheck(); // The server announces itself first
			this.sendAndCheck("HELO " + heloHost);
		}
		catch (SMTPException e)
		{
			this.quit();
			throw e;
		}
		catch (IOException e)
		{
			this.close(); // just close the socket, issuing QUIT is hopeless now
			throw e;
		}
	}

	/**
	 * Returns the server response. It takes note of a 421 response code, so
	 * QUIT will not be issued unnecessarily.
	 */
	@Override
	protected Response receive() throws IOException
	{
		Response response = super.receive();
		if (response.getCode() == 421)
			serverClosingTransmissionChannel = true;
		return response;
	}

	/** */
	public void from(String from) throws IOException, SMTPException
	{
		this.sendAndCheck("MAIL FROM: <" + from + ">");
		this.sentFrom = true;
	}

	/** */
	public void to(String to) throws IOException, SMTPException
	{
		this.sendAndCheck("RCPT TO: <" + to + ">");
		this.recipientCount++;
	}

	/**
	 * Prelude to writing data
	 */
	public void dataStart() throws IOException, SMTPException
	{
		this.sendAndCheck("DATA");
	}

	/**
	 * Actually write some data
	 */
	public void dataWrite(byte[] data, int numBytes) throws IOException
	{
		this.dataOutput.write(data, 0, numBytes);
	}

	/**
	 * Last step after writing data
	 */
	public void dataEnd() throws IOException, SMTPException
	{
		this.dataOutput.flush();
		this.dotTerminatedOutput.writeTerminatingSequence();
		this.dotTerminatedOutput.flush();

		this.receiveAndCheck();
	}

	/**
	 * Quit and close down the connection. Ignore any errors.
	 * <p>
	 * It still closes the connection, but it does not send the QUIT command if
	 * a 421 Service closing transmission channel is received previously. In
	 * these cases QUIT would fail anyway.
	 * 
	 * @see <a href="http://tools.ietf.org/html/rfc5321#section-3.8">RFC 5321
	 *      Terminating Sessions and Connections</a>
	 */
	public void quit()
	{
		try
		{
			if (!this.serverClosingTransmissionChannel)
				this.sendAndCheck("QUIT");
		}
		catch (IOException ex)
		{
			log.warn("Failed to issue QUIT to " + this.hostPort);
		}

		this.close();
	}

	/**
	 * @return true if we have already specified from()
	 */
	public boolean sentFrom()
	{
		return this.sentFrom;
	}

	/**
	 * @return true if we have already specified to()
	 */
	public boolean sentTo()
	{
		return this.recipientCount > 0;
	}

	/**
	 * @return the number of recipients that have been accepted by the server
	 */
	public int getRecipientCount()
	{
		return this.recipientCount;
	}

	/**
	 * Sets the domain name or address literal of this system, which name will
	 * be sent to the server in the parameter of the HELO and EHLO commands.
	 * This has no default and is required.
	 */
	public void setHeloHost(String myHost)
	{
		this.heloHost = myHost;
	}

	/**
	 * Returns the HELO name of this system.
	 */
	public String getHeloHost()
	{
		return heloHost;
	}
}
