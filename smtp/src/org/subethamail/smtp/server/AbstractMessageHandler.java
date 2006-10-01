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
	public void from(String from) throws RejectException
	{
	}

	public void recipient(String recipient) throws RejectException
	{
	}
}
