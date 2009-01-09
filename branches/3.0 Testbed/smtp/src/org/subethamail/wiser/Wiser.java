/*
 * $Id$
 * $URL$
 */

package org.subethamail.wiser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

/**
 * Wiser is a tool for unit testing applications that send mail.  Your unit
 * tests can start Wiser, run tests which generate emails, then examine the
 * emails that Wiser received and verify their integrity.
 *
 * Wiser is not intended to be a "real" mail server and is not adequate
 * for that purpose; it simply stores all mail in memory.  Use the
 * MessageHandlerFactory interface (optionally with the SimpleMessageListenerAdapter)
 * of SubEthaSMTP instead.
 *
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class Wiser implements SimpleMessageListener
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
		this.server = new SMTPServer(new SimpleMessageListenerAdapter(this));
		this.server.setPort(25);
	}

	/**
	 *
	 */
	public Wiser(boolean acceptAuth)
	{

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

	/** A main() for this class. Starts up the server. */
	public static void main(String[] args) throws Exception
	{
		Wiser wiser = new Wiser();
		wiser.start();
	}

	/** Always accept everything */
	public boolean accept(String from, String recipient)
	{
		return true;
	}

	/** Cache the messages in memory */
	public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		data = new BufferedInputStream(data);

		// read the data from the stream
		int current;
		while ((current = data.read()) >= 0)
		{
			out.write(current);
		}

		// create a new WiserMessage.
		this.messages.add(new WiserMessage(this, from, recipient, out.toByteArray()));
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

	/**
	 * @return the server implementation
	 */
	public SMTPServer getServer()
	{
		return this.server;
	}
}
