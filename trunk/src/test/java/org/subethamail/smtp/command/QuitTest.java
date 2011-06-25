package org.subethamail.smtp.command;

import org.subethamail.smtp.util.ServerTestCase;

/**
 * @author Jon Stevens
 */
public class QuitTest extends ServerTestCase
{
	/** */
	public QuitTest(String name)
	{
		super(name);
	}

	/** */
	public void testQuit() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("MAIL FROM: test@example.com");
		this.expect("250 Ok");

		this.send("QUIT");
		this.expect("221 Bye");
	}
}
