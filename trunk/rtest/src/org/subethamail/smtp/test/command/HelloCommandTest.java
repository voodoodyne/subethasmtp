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

		// Correct!
		commandHandler.handleCommand(getContext(), "HELO foo.com");
		assertEquals("250 127.0.0.1", getContext().getResponse());
	}

	public void testHelloReset() throws Exception
	{
		commandHandler.handleCommand(getContext(), "HELO foo.com");
		assertEquals("250 127.0.0.1", getContext().getResponse());

		commandHandler.handleCommand(getContext(), "MAIL FROM: test@foo.com");
		assertEquals("250 Ok", getContext().getResponse());

		commandHandler.handleCommand(getContext(), "RSET");
		assertEquals("250 Ok", getContext().getResponse());

		commandHandler.handleCommand(getContext(), "MAIL FROM: test@foo.com");
		assertEquals("250 Ok", getContext().getResponse());
	}
	
	
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		session.reset();
	}
}
