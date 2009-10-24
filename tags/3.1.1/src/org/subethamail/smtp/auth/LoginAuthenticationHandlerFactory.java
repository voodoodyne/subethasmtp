package org.subethamail.smtp.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.util.Base64;

/**
 * Implements the SMTP AUTH LOGIN mechanism.<br>
 * You are only required to plug your UsernamePasswordValidator implementation
 * for username and password validation to take effect.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Jeff Schnitzer
 */
public class LoginAuthenticationHandlerFactory implements AuthenticationHandlerFactory
{
	static List<String> MECHANISMS = new ArrayList<String>(1);
	static {
		MECHANISMS.add("LOGIN");
	}

	private UsernamePasswordValidator helper;

	/** */
	public LoginAuthenticationHandlerFactory(UsernamePasswordValidator helper)
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
			String token = stk.nextToken();
			if (token.trim().equalsIgnoreCase("AUTH"))
			{
				// The RFC2554 "initial-response" parameter must not be present
				// The line must be in the form of "AUTH LOGIN"
				if (!stk.nextToken().trim().equalsIgnoreCase("LOGIN"))
				{
					// Mechanism mismatch
					throw new RejectException(504, "AUTH mechanism mismatch");
				}

				if (stk.hasMoreTokens())
				{
					// the client submitted an initial response
					throw new RejectException(535, "Initial response not allowed in AUTH LOGIN");
				}

				return "334 " + Base64.encodeToString("Username:".getBytes(), false);
			}

			if (this.username == null)
			{
				byte[] decoded = Base64.decode(clientInput);
				if (decoded == null)
				{
					throw new RejectException();
				}

				this.username = new String(decoded);

				return "334 " + Base64.encodeToString("Password:".getBytes(), false);
			}

			byte[] decoded = Base64.decode(clientInput);
			if (decoded == null)
			{
				throw new RejectException();
			}

			this.password = new String(decoded);
			try
			{
				LoginAuthenticationHandlerFactory.this.helper.login(this.username, this.password);
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
