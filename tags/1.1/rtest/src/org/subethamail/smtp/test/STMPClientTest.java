package org.subethamail.smtp.test;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.wiser.Wiser;

/**
 * This class serves as a test case for both Wiser (since it is used
 * internally here) as well as harder to reach code within the SMTP
 * server that tests a roundtrip message through the DATA portion
 * of the SMTP spec.
 * 
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class STMPClientTest extends TestCase
{
	/** */
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(STMPClientTest.class);
	
	/** */
	public static final int PORT = 2566;

	/** */
	protected Wiser wiser;
	protected Session session;
	
	/** */
	public STMPClientTest(String name) { super(name); }
	
	/** */
	protected void setUp() throws Exception
	{
		super.setUp();
		
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		props.setProperty("mail.smtp.port", Integer.toString(PORT));
		this.session = Session.getDefaultInstance(props);
		
		this.wiser = new Wiser();
		this.wiser.setPort(PORT);
		
		// make this really small so that we can promise
		// to have to hit the disk.
		this.wiser.getServer().setDataDeferredSize(10);
		this.wiser.start();
	}
	
	/** */
	protected void tearDown() throws Exception
	{
		this.wiser.stop();
		this.wiser = null;

		this.session = null;
		
		super.tearDown();
	}
	
	/** */
	public void testMultipleRecipients() throws Exception
	{
		MimeMessage message = new MimeMessage(this.session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone@anywhere.com"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone2@anywhere.com"));
		message.setFrom(new InternetAddress("someone@somewhereelse.com"));
		message.setSubject("barf");
		message.setText("body");

		Transport.send(message);
		
		assertEquals(2, this.wiser.getMessages().size());
	}

	/** */
	public void testLargeMessage() throws Exception
	{
		MimeMessage message = new MimeMessage(this.session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone@anywhere.com"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone2@anywhere.com"));
		message.setFrom(new InternetAddress("someone@somewhereelse.com"));
		message.setSubject("barf");
		message.setText("bodyalksdjflkasldfkasjldfkjalskdfjlaskjdflaksdjflkjasdlfkjl");

		Transport.send(message);
		
		assertEquals(2, this.wiser.getMessages().size());
		
		assertEquals("barf", this.wiser.getMessages().get(0).getMimeMessage().getSubject());
		assertEquals("barf", this.wiser.getMessages().get(1).getMimeMessage().getSubject());
	}
	
	
	/** */
	public static Test suite()
	{
		return new TestSuite(STMPClientTest.class);
	}
}
