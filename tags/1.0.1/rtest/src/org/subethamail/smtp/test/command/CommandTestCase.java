package org.subethamail.smtp.test.command;

import junit.framework.TestCase;

import org.subethamail.smtp.server.CommandHandler;
import org.subethamail.smtp.server.Session;
import org.subethamail.smtp.test.DummyContext;
import org.subethamail.smtp.test.DummyServer;

/**
 * @author Jon Stevens
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public abstract class CommandTestCase extends TestCase
{
	protected CommandHandler commandHandler;
	protected Session session;
	protected DummyContext context;
	protected DummyServer server;
	
	protected void setUp() throws Exception
	{
		super.setUp();

		server = new DummyServer();
		server.setPort(1999);
		server.setHostname("127.0.0.1");
		// No need to start it!
		//server.start();

		commandHandler = server.getServer().getCommandHandler();
		session = new Session();
		context = new DummyContext(session, server.getServer());
	}
	
	public DummyContext getContext()
	{
		return this.context;
	}
	
	public Session getSession()
	{
		return this.session;
	}

	public void doHelo() throws Exception
	{
		commandHandler.handleCommand(getContext(), "HELO foo.com");
	}

	public void doMail() throws Exception
	{
		commandHandler.handleCommand(getContext(), "MAIL FROM: <test@subethamail.org>");
	}

	public void doRcpt() throws Exception
	{
		commandHandler.handleCommand(getContext(), "RCPT TO: <testuser@subethamail.org>");
	}
}
