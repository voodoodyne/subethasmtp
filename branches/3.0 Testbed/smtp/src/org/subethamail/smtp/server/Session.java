package org.subethamail.smtp.server;

import org.subethamail.smtp.MessageHandler;

/**
 * A sesssion describes events which happen during a
 * SMTP session. It keeps track of all of the recipients
 * who will receive the message.
 * 
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
@SuppressWarnings("serial")
public class Session
{
	private boolean authenticated =false;
	private boolean dataMode = false;
	private boolean hasSeenHelo = false;
	private boolean active = true;
	private boolean hasSender = false;
	private int recipientCount = 0;
	private MessageHandler messageHandler;

	public Session(MessageHandler exchange)
	{
		this.messageHandler = exchange;
	}

	public boolean isActive()
	{
		return this.active;
	}

	public void quit()
	{
		this.active = false;
	}

	public boolean getHasSender()
	{
		return this.hasSender;
	}

	public void setHasSender(boolean value)
	{
		this.hasSender = value;
	}

	public boolean getHasSeenHelo()
	{
		return this.hasSeenHelo;
	}

	public void setHasSeenHelo(boolean hasSeenHelo)
	{
		this.hasSeenHelo = hasSeenHelo;
	}

	public boolean isDataMode()
	{
		return this.dataMode;
	}

	public void setDataMode(boolean dataMode)
	{
		this.dataMode = dataMode;
	}
	
	public void addRecipient()
	{
		this.recipientCount++;
	}
	
	public int getRecipientCount()
	{
		return this.recipientCount;
	}
	
	public MessageHandler getMessageHandler()
	{
		return this.messageHandler;
	}

	public boolean isAuthenticated()
	{
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated)
	{
		this.authenticated = authenticated;
	}
	
	/**
	 * Executes a full reset() of the session
	 * which requires a new HELO command to be sent
	 */
	public void reset()
	{
		reset(false);
		setAuthenticated(false);
	}

	public void reset(boolean hasSeenHelo)
	{
		this.messageHandler.resetMessageState();
		this.hasSender = false;
		this.dataMode = false;
		this.active = true;
		this.hasSeenHelo = hasSeenHelo;
		this.recipientCount = 0;
	}
}
