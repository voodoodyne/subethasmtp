package org.subethamail.smtp.test.command;


/**
 * @author Michael Wildpaner &lt;mike@wildpaner.com&gt;
 */
public class StartTLSCommandTest extends CommandTestCase
{
	public void testStartTLSCommand() throws Exception
	{
		commandHandler.handleCommand(getContext(), "HELO foo.com");
		assertEquals("250 127.0.0.1", getContext().getResponse());

		commandHandler.handleCommand(getContext(), "STARTTLS foo");
		assertEquals("501 Syntax error (no parameters allowed)", getContext().getResponse());

//		commandHandler.handleCommand(getContext(), "STARTTLS");
//		assertEquals("220 Ready to start TLS", getContext().getResponse());
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		session.reset();
	}
}
