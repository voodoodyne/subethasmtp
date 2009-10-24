package org.subethamail.smtp.command;

import java.io.IOException;
import java.util.List;

import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;
import org.subethamail.smtp.util.TextUtils;

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
		String[] args = this.getArgs(commandString);
		if (args.length < 2)
		{
			sess.sendResponse("501 Syntax: EHLO hostname");
			return;
		}
		
		sess.setHelo(args[1]);

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

		// Hiding TLS is a server setting
		if (!sess.getServer().getHideTLS())
		{
			response.append("\r\n" + "250-STARTTLS");
		}

		// Check to see if we support authentication
		AuthenticationHandlerFactory authFact = sess.getServer().getAuthenticationHandlerFactory();
		if (authFact != null)
		{
			List<String> supportedMechanisms = authFact.getAuthenticationMechanisms();
			if (!supportedMechanisms.isEmpty())
			{
				response.append("\r\n" + "250-" + AuthCommand.VERB + " ");
				response.append(TextUtils.joinTogether(supportedMechanisms, " "));
			}
		}

		response.append("\r\n" + "250 Ok");

		sess.sendResponse(response.toString());
	}
}
