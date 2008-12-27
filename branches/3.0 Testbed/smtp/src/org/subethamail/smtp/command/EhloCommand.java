package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionHandler;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
public class EhloCommand extends BaseCommand
{
	public EhloCommand()
	{
		super("EHLO", "Introduce yourself.", "<hostname>");
	}

	@Override
	public void execute(String commandString, ConnectionHandler sess) throws IOException
	{
		String[] args = getArgs(commandString);
		if (args.length < 2)
		{
			sess.sendResponse("501 Syntax: EHLO hostname");
			return;
		}
		
//		postfix returns...
//		250-server.host.name
//		250-PIPELINING
//		250-SIZE 10240000
//		250-ETRN
//		250 8BITMIME

		if (!sess.getHasSeenHelo())
		{
			sess.setHasSeenHelo(true);
			String response = "250-" + sess.getServer().getHostName() + "\r\n" + "250-8BITMIME";

			if (sess.getServer().getCommandHandler().containsCommand("STARTTLS"))
			{
				response = response + "\r\n" + "250-STARTTLS";
			}

			if (sess.getServer().getCommandHandler().containsCommand(AuthCommand.VERB))
			{
				response = response + AuthCommand.getEhloString(sess.getServer().getAuthenticationHandlerFactory());
			}
			
			response = response + "\r\n" + "250 Ok";
			sess.sendResponse(response);
		}
		else
		{
			String remoteHost = args[1];
			sess.sendResponse("503 " + remoteHost + " Duplicate EHLO");
		}
	}
}
