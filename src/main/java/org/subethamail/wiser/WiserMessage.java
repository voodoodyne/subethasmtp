package org.subethamail.wiser;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * This class wraps a received message and provides
 * a way to generate a JavaMail MimeMessage from the data.
 *
 * @author Jon Stevens
 */
public class WiserMessage
{
	static final long NO_ID = -1L;
	
	byte[] messageData;
	Wiser wiser;
	String envelopeSender;
	String envelopeReceiver;
	Date   receivedAt;
	long   id = NO_ID;

	WiserMessage(Wiser wiser, String envelopeSender, String envelopeReceiver, byte[] messageData)
	{
		this.wiser = wiser;
		this.envelopeSender = envelopeSender;
		this.envelopeReceiver = envelopeReceiver;
		this.messageData = messageData;
		this.receivedAt = new Date();
		this.id = NO_ID; // id is set by Wiser.deliver
	}

	/**
	 * Generate a JavaMail MimeMessage.
	 * @throws MessagingException
	 */
	public MimeMessage getMimeMessage() throws MessagingException
	{
		return new MimeMessage(this.wiser.getSession(), new ByteArrayInputStream(this.messageData));
	}

	/**
	 * Get message identifier
	 */

	public long getId() {
		return this.id;
	}
	
	/**
	 * Set message identifier
	 */

	public void setId( long id ) {
		this.id = id;
	}
	
	/**
	 * Get's the raw message DATA.
	 */
	public byte[] getData()
	{
		return this.messageData;
	}

	/**
	 * Get's the RCPT TO:
	 */
	public String getEnvelopeReceiver()
	{
		return this.envelopeReceiver;
	}

	/**
	 * Get's the MAIL FROM:
	 */
	public String getEnvelopeSender()
	{
		return this.envelopeSender;
	}
	
	/**
	 * Get's date the mail was received
	 */
	public Date getReceivedAt()
	{
		return this.receivedAt;
	}

	/**
	 * Dumps the rough contents of the message for debugging purposes
	 */
	public void dumpMessage(PrintStream out) throws MessagingException
	{
		out.println("===== Dumping message =====");

		out.println("Envelope sender: " + this.getEnvelopeSender());
		out.println("Envelope recipient: " + this.getEnvelopeReceiver());

		// It should all be convertible with ascii or utf8
		String content = new String(this.getData());
		out.println(content);

		out.println("===== End message dump =====");
	}

	/**
	 * Implementation of toString()
	 *
	 * @return getData() as a string or an empty string if getData is null
	 */
	@Override
	public String toString()
	{
		if (this.getData() == null)
			return "";

		return new String(this.getData());
	}
}
