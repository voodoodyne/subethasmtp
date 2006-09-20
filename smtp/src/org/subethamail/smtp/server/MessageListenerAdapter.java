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

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.MessageListener;
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
	private Collection<MessageListener> listeners;
	
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
	 * Needed by the ConversationAdapter to track which listeners need delivery. 
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
	class Handler extends AbstractMessageHandler
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
		@Override
		public boolean from(String from)
		{
			this.from = from;
			return true;
		}
		
		/** */
		@Override
		public boolean recipient(String recipient)
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

			return addedListener;
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
				DeferredFileOutputStream dfos = new DeferredFileOutputStream(
						this.ctx.getSMTPServer().getDataDeferredSize());

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
	}
}
