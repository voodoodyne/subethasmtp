package org.subethamail.smtp.command;


/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class HelloCommandTest extends CommandTestCase {
  public void testHelloCommand() throws Exception {
    assertEquals("501 Syntax: HELO <hostname>",
      commandDispatcher.executeCommand("HELO", session));
    assertEquals("250 test.subethamail.org Hello remotehost.example.com/192.0.2.1",
      commandDispatcher.executeCommand("HELO remotehost.example.com", session));
    assertEquals("503 remotehost.example.com Duplicate HELO/EHLO",
      commandDispatcher.executeCommand("HELO remotehost.example.com", session));
  }

  public void testBlackholedHelo() throws Exception {
    assertTrue(session.isActive());
    assertEquals("221 test.subethamail.org closing connection. Traffic from your server denied access.",
        commandDispatcher.executeCommand("HELO spambox.blackhat.org", session));
    // TODO(imf): Fix termination behavior to conform to RFC 2821
    assertFalse(session.isActive());
  }

  // TODO(imf): Add test to assert that remote host is same as declared remote host.

  public void testHelloCommandHelp() throws Exception {
    new VerifyCommand(commandDispatcher);
    assertEquals("214-HELO <hostname>\n" +
        "214-    Introduce yourself.\n" +
        "214 End of HELO info", commandDispatcher.getHelpMessage("HELO").toOutputString());
  }

  protected void setUp() throws Exception {
    super.setUp();
    new HelloCommand(commandDispatcher);
  }
}
