package org.subethamail.smtp.server;

import java.io.IOException;

/**
 * Describes a SMTP command
 * 
 * @author Jon Stevens
 * @author Scott Hernandez
 */
public interface Command
{
	public void execute(String commandString, Session sess) throws IOException;
	public HelpMessage getHelp() throws CommandException;
	public String getName();
}
