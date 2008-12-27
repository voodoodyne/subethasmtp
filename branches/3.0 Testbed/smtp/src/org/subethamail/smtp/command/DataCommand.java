package org.subethamail.smtp.command;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionHandler;
import org.subethamail.smtp.server.Session;
import org.subethamail.smtp.server.io.CharTerminatedInputStream;
import org.subethamail.smtp.server.io.DotUnstuffingInputStream;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class DataCommand extends BaseCommand
{
    private final static char[] SMTP_TERMINATOR = { '\r', '\n', '.', '\r', '\n' };

    public DataCommand()
	{
		super("DATA",
				"Following text is collected as the message.\n"
				+ "End data with <CR><LF>.<CR><LF>");
	}

	@Override
	public void execute(String commandString, ConnectionHandler context) throws IOException
	{
		Session session = context.getSession();

		if (!session.getHasSender())
		{
			context.sendResponse("503 Error: need MAIL command");
			return;
		}
		else if (session.getRecipientCount() == 0)
		{
			context.sendResponse("503 Error: need RCPT command");
			return;
		}

		context.sendResponse("354 End data with <CR><LF>.<CR><LF>");

		InputStream stream = context.getConnection().getRawInput();
		stream = new BufferedInputStream(stream);
		stream = new CharTerminatedInputStream(stream, SMTP_TERMINATOR);
		stream = new DotUnstuffingInputStream(stream);

		try
		{
			session.getMessageHandler().data(stream);
			context.sendResponse("250 Ok");
		}
		catch (RejectException ex)
		{
			context.sendResponse(ex.getMessage());
		}

		session.reset(true); // reset session, but don't require new HELO/EHLO
	}
}
