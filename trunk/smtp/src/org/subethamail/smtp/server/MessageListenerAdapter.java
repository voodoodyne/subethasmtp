/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.mail.util.SharedByteArrayInputStream;
import javax.mail.util.SharedFileInputStream;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.auth.DummyAuthenticationHandler;
import org.subethamail.smtp.command.DataEndCommand;
import org.subethamail.smtp.server.io.CharTerminatedInputStream;
import org.subethamail.smtp.server.io.DotUnstuffingInputStream;

/**
 * MessageHandlerFactory implementation which adapts to a collection of
 * MessageListeners.  This allows us to preserve the old, convenient
 * interface.
 *
 * @author Jeff Schnitzer
 */
public class MessageListenerAdapter implements MessageHandlerFactory
{
	private Collection<MessageListener> listeners;
	
	private AuthenticationHandlerFactory authenticationHandlerFactory;
	
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
		return new Handler(ctx);
	}
	
	/**
	 * Needed by this class to track which listeners need delivery.
	 */
	static class Delivery
	{
		MessageListener listener;
		public MessageListener getListener() { return this.listener; }
		
		String recipient;
		public String getRecipient() { return this.recipient; }
		
		public Delivery(MessageListener listener, String recipient)
		{
			this.listener = listener;
			this.recipient = recipient;
		}
	}
	
	/**
	 * Class which implements the actual handler interface.
	 */
	class Handler extends AbstractMessageHandler implements AuthenticationHandler
	{
		MessageContext ctx;
		String from;
		List<Delivery> deliveries = new ArrayList<Delivery>();
		
		AuthenticationHandler authHandler;
		
		/**
		 * Holds the AuthenticationHandler instantiation logic.
		 * Either try to use a user defined AuthHandlerFactory
		 * or default to the internal class DummyAuthenticationHandler
		 * which always returns true.
		 *
		 * @return a new AuthenticationHandler
		 */
		private AuthenticationHandler getAuthenticationHandler()
		{
			if( this.authHandler != null )
			{
				return this.authHandler;
			}
			if( getAuthenticationHandlerFactory() != null )
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
		
		/** */
		public Handler(MessageContext ctx)
		{
			this.ctx = ctx;
		}
		
		/** */
		@Override
		public void from(String from) throws RejectException
		{
			this.from = from;
		}
		
		/** */
		@Override
		public void recipient(String recipient) throws RejectException
		{
			boolean addedListener = false;
			
			for (MessageListener listener: listeners)
			{
				if (listener.accept(this.from, recipient))
				{
					this.deliveries.add(new Delivery(listener, recipient));
					addedListener = true;
				}
			}
			
			if (!addedListener)
				throw new RejectException(553, "<" + recipient + "> address unknown.");
		}
		
		/** */
		@Override
		public void resetMessageState()
		{
			this.deliveries.clear();
		}

		/**
		 * Implementation of the data receiving portion of things. By default
		 * deliver a copy of the stream to each recipient of the message. If
		 * you would like to change this behavior, then you should implement
		 * the MessageHandler interface yourself.
		 */
		public void data(InputStream data) throws TooMuchDataException, IOException
		{
			InputStream in = data;
			
			for (Delivery delivery: this.deliveries)
			{				
				if (in == data)
				{
					delivery.getListener().deliver(this.from, delivery.getRecipient(), data);
					in = null;
					continue;
				}
				else
					in = ctx.getInput().getInputStream();

				if (ctx.getInput().isThresholdReached())
					in = ((SharedFileInputStream) in).newStream(0, -1);
				else
					in = ((SharedByteArrayInputStream) in).newStream(0, -1);
			
				in = new CharTerminatedInputStream(in, DataEndCommand.SMTP_TERMINATOR);
				in = new DotUnstuffingInputStream(in);
				
				delivery.getListener().deliver(this.from, delivery.getRecipient(), in);
			}
		}
		
		public List<String> getAuthenticationMechanisms()
		{
			return getAuthenticationHandler().getAuthenticationMechanisms();
		}
		
		public boolean auth(String clientInput, StringBuilder response, ConnectionContext ctx) throws RejectException
		{
			return getAuthenticationHandler().auth(clientInput, response, ctx);
		}
		
		public void resetState()
		{
			getAuthenticationHandler().resetState();
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
	public void setAuthenticationHandlerFactory(AuthenticationHandlerFactory authenticationHandlerFactory)
	{
		this.authenticationHandlerFactory = authenticationHandlerFactory;
	}
}
