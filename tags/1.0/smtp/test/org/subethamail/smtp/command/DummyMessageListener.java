package org.subethamail.smtp.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.subethamail.smtp.i.MessageListener;
import org.subethamail.smtp.i.TooMuchDataException;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class DummyMessageListener implements MessageListener {
  private String message;

  public boolean accept(String from, String recipient) {
    if (recipient.equals("validuser@subethamail.org")) return true;
    return false;
  }

  public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(data));
    StringBuilder buffer = new StringBuilder();
    String line;
    while ((line = in.readLine()) != null) {
      buffer.append(line);
      buffer.append("\n");
    }
    message = buffer.toString();
    if (message.length() > 1024)
    // TODO(imf): Add test for TooMuchDataException being thrown.
      throw new TooMuchDataException("Message greater than 1K characters (Hardcoded limit for testing)");
  }

//  public void deliver(String from, String recipient, byte[] data) {
//    this.message = new String(data);
//  }

  public String getMessage() {
    return message;
  }
}
