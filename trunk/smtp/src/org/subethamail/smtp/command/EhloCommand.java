package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
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
		if (!session.getHasSeenHelo())
		{
			session.setHasSeenHelo(true);
			String response = "250-" + context.getServer().getHostName() + "\r\n" + 
								"250-8BITMIME";
			if (context.getServer().getCommandHandler().containsCommand("STARTTLS"))
			{
				response = response + "\r\n" + "250 STARTTLS";
			}
			context.sendResponse(response);
		}
		else
		{
			String remoteHost = args[1];
			context.sendResponse("503 " + remoteHost + " Duplicate EHLO");
		}
	}
}
