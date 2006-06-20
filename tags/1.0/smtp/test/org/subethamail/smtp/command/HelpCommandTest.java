package org.subethamail.smtp.command;


/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class HelpCommandTest extends CommandTestCase {
  private HelpCommand helpCommand;

  public void testHelpCommandWithoutArgList() throws Exception {
    new NoopCommand(commandDispatcher);
    assertEquals(HelpMessageTest.NOOP_HELP_OUTPUT, helpCommand.execute("help noop", session));
  }

  public void testHelpCommandWithArgList() throws Exception {
    assertEquals(HelpMessageTest.HELP_HELP_OUTPUT, helpCommand.execute("help help", session));
  }

  public void testUnknownTopic() throws Exception {
    assertEquals("504 HELP topic \"foo\" unknown.", helpCommand.execute("HELP foo", session));
  }

  public void testHelp() throws Exception {
    commandDispatcher.setServerContext(new DummySMTPServerContext("1.0a2", "example.subethamail.org", 25));

    String expectedOutput = "214-This is the SubEthaMail SMTP Server version 1.0a2 running on example.subethamail.org\r\n" +
        "214-Topics:\r\n" +
        "214-    HELP\r\n" +
        "214-For more info use \"HELP <topic>\".\r\n" +
        "214-For more information about this server, visit:\r\n" +
        "214-    http://subetha.tigris.org\r\n" +
        "214-To report bugs in the implementation, send email to:\r\n" +
        "214-    issues@subetha.tigris.org\r\n" +
        "214-For local information send email to Postmaster at your site.\r\n" +
        "214 End of HELP info";
    assertEquals(expectedOutput, commandDispatcher.executeCommand("HELP", session));
  }

  public void testHelpDSN() throws Exception {
    // TODO(imf): Implement
  }

  public void testHelpTopicListing() throws Exception {
    assertEquals("214-    HELP\r\n", helpCommand.getFormattedTopicList());
    new NoopCommand(commandDispatcher);
    assertEquals("214-    HELP    NOOP\r\n", helpCommand.getFormattedTopicList());
  }

  protected void setUp() throws Exception {
  super.setUp();
    helpCommand = new HelpCommand(commandDispatcher);
  }
}
