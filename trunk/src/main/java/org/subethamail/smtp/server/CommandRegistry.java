/*
 * Commands.java Created on November 18, 2006, 12:26 PM To change this template,
 * choose Tools | Template Manager and open the template in the editor.
 */

package org.subethamail.smtp.server;

import org.subethamail.smtp.command.AuthCommand;
import org.subethamail.smtp.command.DataCommand;
import org.subethamail.smtp.command.EhloCommand;
import org.subethamail.smtp.command.HelloCommand;
import org.subethamail.smtp.command.HelpCommand;
import org.subethamail.smtp.command.MailCommand;
import org.subethamail.smtp.command.NoopCommand;
import org.subethamail.smtp.command.QuitCommand;
import org.subethamail.smtp.command.ReceiptCommand;
import org.subethamail.smtp.command.ResetCommand;
import org.subethamail.smtp.command.StartTLSCommand;
import org.subethamail.smtp.command.VerifyCommand;

/**
 * Enumerates all the Commands made available in this release.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public enum CommandRegistry
{
	AUTH(new AuthCommand()),
	DATA(new DataCommand()),
	EHLO(new EhloCommand(), false),
	HELO(new HelloCommand()),
	HELP(new HelpCommand()),
	MAIL(new MailCommand()),
	NOOP(new NoopCommand(), false),
	QUIT(new QuitCommand(), false),
	RCPT(new ReceiptCommand()),
	RSET(new ResetCommand()),
	STARTTLS(new StartTLSCommand(), false),
	VRFY(new VerifyCommand());

	private Command command;

	/** */
	private CommandRegistry(Command cmd)
	{
		this(cmd, true);
	}

	/** */
	private CommandRegistry(Command cmd, boolean checkForStartedTLSWhenRequired)
	{
		if (checkForStartedTLSWhenRequired)
			this.command = new RequireTLSCommandWrapper(cmd);
		else
			this.command = cmd;
	}

	/** */
	public Command getCommand()
	{
		return this.command;
	}
}
