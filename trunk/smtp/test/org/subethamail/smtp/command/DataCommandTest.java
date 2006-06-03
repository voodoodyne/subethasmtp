package org.subethamail.smtp.command;

import org.subethamail.smtp.session.DummySession;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class DataCommandTest extends CommandTestCase {
  public void testDataCommand() throws Exception {
    session = new DummySession("test.example.com", new DummySMTPServerContext());
    ((DummySession) session).setMessageId("dummy-message-id");
    DummyMessageListener messageListener = new DummyMessageListener();
    commandDispatcher.getServerContext().register(messageListener);
    assertEquals("503 Need MAIL command.", commandDispatcher.executeCommand("DATA", session));
    session.setSender("test@example.com");
    assertEquals("503 Need RCPT (recipient)", commandDispatcher.executeCommand("DATA", session));
    session.addRecipient("validuser@subethamail.org");
    assertEquals("354 Enter mail, end with \".\" on a line by itself.", commandDispatcher.executeCommand("DATA", session));
    commandDispatcher.executeCommand("From: test@example.com", session);
    commandDispatcher.executeCommand("To: validuser@subethamail.org", session);
    commandDispatcher.executeCommand("Subject: Test Message", session);
    commandDispatcher.executeCommand("", session);
    commandDispatcher.executeCommand("Some text.", session);
    commandDispatcher.executeCommand("Some more text.", session);
    // A period at the beginning of a line should be swallowed. The following line should have one more period
    // at the front than the resulting message.
    commandDispatcher.executeCommand("..Some text starting with one period.", session);
    assertEquals("250 Message ID <dummy-message-id> accepted for delivery.", commandDispatcher.executeCommand(".", session));
    assertEquals("From: test@example.com\n" +
        "To: validuser@subethamail.org\n" +
        "Subject: Test Message\n" +
        "\n" +
        "Some text.\n" +
        "Some more text.\n" +
        ".Some text starting with one period.\n", messageListener.getMessage());
  }

  public void testDataCommandHelp() throws Exception {
    new ResetCommand(commandDispatcher);
    assertEquals("214-DATA\n" +
        "214-    Following text is collected as the message.\n" +
        "214-    End data with <CR><LF>.<CR><LF>\n" +
        "214 End of DATA info", commandDispatcher.getHelpMessage("DATA").toOutputString());
  }

  protected void setUp() throws Exception {
    super.setUp();
    new DataCommand(commandDispatcher);
  }


}
