package org.subethamail.smtp.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.AuthenticationHandler;

/**
 * This handler makes it possible to sum the capabilities of two or more handlers types.<br>
 * Hence, you can design a single handler for each authentication mechanism and decide how many
 * authentication mechanisms to support by simply plugging them here.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public class PluginAuthenticationHandler implements AuthenticationHandler
{

	private List<AuthenticationHandler> plugins;

	private AuthenticationHandler activeHandler;

	/** Creates a new instance of PluginAuthenticationHandler */
	public PluginAuthenticationHandler()
	{
		this.plugins = new ArrayList<AuthenticationHandler>();
	}

	public List<String> getAuthenticationMechanisms()
	{
		List<String> ret = new ArrayList<String>();
		for (AuthenticationHandler plugin : plugins)
		{
			ret.addAll(plugin.getAuthenticationMechanisms());
		}
		return ret;
	}

	public boolean auth(String clientInput, StringBuilder response)
			throws RejectException
	{
		StringTokenizer stk = new StringTokenizer(clientInput);
		if (stk.nextToken().equalsIgnoreCase("AUTH"))
		{
			resetState();
			activateHandler(stk.nextToken().toUpperCase());
		}
		return getActiveHandler().auth(clientInput, response);
	}

	public void resetState()
	{
		if (getActiveHandler() != null)
		{
			getActiveHandler().resetState();
		}
		setActiveHandler(null);
	}

	private void activateHandler(final String mechanism)
	{
		for (AuthenticationHandler plugin : plugins)
		{
			if (plugin.getAuthenticationMechanisms().contains(
					mechanism))
			{
				setActiveHandler(plugin);
				return;
			}
		}
	}

	public List<AuthenticationHandler> getPlugins()
	{
		return plugins;
	}

	public void addPlugin(AuthenticationHandler plugin)
	{
		plugins.add(plugin);
	}

	public void setPlugins(List<AuthenticationHandler> plugins)
	{
		this.plugins = plugins;
	}

	public AuthenticationHandler getActiveHandler()
	{
		return activeHandler;
	}

	public void setActiveHandler(AuthenticationHandler activeHandler)
	{
		this.activeHandler = activeHandler;
	}
}
