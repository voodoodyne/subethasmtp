package org.subethamail.smtp.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.util.Base64;
import org.subethamail.smtp.util.TextUtils;

/**
 * Implements the SMTP AUTH LOGIN mechanism.<br>
 * You are only required to plug your UsernamePasswordValidator implementation
 * for username and password validation to take effect.
 * <p>
 * LOGIN is an obsolete authentication method which has no formal specification.
 * There is an expired IETF draft for informational purposes. A Microsoft
 * document can also be found, which intends to specify the LOGIN mechanism. The
 * latter is not entirely compatible, neither with the IETF draft nor with RFC
 * 4954 (SMTP Service Extension for Authentication). However this implementation
 * is likely usable with clients following any of the two documents.
 * 
 * @see <a href="http://tools.ietf.org/html/draft-murchison-sasl-login-00">The
 *      LOGIN SASL Mechanism</a>
 * @see <a
 *      href="http://download.microsoft.com/download/5/d/d/5dd33fdf-91f5-496d-9884-0a0b0ee698bb/%5BMS-XLOGIN%5D.pdf">[MS-XLOGIN]</a>
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

		@Override
		public String auth(String clientInput) throws RejectException
		{
			StringTokenizer stk = new StringTokenizer(clientInput);
			String token = stk.nextToken();
			if (token.trim().equalsIgnoreCase("AUTH"))
			{
				if (!stk.nextToken().trim().equalsIgnoreCase("LOGIN"))
				{
					// Mechanism mismatch
					throw new RejectException(504, "AUTH mechanism mismatch");
				}

				if (stk.hasMoreTokens())
				{
					// The client submitted an initial response, which should be
					// the username.
					// .Net's built in System.Net.Mail.SmtpClient sends its
					// authentication this way (and this way only).
					username = TextUtils.getStringUtf8(Base64.decode(stk
							.nextToken()));

					return "334 "
							+ Base64.encodeToString(
									TextUtils.getAsciiBytes("Password:"),
									false);
				} else {
					return "334 "
							+ Base64.encodeToString(
									TextUtils.getAsciiBytes("Username:"), false);
				}
			}

			if (this.username == null)
			{
				byte[] decoded = Base64.decode(clientInput);
				if (decoded == null)
				{
					throw new RejectException();
				}

				this.username = TextUtils.getStringUtf8(decoded);

				return "334 "
						+ Base64.encodeToString(
								TextUtils.getAsciiBytes("Password:"), false);
			}

			byte[] decoded = Base64.decode(clientInput);
			if (decoded == null)
			{
				throw new RejectException();
			}

			this.password = TextUtils.getStringUtf8(decoded);
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
