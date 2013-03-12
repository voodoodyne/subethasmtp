package org.subethamail.smtp.server;

import org.subethamail.smtp.DropConnectionException;

import java.io.IOException;

/**
 * Thin wrapper around any command to make sure authentication
 * has been performed.
 *
 * @author Evgeny Naumenko
 */
public class RequireAuthCommandWrapper implements Command
{

    private Command wrapped;

    /**
     * @param wrapped the wrapped command (not null)
     */
    public RequireAuthCommandWrapper(Command wrapped)
    {
        this.wrapped = wrapped;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(String commandString, Session sess)
            throws IOException, DropConnectionException
    {
        if (!sess.getServer().getRequireAuth() || sess.isAuthenticated())
            wrapped.execute(commandString, sess);
        else
            sess.sendResponse("530 5.7.0  Authentication required");
    }

    /**
     * {@inheritDoc}
     */
    public HelpMessage getHelp() throws CommandException
    {
        return wrapped.getHelp();
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return wrapped.getName();
    }
}
