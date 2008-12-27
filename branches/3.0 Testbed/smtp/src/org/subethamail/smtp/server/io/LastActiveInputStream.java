package org.subethamail.smtp.server.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is an input stream filter that tracks whether or not it
 * is waiting for data and how long.  Using this, the SMTP server
 * watchdog thread can shutdown connections which are stale.
 * 
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
public class LastActiveInputStream extends FilterInputStream
{
	/** This tracks the time we have spent waiting.  If set to 0, it means we are not waiting. */
	private long waitStartTime = 0;
	
	/** */
    public LastActiveInputStream(InputStream in)
    {
        super(in);
    }

	/** */
	@Override
	public int read() throws IOException
	{
		int data;
		
		try
		{
			synchronized(this) { this.waitStartTime = System.currentTimeMillis(); }
			data = super.read();
		}
		finally
		{
			synchronized(this) { this.waitStartTime = 0; }
		}
		
		return data;
	}

	/** */
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int data;
		
		try
		{
			synchronized(this) { this.waitStartTime = System.currentTimeMillis(); }
			data = super.read(b, off, len);
		}
		finally
		{
			synchronized(this) { this.waitStartTime = 0; }
		}
		
		return data;
	}

	/** */
	@Override
	public int read(byte[] b) throws IOException
	{
		int data;
		
		try
		{
			synchronized(this) { this.waitStartTime = System.currentTimeMillis(); }
			data = super.read(b);
		}
		finally
		{
			synchronized(this) { this.waitStartTime = 0; }
		}
		
		return data;
	}
	
	/**
	 * @return true if the input stream is waiting for client data longer than the 
	 *  specified number of milliseconds.
	 */
	public synchronized boolean isWaitingMoreThan(long millis)
	{
		if (waitStartTime == 0)
			return false;
		else
			return (System.currentTimeMillis() - waitStartTime) > millis; 
	}
}
