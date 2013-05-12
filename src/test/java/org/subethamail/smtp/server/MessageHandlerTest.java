package org.subethamail.smtp.server;

import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.smtp.util.TextUtils;

/**
 * This class tests whether the event handler methods defined in MessageHandler 
 * are called at the appropriate times and in good order.  
 */
public class MessageHandlerTest {
	@Mocked
	private MessageHandlerFactory messageHandlerFactory;

	@Mocked
	private MessageHandler messageHandler;

	@Mocked
	private MessageHandler messageHandler2;

	private SMTPServer smtpServer;

	@Before
	public void setup() {
		smtpServer = new SMTPServer(messageHandlerFactory);
		smtpServer.setPort(2566);
		smtpServer.start();
	}

	@Test
	public void testCompletedMailTransaction() throws Exception {

		new Expectations() {
			{
				messageHandlerFactory.create((MessageContext) any);
				result = messageHandler;

				messageHandler.from(anyString);
				messageHandler.recipient(anyString);
				messageHandler.data((InputStream) any);
				messageHandler.done();
			}
		};

		SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
				"localhost");
		client.from("john@example.com");
		client.to("jane@example.com");
		client.dataStart();
		client.dataWrite(TextUtils.getAsciiBytes("body"), 4);
		client.dataEnd();
		client.quit();
		smtpServer.stop(); // wait for the server to catch up
	}

	@Test
	public void testDisconnectImmediately() throws Exception {

		new Expectations() {
			{
				messageHandlerFactory.create((MessageContext) any);
				times = 0;
			}
		};

		SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
				"localhost");
		client.quit();
		smtpServer.stop(); // wait for the server to catch up
	}

	@Test
	public void testAbortedMailTransaction() throws Exception {

		new Expectations() {
			{
				messageHandlerFactory.create((MessageContext) any);
				result = messageHandler;

				messageHandler.from(anyString);
				messageHandler.done();
			}
		};

		SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
				"localhost");
		client.from("john@example.com");
		client.quit();
		smtpServer.stop(); // wait for the server to catch up
	}

	@Test
	public void testTwoMailsInOneSession() throws Exception {

		new Expectations() {
			{
				messageHandlerFactory.create((MessageContext) any);
				result = messageHandler;

				onInstance(messageHandler).from(anyString);
				onInstance(messageHandler).recipient(anyString);
				onInstance(messageHandler).data((InputStream) any);
				onInstance(messageHandler).done();

				messageHandlerFactory.create((MessageContext) any);
				result = messageHandler2;

				onInstance(messageHandler2).from(anyString);
				onInstance(messageHandler2).recipient(anyString);
				onInstance(messageHandler2).data((InputStream) any);
				onInstance(messageHandler2).done();
			}
		};

		SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
				"localhost");

		client.from("john1@example.com");
		client.to("jane1@example.com");
		client.dataStart();
		client.dataWrite(TextUtils.getAsciiBytes("body1"), 5);
		client.dataEnd();

		client.from("john2@example.com");
		client.to("jane2@example.com");
		client.dataStart();
		client.dataWrite(TextUtils.getAsciiBytes("body2"), 5);
		client.dataEnd();

		client.quit();

		smtpServer.stop(); // wait for the server to catch up
	}
	
	/**
	 * Test for issue 56: rejecting a Mail From causes IllegalStateException in
	 * the next Mail From attempt.
	 * @see <a href=http://code.google.com/p/subethasmtp/issues/detail?id=56>Issue 56</a>
	 */
	@Test
	public void testMailFromRejectedFirst() throws IOException, MessagingException
	{
		new Expectations() {
			{
				messageHandlerFactory.create((MessageContext) any);
				result = messageHandler;

				onInstance(messageHandler).from(anyString);
				result = new RejectException("Test MAIL FROM rejection");
				onInstance(messageHandler).done();

				messageHandlerFactory.create((MessageContext) any);
				result = messageHandler2;

				onInstance(messageHandler2).from(anyString);
				onInstance(messageHandler2).done();
			}
		};

		SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
				"localhost");

		boolean expectedRejectReceived = false;
		try {
			client.from("john1@example.com");
		} catch (SMTPException e) {
			expectedRejectReceived = true;
		}
		Assert.assertTrue(expectedRejectReceived);
		
		client.from("john2@example.com");
		client.quit();

		smtpServer.stop(); // wait for the server to catch up
		
	}
	
}
