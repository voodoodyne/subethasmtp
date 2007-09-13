package org.subethamail.smtp.server;

import java.util.StringTokenizer;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public class HelpMessage
{
	private String commandName;

	private String argumentDescription;

	private String helpMessage;

	private String outputString;

	public HelpMessage(String commandName, String helpMessage, String argumentDescription)
	{
		this.commandName = commandName;
		this.argumentDescription = argumentDescription == null ? "" : " "
				+ argumentDescription;
		this.helpMessage = helpMessage;
		buildOutputString();
	}

	public HelpMessage(String commandName, String helpMessage)
	{
		this(commandName, helpMessage, null);
	}

	public String getName()
	{
		return this.commandName;
	}

	public String toOutputString()
	{
		return outputString;
	}

	private void buildOutputString()
	{
		StringTokenizer stringTokenizer = new StringTokenizer(helpMessage, "\n");
		StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("214-").append(commandName).append(argumentDescription);
		while (stringTokenizer.hasMoreTokens())
		{
			stringBuilder.append("\n214-    ").append(
					stringTokenizer.nextToken());
		}
		stringBuilder.append("\n214 End of ").append(commandName).append(
				" info");
		outputString = stringBuilder.toString();
	}

	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final HelpMessage that = (HelpMessage) o;
		if (argumentDescription != null ? !argumentDescription
				.equals(that.argumentDescription)
				: that.argumentDescription != null)
			return false;
		if (commandName != null ? !commandName.equals(that.commandName)
				: that.commandName != null)
			return false;
		if (helpMessage != null ? !helpMessage.equals(that.helpMessage)
				: that.helpMessage != null)
			return false;
		return true;
	}

	public int hashCode()
	{
		int result;
		result = (commandName != null ? commandName.hashCode() : 0);
		result = 29
				* result
				+ (argumentDescription != null ? argumentDescription.hashCode()
						: 0);
		result = 29 * result
				+ (helpMessage != null ? helpMessage.hashCode() : 0);
		return result;
	}
}
