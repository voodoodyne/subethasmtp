package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
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
	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		Session session = context.getSession();
		if (session.getSender() == null)
		{
			context.sendResponse("503 Error: need MAIL command");
			return;
		}
		else if (session.getDeliveries().size() >= context.getServer().getMaxRecipients())
		{
			context.sendResponse("452 Error: too many recipients");
			return;
		}

		String args = getArgPredicate(commandString);
		if (!args.toUpperCase().startsWith("TO:"))
		{
			context.sendResponse(
					"501 Syntax: RCPT TO: <address>  Error in parameters: \""
					+ args + "\"");
			return;
		}
		else
		{
			String recipientAddress = extractEmailAddress(args, 3);
			if (handleRecipient(recipientAddress, context))
			{
				context.sendResponse("250 Ok");
			}
			else
			{
				context.sendResponse("553 <" + recipientAddress + "> address unknown.");
			}
		}
	}

	/**
	 * Loops through all of the MessageListeners and executes the accept()
	 * method on them. If true, then the MessageListener is added to the session
	 * for later delivery() after the DATA has been received.
	 * 
	 * @param recipientAddress
	 * @param context
	 * @return false if the recipientAddress is unknown.
	 */
	private boolean handleRecipient(String recipientAddress, ConnectionContext context)
	{
		Session session = context.getSession();
		boolean addedListener = false;

		for (MessageListener listener : ((SMTPServer)context.getServer()).getListeners())
		{
			if (listener.accept(session.getSender(), recipientAddress))
			{
				session.addListener(listener, recipientAddress);
				addedListener = true;
			}
		}

		return addedListener;
	}
}
