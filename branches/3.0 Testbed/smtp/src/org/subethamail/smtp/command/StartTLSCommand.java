package org.subethamail.smtp.command;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;

/**
 * @author Michael Wildpaner &lt;mike@wildpaner.com&gt;
 * @author Jeff Schnitzer
 */
public class StartTLSCommand extends BaseCommand
{
	private static Log log = LogFactory.getLog(StartTLSCommand.class);

	public StartTLSCommand()
	{
		super("STARTTLS", "The starttls command");
	}

	@Override
	public void execute(String commandString, Session sess) throws IOException
	{
		if (!commandString.trim().toUpperCase().equals(this.getName()))
		{
			sess.sendResponse("501 Syntax error (no parameters allowed)");
			return;
		}

		try
		{
			Socket socket = sess.getSocket();
			if (socket instanceof SSLSocket)
			{
				sess.sendResponse("454 TLS not available due to temporary reason: TLS already active");
				return;
			}

			sess.sendResponse("220 Ready to start TLS");

			InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
			SSLSocketFactory sf = ((SSLSocketFactory) SSLSocketFactory.getDefault());
			SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(), socket.getPort(), true));

			// we are a server
			s.setUseClientMode(false);

			// allow all supported cipher suites
			s.setEnabledCipherSuites(s.getSupportedCipherSuites());
			
			s.startHandshake();

			sess.setSocket(s);
			sess.resetMessageState(); // clean slate
		}
		catch (IOException e)
		{
			log.warn("startTLS() failed: " + e.getMessage(), e);
		}
	}
}
