package org.subethamail.smtp;

import java.io.InputStream;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.util.TextUtils;

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
}
