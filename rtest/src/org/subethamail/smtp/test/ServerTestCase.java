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

	/** */
	protected Wiser wiser;
	
	/** */
	public ServerTestCase(String name) { super(name); }

	protected Client c;

	/** */
	protected void setUp() throws Exception
	{
		super.setUp();
		
		this.wiser = new Wiser();
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
