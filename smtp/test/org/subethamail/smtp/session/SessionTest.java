package org.subethamail.smtp.session;

import junit.framework.TestCase;

import org.subethamail.smtp.command.DummySMTPServerContext;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class SessionTest extends TestCase {
  private Session session;

  public void testSessionReset() throws Exception {
    assertFalse(session.isVerbose());
    session.setVerbose(true);
    assertTrue(session.isVerbose());
    session.reset();
    assertFalse(session.isVerbose());
    assertEquals("foo.example.com", session.getRemoteHostname());
  }

  public void testMessageAccumulation() throws Exception {
    session.addData("A String");
    session.addData("Another String");
    assertEquals("A String\n" +
        "Another String\n", session.getMessage());
    session.reset();
    assertEquals("", session.getMessage());
  }

  protected void setUp() throws Exception {
  super.setUp();
    session = new Session(new DummySMTPServerContext(), "foo.example.com");
  }
}
