package org.subethamail.smtp.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.RejectException;

/**
 * This handler combines the behavior of several other authentication handler factories.
 *
 * @author Jeff Schnitzer
 */
public class MultipleAuthenticationHandlerFactory implements AuthenticationHandlerFactory
{
	/** Maps the auth type (eg "PLAIN") to a handler */
	Map<String, AuthenticationHandlerFactory> plugins = new HashMap<String, AuthenticationHandlerFactory>();
	
	/** A more orderly list of the supported mechanisms */
	List<String> mechanisms = new ArrayList<String>();

	/** */
	public MultipleAuthenticationHandlerFactory(Collection<AuthenticationHandlerFactory> factories)
	{
		for (AuthenticationHandlerFactory fact: factories)
		{
			List<String> partialMechanisms = fact.getAuthenticationMechanisms();
			for (String mechanism: partialMechanisms)
			{
				if (!this.mechanisms.contains(mechanism))
				{
					this.mechanisms.add(mechanism);
					this.plugins.put(mechanism, fact);
				}
			}
		}
	}

	/** */
	public List<String> getAuthenticationMechanisms()
	{
		return this.mechanisms;
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
		AuthenticationHandler active;
		
		/** */
		public String auth(String clientInput) throws RejectException
		{
			if (this.active != null)
			{
				StringTokenizer stk = new StringTokenizer(clientInput);
				String auth = stk.nextToken();
				if (!"AUTH".equals(auth))
					throw new IllegalArgumentException("Not an AUTH command: " + clientInput);
				
				String method = stk.nextToken();
				AuthenticationHandlerFactory fact = plugins.get(method);
				
				if (fact == null)
					throw new RejectException(504, "Method not supported");
	
				this.active = fact.create();
			}
			
			return this.active.auth(clientInput);
		}
	}
}
