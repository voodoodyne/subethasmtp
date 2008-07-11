package org.subethamail.smtp.server;

import java.io.IOException;

import org.apache.mina.common.IoSession;
import org.subethamail.smtp.auth.Credential;

/**
 * This context is used for managing information
 * about a connection.
 * 
 * @author Jon Stevens
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public interface ConnectionContext extends BaseContext
{
	public IoSession getIOSession();	
	public Session getSession();
	public void sendResponse(String response) throws IOException;
	public void setCredential(Credential cred);
}
