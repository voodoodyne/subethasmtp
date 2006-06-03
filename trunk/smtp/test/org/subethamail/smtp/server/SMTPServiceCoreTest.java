package org.subethamail.smtp.server;

import junit.framework.TestCase;

import org.subethamail.smtp.command.DummySMTPServerContext;

/**
 * Created by IntelliJ IDEA.
 * User: imf
 * Date: Apr 23, 2006
 * Time: 3:56:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class SMTPServiceCoreTest extends TestCase {
  public void testSMTPServiceCore() throws Exception {
    SMTPServerContext serverContext = new DummySMTPServerContext("1.0", "test.subethamail.org", 11111);
    SMTPServiceCore serviceCore = new SMTPServiceCore(serverContext);
    serviceCore.start();
    serviceCore.stop();
  }

}
