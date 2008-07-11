package org.subethamail.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * Default class that extends the {@link AbstractMessageHandler} class.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class DefaultMessageHandler
	extends AbstractMessageHandler
{
	/**
	 * Needed by this class to track which listeners need delivery.
	 */
	public static class Delivery
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
	
	private List<Delivery> deliveries = new ArrayList<Delivery>();
	private String from;
	
	public DefaultMessageHandler(MessageContext ctx, AuthenticationHandler authHandler)
	{
		super(ctx, authHandler);
	}
	
	/** */
	public void from(String from) 
		throws RejectException
	{
		this.from = from;
	}
	
	/** */
	public void recipient(String recipient) 
		throws RejectException
	{
		boolean addedListener = false;
		
		for (MessageListener listener: getListeners())
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
	public void resetMessageState()
	{
		this.deliveries.clear();
	}
	
	/**
	 * Implementation of the data receiving portion of things. By default
	 * deliver a copy of the stream to each recipient of the message(the first 
	 * recipient is provided the original stream to save memory space). If
	 * you would like to change this behavior, then you should implement
	 * the MessageHandler interface yourself.
	 */
	public void data(InputStream data) 
		throws TooMuchDataException, IOException
	{
		boolean notFirstLoop = false;
		
		for (Delivery delivery: this.deliveries)
		{				
		    delivery.getListener().deliver(this.from, 
		    		delivery.getRecipient(), getPrivateInputStream(notFirstLoop, data));
		    if  (!notFirstLoop)
		    	notFirstLoop = true;
		}
	}
}