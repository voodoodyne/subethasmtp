package org.subethamail.smtp.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.test.util.Client;
import org.subethamail.wiser.Wiser;

/**
 * This class tests if reset between two mails in the same session
 * is well done.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class ResetTest extends TestCase 
{
	private static final Logger log = LoggerFactory.getLogger(ResetTest.class);
	
	public void testReset() 
	{
		Wiser wiser = new Wiser() {
			int receivedMailsCount;
			
			@Override
			public void deliver(String from, String recipient, InputStream data)
					throws TooMuchDataException, IOException 
			{
				super.deliver(from, recipient, data);
				receivedMailsCount++;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] array = new byte[10000];
				int size = 0;
				for (int i = data.read(array); i != -1; i = data.read(array)) 
				{
					bos.write(array, 0, i);
					size +=i;
				}
				
				log.info("----------------------------------");
				log.info("From: " + from);
				log.info("To: " + recipient);
				log.info("Length of incoming mail: " + bos.size());
				log.info(new String(bos.toByteArray(), 0, 100));
				log.info("----------------------------------\n\n");
				
				if (receivedMailsCount == 1)
				{
					assertEquals("test1From@example.com", from);
					assertTrue(size > 500);
				}
				else
				{
					assertEquals("test2From@example.com", from);
					assertTrue(size < 500);
				}
				
			}
		};
		wiser.start();
		wiser.setDataDeferredSize(2 * 1024);

		try 
		{
			Client c = new Client("localhost", 25);
			c.expect("220");
			c.send("HELO foo.com");
			c.expect("250");
			
			// send first mail that causes deferred data
			c.send("MAIL FROM:<test1From@example.com>");
			c.expect("250");
			c.send("RCPT TO:<test1To@example.com>");
			c.expect("250");
			c.send("DATA");
			c.expect("354");
			c.send(generateMailContent("BIG MAIL", 5 * 1024, 'A'));
			c.expect("250");
			
			// send reset
			c.send("RSET");
			c.expect("250");
			
			// send second mail which is small and shouldn't be written to a temp file
			c.send("MAIL FROM:<test2From@example.com>");
			c.expect("250");
			c.send("RCPT TO:<test2To@example.com>");
			c.expect("250");
			c.send("DATA");
			c.expect("354");
			c.send(generateMailContent("SHORT MAIL", 100, 'B'));
			c.expect("250");
			c.send("QUIT");
			c.expect("221");
			
			c.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		wiser.stop();
	}

	public static String generateMailContent(String subject, int size, char c) 
		throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(("Subject: " + subject + "\r\n\r\n").getBytes());

		for (int i = 0; i < size; i++) 
		{
			if ((i % 70) == 69)
				bos.write("\r\n".getBytes());
			bos.write(c);
		}
		bos.write("\r\n.".getBytes());
		
		return new String(bos.toByteArray());
	}
}
