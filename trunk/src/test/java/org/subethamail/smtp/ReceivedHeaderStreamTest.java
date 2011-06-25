package org.subethamail.smtp;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.io.ReceivedHeaderStream;

/**
 * This class tests a bug in ReceivedHeaderStream which
 * has since been fixed.
 *
 * @see <a href="http://www.subethamail.org/se/archive_msg.jsp?msgId=59719">http://www.subethamail.org/se/archive_msg.jsp?msgId=59719</a>
 */
public class ReceivedHeaderStreamTest extends TestCase
{
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(ReceivedHeaderStreamTest.class);

	/** */
	public ReceivedHeaderStreamTest(String name)
	{
		super(name);
	}

	/** */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/** */
	public void testReceivedHeader() throws Exception
	{
		int BUF_SIZE = 10000;
		int offset = 10;
		ByteArrayInputStream in = new ByteArrayInputStream("hello world".getBytes());
		ReceivedHeaderStream hdrIS = new ReceivedHeaderStream(in, "ehlo", InetAddress.getLocalHost(), "foo", null, null);
		byte[] buf = new byte[BUF_SIZE];
		int len = hdrIS.read(buf, offset, BUF_SIZE-offset);

		String result = new String(buf, offset, len);

		assertTrue(result.endsWith("\nhello world"));
	}
}