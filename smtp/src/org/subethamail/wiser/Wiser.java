/*
 * $Id$
 * $URL$
 */

package org.subethamail.wiser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.auth.LoginAuthenticationHandler;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.PlainAuthenticationHandler;
import org.subethamail.smtp.auth.PluginAuthenticationHandler;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.server.SMTPServer;

/**
 * Wiser is a smart mail testing application.
 * 
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class Wiser implements MessageListener
{
	/** */
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(Wiser.class);
	
	/** */
	SMTPServer server;
	
	/** */
	List<WiserMessage> messages = Collections.synchronizedList(new ArrayList<WiserMessage>());

	/**
	 * Create a new SMTP server with this class as the listener.
	 * The default port is set to 25. Call setPort()/setHostname() before
	 * calling start().
	 */
	public Wiser()
	{
		Collection<MessageListener> listeners = new ArrayList<MessageListener>(1);
		listeners.add(this);
		
		this.server = new SMTPServer(listeners);
		this.server.setPort(25);
		
		// Set max connections much higher since we use NIO now.		
        this.server.setMaxConnections(30000);
        
        // Removed in order that JUNIT tests could pass. They were incorrectly 
        // assuming that anonymous mode was always available.
		/*((MessageListenerAdapter)server.getMessageHandlerFactory())
			.setAuthenticationHandlerFactory(new AuthHandlerFactory());*/        
	}

	/**
	 * The port that the server should listen on.
	 * @param port
	 */
	public void setPort(int port)
	{
		this.server.setPort(port);
	}
	
	/**
	 * Set the size at which the mail will be temporary 
	 * stored on disk.
	 * @param dataDeferredSize
	 */
	public void setDataDeferredSize(int dataDeferredSize)
	{
		this.server.setDataDeferredSize(dataDeferredSize);
	}
	
	/**
	 * Set the receive buffer size.
	 * @param size
	 */
	public void setReceiveBufferSize(int size)
	{
		this.server.setReceiveBufferSize(size);
	}
	
	/**
	 * The hostname that the server should listen on.
	 * @param hostname
	 */
	public void setHostname(String hostname)
	{
		this.server.setHostName(hostname);
	}

	/**
	 * Starts the SMTP Server
	 */
	public void start()
	{
		this.server.start();
	}
	
	/**
	 * Stops the SMTP Server
	 */
	public void stop()
	{
		this.server.stop();
	}

	/**
	 * A main() for this class. Starts up the server.
	 */
	public static void main(String[] args) throws Exception
	{
		Wiser wiser = new Wiser();
		wiser.start();
	}

	/**
	 * Always accept everything
	 */
	public boolean accept(String from, String recipient)
	{
		return true;
	}

	/**
	 * Cache the messages in memory. Now avoids unnecessary memory copying. 
	 */
	public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException
	{
		log.debug("Delivering new message ...");
		WiserMessage msg = new WiserMessage(this, from, recipient, data);
		this.messages.add(msg);		
	}
	
	/**
	 * Creates the JavaMail Session object for use in WiserMessage
	 */
	protected Session getSession()
	{
		return Session.getDefaultInstance(new Properties());
	}

	/**
	 * @return the list of WiserMessages
	 */
	public List<WiserMessage> getMessages()
	{
		return this.messages;
	}

	public SMTPServer getServer()
	{
		return this.server;
	}

	/**
	 * Creates the AuthHandlerFactory which logs the user/pass.
	 */
	public class AuthHandlerFactory implements AuthenticationHandlerFactory
	{
		public AuthenticationHandler create()
		{
			PluginAuthenticationHandler ret = new PluginAuthenticationHandler();
			UsernamePasswordValidator validator = new UsernamePasswordValidator()
			{
				public void login(String username, String password)
						throws LoginFailedException
				{
					log.debug("Username=" + username);
					log.debug("Password=" + password);
				}
			};
			ret.addPlugin(new PlainAuthenticationHandler(validator));
			ret.addPlugin(new LoginAuthenticationHandler(validator));
			return ret;
		}
	}
}