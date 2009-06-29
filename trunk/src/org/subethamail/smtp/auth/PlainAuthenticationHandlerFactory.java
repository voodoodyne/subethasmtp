package org.subethamail.smtp.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.util.Base64;

/**
 * Implements the SMTP AUTH PLAIN mechanism.<br>
 * You are only required to plug your UsernamePasswordValidator implementation
 * for username and password validation to take effect.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Jeff Schnitzer
 */
public class PlainAuthenticationHandlerFactory implements AuthenticationHandlerFactory
{
	static List<String> MECHANISMS = new ArrayList<String>(1);
	static {
		MECHANISMS.add("PLAIN");
	}

	private UsernamePasswordValidator helper;

	/** */
	public PlainAuthenticationHandlerFactory(UsernamePasswordValidator helper)
	{
		this.helper = helper;
	}

	/** */
	public List<String> getAuthenticationMechanisms()
	{
		return MECHANISMS;
	}

	/** */
	public AuthenticationHandler create()
	{
		return new Handler();
	}

	/**
	 */
	class Handler implements AuthenticationHandler
	{
		private String username;
		private String password;

		/* */
		public String auth(String clientInput) throws RejectException
		{
			StringTokenizer stk = new StringTokenizer(clientInput);
			String secret = stk.nextToken();
			if (secret.trim().equalsIgnoreCase("AUTH"))
			{
				// Let's read the RFC2554 "initial-response" parameter
				// The line could be in the form of "AUTH PLAIN <base64Secret>"
				if (!stk.nextToken().trim().equalsIgnoreCase("PLAIN"))
				{
					// Mechanism mismatch
					throw new RejectException(504, "AUTH mechanism mismatch");
				}

				if (stk.hasMoreTokens())
				{
					// the client submitted an initial response
					secret = stk.nextToken();
				}
				else
				{
					// the client did not submit an initial response, we'll get it in the next pass
					return "334 Ok";
				}
			}

			byte[] decodedSecret = Base64.decode(secret);
			if (decodedSecret == null)
				throw new RejectException();

			int usernameStop = -1;
			for (int i = 1; (i < decodedSecret.length) && (usernameStop < 0); i++)
			{
				if (decodedSecret[i] == 0)
				{
					usernameStop = i;
				}
			}

			this.username = new String(decodedSecret, 1, usernameStop - 1);
			this.password = new String(decodedSecret, usernameStop + 1,
					decodedSecret.length - usernameStop - 1);
			try
			{
				PlainAuthenticationHandlerFactory.this.helper.login(this.username.toString(), this.password);
			}
			catch (LoginFailedException lfe)
			{
				throw new RejectException();
			}

			return null;
		}

		/* */
		public Object getIdentity()
		{
			return this.username;
		}
	}
}
