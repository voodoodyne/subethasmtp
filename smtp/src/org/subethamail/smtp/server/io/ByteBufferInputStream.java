package org.subethamail.smtp.server.io;

import java.io.IOException;
import java.io.InputStream;

import org.apache.mina.common.ByteBuffer;

/**
 * Wrapper around InputStream to convert data into a ByteBuffer. 
 */
public class ByteBufferInputStream extends InputStream
{
	private final Object mutex = new Object();
	private final ByteBuffer buf;
	private volatile boolean closed;
	private volatile boolean released;
	private IOException exception;

	public ByteBufferInputStream()
	{
		buf = ByteBuffer.allocate(16);
		buf.setAutoExpand(true);
		buf.limit(0);
	}

	public int available()
	{
		if (released)
		{
			return 0;
		}
		else
		{
			synchronized (mutex)
			{
				return buf.remaining();
			}
		}
	}

	public void close()
	{
		if (closed)
		{
			return;
		}

		synchronized (mutex)
		{
			closed = true;
			releaseBuffer();

			mutex.notifyAll();
		}
	}

	public int read() throws IOException
	{
		synchronized (mutex)
		{
			if (!waitForData())
			{
				return -1;
			}

			return buf.get() & 0xff;
		}
	}

	public int read(byte[] b, int off, int len) throws IOException
	{
		synchronized (mutex)
		{
			if (!waitForData())
			{
				return -1;
			}

			int readBytes;

			if (len > buf.remaining())
			{
				readBytes = buf.remaining();
			}
			else
			{
				readBytes = len;
			}

			buf.get(b, off, readBytes);

			return readBytes;
		}
	}

	private boolean waitForData() throws IOException
	{
		if (released)
		{
			return false;
		}

		synchronized (mutex)
		{
			while (!released && buf.remaining() == 0 && exception == null)
			{
				try
				{
					mutex.wait();
				}
				catch (InterruptedException e)
				{
					IOException ioe = new IOException(
							"Interrupted while waiting for more data");
					ioe.initCause(e);
					throw ioe;
				}
			}
		}

		if (exception != null)
		{
			releaseBuffer();
			throw exception;
		}

		if (closed && buf.remaining() == 0)
		{
			releaseBuffer();

			return false;
		}

		return true;
	}

	private void releaseBuffer()
	{
		if (released)
		{
			return;
		}

		released = true;
		buf.release();
	}

	public void write(byte[] src)
	{
		synchronized (mutex)
		{
			if (closed)
			{
				return;
			}

			if (buf.hasRemaining())
			{
				this.buf.compact();
				this.buf.put(src);
				this.buf.flip();
			}
			else
			{
				this.buf.clear();
				this.buf.put(src);
				this.buf.flip();
				mutex.notifyAll();
			}
		}
	}

	public void throwException(IOException e)
	{
		synchronized (mutex)
		{
			if (exception == null)
			{
				exception = e;

				mutex.notifyAll();
			}
		}
	}
}
