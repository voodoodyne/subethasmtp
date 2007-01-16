package org.subethamail.smtp.command;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;

/**
 * @author Michael Wildpaner &lt;mike@wildpaner.com&gt;
 */
public class StartTLSCommand extends BaseCommand
{
	private static Log log = LogFactory.getLog(StartTLSCommand.class);

	public StartTLSCommand()
	{
		super("STARTTLS", "The starttls command");
	}

	@Override
	public void execute(String commandString, ConnectionContext context) throws IOException
	{
		if (!commandString.trim().toUpperCase().equals(this.getName()))
		{
			context.sendResponse("501 Syntax error (no parameters allowed)");
			return;
		}

		try
		{
			Socket socket = context.getConnection().getSocket();
			if (socket instanceof SSLSocket)
			{
				context.sendResponse("454 TLS not available due to temporary reason: TLS already active");
				return;
			}

			context.sendResponse("220 Ready to start TLS");

			InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
			SSLSocketFactory sf = ((SSLSocketFactory) SSLSocketFactory.getDefault());
			SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(), socket.getPort(), true));

			// we are a server
			s.setUseClientMode(false);

			// allow all supported cipher suites
			s.setEnabledCipherSuites(s.getSupportedCipherSuites());
			
			s.startHandshake();

			context.getConnection().setSocket(s);
			context.getSession().reset(); // clean slate
		}
		catch (IOException e)
		{
			log.warn("startTLS() failed: " + e.getMessage(), e);
		}
	}
}
