package org.subethamail.smtp.server;

import java.io.IOException;
import java.net.Socket;

/**
 * This context is used for managing information
 * about a connection.
 * 
 * @author Jon Stevens
 */
public interface ConnectionContext
{
	public Session getSession();
	public ConnectionHandler getConnection();
	public SMTPServer getServer();
	public Socket getSocket();
	public void sendResponse(String response) throws IOException;
}
