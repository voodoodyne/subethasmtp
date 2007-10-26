package org.subethamail.smtp.server;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.command.AuthCommand;

/**
 * This class manages execution of a SMTP command.
 *
 * @author Jon Stevens
 */
public class CommandHandler
{
	private Map<String, Command> commandMap = new HashMap<String, Command>();
	private static Logger log = LoggerFactory.getLogger(CommandHandler.class);
	
	/**
	 * Populates a default set of commands based on what is in the CommandRegistry.
	 */
	public CommandHandler()
	{
		for(CommandRegistry registry : CommandRegistry.values())
		{
			addCommand(registry.getCommand());
		}
	}

	/**
	 * Pass in a Collection of Command objects.
	 * @param availableCommands
	 */
	public CommandHandler(Collection<Command> availableCommands)
	{
		for(Command command :availableCommands )
		{
			addCommand(command);
		}
	}
	
	/**
	 * Adds a new command to the map.
	 * @param command
	 */
	public void addCommand(Command command)
	{
		if (log.isDebugEnabled())
			log.debug("Added command: " + command.getName());
		this.commandMap.put(command.getName(), command);
	}

	/**
	 * Does the map contain the named command?
	 * @param command
	 * @return true if the command exists
	 */
	public boolean containsCommand(String command)
	{
		return this.commandMap.containsKey(command);
	}

	/**
	 * Calls the execute method on a command.
	 * 
	 * @param context
	 * @param commandString
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
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
	
	/**
	 * Executes an auth command.
	 * 
	 * @param context
	 * @param commandString
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	public void handleAuthChallenge(ConnectionContext context, String commandString)
		throws SocketTimeoutException, IOException
	{
		Command command = this.commandMap.get(AuthCommand.VERB);
		command.execute(commandString, context);
	}	
	
	/**
	 * Given a string, find the Command object.
	 * 
	 * @param commandString
	 * @return The command object.
	 * @throws UnknownCommandException
	 * @throws InvalidCommandNameException
	 */
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
	
	/** */
	private String toKey(String string) throws InvalidCommandNameException
	{
		if (string == null || string.length() < 4)
			throw new InvalidCommandNameException("Error: bad syntax");
		
		return string.substring(0, 4).toUpperCase();
	}
	
	/** */
	private String toVerb(String string) throws InvalidCommandNameException
	{
		StringTokenizer stringTokenizer = new StringTokenizer(string);
		if (!stringTokenizer.hasMoreTokens())
			throw new InvalidCommandNameException("Error: bad syntax");
		
		return stringTokenizer.nextToken().toUpperCase();
	}
}
