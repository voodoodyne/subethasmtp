package org.subethamail.smtp.test;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	
	/** */
	protected void setUp() throws Exception
	{
		super.setUp();
		
		this.wiser = new Wiser();
		this.wiser.setPort(PORT);
		
		// make this really small so that we can promise
		// to have to hit the disk.
		this.wiser.getServer().setDataDeferredSize(10);
		this.wiser.start();
	}
	
	/** */
	protected void tearDown() throws Exception
	{
		this.wiser.stop();
		this.wiser = null;

		super.tearDown();
	}
}
