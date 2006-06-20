package org.subethamail.smtp.command;


/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class ResetCommandTest extends CommandTestCase {
  public void testRsetCommand() throws Exception {
    new ResetCommand(commandDispatcher);
    session.setVerbose(true);
    session.setSender("sender@example.com");
    session.setEsmtp(true);
    commandDispatcher.executeCommand("RSET", session);
    assertFalse(session.isVerbose());
    assertFalse(session.isEsmtp());
    assertNull(session.getSender());
  }

  public void testRsetCommandHelp() throws Exception {
    new ResetCommand(commandDispatcher);
    assertEquals("214-RSET\n" +
        "214-    Resets the system.\n" +
        "214 End of RSET info", commandDispatcher.getHelpMessage("RSET").toOutputString());
  }

}
