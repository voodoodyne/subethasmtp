package org.subethamail.smtp.command;

import java.io.IOException;
import java.io.InputStream;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.Session;

/**
 * Data command splitted to adapt to MINA framework.
 * 
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class DataEndCommand extends BaseCommand
{
	public DataEndCommand()
	{
		super("DATA_END", "Marks the end of data reception when <CR><LF>.<CR><LF> received");
	}

	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		Session session = context.getSession();
		InputStream stream = context.getInputStream();

		try
		{
			session.getMessageHandler().data(stream);
			session.reset(true); // reset session, but don't require new HELO/EHLO
			context.sendResponse("250 Ok");
		}
		catch (RejectException ex)
		{
			session.reset(true); // reset session, but don't require new HELO/EHLO
			context.sendResponse(ex.getMessage());
		}
	}
}