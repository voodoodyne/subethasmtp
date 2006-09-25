package org.subethamail.smtp.test.command;

import org.subethamail.smtp.test.ServerTestCase;

/**
 * @author Jon Stevens
 */
public class CommandTest extends ServerTestCase
{
	public CommandTest(String name)
	{
		super(name);
	}

	public void testCommandHandling() throws Exception
	{
		expect("220");

		send("blah blah blah");
		expect("500 Error: command not implemented");
	}
}
