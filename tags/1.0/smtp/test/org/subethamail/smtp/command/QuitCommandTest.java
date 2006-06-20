package org.subethamail.smtp.command;


/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class QuitCommandTest extends CommandTestCase {
  public void testQuitCommand() throws Exception {
    assertTrue(session.isActive());
    assertEquals("221 test.subethamail.org closing connection.", commandDispatcher.executeCommand("QUIT", session));
    assertFalse(session.isActive());
  }

  public void testQuitCommandHelp() throws Exception {
    assertEquals("214-QUIT\n" +
        "214-    Exit the SMTP session.\n" +
        "214 End of QUIT info", commandDispatcher.getHelpMessage("QUIT").toOutputString());
  }

  protected void setUp() throws Exception {
    super.setUp();
    new QuitCommand(commandDispatcher);
  }
}
