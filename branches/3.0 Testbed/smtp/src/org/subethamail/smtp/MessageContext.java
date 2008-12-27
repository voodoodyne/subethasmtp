/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp;

import java.net.SocketAddress;

import org.subethamail.smtp.server.SMTPServer;


/**
 * Interface which provides context to the message handlers.
 * 
 * @author Jeff Schnitzer
 */
public interface MessageContext
{
	/**
	 * @return the SMTPServer object.
	 */
	public SMTPServer getSMTPServer();
	
	/**
	 * @return the IP address of the remote server.
	 */
	public SocketAddress getRemoteAddress();
	
	/**
	 * @return the handler that was used to authenticate.  You will
	 *  probably want to upcast this object to the actual implementation
	 *  to pull out information like user identity. 
	 */
	public AuthenticationHandler getAuthenticationHandler();
}
