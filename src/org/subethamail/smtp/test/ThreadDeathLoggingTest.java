package org.subethamail.smtp.test;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

public class ThreadDeathLoggingTest
{
	/**
	 * This test can be used to check if an Error or RuntimeException actually
	 * logged, but it requires manual running. For example remove mail.jar from
	 * the classpath, and check the error log if it contains logged NoClassDef
	 * exception. See also the comment within the function how to check for a
	 * NPE. Note that any exception that causes a thread death is printed on
	 * stderr by the default uncaughtExceptionHandler of the JRE, but this is
	 * not what you are looking for.
	 */
	@Ignore("Requires manual setup and verification")
	@Test()
	public void testNoMailJar() throws SMTPException, IOException
	{
		// if this variable is set to null, than a NPE will be thrown, which is
		// also good for testing.
		MessageHandlerFactory handlerFactory = new SimpleMessageListenerAdapter(new SimpleMessageListener()
		{

			@Override
			public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException,
					IOException
			{
				return;
			}

			@Override
			public boolean accept(String from, String recipient)
			{
				return false;
			}
		});
		SMTPServer smtpServer = new SMTPServer(handlerFactory);
		smtpServer.setPort(0);
		smtpServer.start();
		try
		{
			SmartClient client = new SmartClient("localhost", smtpServer.getPort(), "test-client.example.org");
			client.from("john@exmaple.com");
			client.to("jane@example.org");
			client.quit();
		}
		finally
		{
			smtpServer.stop();
		}
	}
}
