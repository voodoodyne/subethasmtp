package org.subethamail.smtp.command;


/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class ExpnCommandTest extends CommandTestCase {
  public void testExpnCommand() throws Exception {
    new ExpnCommand(commandDispatcher);
    assertEquals("502 Sorry, we do not allow this operation.", commandDispatcher.executeCommand("EXPN", session));
  }

  public void testNoopCommandHelp() throws Exception {
    new ExpnCommand(commandDispatcher);
    assertEquals("214-EXPN <recipient>\n" +
        "214-    Expand an address. If the address is a mailing list, return\n" +
        "214-    the contents of the list.\n" +
        "214-    This command is often disabled for security reasons.\n" +
        "214 End of EXPN info", commandDispatcher.getHelpMessage("EXPN").toOutputString());
  }

}
