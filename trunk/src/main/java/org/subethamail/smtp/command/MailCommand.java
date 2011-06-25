package org.subethamail.smtp.command;

import java.io.IOException;
import java.util.Locale;

import org.subethamail.smtp.DropConnectionException;
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
	/** */
	public MailCommand()
	{
		super("MAIL",
				"Specifies the sender.",
				"FROM: <sender> [ <parameters> ]");
	}

	/** */
	/* (non-Javadoc)
	 * @see org.subethamail.smtp.server.BaseCommand#execute(java.lang.String, org.subethamail.smtp.server.Session)
	 */
	@Override
	public void execute(String commandString, Session sess) throws IOException
	{
		if (sess.getHasMailFrom())
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

			String args = this.getArgPredicate(commandString);
			if (!args.toUpperCase(Locale.ENGLISH).startsWith("FROM:"))
			{
				sess.sendResponse(
						"501 Syntax: MAIL FROM: <address>  Error in parameters: \"" +
						this.getArgPredicate(commandString) + "\"");
				return;
			}

			String emailAddress = EmailUtils.extractEmailAddress(args, 5);
			if (EmailUtils.isValidEmailAddress(emailAddress))
			{
				// extract SIZE argument from MAIL FROM command.
				// disregard unknown parameters. TODO: reject unknown
				// parameters.
				int size = 0;
				String largs = args.toLowerCase(Locale.ENGLISH);
				int sizec = largs.indexOf(" size=");
				if (sizec > -1)
				{
					// disregard non-numeric values.
					String ssize = largs.substring(sizec + 6).trim();
					if (ssize.length() > 0 && ssize.matches("[0-9]+"))
					{
						size = Integer.parseInt(ssize);
					}
				}
				// Reject the message if the size supplied by the client
				// is larger than what we advertised in EHLO answer.
				if (size > sess.getServer().getMaxMessageSize())
				{
					sess.sendResponse("552 5.3.4 Message size exceeds fixed limit");
					return;
				}
				
				try
				{
					sess.getMessageHandler().from(emailAddress);
					sess.setDeclaredMessageSize(size);
					sess.setHasMailFrom(true);
					sess.sendResponse("250 Ok");
				}
				catch (DropConnectionException ex)
				{
					throw ex; // Propagate this
				}
				catch (RejectException ex)
				{
					sess.sendResponse(ex.getErrorResponse());
				}
			}
			else
			{
				sess.sendResponse("553 <" + emailAddress + "> Invalid email address.");
			}
		}
	}
}
