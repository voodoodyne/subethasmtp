package org.subethamail.smtp.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
abstract public class BaseCommand implements Command
{
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(BaseCommand.class);
		
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
}