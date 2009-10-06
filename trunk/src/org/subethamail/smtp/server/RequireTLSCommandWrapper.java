package org.subethamail.smtp.server;

import java.io.IOException;

/**
 * Verifies the presence of a TLS connection if TLS is required.
 * The wrapped command is executed when the test succeeds.
 *
 * @author Erik van Oosten
 */
public class RequireTLSCommandWrapper implements Command
{

	private Command wrapped;

	/**
	 * @param wrapped the wrapped command (not null)
	 */
	public RequireTLSCommandWrapper(Command wrapped)
	{
		this.wrapped = wrapped;
	}

	public void execute(String commandString, Session sess) throws IOException
	{
		if (!sess.getServer().getRequireTLS() || sess.isTLSStarted())
			wrapped.execute(commandString, sess);
		else
			sess.sendResponse("530 Must issue a STARTTLS command first");
	}

	public HelpMessage getHelp() throws CommandException
	{
		return wrapped.getHelp();
	}

	public String getName()
	{
		return wrapped.getName();
	}
}
