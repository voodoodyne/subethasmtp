package org.subethamail.smtp.server;

import junit.framework.TestCase;

import org.subethamail.testing.HasExternalDependency;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class ServerTest extends TestCase {
  private SMTPServer server;

  @HasExternalDependency
  public void testResolveHost() throws Exception {
    assertEquals("copper.neo.com/64.127.105.2", server.resolveHost("copper.neo.com"));
  }

  @HasExternalDependency
  public void testDisableHostResolution() throws Exception {
    server.disableHostResolution();
    assertEquals("copper.neo.com", server.resolveHost("copper.neo.com"));
    server.enableHostResolution();
    assertEquals("copper.neo.com/64.127.105.2", server.resolveHost("copper.neo.com"));
  }

  protected void setUp() throws Exception {
  super.setUp();
    server = new SMTPServer("localhost", 2567);
  }
}
