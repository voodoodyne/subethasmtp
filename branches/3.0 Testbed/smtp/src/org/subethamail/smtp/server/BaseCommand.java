package org.subethamail.smtp.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
abstract public class BaseCommand implements Command
{
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(BaseCommand.class);
		
	/** Name of the command, ie HELO */
	private String name;
	/** The help message for this command*/
	private HelpMessage helpMsg;
	
	protected BaseCommand(String name, String help)
	{
		this.name = name;
		this.helpMsg = new HelpMessage(name, help);
	}
	
	protected BaseCommand(String name, String help, String argumentDescription)
	{
		this.name = name;
		this.helpMsg =  new HelpMessage(name, help, argumentDescription);
	}
	
	/**
	 * This is the main method that you need to override in order to implement a command.
	 */
	abstract public void execute(String commandString, Session context) throws IOException;
	
	public HelpMessage getHelp()
	{
		return this.helpMsg;
	}
	
	
	public String getName()
	{
		return name;
	}
	
	protected String getArgPredicate(String commandString)
	{
		if (commandString == null || commandString.length() < 4)
			return "";
		
		return commandString.substring(4).trim();
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