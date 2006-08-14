package org.subethamail.smtp.test;

import java.io.IOException;
import java.net.Socket;

import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.ConnectionHandler;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.server.Session;

public class DummyContext implements ConnectionContext
{
	String response;
	Session session;
	SMTPServer server;

	public DummyContext(Session session, SMTPServer server)
	{
		this.session = session;
		this.server = server;
	}

	public ConnectionHandler getConnection()
	{
		return null;
	}

	public SMTPServer getServer()
	{
		return this.server;
	}

	public Session getSession()
	{
		return this.session;
	}

	public Socket getSocket()
	{
		return null;
	}

	public void sendResponse(String response) throws IOException
	{
		this.response = response;
	}

	public String getResponse()
	{
		String tmp = new String(this.response);
		this.response = null;
		return tmp;
	}
}
