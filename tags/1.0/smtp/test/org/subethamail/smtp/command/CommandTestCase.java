package org.subethamail.smtp.command;

import junit.framework.TestCase;

import org.subethamail.smtp.session.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public abstract class CommandTestCase extends TestCase {
  protected CommandDispatcher commandDispatcher;
  protected Session session;
  protected static final String OK = "250 OK";

  protected void setUp() throws Exception {
  super.setUp();
    DummySMTPServerContext serverContext = new DummySMTPServerContext();
    commandDispatcher = new CommandDispatcher(serverContext);
    session = new Session(serverContext, "mail.example.com");
  }
}
