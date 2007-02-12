package org.subethamail.smtp.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.auth.LoginAuthenticationHandler;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.util.Base64;

/**
 * @author marco
 */
public class TestLoginAuthenticationHandler
{
	private static Log log = LogFactory.getLog(TestLoginAuthenticationHandler.class);

	/** Creates a new instance of TestLoginAuthenticationHandler */
	public TestLoginAuthenticationHandler()
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

		LoginAuthenticationHandler auth = new LoginAuthenticationHandler(hlp);
		try
		{
			StringBuffer sb = new StringBuffer();
			String uname = "myName", pwd = "mySecret01";
			auth.auth("AUTH LOGIN ", sb);
			log.debug(sb.toString());
			sb = new StringBuffer();
			auth.auth(Base64.encodeToString(uname.getBytes(), false), sb);
			log.debug(sb.toString());
			sb = new StringBuffer();
			auth.auth(Base64.encodeToString(pwd.getBytes(), false), sb);
			log.debug(sb.toString());

			sb = new StringBuffer();
			auth.auth("AUTH LOGIN "
					+ Base64.encodeToString(uname.getBytes(), false), sb);
			log.debug(sb.toString());

		}
		catch (RejectException e)
		{
			e.printStackTrace();
		}
	}
}
