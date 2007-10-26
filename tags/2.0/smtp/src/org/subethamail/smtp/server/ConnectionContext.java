package org.subethamail.smtp.server;

import java.io.IOException;
import java.net.SocketAddress;

import org.apache.mina.common.IoSession;
import org.subethamail.smtp.server.io.ByteBufferInputStream;

/**
 * This context is used for managing information
 * about a connection.
 * 
 * @author Jon Stevens
 */
public interface ConnectionContext
{
	public ByteBufferInputStream getInput();
	public Session getSession();
	public void sendResponse(String response) throws IOException;
	public SocketAddress getRemoteAddress();
	public SMTPServer getSMTPServer();
	public IoSession getIOSession();
}
