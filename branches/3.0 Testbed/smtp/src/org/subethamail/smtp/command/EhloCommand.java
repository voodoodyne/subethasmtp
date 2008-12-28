package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;

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
	public void execute(String commandString, Session sess) throws IOException
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

		// Once upon a time this code tracked whether or not HELO/EHLO has been seen
		// already and gave an error msg.  However, this is stupid and pointless.
		// Postfix doesn't care, so we won't either.  If you want more, read:
		// http://homepages.tesco.net/J.deBoynePollard/FGA/smtp-avoid-helo.html
		
		StringBuilder response = new StringBuilder();
		
		response.append("250-");
		response.append(sess.getServer().getHostName());
		response.append("\r\n" + "250-8BITMIME");

		if (!sess.getServer().getHideTLS() && sess.getServer().getCommandHandler().containsCommand("STARTTLS"))
		{
			response.append("\r\n" + "250-STARTTLS");
		}

		if (sess.getServer().getCommandHandler().containsCommand(AuthCommand.VERB))
		{
			response.append(AuthCommand.getEhloString(sess.getServer().getAuthenticationHandlerFactory()));
		}
		
		response.append("\r\n" + "250 Ok");
		
		sess.sendResponse(response.toString());
	}
}
