package org.subethamail.smtp.server;

import org.subethamail.smtp.util.Client;
import org.subethamail.smtp.util.ServerTestCase;

/**
 * @author Erik van Oosten
 */
public class RquireTlsTest extends ServerTestCase
{

	/** */
	public RquireTlsTest(String name)
	{
		super(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.subethamail.smtp.ServerTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		this.wiser = new TestWiser();
		this.wiser.setHostname("localhost");
		this.wiser.setPort(PORT);
		this.wiser.getServer().setRequireTLS(true);

		this.wiser.start();
		this.c = new Client("localhost", PORT);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.subethamail.smtp.ServerTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	/** */
	public void testNeedSTARTTLS() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("530 Must issue a STARTTLS command first");

		this.send("EHLO foo.com");
		this.expect("250");

		this.send("NOOP");
		this.expect("250");

		this.send("MAIL FROM: test@example.com");
		this.expect("530 Must issue a STARTTLS command first");

		this.send("STARTTLS foo");
		this.expect("501 Syntax error (no parameters allowed)");

		this.send("QUIT");
		this.expect("221 Bye");
	}

}