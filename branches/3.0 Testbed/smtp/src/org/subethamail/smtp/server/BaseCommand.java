package org.subethamail.smtp.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
abstract public class BaseCommand implements Command
{
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(BaseCommand.class);
	
	/** All the help messages keyed by the command name */
	private static Map<String, HelpMessage> helpMessageMap = new HashMap<String, HelpMessage>();
	
	/** Name of the command, ie HELO */
	private String name;
	
	public BaseCommand(String name, String help)
	{
		this.name = name;
		registerHelp(new HelpMessage(name, help));
	}
	
	public BaseCommand(String name, String help, String argumentDescription)
	{
		this.name = name;
		registerHelp(new HelpMessage(name, help, argumentDescription));
	}
	
	/**
	 * This is the main method that you need to override in order to implement a command.
	 */
	abstract public void execute(String commandString, Session context) throws IOException;
	
	static public void registerHelp(HelpMessage helpMessage)
	{
		helpMessageMap.put(helpMessage.getName().toUpperCase(), helpMessage);
	}
	
	public HelpMessage getHelp(String commandName)
		throws CommandException
	{
		HelpMessage msg = helpMessageMap.get(commandName.toUpperCase());
		if (msg == null)
			throw new CommandException();
		return msg;
	}
	
	public Map<String, HelpMessage> getHelp()
	{
		return helpMessageMap;
	}
	
	protected String getArgPredicate(String commandString)
	{
		if (commandString == null || commandString.length() < 4)
			return "";
		
		return commandString.substring(4).trim();
	}
	
	public String getName()
	{
		return name;
	}
	
	protected String[] getArgs(String commandString)
	{
		List<String> strings = new ArrayList<String>();
		StringTokenizer stringTokenizer = new StringTokenizer(commandString);
		while (stringTokenizer.hasMoreTokens())
		{
			strings.add(stringTokenizer.nextToken());
		}
		
		return strings.toArray(new String[strings.size()]);
	}
	
	protected String extractEmailAddress(String args, int subcommandOffset)
	{
		String address = args.substring(subcommandOffset).trim();
		if (address.indexOf('<') == 0)
			address = address.substring(1, address.indexOf('>'));
		
		return address;
	}
}
