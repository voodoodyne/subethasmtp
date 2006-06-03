package org.subethamail.smtp.command;


/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class NoopCommandTest extends CommandTestCase {

  public void testNoopCommand() throws Exception {
    new NoopCommand(commandDispatcher);
    assertEquals(OK, commandDispatcher.executeCommand("NOOP", session));
  }

  public void testNoopCommandHelp() throws Exception {
    new NoopCommand(commandDispatcher);
    assertEquals(HelpMessageTest.NOOP_HELP_OUTPUT, commandDispatcher.getHelpMessage("NOOP").toOutputString());
  }


}
