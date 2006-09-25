package org.subethamail.smtp.test.command;

import org.subethamail.smtp.test.ServerTestCase;

/**
 * @author Jon Stevens
 */
public class StartTLSTest extends ServerTestCase
{
	public StartTLSTest(String name)
	{
		super(name);
	}

	public void testQuit() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("STARTTLS foo");
		expect("501 Syntax error (no parameters allowed)");

		send("QUIT");
		expect("221 Bye");
	}
}
