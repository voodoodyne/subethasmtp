package org.subethamail.smtp.command;

import org.subethamail.smtp.server.SMTPServerContext;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class ReceiptCommandTest extends CommandTestCase {

  public void testReceiptCommand() throws Exception {
    final SMTPServerContext serverContext = session.getServerContext();
    ((DummySMTPServerContext) serverContext).addRecipientHost("subethamail.org");
    serverContext.register(new DummyMessageListener());

    assertNull(session.getSender());
    assertEquals("503 Need MAIL before RCPT.", commandDispatcher.executeCommand("RCPT TO: test@subethamail.org", session));
    session.setSender("test@example.com");
    assertEquals("501 Syntax: RCPT TO: <address>  Error in parameters: \"\"", commandDispatcher.executeCommand("RCPT", session));
    assertEquals("550 <test@otherdomain.org> Relaying denied.",
        commandDispatcher.executeCommand("RCPT TO: test@otherdomain.org", session));
    assertEquals(0, session.getRecipients().size());
    assertEquals("553 <test@subethamail.org> User or list unknown.",
        commandDispatcher.executeCommand("RCPT TO: test@subethamail.org", session));
    assertEquals(0, session.getRecipients().size());
    assertEquals("250 <validuser@subethamail.org> Recipient ok.",
        commandDispatcher.executeCommand("RCPT TO: validuser@subethamail.org", session));
    assertEquals(1, session.getRecipients().size());
  }

  public void testDisabledDomainRestrictions() throws Exception {
    final SMTPServerContext serverContext = session.getServerContext();
    serverContext.register(new DummyMessageListener());
    serverContext.setRecipientDomainFilteringEnabled(false);

    assertNull(session.getSender());
    assertEquals("503 Need MAIL before RCPT.", commandDispatcher.executeCommand("RCPT TO: test@subethamail.org", session));
    session.setSender("test@example.com");
    assertEquals("553 <test@otherdomain.net> User or list unknown.",
        commandDispatcher.executeCommand("RCPT TO: test@otherdomain.net", session));
    assertEquals(0, session.getRecipients().size());
    assertEquals("250 <validuser@subethamail.org> Recipient ok.",
        commandDispatcher.executeCommand("RCPT TO: validuser@subethamail.org", session));
    assertEquals(1, session.getRecipients().size());
  }

  public void testRcptWithoutWhitespace() throws Exception {
    final SMTPServerContext serverContext = session.getServerContext();
    serverContext.register(new DummyMessageListener());
    serverContext.setRecipientDomainFilteringEnabled(false);

    assertNull(session.getSender());
    session.setSender("test@example.com");
    assertEquals("250 <validuser@subethamail.org> Recipient ok.",
        commandDispatcher.executeCommand("RCPT TO:<validuser@subethamail.org>", session));
    assertEquals(1, session.getRecipients().size());
  }


  public void testReceiptCommandHelp() throws Exception {
    assertEquals("214-RCPT TO: <recipient> [ <parameters> ]\n" +
        "214-    Specifies the recipient. Can be used any number of times.\n" +
        "214-    Parameters are ESMTP extensions. See \"HELP DSN\" for details.\n" +
        "214 End of RCPT info", commandDispatcher.getHelpMessage("RCPT").toOutputString());
  }

  protected void setUp() throws Exception {
    super.setUp();
    new ReceiptCommand(commandDispatcher);
  }

}
