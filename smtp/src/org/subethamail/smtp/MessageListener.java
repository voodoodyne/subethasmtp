/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp;

import java.io.IOException;
import java.io.InputStream;

/**
 * Objects which want access to messages received with SMTP should implement
 * this interface.
 * 
 * While the SMTP message is being received, all listeners are asked if they
 * want to accept each recipient. After the message has arrived, the message is
 * handed off to all accepting listeners.
 * 
 * @author Jeff Schnitzer
 */
public interface MessageListener
{
	/**
	 * Called once for every RCPT TO during a SMTP exchange.
	 * 
	 * @param from is a rfc822-compliant email address.
	 * @param recipient is a rfc822-compliant email address.
	 * 
	 * @return true if the listener wants delivery of the message, false if the
	 *         message is not for this listener.
	 */
	public boolean accept(String from, String recipient);

	/**
	 * When message data arrives, this method will be called for every recipient
	 * this listener accepted.
	 * 
	 * @param from is the envelope sender in rfc822 form
	 * @param recipient will be an accepted recipient in rfc822 form
	 * @param data will be the smtp data stream, stripped of any extra '.' chars
	 * 
	 * @throws TooMuchDataException if the listener can't handle that much data.
	 *         An error will be reported to the client.
	 * @throws IOException if there is an IO error reading the input data.
	 */
	public void deliver(String from, String recipient, InputStream data)
			throws TooMuchDataException, IOException;
}
