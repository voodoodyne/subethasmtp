package org.subethamail.smtp.test;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

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
public class TimeoutTest extends TestCase
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(TimeoutTest.class);

	/** */
	public static final int PORT = 2566;

	/** */
	public TimeoutTest(String name)
	{
		super(name);
	}

	/** */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/** */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testTimeout() throws Exception
	{
		Wiser wiser = new Wiser();
		wiser.setPort(PORT);
		wiser.getServer().setConnectionTimeout(1000);

		wiser.start();

		Socket sock = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStream out = sock.getOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
		
		writer.print("HELO foo\r\n");
		assert(!writer.checkError());
		
		Thread.sleep(2000);
		
		writer.print("HELO bar\r\n");
		assert(writer.checkError());
		
		wiser.stop();
	}

	/** */
	public static Test suite()
	{
		return new TestSuite(TimeoutTest.class);
	}
}
