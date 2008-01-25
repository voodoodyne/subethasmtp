package org.subethamail.smtp.auth;

import java.util.ArrayList;
import java.util.List;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.ConnectionContext;

/**
 * Implements a dummy AUTH mechanism.<br />
 * Will always allow to login without asking for any parameter.
 * 
 * @author Jeff Schnitzer
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class DummyAuthenticationHandler implements AuthenticationHandler
{
	public List<String> getAuthenticationMechanisms()
	{
		return new ArrayList<String>();
	}
	
	public boolean auth(String clientInput, StringBuilder response, ConnectionContext ctx) throws RejectException
	{
		return true;
	}
	
	public void resetState()
	{
	}
}