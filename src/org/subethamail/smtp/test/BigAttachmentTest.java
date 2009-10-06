package org.subethamail.smtp.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import com.sun.mail.smtp.SMTPTransport;

/**
 * This class tests the transfer speed of emails that carry
 * attached files.
 *
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class BigAttachmentTest extends TestCase
{
	private final static Logger log = LoggerFactory.getLogger(BigAttachmentTest.class);

	private final static int SMTP_PORT = 1081;
	private final static String TO_CHANGE = "<path>/<your_bigfile.ext>";
	private final static int BUFFER_SIZE = 32768;

	// Set the full path name of the big file to use for the test.
	private final static String BIGFILE_PATH = TO_CHANGE;

	private Wiser server;

	/** */
	public BigAttachmentTest(String name)
	{
		super(name);
	}

	/** */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.server = new Wiser();
		this.server.setPort(SMTP_PORT);
		this.server.start();
	}

	/** */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		try
		{
			this.server.stop();
		} catch (Exception e)
		{
			e.printStackTrace();
		};
	}

	/** */
	public void testAttachments() throws Exception
	{
		if (BIGFILE_PATH.equals(TO_CHANGE))
		{
			log.error("BigAttachmentTest: To complete this test you must change the BIGFILE_PATH var to point out a file on your disk !");
		}
		assertNotSame("BigAttachmentTest: To complete this test you must change the BIGFILE_PATH var to point out a file on your disk !", TO_CHANGE, BIGFILE_PATH);
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", "localhost");
		props.setProperty("mail.smtp.port", SMTP_PORT+"");
		Session session = Session.getInstance(props);

		MimeMessage baseMsg = new MimeMessage(session);
		MimeBodyPart bp1 = new MimeBodyPart();
		bp1.setHeader("Content-Type", "text/plain");
		bp1.setContent("Hello World!!!", "text/plain; charset=\"ISO-8859-1\"");

		// Attach the file
		MimeBodyPart bp2 = new MimeBodyPart();
		FileDataSource fileAttachment = new FileDataSource(BIGFILE_PATH);
		DataHandler dh = new DataHandler(fileAttachment);
		bp2.setDataHandler(dh);
		bp2.setFileName(fileAttachment.getName());

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(bp1);
		multipart.addBodyPart(bp2);

		baseMsg.setFrom(new InternetAddress("Ted <ted@home.com>"));
		baseMsg.setRecipient(Message.RecipientType.TO, new InternetAddress(
				"success@subethamail.org"));
		baseMsg.setSubject("Test Big attached file message");
		baseMsg.setContent(multipart);
		baseMsg.saveChanges();

		log.debug("Send started");
		Transport t = new SMTPTransport(session, new URLName("smtp://localhost:"+SMTP_PORT));
		long started = System.currentTimeMillis();
		t.connect();
		t.sendMessage(baseMsg, new Address[] {new InternetAddress(
				"success@subethamail.org")});
		t.close();
		started = System.currentTimeMillis() - started;
		log.info("Elapsed ms = "+started);

		WiserMessage msg = this.server.getMessages().get(0);

		assertEquals(1, this.server.getMessages().size());
		assertEquals("success@subethamail.org", msg.getEnvelopeReceiver());

		File compareFile = File.createTempFile("attached", ".tmp");
		log.debug("Writing received attachment ...");

		FileOutputStream fos = new FileOutputStream(compareFile);
		((MimeMultipart) msg.getMimeMessage().getContent()).getBodyPart(1).getDataHandler().writeTo(fos);
		fos.close();
		log.debug("Checking integrity ...");
		assertTrue(this.checkIntegrity(new File(BIGFILE_PATH), compareFile));
		log.debug("Checking integrity DONE");
		compareFile.delete();
	}

	/** */
	private boolean checkIntegrity(File src, File dest) throws IOException, NoSuchAlgorithmException
	{
		BufferedInputStream ins = new BufferedInputStream(new FileInputStream(src));
		BufferedInputStream ind = new BufferedInputStream(new FileInputStream(dest));
		MessageDigest md1 = MessageDigest.getInstance("MD5");
		MessageDigest md2 = MessageDigest.getInstance("MD5");

		if (ins == null || ind == null)
			return false;

		int r = 0;
		byte[] buf1 = new byte[BUFFER_SIZE];
		byte[] buf2 = new byte[BUFFER_SIZE];

		while (r !=-1)
		{
			r = ins.read(buf1);
			ind.read(buf2);

			md1.update(buf1);
			md2.update(buf2);
		}

		ins.close();
		ind.close();
		return MessageDigest.isEqual(md1.digest(), md2.digest());
	}
}