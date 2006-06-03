package org.subethamail.smtp.command;


/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class VerifyCommandTest extends CommandTestCase {
  public void testVerifyCommand() throws Exception {
    assertEquals("252 Cannot VRFY user; try RCPT to attemt delivery.", commandDispatcher.executeCommand("VRFY", session));
  }

  public void testVerifyCommandHelp() throws Exception {
    assertEquals("214-VRFY <recipient>\n" +
        "214-    Verify an address. To see the address to which it aliases,\n" +
        "214-    use EXPN instead.\n" +
        "214-    This command is often disabled for security reasons.\n" +
        "214 End of VRFY info", commandDispatcher.getHelpMessage("VRFY").toOutputString());
  }

  protected void setUp() throws Exception {
    super.setUp();
    new VerifyCommand(commandDispatcher);
  }
}
