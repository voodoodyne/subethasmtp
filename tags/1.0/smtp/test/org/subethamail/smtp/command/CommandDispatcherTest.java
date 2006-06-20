package org.subethamail.smtp.command;

import junit.framework.Assert;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class CommandDispatcherTest extends CommandTestCase {

  public void testUnrecognizedCommand() throws Exception {
    Assert.assertEquals("500 Command unrecognized: \"foop\"", commandDispatcher.executeCommand("foop", session));
    Assert.assertEquals("500 Command unrecognized: \"\"", commandDispatcher.executeCommand("", session));
    Assert.assertEquals("500 Command unrecognized: \"null\"", commandDispatcher.executeCommand(null, session));
  }

  public void testHelp() throws Exception {
    new NoopCommand(commandDispatcher);
    new HelpCommand(commandDispatcher);
    Assert.assertEquals(HelpMessageTest.NOOP_HELP_OUTPUT, commandDispatcher.executeCommand("HELP NOOP", session));
  }

  public void testGetCommandList() throws Exception {
    Assert.assertEquals(0, commandDispatcher.getCommandList().size());
    new NoopCommand(commandDispatcher);
    Assert.assertEquals(1, commandDispatcher.getCommandList().size());
    new HelpCommand(commandDispatcher);
    Assert.assertEquals(2, commandDispatcher.getCommandList().size());
  }

  public void testCommandDispatcher() throws Exception {
    new HelloCommand(commandDispatcher);
//    new ExtendedHelloCommand(commandDispatcher);
    new MailCommand(commandDispatcher);
    new ReceiptCommand(commandDispatcher);
    new DataCommand(commandDispatcher);
    new ResetCommand(commandDispatcher);
    new NoopCommand(commandDispatcher);
    new QuitCommand(commandDispatcher);
    new HelpCommand(commandDispatcher);
    new VerifyCommand(commandDispatcher);
    new ExpnCommand(commandDispatcher);
    new VerboseCommand(commandDispatcher);
//--    new EtrnCommand(commandDispatcher);
//    new DsnCommand(commandDispatcher);

  }


}
