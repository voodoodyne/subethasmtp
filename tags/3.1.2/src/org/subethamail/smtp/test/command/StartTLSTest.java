package org.subethamail.smtp.test.command;

import org.subethamail.smtp.test.util.ServerTestCase;

/**
 * @author Jon Stevens
 */
public class StartTLSTest extends ServerTestCase
{
	/** */
	public StartTLSTest(String name)
	{
		super(name);
	}

	/** */
	public void testQuit() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("STARTTLS foo");
		this.expect("501 Syntax error (no parameters allowed)");

		this.send("QUIT");
		this.expect("221 Bye");
	}
}
