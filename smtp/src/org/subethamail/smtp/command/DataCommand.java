package org.subethamail.smtp.command;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.BaseCommand;
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
    private final static int BUFFER_SIZE = 1024 * 32;	// 32k seems reasonable

    public DataCommand()
	{
		super("DATA",
				"Following text is collected as the message.\n"
				+ "End data with <CR><LF>.<CR><LF>");
	}

	@Override
	public void execute(String commandString, Session sess) throws IOException
	{
		if (!sess.getHasMailFrom())
		{
			sess.sendResponse("503 Error: need MAIL command");
			return;
		}
		else if (sess.getRecipientCount() == 0)
		{
			sess.sendResponse("503 Error: need RCPT command");
			return;
		}

		sess.sendResponse("354 End data with <CR><LF>.<CR><LF>");

		InputStream stream = sess.getRawInput();
		stream = new BufferedInputStream(stream, BUFFER_SIZE);
		stream = new CharTerminatedInputStream(stream, SMTP_TERMINATOR);
		stream = new DotUnstuffingInputStream(stream);

		try
		{
			sess.getMessageHandler().data(stream);
			sess.sendResponse("250 Ok");
		}
		catch (RejectException ex)
		{
			sess.sendResponse(ex.getMessage());
		}

		sess.resetMessageState(); // reset session, but don't require new HELO/EHLO
	}
}
