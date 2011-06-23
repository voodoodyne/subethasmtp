package org.subethamail.smtp.test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.subethamail.smtp.io.DotTerminatedOutputStream;

public class DotTerminatedOutputStreamTest
{
	@Test
	public void testEmpty() throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DotTerminatedOutputStream stream = new DotTerminatedOutputStream(out);
		stream.writeTerminatingSequence();
		assertArrayEquals(".\r\n".getBytes("US-ASCII"), out.toByteArray());
	}

	@Test
	public void testMissingCrLf() throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DotTerminatedOutputStream stream = new DotTerminatedOutputStream(out);
		stream.write('a');
		stream.writeTerminatingSequence();
		assertArrayEquals("a\r\n.\r\n".getBytes("US-ASCII"), out.toByteArray());
	}

	@Test
	public void testMissingCrLfByteArray() throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DotTerminatedOutputStream stream = new DotTerminatedOutputStream(out);
		stream.write(new byte[]{
			'a'
		});
		stream.writeTerminatingSequence();
		assertArrayEquals("a\r\n.\r\n".getBytes("US-ASCII"), out.toByteArray());
	}

	@Test
	public void testExistingCrLf() throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DotTerminatedOutputStream stream = new DotTerminatedOutputStream(out);
		stream.write('a');
		stream.write('\r');
		stream.write('\n');
		stream.writeTerminatingSequence();
		assertArrayEquals("a\r\n.\r\n".getBytes("US-ASCII"), out.toByteArray());
	}

	@Test
	public void testExistingCrLfByteArray() throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DotTerminatedOutputStream stream = new DotTerminatedOutputStream(out);
		stream.write(new byte[]{
				'a', '\r', '\n'
		});
		stream.writeTerminatingSequence();
		assertArrayEquals("a\r\n.\r\n".getBytes("US-ASCII"), out.toByteArray());
	}
}
