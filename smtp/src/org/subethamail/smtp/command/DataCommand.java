package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class DataCommand extends BaseCommand
{
	public DataCommand()
	{
		super("DATA", "Following text is collected as the message.\n" + "End data with <CR><LF>.<CR><LF>");
	}

	@Override
	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		Session session = context.getSession();

		if (!session.getHasSender())
		{
			context.sendResponse("503 Error: need MAIL command");
			return;
		}
		else if (session.getRecipientCount() == 0)
		{
			context.sendResponse("503 Error: need RCPT command");
			return;
		}

		session.setDataMode(true);
		context.sendResponse("354 End data with <CR><LF>.<CR><LF>");		
	}
}
