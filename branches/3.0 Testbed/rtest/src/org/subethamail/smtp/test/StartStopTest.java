package org.subethamail.smtp.test;

import java.util.Properties;

import javax.mail.Session;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.wiser.Wiser;

/**
 * This class attempts to quickly start/stop 10 Wiser
 * servers. It makes sure that the socket bind address is
 * correctly shut down.
 * 
 * @author Jon Stevens
 */
public class StartStopTest extends TestCase
{
	/** */
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(StartStopTest.class);
	
	/** */
	public static final int PORT = 2566;

	/** */
	protected Session session;
	protected int counter = 0;
	
	/** */
	public StartStopTest(String name) { super(name); }
	
	/** */
	protected void setUp() throws Exception
	{
		super.setUp();		

		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "localhost");
		props.setProperty("mail.smtp.port", Integer.toString(PORT));
		this.session = Session.getDefaultInstance(props);
	}
	
	/** */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testMultipleStartStop() throws Exception
	{
		for(int i=0; i<10; i++)
		{
			startStop();
		}
		assertEquals(counter, 10);
	}

	private void startStop() throws Exception
	{
		Wiser wiser = new Wiser();
		wiser.setPort(PORT);
		
		wiser.start();

		wiser.stop();
		
		counter++;
	}

	/** */
	public static Test suite()
	{
		return new TestSuite(StartStopTest.class);
	}
}
