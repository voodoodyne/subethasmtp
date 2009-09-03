package org.subethamail.smtp.test.command;

import org.subethamail.smtp.auth.EasyAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.test.util.Client;
import org.subethamail.smtp.test.util.ServerTestCase;
import org.subethamail.smtp.util.Base64;
import org.subethamail.smtp.util.TextUtils;

/**
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Jeff Schnitzer
 */
public class AuthTest extends ServerTestCase
{
	static final String REQUIRED_USERNAME = "myUserName";
	static final String REQUIRED_PASSWORD = "mySecret01";

	class RequiredUsernamePasswordValidator implements UsernamePasswordValidator
	{
		public void login(String username, String password) throws LoginFailedException
		{
			if (!username.equals(REQUIRED_USERNAME) || !password.equals(REQUIRED_PASSWORD))
			{
				throw new LoginFailedException();
			}
		}
	}

	/** */
	public AuthTest(String name)
	{
		super(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.subethamail.smtp.test.ServerTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		this.wiser = new TestWiser();
		this.wiser.setHostname("localhost");
		this.wiser.setPort(PORT);

		UsernamePasswordValidator validator = new RequiredUsernamePasswordValidator();

		EasyAuthenticationHandlerFactory fact = new EasyAuthenticationHandlerFactory(validator);
		this.wiser.getServer().setAuthenticationHandlerFactory(fact);

		this.wiser.start();
		this.c = new Client("localhost", PORT);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.subethamail.smtp.test.ServerTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	/**
	 * Test method for AUTH PLAIN.
	 * The sequence under test is as follows:
	 * <ol>
	 * <li>HELO test</li>
	 * <li>User starts AUTH PLAIN</li>
	 * <li>User sends username+password</li>
	 * <li>We expect login to be successful. Also the Base64 transformations are tested.</li>
	 * <li>User issues another AUTH command</li>
	 * <li>We expect an error message</li>
	 * </ol>
	 * {@link org.subethamail.smtp.command.AuthCommand#execute(java.lang.String, org.subethamail.smtp.server.Session)}.
	 */
	public void testAuthPlain() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("AUTH PLAIN");
		this.expect("334");

		String authString = new String(new byte[] {0}) + REQUIRED_USERNAME
						+ new String(new byte[] {0}) + REQUIRED_PASSWORD;

		String enc_authString = Base64.encodeToString(TextUtils.getAsciiBytes(authString), false);
		this.send(enc_authString);
		this.expect("235");

		this.send("AUTH");
		this.expect("503");
	}

	/**
	 * Test method for AUTH LOGIN.
	 * The sequence under test is as follows:
	 * <ol>
	 * <li>HELO test</li>
	 * <li>User starts AUTH LOGIN</li>
	 * <li>User sends username</li>
	 * <li>User cancels authentication by sending "*"</li>
	 * <li>User restarts AUTH LOGIN</li>
	 * <li>User sends username</li>
	 * <li>User sends password</li>
	 * <li>We expect login to be successful. Also the Base64 transformations are tested.</li>
	 * <li>User issues another AUTH command</li>
	 * <li>We expect an error message</li>
	 * </ol>
	 * {@link org.subethamail.smtp.command.AuthCommand#execute(java.lang.String, org.subethamail.smtp.server.Session)}.
	 */
	public void testAuthLogin() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("AUTH LOGIN");
		this.expect("334");

		String enc_username = Base64.encodeToString(TextUtils.getAsciiBytes(REQUIRED_USERNAME), false);

		this.send(enc_username);
		this.expect("334");

		this.send("*");
		this.expect("501");

		this.send("AUTH LOGIN");
		this.expect("334");

		this.send(enc_username);
		this.expect("334");

		String enc_pwd = Base64.encodeToString(TextUtils.getAsciiBytes(REQUIRED_PASSWORD), false);
		this.send(enc_pwd);
		this.expect("235");

		this.send("AUTH");
		this.expect("503");
	}
}