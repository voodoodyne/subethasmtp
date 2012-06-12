package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
public class HelloCommand extends BaseCommand
{
	/** */
	public HelloCommand()
	{
		super("HELO", "Introduce yourself.", "<hostname>");
	}

	/** */
	@Override
	public void execute(String commandString, Session sess) throws IOException
	{
		String[] args = this.getArgs(commandString);
		if (args.length < 2)
		{
			sess.sendResponse("501 Syntax: HELO <hostname>");
			return;
		}

		sess.resetMessageState();
		sess.setHelo(args[1]);

		sess.sendResponse("250 " + sess.getServer().getHostName());
	}
}
