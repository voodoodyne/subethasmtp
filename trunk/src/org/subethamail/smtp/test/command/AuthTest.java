package org.subethamail.smtp.test.command;

import org.subethamail.smtp.auth.EasyAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.test.util.Client;
import org.subethamail.smtp.test.util.ServerTestCase;
import org.subethamail.smtp.util.Base64;

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
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("AUTH PLAIN");
		expect("334");
		
		String authString = new String(new byte[] {0}) + REQUIRED_USERNAME
						+ new String(new byte[] {0}) + REQUIRED_PASSWORD;

		String enc_authString = Base64.encodeToString(authString.getBytes(), false);
		send(enc_authString);
		expect("235");

		send("AUTH");
		expect("503");
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
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("AUTH LOGIN");
		expect("334");

		String enc_username = Base64.encodeToString(REQUIRED_USERNAME.getBytes(), false);

		send(enc_username);
		expect("334");

		send("*");
		expect("501");

		send("AUTH LOGIN");
		expect("334");

		send(enc_username);
		expect("334");

		String enc_pwd = Base64.encodeToString(REQUIRED_PASSWORD.getBytes(), false);
		send(enc_pwd);
		expect("235");

		send("AUTH");
		expect("503");
	}
}