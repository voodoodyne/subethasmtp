/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp;

import java.io.IOException;
import java.io.InputStream;

/**
 * The interface that defines the conversational exchange of a single message
 * on an SMTP connection.
 * 
 * @author Jeff Schnitzer
 */
public interface MessageHandler
{
	/**
	 * Called first, after the MAIL FROM during a SMTP exchange.
	 * 
	 * @param from is the sender as specified by the client.  It will
	 *  be a rfc822-compliant email address, already validated by
	 *  the server.
	 * @return true to accept, false to reject.  If rejected,
	 *  this method might be called again.
	 */
	public boolean from(String from);
	
	/**
	 * Called once for every RCPT TO during a SMTP exchange.
	 * This will occur after a from() call.
	 * 
	 * @param recipient is a rfc822-compliant email address,
	 *  validated by the server.
	 * 
	 * @return true if the recipient should be accepted, false
	 *  if the recipient should be rejected.
	 */
	public boolean recipient(String recipient);
	
	/**
	 * Called when the DATA part of the SMTP exchange begins.  Will
	 * only be called if at least one recipient was accepted.
	 * 
	 * @param data will be the smtp data stream, stripped of any extra '.' chars
	 * 
	 * @throws TooMuchDataException if the listener can't handle that much data.
	 *         An error will be reported to the client.
	 * @throws IOException if there is an IO error reading the input data.
	 */
	public void data(InputStream data) throws TooMuchDataException, IOException;
}
