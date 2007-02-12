package org.subethamail.smtp.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.PlainAuthenticationHandler;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.util.Base64;

/**
 * @author marco
 */
public class TestPlainAuthenticationHandler
{
	private static Log log = LogFactory.getLog(TestPlainAuthenticationHandler.class);

	/** Creates a new instance of PlainAuthenticationHandlerTest */
	public TestPlainAuthenticationHandler()
	{
	}

	public static void main(String[] argv)
	{
		UsernamePasswordValidator hlp = new UsernamePasswordValidator()
		{
			public void login(String username, String password)
					throws LoginFailedException
			{
				log.debug("Username=" + username);
				log.debug("Password=" + password);
			}
		};
		PlainAuthenticationHandler auth = new PlainAuthenticationHandler(hlp);
		StringBuffer sb = new StringBuffer();
		try
		{
			String authString = new String(new byte[] {0}) + "marco"
					+ new String(new byte[] {0}) + "mySecret01";
			auth.auth("AUTH PLAIN "
					+ Base64.encodeToString(authString.getBytes(), false), sb);

			sb = new StringBuffer();
			auth.auth("AUTH PLAIN ", sb);
			log.debug(sb.toString());
			auth.auth(Base64.encodeToString(authString.getBytes(), false), sb);

		}
		catch (RejectException e)
		{
			e.printStackTrace();
		}
	}
}
