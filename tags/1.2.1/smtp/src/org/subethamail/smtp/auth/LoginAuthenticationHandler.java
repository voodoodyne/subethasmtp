package org.subethamail.smtp.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.util.Base64;

/**
 * Implements the SMTP AUTH LOGIN mechanism.<br>
 * You are only required to plug your UsernamePasswordValidator implementation
 * for username and password validation to take effect.
 * 
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public class LoginAuthenticationHandler implements AuthenticationHandler
{
	private String username;
	private String password;
	private UsernamePasswordValidator helper;

	/** Creates a new instance of PlainAuthenticationHandler */
	public LoginAuthenticationHandler(UsernamePasswordValidator helper)
	{
		this.helper = helper;
	}

	public List<String> getAuthenticationMechanisms()
	{
		List<String> ret = new ArrayList<String>(1);
		ret.add("LOGIN");
		return ret;
	}

	public boolean auth(String clientInput, StringBuffer response)
			throws RejectException
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
				response.append("504 AUTH mechanism mismatch.");
				return true;
			}
			if (stk.hasMoreTokens())
			{
				// the client submitted an initial response
				response
						.append("535 Initial response not allowed in AUTH LOGIN.");
				return true;
			}
			response.append("334 ").append(
					Base64.encodeToString("Username:".getBytes(), false));
			return false;
		}
		if (username == null)
		{
			byte[] decoded = Base64.decode(clientInput);
			if (decoded == null)
			{
				throw new RejectException();
			}
			this.username = new String(decoded);
			response.append("334 ").append(
					Base64.encodeToString("Password:".getBytes(), false));
			return false;
		}

		byte[] decoded = Base64.decode(clientInput);
		if (decoded == null)
		{
			throw new RejectException();
		}

		this.password = new String(decoded);
		try
		{
			helper.login(username, password);
			resetState();
		}
		catch (LoginFailedException lfe)
		{
			resetState();
			throw new RejectException();
		}
		return true;
	}

	public void resetState()
	{
		this.username = null;
		this.password = null;
	}
}
