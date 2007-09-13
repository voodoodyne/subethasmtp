package org.subethamail.smtp.server;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages execution of a SMTP command.
 *
 * @author Jon Stevens
 */
public class CommandHandler
{
	private Map<String, Command> commandMap = new HashMap<String, Command>();
	private static Logger log = LoggerFactory.getLogger(CommandHandler.class);
	
	public CommandHandler()
	{
		// This solution should be more robust than the earlier "manual" configuration.
		for(CommandRegistry registry : CommandRegistry.values())
		{
			addCommand(registry.getCommand());
		}
	}

	public CommandHandler(Collection<Command> availableCommands)
	{
		for(Command command :availableCommands )
		{
			addCommand(command);
		}
	}
	
	public void addCommand(Command command)
	{
		if (log.isDebugEnabled())
			log.debug("Added command: " + command.getName());
		this.commandMap.put(command.getName(), command);
	}
	
	public boolean containsCommand(String command)
	{
		return this.commandMap.containsKey(command);
	}
	
	public void handleCommand(ConnectionContext context, String commandString)
		throws SocketTimeoutException, IOException
	{
		try
		{
			Command command = getCommandFromString(commandString);
			command.execute(commandString, context);
		}
		catch (CommandException e)
		{
			context.sendResponse("500 " + e.getMessage());
		}
	}
	
	private Command getCommandFromString(String commandString)
		throws UnknownCommandException, InvalidCommandNameException
	{
		Command command = null;
		String key = toKey(commandString);
		if (key != null)
		{
			command = this.commandMap.get(key);
		}
		if (command == null)
		{
			// some commands have a verb longer than 4 letters
			String verb = toVerb(commandString);
			if (verb != null)
			{
				command = this.commandMap.get(verb);
			}
		}
		if (command == null)
		{
			throw new UnknownCommandException("Error: command not implemented");
		}
		return command;
	}
	
	private String toKey(String string) throws InvalidCommandNameException
	{
		if (string == null || string.length() < 4)
			throw new InvalidCommandNameException("Error: bad syntax");
		
		return string.substring(0, 4).toUpperCase();
	}
	
	private String toVerb(String string) throws InvalidCommandNameException
	{
		StringTokenizer stringTokenizer = new StringTokenizer(string);
		if (!stringTokenizer.hasMoreTokens())
			throw new InvalidCommandNameException("Error: bad syntax");
		
		return stringTokenizer.nextToken().toUpperCase();
	}
}
