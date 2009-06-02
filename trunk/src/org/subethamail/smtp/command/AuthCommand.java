package org.subethamail.smtp.command;

import java.io.IOException;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.io.CRLFTerminatedReader;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;

/**
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
public class AuthCommand extends BaseCommand
{

	public static final String VERB = "AUTH";
	public static final String AUTH_CANCEL_COMMAND = "*";

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
	public void execute(String commandString, Session sess)
			throws IOException
	{
		if (sess.isAuthenticated())
		{
			sess.sendResponse("503 Refusing any other AUTH command.");
			return;
		}

		AuthenticationHandlerFactory authFactory = sess.getServer().getAuthenticationHandlerFactory();

		if (authFactory == null)
		{
			sess.sendResponse("502 Authentication not supported");
			return;
		}

		AuthenticationHandler authHandler = authFactory.create();

		String[] args = this.getArgs(commandString);
		// Let's check the command syntax
		if (args.length < 2)
		{
			sess.sendResponse("501 Syntax: " + VERB + " mechanism [initial-response]");
			return;
		}

		// Let's check if we support the required authentication mechanism
		String mechanism = args[1];
		if (!authFactory.getAuthenticationMechanisms().contains(mechanism.toUpperCase()))
		{
			sess.sendResponse("504 The requested authentication mechanism is not supported");
			return;
		}
		// OK, let's go trough the authentication process.
		try
		{
			// The authentication process may require a series of challenge-responses
			CRLFTerminatedReader reader = sess.getReader();

			String response = authHandler.auth(commandString);
			if (response != null)
			{
				// challenge-response iteration
				sess.sendResponse(response);
			}

			while (response != null)
			{
				String clientInput = reader.readLine();
				if (clientInput.trim().equals(AUTH_CANCEL_COMMAND))
				{
					// RFC 2554 explicitly states this:
					sess.sendResponse("501 Authentication canceled by client.");
					return;
				}
				else
				{
					response = authHandler.auth(clientInput);
					if (response != null)
					{
						// challenge-response iteration
						sess.sendResponse(response);
					}
				}
			}

			sess.sendResponse("235 Authentication successful.");
			sess.setAuthenticationHandler(authHandler);
		}
		catch (RejectException authFailed)
		{
			sess.sendResponse("535 Authentication failure.");
		}
	}
}
