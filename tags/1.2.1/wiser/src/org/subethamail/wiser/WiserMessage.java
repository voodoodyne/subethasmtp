package org.subethamail.wiser;

import java.io.ByteArrayInputStream;

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
	byte[] messageData;
	Wiser wiser;
	String envelopeSender;
	String envelopeReceiver;
	
	WiserMessage(Wiser wiser, String envelopeSender, String envelopeReceiver, byte[] messageData)
	{
		this.wiser = wiser;
		this.envelopeSender = envelopeSender;
		this.envelopeReceiver = envelopeReceiver;
		this.messageData = messageData;
	}

	/**
	 * Generate a JavaMail MimeMessage.
	 * @throws MessagingException
	 */
	public MimeMessage getMimeMessage() throws MessagingException
	{
		return new MimeMessage(this.wiser.getSession(), new ByteArrayInputStream(messageData));
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
}
