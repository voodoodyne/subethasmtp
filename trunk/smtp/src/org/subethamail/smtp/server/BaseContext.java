/*
 * $Id: MessageContext.java 72 2006-09-20 08:36:18Z lhoriman $
 * $URL: http://subethasmtp.tigris.org/svn/subethasmtp/trunk/smtp/src/org/subethamail/smtp/MessageContext.java $
 */
package org.subethamail.smtp.server;

import java.net.SocketAddress;

import org.subethamail.smtp.server.io.SMTPMessageDataStream;


/**
 * Interface which provides a basic context interface.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public interface BaseContext
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
	 * @return the original data stream.
	 */
	public SMTPMessageDataStream getInput();
}
