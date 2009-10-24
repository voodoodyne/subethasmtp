package org.subethamail.smtp.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

public class WiserFailuresTest extends TestCase
{
	private final static String FROM_ADDRESS = "from-addr@localhost";
	private final static String HOST_NAME = "localhost";
	private final static String TO_ADDRESS = "to-addr@localhost";
	private final static int SMTP_PORT = 1081;
	private static Logger log = LoggerFactory.getLogger(WiserFailuresTest.class);
	private BufferedReader input;
	private PrintWriter output;
	private Wiser server;
	private Socket socket;

	public WiserFailuresTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		server = new Wiser();
		server.setPort(SMTP_PORT);
		server.start();
		socket = new Socket(HOST_NAME, SMTP_PORT);
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
		try { input.close(); } catch (Exception e){};
		try { output.close(); } catch (Exception e){};
		try { socket.close(); } catch (Exception e){};
		try { server.stop(); } catch (Exception e){};
	}

	/**
	 * See http://sourceforge.net/tracker/index.php?func=detail&aid=1474700&group_id=78413&atid=553186 for discussion
	 * about this bug
	 */
	public void testMailFromAfterReset() throws IOException, MessagingException
	{
		log.info("testMailFromAfterReset() start");

		assertConnect();
		sendExtendedHello(HOST_NAME);
		sendMailFrom(FROM_ADDRESS);
		sendReceiptTo(TO_ADDRESS);
		sendReset();
		sendMailFrom(FROM_ADDRESS);
		sendReceiptTo(TO_ADDRESS);
		sendDataStart();
		send("");
		send("Body");
		sendDataEnd();
		sendQuit();

		assertEquals(1, server.getMessages().size());
		Iterator<WiserMessage> emailIter = server.getMessages().iterator();
		WiserMessage email = (WiserMessage)emailIter.next();
		assertEquals("Body", email.getMimeMessage().getContent().toString());
	}

	/**
	 * See http://sourceforge.net/tracker/index.php?func=detail&aid=1474700&group_id=78413&atid=553186 for discussion
	 * about this bug
	 */
	public void testMailFromWithInitialReset() throws IOException, MessagingException
	{
		assertConnect();
		sendReset();
		sendMailFrom(FROM_ADDRESS);
		sendReceiptTo(TO_ADDRESS);
		sendDataStart();
		send("");
		send("Body");
		sendDataEnd();
		sendQuit();

		assertEquals(1, server.getMessages().size());
		Iterator<WiserMessage> emailIter = server.getMessages().iterator();
		WiserMessage email = (WiserMessage)emailIter.next();
		assertEquals("Body", email.getMimeMessage().getContent().toString());
	}

  public void testSendEncodedMessage() throws IOException, MessagingException
  {
		String body = "\u3042\u3044\u3046\u3048\u304a"; // some Japanese letters
		String charset = "iso-2022-jp";
		
		try 
		{
			sendMessageWithCharset(SMTP_PORT, "sender@hereagain.com",
					"EncodedMessage", body, "receivingagain@there.com", charset);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			fail("Unexpected exception: " + e);
		}

		assertEquals(1, server.getMessages().size());
		Iterator<WiserMessage> emailIter = server.getMessages().iterator();
		WiserMessage email = (WiserMessage)emailIter.next();
		assertEquals(body, email.getMimeMessage().getContent().toString());
	}
	  
	public void testSendMessageWithCarriageReturn() throws IOException, MessagingException
	{
		String bodyWithCR = "\r\n\r\nKeep these\r\npesky\r\n\r\ncarriage returns\r\n";
		try
		{
			sendMessage(SMTP_PORT, "sender@hereagain.com", "CRTest", bodyWithCR, "receivingagain@there.com");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected exception: " + e);
		}

		assertEquals(1, server.getMessages().size());
		Iterator<WiserMessage> emailIter = server.getMessages().iterator();
		WiserMessage email = (WiserMessage)emailIter.next();
		assertEquals(email.getMimeMessage().getContent().toString(), bodyWithCR);
	}

	public void testSendTwoMessagesSameConnection()
		throws IOException
	{
		try
		{
			MimeMessage[] mimeMessages = new MimeMessage[2];
			Properties mailProps = getMailProperties(SMTP_PORT);
			Session session = Session.getInstance(mailProps, null);
			// session.setDebug(true);

			mimeMessages[0] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle1", "Bug1");
			mimeMessages[1] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle2", "Bug2");

			Transport transport = session.getTransport("smtp");
			transport.connect("localhost", SMTP_PORT, null, null);

			for (int i = 0; i < mimeMessages.length; i++)
			{
				MimeMessage mimeMessage = mimeMessages[i];
				transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
			}

			transport.close();
		}
		catch (MessagingException e)
		{
			e.printStackTrace();
			fail("Unexpected exception: " + e);
		}

		assertEquals(2, server.getMessages().size());
	}

	public void testSendTwoMsgsWithLogin() throws MessagingException, IOException
	{
		try
		{
			String From = "sender@here.com";
			String To = "receiver@there.com";
			String Subject = "Test";
			String body = "Test Body";

			Session session = Session.getInstance(getMailProperties(SMTP_PORT), null);
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(From));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(To, false));
			msg.setSubject(Subject);

			msg.setText(body);
			msg.setHeader("X-Mailer", "musala");
			msg.setSentDate(new Date());

			Transport transport = null;

			try
			{
				transport = session.getTransport("smtp");
				transport.connect(HOST_NAME, SMTP_PORT, "ddd", "ddd");
				assertEquals(0, server.getMessages().size());
				transport.sendMessage(msg, InternetAddress.parse(To, false));
				assertEquals(1, server.getMessages().size());
				transport.sendMessage(msg, InternetAddress.parse("dimiter.bakardjiev@musala.com", false));
				assertEquals(2, server.getMessages().size());
			}
			catch (javax.mail.MessagingException me)
			{
				me.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (transport != null)
					transport.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Iterator<WiserMessage> emailIter = server.getMessages().iterator();
		WiserMessage email = (WiserMessage)emailIter.next();
		MimeMessage mime = email.getMimeMessage();
		assertTrue(mime.getHeader("Subject")[0].equals("Test"));
		assertTrue(mime.getContent().toString().equals("Test Body"));
	}

	private Properties getMailProperties(int port)
	{
		Properties mailProps = new Properties();
		mailProps.setProperty("mail.smtp.host", "localhost");
		mailProps.setProperty("mail.smtp.port", "" + port);
		mailProps.setProperty("mail.smtp.sendpartial", "true");
		return mailProps;
	}

	private void sendMessage(int port, String from, String subject, String body, String to) throws MessagingException, IOException
	{
		Properties mailProps = getMailProperties(SMTP_PORT);
		Session session = Session.getInstance(mailProps, null);
		//session.setDebug(true);

		MimeMessage msg = createMessage(session, from, to, subject, body);
		Transport.send(msg);
	}

	private MimeMessage createMessage(Session session, String from, String to, String subject, String body)
		throws MessagingException, IOException
	{
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		msg.setSubject(subject);
		msg.setSentDate(new Date());
		msg.setText(body);		
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
		return msg;
	}

    private void sendMessageWithCharset(int port, String from, String subject, String body, String to, String charset) 
		throws MessagingException 
	{
	   Properties mailProps = getMailProperties(port);
	   Session session = Session.getInstance(mailProps, null);
	   //session.setDebug(true);
	
	   MimeMessage msg = createMessageWithCharset(session, from, to, subject, body, charset);
	   Transport.send(msg);
	 }
	
	private MimeMessage createMessageWithCharset(
	  Session session, String from, String to, String subject, String body, String charset) 
		throws MessagingException 
	{
	   MimeMessage msg = new MimeMessage(session);
	   msg.setFrom(new InternetAddress(from));
	   msg.setSubject(subject);
	   msg.setSentDate(new Date());
	   if (charset != null) 
	   {
		   msg.setText(body, charset);
		   msg.setHeader("Content-Transfer-Encoding", "7bit");
	   } 
	   else
		   msg.setText(body);
	
	   msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
	   return msg;
	 }
	
	private void assertConnect() throws IOException
	{
		String response = readInput();
		assertTrue(response, response.startsWith("220"));
	}

	private void sendDataEnd() throws IOException
	{
		send(".");
		String response = readInput();
		assertTrue(response, response.startsWith("250"));
	}

	private void sendDataStart() throws IOException
	{
		send("DATA");
		String response = readInput();
		assertTrue(response, response.startsWith("354"));
	}

	private void sendExtendedHello(String hostName) throws IOException
	{
		send("EHLO " + hostName);
		String response = readInput();
		assertTrue(response, response.startsWith("250"));
	}

	private void sendMailFrom(String fromAddress) throws IOException
	{
		send("MAIL FROM:<" + fromAddress + ">");
		String response = readInput();
		assertTrue(response, response.startsWith("250"));
	}

	private void sendQuit() throws IOException
	{
		send("QUIT");
		String response = readInput();
		assertTrue(response, response.startsWith("221"));
	}

	private void sendReceiptTo(String toAddress) throws IOException
	{
		send("RCPT TO:<" + toAddress + ">");
		String response = readInput();
		assertTrue(response, response.startsWith("250"));
	}

	private void sendReset() throws IOException
	{
		send("RSET");
		String response = readInput();
		assertTrue(response, response.startsWith("250"));
	}

	private void send(String msg) throws IOException
	{
		// Force \r\n since println() behaves differently on different platforms
		output.print(msg + "\r\n");
		output.flush();
	}

	private String readInput()
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			do
			{
				sb.append(input.readLine()).append("\n");
			}
			while (input.ready());

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return sb.toString();
	}
}
