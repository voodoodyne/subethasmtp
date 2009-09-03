package org.subethamail.smtp.command;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.Session;

/**
 * @author Michael Wildpaner &lt;mike@wildpaner.com&gt;
 * @author Jeff Schnitzer
 */
public class StartTLSCommand extends BaseCommand
{
	private final static Logger log = LoggerFactory.getLogger(StartTLSCommand.class);

	/** */
	public StartTLSCommand()
	{
		super("STARTTLS", "The starttls command");
	}

	/** */
	@Override
	public void execute(String commandString, Session sess) throws IOException
	{
		if (!commandString.trim().toUpperCase(Locale.ENGLISH).equals(this.getName()))
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
		catch (SSLHandshakeException ex)
		{
			// "no cipher suites in common" is common and puts a lot of crap in the logs.
			// This will at least limit it to a single WARN line and not a whole stacktrace.
			// Unfortunately it might catch some other types of SSLHandshakeException (if
			// in fact other types exist), but oh well.
			log.warn("startTLS() failed: " + ex);
		}
		catch (IOException ex)
		{
			log.warn("startTLS() failed: " + ex.getMessage(), ex);
		}
	}
}
