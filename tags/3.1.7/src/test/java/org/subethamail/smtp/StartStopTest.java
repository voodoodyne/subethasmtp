package org.subethamail.smtp;

import java.util.Properties;

import javax.mail.Session;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;

/**
 * This class attempts to quickly start/stop 10 Wiser servers. It makes sure that the socket bind address is correctly
 * shut down.
 *
 * @author Jon Stevens
 */
public class StartStopTest extends TestCase
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(StartStopTest.class);

	/** */
	public static final int PORT = 2566;

	/** */
	protected Session session;

	protected int counter = 0;

	/** */
	public StartStopTest(String name)
	{
		super(name);
	}

	/** */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		props.setProperty("mail.smtp.port", Integer.toString(PORT));
		this.session = Session.getDefaultInstance(props);
	}

	/** */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	/** */
	public void testMultipleStartStop() throws Exception
	{
		for (int i = 0; i < 10; i++)
		{
			this.startStop(i > 5);
		}
		assertEquals(this.counter, 10);
	}

	/** */
	private void startStop(boolean pause) throws Exception
	{
		Wiser wiser = new Wiser();
		wiser.setPort(PORT);

		wiser.start();

		if (pause)
			Thread.sleep(1000);

		wiser.stop();

		this.counter++;
	}

	/** */
	public static Test suite()
	{
		return new TestSuite(StartStopTest.class);
	}
}
