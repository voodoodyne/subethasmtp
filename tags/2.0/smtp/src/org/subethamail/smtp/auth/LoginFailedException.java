package org.subethamail.smtp.auth;

/**
 * Exception expected to be thrown by a validator (i.e UsernamePasswordValidator)
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
@SuppressWarnings("serial")
public class LoginFailedException extends Exception
{
	
	/** Creates a new instance of LoginFailedException */
	public LoginFailedException()
	{
		super("Login failed.");
	}
	
	/** Creates a new instance of LoginFailedException */
	public LoginFailedException(String msg)
	{
		super(msg);
	}
	
}
