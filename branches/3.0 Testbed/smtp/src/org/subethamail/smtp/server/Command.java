package org.subethamail.smtp.server;

import java.io.IOException;

/**
 * Describes a SMTP command
 * 
 * @author Jon Stevens
 */
public interface Command
{
	public void execute(String commandString, ConnectionHandler sess) throws IOException;
	public HelpMessage getHelp(String commandName) throws CommandException;
	public String getName();
}
