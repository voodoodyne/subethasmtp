package org.subethamail.smtp.auth;

/**
 * Use this when your authentication scheme uses a username and a password.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public interface UsernamePasswordValidator
{
	public void login(final String username, final String password)
			throws LoginFailedException;
}
