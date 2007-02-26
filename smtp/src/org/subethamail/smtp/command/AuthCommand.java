package org.subethamail.smtp.command;

import java.io.IOException;
import java.util.List;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.io.CRLFTerminatedReader;

/**
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
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
		{
			return "";
		}
		else
		{
			return "\r\n" + "250-" + VERB + " "
					+ getTokenizedString(supportedMechanisms, " ");
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
		// OK, let's go trough the authentication process.
		try
		{
			StringBuffer response = new StringBuffer();
			// The authentication process may require a series of
			// challenge-responses
			CRLFTerminatedReader reader = instantiateReader(context);
			boolean finished = msgHandler.auth(commandString, response);
			if (!finished)
			{
				// challenge-response iteration
				context.sendResponse(response.toString());
			}
			while (!finished)
			{
				response = new StringBuffer();
				String clientInput = reader.readLine();
				if (clientInput.trim().equals(AUTH_CANCEL_COMMAND))
				{
					// RFC 2554 explicitly states this:
					context.sendResponse("501 Authentication canceled by client.");
					return;
				}
				else
				{
					finished = msgHandler.auth(clientInput, response);
					if (!finished)
					{
						// challenge-response iteration
						context.sendResponse(response.toString());
					}
				}
			}
			context.sendResponse("235 Authentication successful.");
			context.getSession().setAuthenticated(true);
		}
		catch (RejectException authFailed)
		{
			context.sendResponse("535 Authentication failure.");
			context.getSession().setAuthenticated(false);
		}
	}

	public CRLFTerminatedReader instantiateReader(ConnectionContext context)
			throws IOException
	{
		return new CRLFTerminatedReader(context.getSocket().getInputStream());
	}

	public MessageHandler getMessageHandler(ConnectionContext context)
	{
		return context.getSession().getMessageHandler();
	}
}
