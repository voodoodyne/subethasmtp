package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionHandler;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class NoopCommand extends BaseCommand
{
	public NoopCommand()
	{
		super("NOOP", "The noop command");
	}

	@Override
	public void execute(String commandString, ConnectionHandler context) throws IOException
	{
		context.sendResponse("250 Ok");
	}
}
