package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionHandler;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class NoopCommand extends BaseCommand
{
	public NoopCommand()
	{
		super("NOOP", "The noop command");
	}

	@Override
	public void execute(String commandString, ConnectionHandler sess) throws IOException
	{
		sess.sendResponse("250 Ok");
	}
}
