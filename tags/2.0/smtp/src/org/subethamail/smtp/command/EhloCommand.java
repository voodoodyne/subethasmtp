package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class EhloCommand extends BaseCommand
{
	public EhloCommand()
	{
		super("EHLO", "Introduce yourself.", "<hostname>");
	}

	@Override
	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		String[] args = getArgs(commandString);
		if (args.length < 2)
		{
			context.sendResponse("501 Syntax: EHLO hostname");
			return;
		}

		//		postfix returns...
		//		250-server.host.name
		//		250-PIPELINING
		//		250-SIZE 10240000
		//		250-ETRN
		//		250 8BITMIME

		Session session = context.getSession();
		StringBuilder response = new StringBuilder();
		if (!session.getHasSeenHelo())
		{
			session.setHasSeenHelo(true);
			response.append("250-");
			response.append(context.getSMTPServer().getHostName());
			response.append("\r\n");
			response.append("250-8BITMIME");

			if (context.getSMTPServer().getCommandHandler().containsCommand("STARTTLS"))
			{
				response.append("\r\n").append("250-STARTTLS");
			}

			if (context.getSMTPServer().getCommandHandler().containsCommand(AuthCommand.VERB))
			{
				response.append(AuthCommand.getEhloString(context.getSession().getMessageHandler()));
			}
			response.append("\r\n");
			response.append("250 Ok");
		}
		else
		{
			String remoteHost = args[1];
			response.append("503 ");
			response.append(remoteHost);
			response.append(" Duplicate EHLO");
		}
		context.sendResponse(response.toString());
	}
}
