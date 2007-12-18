package org.subethamail.smtp.command;

import java.io.IOException;
import java.util.List;

import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;

/**
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * 
 * Updated to comply with MINA integration.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class AuthCommand extends BaseCommand
{
	public static final String VERB = "AUTH";
	public static final String AUTH_CANCEL_COMMAND = "*";

	static String getEhloString(MessageHandler handler)
	{
		List<String> supportedMechanisms = handler
				.getAuthenticationMechanisms();
		if (supportedMechanisms.isEmpty())
			return "";
		else
		{
            StringBuilder sb = new StringBuilder(30);
            sb.append("\r\n");
            sb.append("250-");
            sb.append(VERB);
            sb.append(' ');
            getTokenizedString(sb, supportedMechanisms, " ");
            
			return sb.toString();
		}
	}

	/** Creates a new instance of AuthCommand */
	public AuthCommand()
	{
		super(
				VERB,
				"Authentication service",
				VERB
						+ " <mechanism> [initial-response] \n"
						+ "\t mechanism = a string identifying a SASL authentication mechanism,\n"
						+ "\t an optional base64-encoded response");
	}

	@Override
	public void execute(String commandString, ConnectionContext context)
			throws IOException
	{
		if (context.getSession().isAuthenticated())
		{
			context.sendResponse("503 Refusing any other AUTH command.");
			return;
		}
		MessageHandler msgHandler = getMessageHandler(context);
		boolean authenticating = context.getSession().isAuthenticating();
		
		if (!authenticating)
		{
			String[] args = getArgs(commandString);
			// Let's check the command syntax
			if (args.length < 2)
			{
				context.sendResponse("501 Syntax: " + VERB
						+ " mechanism [initial-response]");
				return;
			}
			// Let's check if we support the required authentication mechanism
			String mechanism = args[1];
			if (!msgHandler.getAuthenticationMechanisms().contains(
					mechanism.toUpperCase()))
			{
				context
						.sendResponse("504 The requested authentication mechanism is not supported");
				return;
			}
		}
		
		// OK, let's go through the authentication process.
		// The authentication process may require a series of
		// challenge-responses
		try
		{						
			if (authenticating && commandString.trim().equals(AUTH_CANCEL_COMMAND))
			{
				// RFC 2554 explicitly states this:
				context.sendResponse("501 Authentication canceled by client.");
				return;
			}
			
			StringBuilder response = new StringBuilder();
			boolean finished = msgHandler.auth(commandString, response);
			
			context.getSession().setAuthenticating(!finished);
			
			if (!finished)
			{
				// challenge-response iteration
				context.sendResponse(response.toString());				
				return;
			}

			context.sendResponse("235 Authentication successful.");
			context.getSession().setAuthenticated(true);
		}
		catch (RejectException authFailed)
		{
			context.sendResponse("535 Authentication failure.");
			context.getSession().setAuthenticated(false);
			context.getSession().setAuthenticating(false);
		}
	}

	public MessageHandler getMessageHandler(ConnectionContext context)
	{
		return context.getSession().getMessageHandler();
	}
}
