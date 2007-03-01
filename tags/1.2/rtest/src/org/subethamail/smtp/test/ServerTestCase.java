package org.subethamail.smtp.test;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.test.util.Client;
import org.subethamail.wiser.Wiser;

/**
 * A base class for testing the SMTP server at the raw protocol level.
 * Handles setting up and tearing down of the server.
 * 
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class ServerTestCase extends TestCase
{
	/** */
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(ServerTestCase.class);
	
	/** */
	public static final int PORT = 2566;

	/**
	 * Override the accept method in Wiser so we can test
	 * the accept method().
	 */
	public class TestWiser extends Wiser
	{
		public boolean accept(String from, String recipient)
		{
			if (recipient.equals("failure@subethamail.org"))
			{
				return false;
			}
			else if (recipient.equals("success@subethamail.org"))
			{
				return true;
			}
			return true;
		}
	}
	
	/** */
	protected TestWiser wiser;
	
	/** */
	protected Client c;

	/** */
	public ServerTestCase(String name)
	{
		super(name);
	}

	/** */
	protected void setUp() throws Exception
	{
		super.setUp();
		
		this.wiser = new TestWiser();
		this.wiser.setHostname("localhost");
		this.wiser.setPort(PORT);
		this.wiser.start();
		
		c = new Client("localhost", PORT);
	}
	
	/** */
	protected void tearDown() throws Exception
	{
		this.wiser.stop();
		this.wiser = null;

		c.close();

		super.tearDown();
	}
	
	public void send(String msg) throws Exception
	{
		c.send(msg);
	}

	public void expect(String msg) throws Exception
	{
		c.expect(msg);
	}
}
