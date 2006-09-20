/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp.server;

import org.subethamail.smtp.MessageHandler;

/**
 * A simple base class to make implementing message handlers easier.  It
 * also makes modification of the interface class easier on users.
 * 
 * @author Jeff Schnitzer
 */
abstract public class AbstractMessageHandler implements MessageHandler
{
	public boolean from(String from)
	{
		return true;
	}

	public boolean recipient(String recipient)
	{
		return true;
	}
}
