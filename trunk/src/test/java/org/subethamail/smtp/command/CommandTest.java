package org.subethamail.smtp.command;

import org.subethamail.smtp.util.ServerTestCase;

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
		this.expect("220");

		this.send("blah blah blah");
		this.expect("500 Error: command not implemented");
	}
}
