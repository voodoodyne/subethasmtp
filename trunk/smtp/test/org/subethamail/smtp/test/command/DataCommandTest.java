package org.subethamail.smtp.test.command;


/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class DataCommandTest extends CommandTestCase
{
	public void testDataCommand() throws Exception
	{
		commandHandler.handleCommand(getContext(), "DATA");
		assertEquals("503 Error: need MAIL command", getContext().getResponse());
		
		doHelo();
		doMail();
		
		commandHandler.handleCommand(getContext(), "DATA");
		assertEquals("503 Error: need RCPT command", getContext().getResponse());
		
		doRcpt();

		// Need to put this in a try/catch cause the DataCommand
		// will NPE when it tries to get an incomming connection
		// that doesn't exist right now with this test harness.
		try
		{
			commandHandler.handleCommand(getContext(), "DATA");
		}
		catch(Exception e)
		{
			assertEquals("354 End data with <CR><LF>.<CR><LF>", getContext().getResponse());
		}
	}
}
