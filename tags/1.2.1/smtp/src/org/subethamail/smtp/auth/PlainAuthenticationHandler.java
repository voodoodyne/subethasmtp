package org.subethamail.smtp.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.util.Base64;

/**
 * Implements the SMTP AUTH PLAIN mechanism.<br>
 * You are only required to plug your UsernamePasswordValidator implementation
 * for username and password validation to take effect.
 * 
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public class PlainAuthenticationHandler implements AuthenticationHandler
{

	private UsernamePasswordValidator helper;

	/** Creates a new instance of PlainAuthenticationHandler */
	public PlainAuthenticationHandler(UsernamePasswordValidator helper)
	{
		this.helper = helper;
	}

	public List<String> getAuthenticationMechanisms()
	{
		List<String> ret = new ArrayList<String>(1);
		ret.add("PLAIN");
		return ret;
	}

	public boolean auth(String clientInput, StringBuffer response)
			throws RejectException
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
				response.append("504 AUTH mechanism mismatch.");
				return true;
			}
			if (stk.hasMoreTokens())
			{
				// the client submitted an initial response
				secret = stk.nextToken();
			}
			else
			{
				// the client did not submit an initial response
				response.append("334 Ok");
				return false;
			}
		}

		byte[] decodedSecret = Base64.decode(secret);
		if (decodedSecret == null)
			throw new RejectException();

		int usernameStop = -1;
		for (int i = 1; i < decodedSecret.length && usernameStop < 0; i++)
		{
			if (decodedSecret[i] == 0)
			{
				usernameStop = i;
			}
		}

		String username = new String(decodedSecret, 1, usernameStop - 1);
		String password = new String(decodedSecret, usernameStop + 1,
				decodedSecret.length - usernameStop - 1);
		try
		{
			helper.login(username.toString(), password);
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
	}
}
