/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.io.DeferredFileOutputStream;

/**
 * MessageHandlerFactory implementation which adapts to a collection of
 * MessageListeners.  This allows us to preserve the old, convenient
 * interface.
 *
 * @author Jeff Schnitzer
 */
public class MessageListenerAdapter implements MessageHandlerFactory
{
	/**
	 * 5 megs by default. The server will buffer incoming messages to disk
	 * when they hit this limit in the DATA received.
	 */
	private static int DEFAULT_DATA_DEFERRED_SIZE = 1024*1024*5;
	
	private Collection<MessageListener> listeners;
	private int dataDeferredSize;
	
	private AuthenticationHandlerFactory authenticationHandlerFactory;
	
	/**
	 * Initializes this factory with the listeners.
	 *
	 * Default data deferred size is 5 megs.
	 */
	public MessageListenerAdapter(Collection<MessageListener> listeners)
	{
		this(listeners, DEFAULT_DATA_DEFERRED_SIZE);
	}
	
	/**
	 * Initializes this factory with the listeners.
	 * @param dataDeferredSize The server will buffer
	 *        incoming messages to disk when they hit this limit in the
	 *        DATA received.
	 */
	public MessageListenerAdapter(Collection<MessageListener> listeners, int dataDeferredSize)
	{
		this.listeners = listeners;
		this.dataDeferredSize = dataDeferredSize;
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
		 * Holds the SMTPAuthenticationHandler instantiation logic.
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
				this.authHandler = new DummyAuthenticatioHandler();
			}
			// Rerurn the variable, which can be null
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

		/** */
		public void data(InputStream data) throws TooMuchDataException, IOException
		{
			if (this.deliveries.size() == 1)
			{
				Delivery delivery = this.deliveries.get(0);
				delivery.getListener().deliver(this.from, delivery.getRecipient(), data);
			}
			else
			{
				DeferredFileOutputStream dfos = new DeferredFileOutputStream(dataDeferredSize);
				
				try
				{
					int value;
					while ((value = data.read()) >= 0)
					{
						dfos.write(value);
					}
					
					for (Delivery delivery: this.deliveries)
					{
						delivery.getListener().deliver(this.from, delivery.getRecipient(), dfos.getInputStream());
					}
				}
				finally
				{
					dfos.close();
				}
			}
		}
		
		public List<String> getAuthenticationMechanisms()
		{
			return getAuthenticationHandler().getAuthenticationMechanisms();
		}
		
		public boolean auth(String clientInput, StringBuffer response) throws RejectException
		{
			return getAuthenticationHandler().auth(clientInput,response);
		}
		
		public void resetState()
		{
			getAuthenticationHandler().resetState();
		}
	}
	
	/**
	 * Auth always return true.
	 */
	class DummyAuthenticatioHandler implements AuthenticationHandler
	{
		public List<String> getAuthenticationMechanisms()
		{
			return new ArrayList<String>();
		}
		
		public boolean auth(String clientInput, StringBuffer response) throws RejectException
		{
			return true;
		}
		
		public void resetState()
		{
		}
	}
	
	public AuthenticationHandlerFactory getAuthenticationHandlerFactory()
	{
		return authenticationHandlerFactory;
	}
	
	public void setAuthenticationHandlerFactory(AuthenticationHandlerFactory authenticationHandlerFactory)
	{
		this.authenticationHandlerFactory = authenticationHandlerFactory;
	}
}
