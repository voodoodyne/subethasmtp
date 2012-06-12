package org.subethamail.smtp;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.subethamail.smtp.io.DotTerminatedInputStream;

public class DotTerminatedInputStreamTest
{
	@Test
	public void testEmpty() throws IOException
	{
		InputStream in = new ByteArrayInputStream(".\r\n".getBytes("US-ASCII"));
		DotTerminatedInputStream stream = new DotTerminatedInputStream(in);
		assertEquals(-1, stream.read());
	}

	@Test
	public void testPreserveLastCrLf() throws IOException
	{
		InputStream in = new ByteArrayInputStream("a\r\n.\r\n".getBytes("US-ASCII"));
		DotTerminatedInputStream stream = new DotTerminatedInputStream(in);
		assertEquals("a\r\n", readFull(stream));
	}

	@Test
	public void testDotDot() throws IOException
	{
		InputStream in = new ByteArrayInputStream("..\r\n.\r\n".getBytes("US-ASCII"));
		DotTerminatedInputStream stream = new DotTerminatedInputStream(in);
		assertEquals("..\r\n", readFull(stream));
	}

	@Test(expected = EOFException.class)
	public void testMissingDotLine() throws IOException
	{
		InputStream in = new ByteArrayInputStream("a\r\n".getBytes("US-ASCII"));
		DotTerminatedInputStream stream = new DotTerminatedInputStream(in);
		readFull(stream);
	}

	private String readFull(DotTerminatedInputStream in) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int ch;
		while (-1 != (ch = in.read()))
			out.write(ch);
		return out.toString("US-ASCII");
	}
}
