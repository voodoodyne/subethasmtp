package org.subethamail.smtp;

import static org.junit.Assert.fail;

import java.net.SocketException;

import org.junit.Test;
import org.subethamail.smtp.client.SMTPClient;
import org.subethamail.wiser.Wiser;

/**
 * This class tests connection timeouts.
 * 
 * @author Jeff Schnitzer
 */
public class TimeoutTest {
	/** */
	public static final int PORT = 2566;

	/** */
	@Test
	public void testTimeout() throws Exception {
		Wiser wiser = new Wiser();
		wiser.setPort(PORT);
		wiser.getServer().setConnectionTimeout(1000);
		wiser.start();

		SMTPClient client = new SMTPClient("localhost", PORT);
		client.sendReceive("HELO foo");
		Thread.sleep(2000);
		try {
			client.sendReceive("HELO bar");
			fail();
		} catch (SocketException e) {
			// expected
		} finally {
			wiser.stop();
		}
	}

}
