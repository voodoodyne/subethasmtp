package org.subethamail.smtp.test;



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
		expect("220", true);

		send("HELO");
		expect("501 Syntax: HELO <hostname>", false);

		send("QUIT");

		// Bad syntax
//		c.send("HELO");
//		c.expect("501 Syntax: HELO <hostname>");
		
		// Correct!
//		c.send("HELO foo.com");
//		c.expect("250 127.0.0.1");

		// Correct!
//		c.send("HELO foo.com");
//		c.expect("250 127.0.0.1");
	}

//	public void testHelloReset() throws Exception
//	{
//		Client c = new Client("localhost", PORT);
//		
//		c.send("HELO foo.com");
//		c.expect("250 127.0.0.1");
//
//		c.send("MAIL FROM: test@foo.com");
//		c.expect("250 Ok");
//
//		c.send("RSET");
//		c.expect("250 Ok");
//
//		c.send("MAIL FROM: test@foo.com");
//		c.expect("250 Ok");
//	}
}
