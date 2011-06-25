package org.subethamail.smtp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;

/**
 * This class serves as a test case for both Wiser (since it is used
 * internally here) as well as harder to reach code within the SMTP
 * server that tests a roundtrip message through the DATA portion
 * of the SMTP spec.
 *
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 * @author Ville Skyttä (contributed some encoding tests)
 */
public class MessageContentTest extends TestCase
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(MessageContentTest.class);

	/** */
	public static final int PORT = 2566;

	/** */
	protected Wiser wiser;
	protected Session session;

	/** */
	public MessageContentTest(String name) { super(name); }

	/** */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		props.setProperty("mail.smtp.port", Integer.toString(PORT));
		this.session = Session.getInstance(props);

		this.wiser = new Wiser();
		this.wiser.setPort(PORT);

		this.wiser.start();
	}

	/** */
	@Override
	protected void tearDown() throws Exception
	{
		this.wiser.stop();
		this.wiser = null;

		this.session = null;

		super.tearDown();
	}

	/** */
	public void testReceivedHeader() throws Exception
	{
		MimeMessage message = new MimeMessage(this.session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone@anywhere.com"));
		message.setFrom(new InternetAddress("someone@somewhereelse.com"));
		message.setSubject("barf");
		message.setText("body");

		Transport.send(message);

		assertEquals(1, this.wiser.getMessages().size());

		String[] receivedHeaders = this.wiser.getMessages().get(0).getMimeMessage().getHeader("Received");

		assertEquals(1, receivedHeaders.length);
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
	public void testUtf8EightBitMessage() throws Exception
	{
		// Beware editor/compiler character encoding issues; safest to put unicode escapes here

		String body = "\u00a4uro ma\u00f1ana\r\n";
		this.testEightBitMessage(body, "UTF-8");

		assertEquals(body, this.wiser.getMessages().get(0).getMimeMessage().getContent());
	}

	/** */
	public void testIso88591EightBitMessage() throws Exception
	{
		// Beware editor/compiler character encoding issues; safest to put unicode escapes here

		String body = "ma\u00f1ana\r\n";	// spanish ene (ie, n with diacritical tilde)
		this.testEightBitMessage(body, "ISO-8859-1");

		assertEquals(body, this.wiser.getMessages().get(0).getMimeMessage().getContent());
	}

	/** */
	public void testIso885915EightBitMessage() throws Exception
	{
		// Beware editor/compiler character encoding issues; safest to put unicode escapes here

		String body = "\u0080uro\r\n"; // should be the euro symbol
		this.testEightBitMessage(body, "ISO-8859-15");

//		String content = (String)this.wiser.getMessages().get(0).getMimeMessage().getContent();
//		for (int i=0; i<content.length(); i++)
//		{
//			log.info("Char is:  " + Integer.toString(content.codePointAt(i), 16));
//		}

		assertEquals(body, this.wiser.getMessages().get(0).getMimeMessage().getContent());
	}

	/** */
	private void testEightBitMessage(String body, String charset) throws Exception
	{
		MimeMessage message = new MimeMessage(this.session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone@anywhere.com"));
		message.setFrom(new InternetAddress("someone@somewhereelse.com"));
		message.setSubject("hello");
		message.setText(body, charset);
		message.setHeader("Content-Transfer-Encoding", "8bit");

		Transport.send(message);
	}

	/** */
	public void testIso2022JPEightBitMessage() throws Exception
  	{
		String body = "\u3042\u3044\u3046\u3048\u304a\r\n"; // some Japanese letters
		this.testEightBitMessage(body, "iso-2022-jp");

		assertEquals(body, this.wiser.getMessages().get(0).getMimeMessage().getContent());
	}

	/** */
	public void testBinaryEightBitMessage() throws Exception
	{
		byte[] body = new byte[64];
		new Random().nextBytes(body);

		MimeMessage message = new MimeMessage(this.session);
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("anyone@anywhere.com"));
		message.setFrom(new InternetAddress("someone@somewhereelse.com"));
		message.setSubject("hello");
		message.setHeader("Content-Transfer-Encoding", "8bit");
		message.setDataHandler(new DataHandler(new ByteArrayDataSource(body, "application/octet-stream")));

		Transport.send(message);

		InputStream in = this.wiser.getMessages().get(0).getMimeMessage().getInputStream();
		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		byte[] buf = new byte[64];
		int n;
		while ((n = in.read(buf)) != -1)
		{
			tmp.write(buf, 0, n);
		}
		in.close();

		assertTrue(Arrays.equals(body, tmp.toByteArray()));
	}

	/** */
	public static Test suite()
	{
		return new TestSuite(MessageContentTest.class);
	}
}
