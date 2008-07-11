/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp.server;

import java.lang.reflect.Constructor;
import java.util.Collection;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.auth.DummyAuthenticationHandler;

/**
 * MessageHandlerFactory implementation which adapts to a collection of
 * MessageListeners.  This allows us to preserve the old, convenient
 * interface.
 *
 * @author Jeff Schnitzer
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt; 
 */
public class MessageListenerAdapter 
	implements MessageHandlerFactory
{	
	private Collection<MessageListener> listeners;
	private AuthenticationHandlerFactory authenticationHandlerFactory;
	private AuthenticationHandler authHandler;
	private Class<? extends AbstractMessageHandler> messageHandlerImpl = 
		DefaultMessageHandler.class;
	
	/**
	 * Initializes this factory with the listeners.
	 */
	public MessageListenerAdapter(Collection<MessageListener> listeners)
	{
		this.listeners = listeners;
	}
	
	/* (non-Javadoc)
	 * @see org.subethamail.smtp.MessageHandlerFactory#create(org.subethamail.smtp.MessageContext)
	 */
	public MessageHandler create(MessageContext ctx)
	{
		return create(ctx, messageHandlerImpl);
	}
	
	/**
	 * Sets the {@link AbstractMessageHandler} implementation to use when creating the
	 * {@link MessageHandler}.
	 */
	public void setMessageHandlerImpl(Class<? extends AbstractMessageHandler> impl) 
	{
		this.messageHandlerImpl = impl;
	}
	
	private MessageHandler create(MessageContext ctx, 
			Class<? extends AbstractMessageHandler> c)
	{
		try 
		{
			Constructor<? extends AbstractMessageHandler> cstr = 
				c.getConstructor(MessageContext.class, AuthenticationHandler.class);
			AbstractMessageHandler handler = cstr.newInstance(ctx, getAuthenticationHandler());
			handler.setListeners(listeners);
			
			return handler;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new IllegalArgumentException("Class must be a child of AbstractMessageHandler class");
		}	
	}
	
	/**
	 * Returns the auth handler factory
	 */
	public AuthenticationHandlerFactory getAuthenticationHandlerFactory()
	{
		return authenticationHandlerFactory;
	}
	
	/**
	 * Sets the auth handler factory.
	 */
	public synchronized void setAuthenticationHandlerFactory(AuthenticationHandlerFactory authenticationHandlerFactory)
	{
		this.authHandler = null;
		this.authenticationHandlerFactory = authenticationHandlerFactory;
	}
	
	/**
	 * Holds the AuthenticationHandler instantiation logic.
	 * Either try to use a user defined AuthHandlerFactory
	 * or default to the internal class DummyAuthenticationHandler
	 * which always returns true.
	 *
	 * @return a new AuthenticationHandler
	 */
	public synchronized AuthenticationHandler getAuthenticationHandler()
	{
		if (this.authHandler != null)
		{
			return this.authHandler;
		}

		if (getAuthenticationHandlerFactory() != null)
		{
			// The user has plugged in a factory. let's use it.
			this.authHandler = getAuthenticationHandlerFactory().create();
		}
		else
		{
			// A placeholder.
			this.authHandler = new DummyAuthenticationHandler();
		}
		
		// Return the variable, which can be null
		return this.authHandler;
	}
}