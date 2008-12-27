package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionHandler;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
public class HelloCommand extends BaseCommand
{
	public HelloCommand()
	{
		super("HELO", "Introduce yourself.", "<hostname>");
	}

	@Override
	public void execute(String commandString, ConnectionHandler sess) throws IOException
	{
		String[] args = getArgs(commandString);
		if (args.length < 2)
		{
			sess.sendResponse("501 Syntax: HELO <hostname>");
			return;
		}
		
		sess.setHasSeenHelo(true);
		sess.sendResponse("250 " + sess.getServer().getHostName());
	}
}
