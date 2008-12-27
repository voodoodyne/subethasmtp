package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.BaseCommand;
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
		super("RCPT",
				"Specifies the recipient. Can be used any number of times.",
				"TO: <recipient> [ <parameters> ]");
	}

	@Override
	public void execute(String commandString, Session sess) throws IOException
	{
		if (!sess.getHasMailFrom())
		{
			sess.sendResponse("503 Error: need MAIL command");
			return;
		}
		else if (sess.getRecipientCount() >= sess.getServer().getMaxRecipients())
		{
			sess.sendResponse("452 Error: too many recipients");
			return;
		}

		String args = getArgPredicate(commandString);
		if (!args.toUpperCase().startsWith("TO:"))
		{
			sess.sendResponse(
					"501 Syntax: RCPT TO: <address>  Error in parameters: \""
					+ args + "\"");
			return;
		}
		else
		{
			String recipientAddress = extractEmailAddress(args, 3);
			try
			{
				sess.getMessageHandler().recipient(recipientAddress);
				sess.addRecipient();
				sess.sendResponse("250 Ok");
			}
			catch (RejectException ex)
			{
				sess.sendResponse(ex.getMessage());
			}
		}
	}
}
