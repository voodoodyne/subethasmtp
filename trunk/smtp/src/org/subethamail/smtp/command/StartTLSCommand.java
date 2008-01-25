package org.subethamail.smtp.command;

import java.io.IOException;

import org.apache.mina.filter.SSLFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.BaseCommand;
import org.subethamail.smtp.server.ConnectionContext;
import org.subethamail.smtp.server.io.DummySSLSocketFactory;

/**
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class StartTLSCommand extends BaseCommand
{
	private static Logger log = LoggerFactory.getLogger(StartTLSCommand.class);

	private static SSLFilter sslFilter;

	static
	{
		try
		{
			DummySSLSocketFactory socketFactory = new DummySSLSocketFactory();
			sslFilter = new SSLFilter(socketFactory.getSSLContext());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public StartTLSCommand()
	{
		super("STARTTLS", "The starttls command");
	}

	/**
	 * Ability to override the SSLFilter
	 * @param filter
	 */
	public static void setSSLFilter(SSLFilter filter)
	{
		if (filter == null)
			throw new IllegalArgumentException("filter argument can't be null");

		sslFilter = filter;
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
			if (sslFilter.isSSLStarted(context.getIOSession()))
			{
				context.sendResponse("454 TLS not available due to temporary reason: TLS already active");
				return;
			}

			// Insert SSLFilter to get ready for handshaking
			context.getIOSession().getFilterChain().addFirst("SSLfilter", sslFilter);

			// Disable encryption temporarilly.
			// This attribute will be removed by SSLFilter
			// inside the Session.write() call below.
			context.getIOSession().setAttribute(SSLFilter.DISABLE_ENCRYPTION_ONCE, Boolean.TRUE);

			// Write StartTLSResponse which won't be encrypted.
			context.sendResponse("220 Ready to start TLS");

			// Now DISABLE_ENCRYPTION_ONCE attribute is cleared.
			assert context.getIOSession().getAttribute(SSLFilter.DISABLE_ENCRYPTION_ONCE) == null;

			context.getSession().reset(); // clean state
		}
		catch (Exception e)
		{
			log.warn("startTLS() failed: " + e.getMessage(), e);
		}
	}
}