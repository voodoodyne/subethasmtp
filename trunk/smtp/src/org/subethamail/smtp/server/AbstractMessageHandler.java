/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp.server;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.mail.util.SharedByteArrayInputStream;

import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.io.CharTerminatedInputStream;
import org.subethamail.smtp.server.io.DotUnstuffingInputStream;
import org.subethamail.smtp.server.io.SharedTmpFileInputStream;

/**
 * A simple base class to make implementing message handlers easier. It
 * also makes modification of the interface class easier on users.
 * 
 * @author Jeff Schnitzer
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
abstract public class AbstractMessageHandler 
	implements MessageHandler
{
	public final static char[] SMTP_TERMINATOR = {'\r', '\n', '.', '\r', '\n'};
		
	private AuthenticationHandler authHandler;
	private Collection<MessageListener> listeners;
	
	protected AbstractMessageHandler(MessageContext ctx, AuthenticationHandler authHandler)
	{
		this.authHandler = authHandler;
	}

	protected void setListeners(Collection<MessageListener> listeners) 
	{
		this.listeners = listeners;
	}

	public Collection<MessageListener> getListeners() 
	{
		return listeners;
	}	
	
	/**
	 * Provides a private unstuffed {@link InputStream} for each invocation unless
	 * <code>useCopy</code> is false in which case the <code>data</code> stream
	 * is unstuffed and returned. Unstuffing is made by encapsulating the stream within
	 * special streams.
	 * 
	 * @see org.subethamail.smtp.server.io.CharTerminatedInputStream
	 * @see org.subethamail.smtp.server.io.DotUnstuffingInputStream
	 */
	public InputStream getPrivateInputStream(boolean useCopy, InputStream data)
	{
		InputStream in = data;
		
		if (useCopy)
		{
			if (data instanceof SharedByteArrayInputStream)
				in = ((SharedByteArrayInputStream) data).newStream(0, -1);
			else
			if (data instanceof SharedTmpFileInputStream)
				in = ((SharedTmpFileInputStream) data).newStream(0, -1);
			else
				throw new IllegalArgumentException("Unexpected data stream type : "
						+data.getClass().getName());
		}
		
		in = new CharTerminatedInputStream(in, SMTP_TERMINATOR);
		in = new DotUnstuffingInputStream(in);
		
		return in;
	}
	
	/** */
	public boolean auth(String clientInput, StringBuilder response, ConnectionContext ctx) 
		throws RejectException
	{
		return authHandler.auth(clientInput, response, ctx);
	}

	/** */
	public void resetState()
	{
		authHandler.resetState();
	}
	
	/** */
	public List<String> getAuthenticationMechanisms()
	{
		return authHandler.getAuthenticationMechanisms();
	}
}