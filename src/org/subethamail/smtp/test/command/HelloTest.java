package org.subethamail.smtp.test.command;

import org.subethamail.smtp.test.util.ServerTestCase;



/**
 * @author Jeff Schnitzer
 */
public class HelloTest extends ServerTestCase
{
	/** */
	public HelloTest(String name)
	{
		super(name);
	}

	/** */
	public void testHelloCommand() throws Exception
	{
		this.expect("220");

		this.send("HELO");
		this.expect("501 Syntax: HELO <hostname>");

		this.send("HELO");
		this.expect("501 Syntax: HELO <hostname>");

		// Correct!
		this.send("HELO foo.com");
		this.expect("250");

		// Correct!
		this.send("HELO foo.com");
		this.expect("250");
	}

	/** */
	public void testHelloReset() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("MAIL FROM: test@foo.com");
		this.expect("250 Ok");

		this.send("RSET");
		this.expect("250 Ok");

		this.send("MAIL FROM: test@foo.com");
		this.expect("250 Ok");
	}
}
