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
	private final static byte[] SMTP_TERMINATOR = { '\r', '\n', '.', '\r', '\n' };

	/** */
	boolean sentFrom;
	int recipientCount;

	/**
	 * @throws UnknownHostException if problem looking up hostname
	 * @throws SMTPException if problem reported by the server
	 * @throws IOException if problem communicating with host
	 */
	public SmartClient(String host, int port, String myHost) throws UnknownHostException, IOException, SMTPException
	{
		this(host, port, null, myHost);
	}

	/**
	 * @throws UnknownHostException if problem looking up hostname
	 * @throws SMTPException if problem reported by the server
	 * @throws IOException if problem communicating with host
	 */
	public SmartClient(String host, int port, SocketAddress bindpoint, String myHost) throws UnknownHostException, IOException, SMTPException
	{
		super(host, port, bindpoint);

		this.receiveAndCheck();	// The server announces itself first
		this.sendAndCheck("HELO " + myHost);
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
		this.rawOutput.write(SMTP_TERMINATOR);
		this.rawOutput.flush();

		this.receiveAndCheck();
	}

	/**
	 * Quit and close down the connection.  Ignore any errors.
	 */
	public void quit()
	{
		try
		{
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
}
