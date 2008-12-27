package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionHandler;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class ResetCommand extends BaseCommand
{
	public ResetCommand()
	{
		super("RSET", "Resets the system.");
	}

	@Override
	public void execute(String commandString, ConnectionHandler sess) throws IOException
	{
		sess.resetMessageState();

		sess.sendResponse("250 Ok");
	}
}
