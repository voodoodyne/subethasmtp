package org.subethamail.smtp.server;

import java.io.IOException;

import org.subethamail.smtp.DropConnectionException;

/**
 * Describes a SMTP command
 *
 * @author Jon Stevens
 * @author Scott Hernandez
 */
public interface Command
{
	/** */
	public void execute(String commandString, Session sess) throws IOException, 
			DropConnectionException;

	/** */
	public HelpMessage getHelp() throws CommandException;

	/**
	 * Returns the name of the command in upper case. For example "QUIT".
	 */
	public String getName();
}
