package org.subethamail.wiser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps a received message and provides
 * a way to generate a JavaMail MimeMessage from the data.
 * 
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class WiserMessage
{
	private final static Logger log = LoggerFactory.getLogger(WiserMessage.class);
	
	Wiser wiser;
	String envelopeSender;
	String envelopeReceiver;
	InputStream stream;
	byte[] array;
	MimeMessage message = null;
	
	WiserMessage(Wiser wiser, String envelopeSender, String envelopeReceiver, InputStream stream)
	{
		this.wiser = wiser;
		this.envelopeSender = envelopeSender;
		this.envelopeReceiver = envelopeReceiver;
		this.stream = stream;
	}

	/**
	 * Generate a JavaMail MimeMessage.
	 * @throws MessagingException
	 */
	public MimeMessage getMimeMessage() throws MessagingException
	{
		if (message == null)
		{
			 message = new MimeMessage(this.wiser.getSession(), stream);
		}
		return message;
	}

	/**
	 * Get's the raw message DATA.
	 * Note : this could result in loading many data into memory in case of big 
	 * attached files. This is why the array is only generated on the first call. 
	 * 
	 * @return the byte array of the raw message or an empty byte array 
	 * if an exception occured. 
	 */
	public byte[] getData()
	{
		if (array == null)
		{			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			BufferedInputStream in;
			
			if (stream instanceof BufferedInputStream)
				in = (BufferedInputStream) stream;
			else
				in = new BufferedInputStream(this.stream);
	
			// read the data from the stream
			try
			{
				int b;
				byte[] buf = new byte[8192];
				while ((b = in.read(buf)) >= 0)
				{
					out.write(buf, 0, b);
				}
				
				array = out.toByteArray();
			}
			catch (IOException ioex)
			{
				array = new byte[0];
			}
			finally
			{
				try 
				{
					in.close();
				} 
				catch (IOException e) {}
			}
		}
		
		return array;
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
	
	public void dispose()
	{
		try
		{
			finalize();
		}
		catch (Throwable t)
		{
			log.error("On WiserMessage dispose", t);
		}
	}
	
	protected void finalize() throws Throwable 
	{
		super.finalize();
		if (this.stream != null)
			this.stream.close();
	}
}
