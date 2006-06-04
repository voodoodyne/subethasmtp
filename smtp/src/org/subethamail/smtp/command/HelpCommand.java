package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.CommandException;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.SMTPServer;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class HelpCommand extends BaseCommand
{
	public HelpCommand()
	{
		super("HELP",
				"The HELP command gives help info about the topic specified.\n"
					+ "For a list of topics, type HELP by itself.",
				"[ <topic> ]");
	}

	@Override
	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		String args = getArgPredicate(commandString);
		if ("".equals(args))
		{
			context.sendResponse(getCommandMessage((SMTPServer)context.getServer()));
			return;
		}
		try
		{
			context.sendResponse(getHelp(args).toOutputString());
		}
		catch (CommandException e)
		{
			context.sendResponse("504 HELP topic \"" + args + "\" unknown.");
		}
	}

	private String getCommandMessage(SMTPServer server)
	{
		return "214-This is the "
				+ server.getNameVersion()
				+ " server running on "
				+ server.getHostName()
				+ "\r\n"
				+ "214-Topics:\r\n"
				+ getFormattedTopicList()
				+ "214-For more info use \"HELP <topic>\".\r\n"
				+ "214-For more information about this server, visit:\r\n"
				+ "214-    http://subetha.tigris.org\r\n"
				+ "214-To report bugs in the implementation, send email to:\r\n"
				+ "214-    issues@subetha.tigris.org\r\n"
				+ "214-For local information send email to Postmaster at your site.\r\n"
				+ "214 End of HELP info";
	}

	protected String getFormattedTopicList()
	{
		StringBuilder sb = new StringBuilder();
	    for (String key : super.getHelp().keySet())
	    {
	    	sb.append("214-     " + key + "\r\n");
	    }
		return sb.toString();
	}
}
