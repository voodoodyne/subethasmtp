package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;
import org.subethamail.smtp.util.EmailUtils;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Scott Hernandez
 * @author Jeff Schnitzer
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
	public void execute(String commandString, Session sess) throws IOException
	{
		if (!sess.getHasSeenHelo())
		{
			sess.sendResponse("503 Error: send HELO/EHLO first");
		}
		else if (sess.getHasMailFrom())
		{
			sess.sendResponse("503 Sender already specified.");
		}
		else
		{
			if (commandString.trim().equals("MAIL FROM:"))
			{
				sess.sendResponse("501 Syntax: MAIL FROM: <address>");
				return;
			}

			String args = getArgPredicate(commandString);
			if (!args.toUpperCase().startsWith("FROM:"))
			{
				sess.sendResponse(
						"501 Syntax: MAIL FROM: <address>  Error in parameters: \"" +
						getArgPredicate(commandString) + "\"");
				return;
			}
			
			String emailAddress = extractEmailAddress(args, 5);
			if (EmailUtils.isValidEmailAddress(emailAddress))
			{
				try
				{
					sess.getMessageHandler().from(emailAddress);
					sess.setHasMailFrom(true);
					sess.sendResponse("250 Ok");
				}
				catch (RejectException ex)
				{
					sess.sendResponse(ex.getMessage());
				}
			}
			else
			{
				sess.sendResponse("553 <" + emailAddress + "> Invalid email address.");
			}
		}
	}
}
