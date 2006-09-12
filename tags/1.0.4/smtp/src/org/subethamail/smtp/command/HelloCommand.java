package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class HelloCommand extends BaseCommand
{
	public HelloCommand()
	{
		super("HELO", "Introduce yourself.", "<hostname>");
	}

	@Override
	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		String[] args = getArgs(commandString);
		if (args.length < 2)
		{
			context.sendResponse("501 Syntax: HELO <hostname>");
			return;
		}
		
		Session session = context.getSession();
		session.setHasSeenHelo(true);
		context.sendResponse("250 " + context.getServer().getHostName());
	}
}
