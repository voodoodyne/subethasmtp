package org.subethamail.smtp.command;


/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class VerboseCommandTest extends CommandTestCase {
  public void testVerifyCommand() throws Exception {
    new VerboseCommand(commandDispatcher);
    session.setVerbose(false);
    assertEquals("250 Verbose mode", commandDispatcher.executeCommand("VERB", session));
    assertTrue(session.isVerbose());
  }

  public void testVerifyCommandHelp() throws Exception {
    new VerboseCommand(commandDispatcher);
    assertEquals("214-VERB\n" +
        "214-    Go into verbose mode. This sends Oxy responses that are\n" +
        "214-    not RFC821 standard (but should be). They are recognized\n" +
        "214-    by humans and other SMTP implementations.\n" +
        "214 End of VERB info", commandDispatcher.getHelpMessage("VERB").toOutputString());
  }


}
