/*
 * Commands.java Created on November 18, 2006, 12:26 PM To change this template,
 * choose Tools | Template Manager and open the template in the editor.
 */

package org.subethamail.smtp.server;

import org.subethamail.smtp.command.AuthCommand;
import org.subethamail.smtp.command.DataCommand;
import org.subethamail.smtp.command.DataEndCommand;
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
 * Enumerates all the {@link Command} available.
 * 
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public enum CommandRegistry
{
	AUTH(new AuthCommand()), 
	DATA(new DataCommand()),
	EHLO(new EhloCommand()), 
	HELO(new HelloCommand()), 
	HELP(new HelpCommand()), 
	MAIL(new MailCommand()), 
	NOOP(new NoopCommand()), 
	QUIT(new QuitCommand()), 
	RCPT(new ReceiptCommand()), 
	RSET(new ResetCommand()), 
	STARTTLS(new StartTLSCommand()), 
	VRFY(new VerifyCommand()),
	
	// Adds a fake command to handle the asynchronous end of DATA 
        DATA_END(new DataEndCommand());

	private Command command;

	private CommandRegistry(Command cmd)
	{
		this.command = cmd;
	}

	public Command getCommand()
	{
		return this.command;
	}

}
