package org.subethamail.smtp.command;

import junit.framework.Assert;

/**
 * @author Jon Stevens
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class CommandTest extends CommandTestCase
{
	public void testCommandHandling() throws Exception
	{
		commandHandler.handleCommand(getContext(), "blah blah blah");
		Assert.assertEquals("500 Error: command not implemented", getContext().getResponse());
	}
}
