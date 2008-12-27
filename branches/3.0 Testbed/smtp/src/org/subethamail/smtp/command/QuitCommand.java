package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionHandler;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class QuitCommand extends BaseCommand
{
	public QuitCommand()
	{
		super("QUIT", "Exit the SMTP session.");
	}

	@Override
	public void execute(String commandString, ConnectionHandler context) throws IOException
	{
		context.sendResponse("221 Bye");
		context.getSession().quit();
	}
}
