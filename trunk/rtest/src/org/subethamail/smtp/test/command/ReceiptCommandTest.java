package org.subethamail.smtp.test.command;


/**
 * @author Jon Stevens
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class ReceiptCommandTest extends CommandTestCase
{
	public void testReceiptCommand() throws Exception
	{
		assertNull(session.getSender());

		doHelo();

		commandHandler.handleCommand(getContext(), "RCPT TO: test@subethamail.org");
		assertEquals("503 Error: need MAIL command", getContext().getResponse());

		doMail();

		commandHandler.handleCommand(getContext(), "RCPT");
		assertTrue(getContext().getResponse()
				.startsWith("501 Syntax: RCPT TO: <address>  Error in parameters:"));

		assertEquals(0, getSession().getDeliveries().size());
		
		commandHandler.handleCommand(getContext(), "RCPT TO: test@subethamail.org");
		assertEquals("553 <test@subethamail.org> address unknown.", getContext().getResponse());

		assertEquals(0, getSession().getDeliveries().size());
		
		commandHandler.handleCommand(getContext(), "RCPT TO: validuser@subethamail.org");
		assertEquals("250 Ok", getContext().getResponse());
		assertEquals(1, getSession().getDeliveries().size());
	}

	public void testRcptWithoutWhitespace() throws Exception
	{
		doHelo();
		doMail();

		commandHandler.handleCommand(getContext(), "RCPT TO:<validuser@subethamail.org>");
		assertEquals("250 Ok", getContext().getResponse());
		assertEquals(1, session.getDeliveries().size());
	}
}
