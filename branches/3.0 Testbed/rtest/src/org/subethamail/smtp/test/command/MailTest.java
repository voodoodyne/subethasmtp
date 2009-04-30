package org.subethamail.smtp.test.command;

import org.subethamail.smtp.test.util.ServerTestCase;

/**
 * @author Jon Stevens
 */
public class MailTest extends ServerTestCase
{
	public MailTest(String name)
	{
		super(name);
	}

	public void testMailNoHello() throws Exception
	{
		expect("220");

		send("MAIL FROM: test@example.com");
		expect("250");
	}

	public void testAlreadySpecified() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: test@example.com");
		expect("250 Ok");

		send("MAIL FROM: another@example.com");
		expect("503 Sender already specified.");
	}

	public void testInvalidSenders() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: test@lkjsd lkjk");
		expect("553 <test@lkjsd lkjk> Invalid email address.");
	}

	public void testMalformedMailCommand() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL");
		expect("501 Syntax: MAIL FROM: <address>  Error in parameters:");
	}

	public void testEmptyFromCommand() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: <>");
		expect("250");
	}

	public void testEmptyEmailFromCommand() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM:");
		expect("501 Syntax: MAIL FROM: <address>");
	}
	
	public void testMailWithoutWhitespace() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM:<validuser@subethamail.org>");
		expect("250 Ok");
	}
}
