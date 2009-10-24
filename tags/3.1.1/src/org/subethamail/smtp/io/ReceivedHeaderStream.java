/*
 *
 */
package org.subethamail.smtp.io;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Prepends a Received: header at the beginning of the input stream.
 */
public class ReceivedHeaderStream extends FilterInputStream
{
	ByteArrayInputStream header;

	/**
	 */
	public ReceivedHeaderStream(InputStream in, String heloHost, InetAddress host, String whoami)
	{
		super(in);

/* Looks like:
Received: from iamhelo (wasabi.infohazard.org [209.237.247.14])
        by mx.google.com with SMTP id 32si2669129wfa.13.2009.05.27.18.27.31;
        Wed, 27 May 2009 18:27:48 -0700 (PDT)
 */
		DateFormat fmt = new SimpleDateFormat("EEE, dd MM yyyy HH:mm:ss Z (z)");
		String timestamp = fmt.format(new Date());

		String header =
			"Received: from " + heloHost + " (" + host.getCanonicalHostName() + " [" + host + "])\r\n" +
			"        by " + whoami + " with SMTP;\r\n" +
			"        " + timestamp + "\r\n";

		this.header = new ByteArrayInputStream(header.getBytes());
	}

	/* */
	@Override
	public int available() throws IOException
	{
		return this.header.available() + super.available();
	}

	/* */
	@Override
	public void close() throws IOException
	{
		super.close();
	}

	/* */
	@Override
	public synchronized void mark(int readlimit)
	{
		throw new UnsupportedOperationException();
	}

	/* */
	@Override
	public boolean markSupported()
	{
		return false;
	}

	/* */
	@Override
	public int read() throws IOException
	{
		if (this.header.available() > 0)
			return this.header.read();
		else
			return super.read();
	}

	/* */
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		if (this.header.available() > 0)
		{
			int countRead = this.header.read(b, off, len);
			if (countRead < len)
			{
				// We need to add a little extra from the normal stream
				int remainder = len - countRead;
				int additionalRead = super.read(b, countRead, remainder);

				return countRead + additionalRead;
			}
			else
				return countRead;
		}
		else
			return super.read(b, off, len);
	}

	/* */
	@Override
	public int read(byte[] b) throws IOException
	{
		return this.read(b, 0, b.length);
	}

	/* */
	@Override
	public synchronized void reset() throws IOException
	{
		throw new UnsupportedOperationException();
	}

	/* */
	@Override
	public long skip(long n) throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
