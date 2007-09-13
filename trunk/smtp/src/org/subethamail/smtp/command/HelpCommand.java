package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.CommandException;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.SMTPServer;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class HelpCommand extends BaseCommand
{
	public HelpCommand()
	{
		super("HELP", "The HELP command gives help info about the topic specified.\n"
						+ "For a list of topics, type HELP by itself.", "[ <topic> ]");
	}

	@Override
	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		String args = getArgPredicate(commandString);
		if ("".equals(args))
		{
			context.sendResponse(getCommandMessage((SMTPServer)context.getSMTPServer()));
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
		StringBuilder response = new StringBuilder();
		response.append("214-This is the ");
		response.append(server.getNameVersion());
		response.append(" server running on ");
		response.append(server.getHostName());
		response.append("\r\n");
		response.append("214-Topics:\r\n");
		getFormattedTopicList(response);
		response.append("214-For more info use \"HELP <topic>\".\r\n");
		response.append("214-For more information about this server, visit:\r\n");
		response.append("214-    http://subetha.tigris.org\r\n");
		response.append("214-To report bugs in the implementation, send email to:\r\n");
		response.append("214-    issues@subetha.tigris.org\r\n");
		response.append("214-For local information send email to Postmaster at your site.\r\n");
		response.append("214 End of HELP info");

		return response.toString();
	}

	protected void getFormattedTopicList(StringBuilder sb)
	{
		for (String key : super.getHelp().keySet())
		{
			sb.append("214-     ");
			sb.append(key);
			sb.append("\r\n");
		}
	}
}
