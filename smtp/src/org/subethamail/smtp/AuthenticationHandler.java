package org.subethamail.smtp;

import java.util.List;

/**
 * The interface that enables challenge-response communication necessary for SMTP AUTH.<p>
 * Since the authentication process can be stateful, an instance of this class can be stateful too.<br>
 * Do not share a single instance of this interface if you don't explicitly need to do so.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public interface AuthenticationHandler
{
	
	/**
	 * If your handler supports RFC 2554 at some degree, then it must return all the supported mechanisms here. <br>
	 * The order you use to populate the list will be preserved in the output of the EHLO command. <br>
	 * If your handler does not support RFC 2554 at all, return an empty list.
	 *
	 * @return the supported authentication mechanisms as List.
	 */
	public List<String> getAuthenticationMechanisms();
	
	/**
	 * Initially called using an input string in the RFC2554 form: "AUTH <mechanism> [initial-response]". <br>
	 * This method must provide the correct reply (by filling the <code>response</code> parameter) at each <code>clientInput</code>.
	 * <p>
	 * Depending on the authentication mechanism, the handshaking process may require
	 * many request-response passes. This method will return <code>true</code> only when the authentication process is finished <br>
	 *
	 * @return <code>true</code> if the authentication process is finished, <code>false</code> otherwise.
	 * @param clientInput The client's input.
	 * @param response a buffer filled with your response to the client input.
	 * @throws org.subethamail.smtp.RejectException if authentication fails.
	 */
	public boolean auth(String clientInput, StringBuilder response) throws RejectException;
	
	/**
	 * Since a so-designed handler has its own state, it seems reasonable to enable resetting
	 * its state. This can be done, for example, after a "*" client response during the AUTH command
	 * processing.
	 */
	public void resetState();
	
}
