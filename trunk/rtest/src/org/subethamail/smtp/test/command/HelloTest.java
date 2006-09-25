package org.subethamail.smtp.test.command;

import org.subethamail.smtp.test.ServerTestCase;



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
		c.expect("220");

		c.send("HELO");
		c.expect("501 Syntax: HELO <hostname>");

		c.send("HELO");
		c.expect("501 Syntax: HELO <hostname>");
		
		// Correct!
		c.send("HELO foo.com");
		c.expect("250 127.0.0.1");

		// Correct!
		c.send("HELO foo.com");
		c.expect("250 127.0.0.1");
	}

	public void testHelloReset() throws Exception
	{
		c.send("HELO foo.com");
		c.expect("250 127.0.0.1");

		c.send("MAIL FROM: test@foo.com");
		c.expect("250 Ok");

		c.send("RSET");
		c.expect("250 Ok");

		c.send("MAIL FROM: test@foo.com");
		c.expect("250 Ok");
	}
}
