package org.subethamail.smtp.command;


/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class MailCommandTest extends CommandTestCase {
  private MailCommand mailCommand;

  public void testMailCommand() throws Exception {
    assertNull(session.getSender());
    assertEquals("250 <test@example.com> Sender ok.", commandDispatcher.executeCommand("MAIL FROM: test@example.com", session));
    assertEquals("test@example.com", session.getSender());
    assertEquals("503 Sender already specified.", commandDispatcher.executeCommand("MAIL FROM: another@example.com", session));
    assertEquals("test@example.com", session.getSender());
  }

  public void testInvalidSenders() throws Exception {
    assertEquals("553 <test> Domain name required.", commandDispatcher.executeCommand("MAIL FROM: test", session));
  }

  public void testMalformedMailCommand() throws Exception {
    assertEquals("501 Syntax: MAIL FROM: <address>  Error in parameters: \"\"", commandDispatcher.executeCommand("MAIL", session));
  }

  public void testMailWithoutWhitespace() throws Exception {
    assertEquals("250 <validuser@subethamail.org> Sender ok.",
        commandDispatcher.executeCommand("MAIL FROM:<validuser@subethamail.org>", session));
    assertEquals("validuser@subethamail.org", session.getSender());
  }

  public void testMailCommandHelp() throws Exception {
    assertEquals("214-MAIL FROM: <sender> [ <parameters> ]\n" +
        "214-    Specifies the sender. Parameters are ESMTP extensions.\n" +
        "214-    See \"HELP DSN\" for details.\n" +
        "214 End of MAIL info", commandDispatcher.getHelpMessage("MAIL").toOutputString());
  }

  protected void setUp() throws Exception {
    super.setUp();
    mailCommand = new MailCommand(commandDispatcher);
  }
}
