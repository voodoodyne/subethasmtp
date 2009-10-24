/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.io.DeferredFileOutputStream;

/**
 * MessageHandlerFactory implementation which adapts to a collection of
 * MessageListeners.  This allows us to preserve the old, convenient
 * interface.
 *
 * @author Jeff Schnitzer
 */
public class SimpleMessageListenerAdapter implements MessageHandlerFactory
{
	/**
	 * 5 megs by default. The server will buffer incoming messages to disk
	 * when they hit this limit in the DATA received.
	 */
	private static int DEFAULT_DATA_DEFERRED_SIZE = 1024*1024*5;
	
	private Collection<SimpleMessageListener> listeners;
	private int dataDeferredSize;
	
	/**
	 * Initializes this factory with a single listener.
	 *
	 * Default data deferred size is 5 megs.
	 */
	public SimpleMessageListenerAdapter(SimpleMessageListener listener)
	{
		this(Collections.singleton(listener), DEFAULT_DATA_DEFERRED_SIZE);
	}
	
	/**
	 * Initializes this factory with the listeners.
	 *
	 * Default data deferred size is 5 megs.
	 */
	public SimpleMessageListenerAdapter(Collection<SimpleMessageListener> listeners)
	{
		this(listeners, DEFAULT_DATA_DEFERRED_SIZE);
	}
	
	/**
	 * Initializes this factory with the listeners.
	 * @param dataDeferredSize The server will buffer
	 *        incoming messages to disk when they hit this limit in the
	 *        DATA received.
	 */
	public SimpleMessageListenerAdapter(Collection<SimpleMessageListener> listeners, int dataDeferredSize)
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
		SimpleMessageListener listener;
		public SimpleMessageListener getListener() { return this.listener; }
		
		String recipient;
		public String getRecipient() { return this.recipient; }
		
		public Delivery(SimpleMessageListener listener, String recipient)
		{
			this.listener = listener;
			this.recipient = recipient;
		}
	}
	
	/**
	 * Class which implements the actual handler interface.
	 */
	class Handler implements MessageHandler
	{
		MessageContext ctx;
		String from;
		List<Delivery> deliveries = new ArrayList<Delivery>();
		
		/** */
		public Handler(MessageContext ctx)
		{
			this.ctx = ctx;
		}
		
		/** */
		public void from(String from) throws RejectException
		{
			this.from = from;
		}
		
		/** */
		public void recipient(String recipient) throws RejectException
		{
			boolean addedListener = false;
			
			for (SimpleMessageListener listener: listeners)
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

		/** */
		public void done()
		{
		}
	}
}
