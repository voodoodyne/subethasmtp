package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionHandler;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class QuitCommand extends BaseCommand
{
	public QuitCommand()
	{
		super("QUIT", "Exit the SMTP session.");
	}

	@Override
	public void execute(String commandString, ConnectionHandler sess) throws IOException
	{
		sess.sendResponse("221 Bye");
		sess.quit();
	}
}
