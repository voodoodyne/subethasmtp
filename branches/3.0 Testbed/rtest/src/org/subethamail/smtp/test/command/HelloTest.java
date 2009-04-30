package org.subethamail.smtp.test.command;

import org.subethamail.smtp.test.util.ServerTestCase;



/**
 * @author Jeff Schnitzer
 */
public class HelloTest extends ServerTestCase
{
	public HelloTest(String name)
	{
		super(name);
	}
	
	public void testHelloCommand() throws Exception
	{
		expect("220");

		send("HELO");
		expect("501 Syntax: HELO <hostname>");

		send("HELO");
		expect("501 Syntax: HELO <hostname>");
		
		// Correct!
		send("HELO foo.com");
		expect("250");

		// Correct!
		send("HELO foo.com");
		expect("250");
	}

	public void testHelloReset() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: test@foo.com");
		expect("250 Ok");

		send("RSET");
		expect("250 Ok");

		send("MAIL FROM: test@foo.com");
		expect("250 Ok");
	}
}
