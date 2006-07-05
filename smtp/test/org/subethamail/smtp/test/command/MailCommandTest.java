package org.subethamail.smtp.test.command;

/**
 * @author Jon Stevens
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class MailCommandTest extends CommandTestCase
{
	public void testMailCommand() throws Exception
	{
		doHelo();

		assertNull(getSession().getSender());

		commandHandler.handleCommand(getContext(), "MAIL FROM: test@example.com");
		assertEquals("250 Ok", getContext().getResponse());

		assertEquals("test@example.com", getSession().getSender());

		commandHandler.handleCommand(getContext(), "MAIL FROM: another@example.com");
		assertEquals("503 Sender already specified.", getContext().getResponse());

		assertEquals("test@example.com", session.getSender());
		
		session.reset();
	}

	public void testInvalidSenders() throws Exception
	{
		doHelo();

		commandHandler.handleCommand(getContext(), "MAIL FROM: test@lkjsd lkjk");
		assertEquals("553 <test@lkjsd lkjk> Invalid email address.", getContext().getResponse());
		
		session.reset();
	}

	public void testMalformedMailCommand() throws Exception
	{
		doHelo();
		
		commandHandler.handleCommand(getContext(), "MAIL");
		assertTrue(getContext().getResponse()
				.startsWith("501 Syntax: MAIL FROM: <address>  Error in parameters:"));

		session.reset();
	}

	public void testMailWithoutWhitespace() throws Exception
	{
		doHelo();
		
		commandHandler.handleCommand(getContext(), "MAIL FROM:<validuser@subethamail.org>");
		assertEquals("250 Ok", getContext().getResponse());
		assertEquals("validuser@subethamail.org", session.getSender());

		session.reset();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		session.reset();
	}
}
