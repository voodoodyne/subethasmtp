package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class MailCommand extends BaseCommand
{
	public MailCommand()
	{
		super("MAIL", 
				"Specifies the sender.",
				"FROM: <sender> [ <parameters> ]");
	}

	@Override
	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		Session session = context.getSession();
		if (!session.getHasSeenHelo())
		{
			context.sendResponse("503 Error: send HELO/EHLO first");
		}
		else if (session.getHasSender())
		{
			context.sendResponse("503 Sender already specified.");
		}
		else
		{
			String args = getArgPredicate(commandString);
			if (!args.toUpperCase().startsWith("FROM:"))
			{
				context.sendResponse(
						"501 Syntax: MAIL FROM: <address>  Error in parameters: \"" +
						getArgPredicate(commandString) + "\"");
				return;
			}
			
			String emailAddress = extractEmailAddress(args, 5);
			if (isValidEmailAddress(emailAddress))
			{
				try
				{
					session.getMessageHandler().from(emailAddress);
					session.setHasSender(true);
					context.sendResponse("250 Ok");
				}
				catch (RejectException ex)
				{
					context.sendResponse(ex.getMessage());
				}
			}
			else
			{
				context.sendResponse("553 <" + emailAddress + "> Invalid email address.");
			}
		}
	}
}
