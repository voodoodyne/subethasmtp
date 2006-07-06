package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class ResetCommand extends BaseCommand
{
	public ResetCommand()
	{
		super("RSET", "Resets the system.");
	}

	@Override
	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		Session session = context.getSession();
		session.reset();
		context.sendResponse("250 Ok");
	}
}
