package org.subethamail.smtp.test.command;


/**
 * @author Jon Stevens
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class HelloCommandTest extends CommandTestCase
{
	public void testHelloCommand() throws Exception
	{
		// Bad syntax
		commandHandler.handleCommand(getContext(), "HELO");
		assertEquals("501 Syntax: HELO <hostname>", getContext().getResponse());
		
		// Correct!
		commandHandler.handleCommand(getContext(), "HELO foo.com");
		assertEquals("250 127.0.0.1", getContext().getResponse());

		// Duplicate helo
		commandHandler.handleCommand(getContext(), "HELO foo.com");
		assertEquals("503 foo.com Duplicate HELO", getContext().getResponse());
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		session.reset();
	}
}
