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
	 * Populates a default set of commands based with the CommandRegistry commands.
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
			handleCommand(context, commandString, command);
		}
		catch (CommandException e)
		{
			context.sendResponse("500 " + e.getMessage());
		}
	}
	
	/**
	 * Calls the execute method on a command.
	 * 
	 * @param context
	 * @param commandString
	 * @throws SocketTimeoutException
	 * @throws IOException
	 */
	public void handleCommand(ConnectionContext context, String commandString, Command command)
		throws SocketTimeoutException, IOException
	{
			command.execute(commandString, context);
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
	protected Command getCommandFromString(String commandString)
		throws UnknownCommandException, InvalidCommandNameException
	{
            String verb = toVerb(commandString);

	        Command command = this.commandMap.get(verb);
		if (command == null)
		    throw new UnknownCommandException("Error: command not implemented");
		
		return command;
	}
	
	/** */
	private String toVerb(String string) throws InvalidCommandNameException
	{
            if (string == null || string.length() < 4)
                throw new InvalidCommandNameException("Error: bad syntax");

            StringTokenizer stringTokenizer = new StringTokenizer(string);
            if (!stringTokenizer.hasMoreTokens())
		throw new InvalidCommandNameException("Error: bad syntax");
		
            return stringTokenizer.nextToken().toUpperCase();
	}
}
