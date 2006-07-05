/*
 * $Id$
 * $URL$
 */

package org.subethamail.smtp.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.SMTPServer;

/**
 * DummyServer is a smart mail testing application.
 * 
 * @author Jon Stevens
 */
public class DummyServer implements MessageListener
{
	/** */
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(DummyServer.class);
	
	/** */
	SMTPServer server;
	List<DummyServer> messages = Collections.synchronizedList(new ArrayList<DummyServer>());
	
	/**
	 * Create a new SMTP server with this class as the listener.
	 * The default port is set to 25. Call setPort()/setHostname() before
	 * calling start().
	 */
	public DummyServer()
	{
		Collection<MessageListener> listeners = new ArrayList<MessageListener>(1);
		listeners.add(this);
		
		this.server = new SMTPServer(listeners);
		this.server.setPort(25);
	}

	public SMTPServer getServer()
	{
		return this.server;
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
	 * The hostname that the server should listen on.
	 * @param hostname
	 */
	public void setHostname(String hostname)
	{
		this.server.setHostName(hostname);
	}

	/** Starts the SMTP Server */
	public void start()
	{
		this.server.start();
	}
	
	/** Stops the SMTP Server */
	public void stop()
	{
		this.server.stop();
	}

	/** Always accept everything */
	public boolean accept(String from, String recipient)
	{
		// Used in the ReciptCommandTest
		if (recipient.equals("test@subethamail.org"))
		{
			return false;
		}
		return true;
	}

	/** Cache the messages in memory */
	public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException
	{
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		data = new BufferedInputStream(data);
//
//		// read the data from the stream
//		int current;
//		while ((current = data.read()) >= 0)
//		{
//			out.write(current);
//		}

		// create a new WiserMessage.
//		messages.add(new WiserMessage(this, from, recipient, out.toByteArray()));
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
//	public List<WiserMessage> getMessages()
//	{
//		return this.messages;
//	}
}
