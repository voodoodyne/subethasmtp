/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp.server;

import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;

/**
 * A simple base class to make implementing message handlers easier.  It
 * also makes modification of the interface class easier on users.
 * 
 * @author Jeff Schnitzer
 */
abstract public class AbstractMessageHandler implements MessageHandler
{
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.MessageHandler#from(java.lang.String)
	 */
	public void from(String from) throws RejectException
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.MessageHandler#recipient(java.lang.String)
	 */
	public void recipient(String recipient) throws RejectException
	{
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.subethamail.smtp.MessageHandler#resetMessageState()
	 */
	public void resetMessageState()
	{
	}
}
