package org.subethamail.smtp.test.command;

/**
 * @author Jon Stevens
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class QuitCommandTest extends CommandTestCase
{
	public void testQuitCommand() throws Exception
	{
		assertTrue(session.isActive());

		commandHandler.handleCommand(getContext(), "QUIT");

		assertEquals("221 Bye", getContext().getResponse());

		assertFalse(session.isActive());
	}
}
