package org.subethamail.smtp.command;

import junit.framework.TestCase;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class HelpMessageTest extends TestCase {
  protected static final String NOOP_HELP_OUTPUT =
      "214-NOOP\n" +
      "214-    Do nothing.\n" +
      "214 End of NOOP info";
  protected static final String HELP_HELP_OUTPUT =
      "214-HELP [ <topic> ]\n" +
      "214-    The HELP command gives help info about the topic specified.\n" +
      "214-    For a list of topics, type HELP by itself.\n" +
      "214 End of HELP info";

  public void testHelpMessage() throws Exception {
    HelpMessage helpMessage = new HelpMessage("NOOP", "Do nothing.");
    assertEquals(NOOP_HELP_OUTPUT, helpMessage.toOutputString());
  }

  /*
214-HELP [ <topic> ]
214-    The HELP command gives help info.
214 End of HELP info
  */
}
