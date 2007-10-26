package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class ReceiptCommand extends BaseCommand
{
	public ReceiptCommand()
	{
		super("RCPT", "Specifies the recipient. Can be used any number of times.", "TO: <recipient> [ <parameters> ]");
	}

	@Override
	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		Session session = context.getSession();
		if (!session.getHasSender())
		{
			context.sendResponse("503 Error: need MAIL command");
			return;
		}
		else if (session.getRecipientCount() >= context.getSMTPServer().getMaxRecipients())
		{
			context.sendResponse("452 Error: too many recipients");
			return;
		}

		String args = getArgPredicate(commandString);
		if (!args.toUpperCase().startsWith("TO:"))
		{
			context.sendResponse("501 Syntax: RCPT TO: <address>  Error in parameters: \"" + args + "\"");
			return;
		}
		else
		{
			String recipientAddress = extractEmailAddress(args, 3);
			try
			{
				session.getMessageHandler().recipient(recipientAddress);
				session.addRecipient();
				context.sendResponse("250 Ok");
			}
			catch (RejectException ex)
			{
				context.sendResponse(ex.getMessage());
			}
		}
	}
}
