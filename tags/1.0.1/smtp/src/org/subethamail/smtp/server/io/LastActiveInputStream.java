package org.subethamail.smtp.server.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.subethamail.smtp.server.ConnectionHandler;

/**
 * This is an input stream filter that updates the last active time
 * of the ConnectionHandler so that someone can't DoS the server
 * after issuing a DATA command. The SMTPServer watchdog thread
 * is responsible for checking the last active time and will
 * shut down any connections where no data has been read for at
 * least 1 minute.
 * 
 * @author Jon Stevens
 */
public class LastActiveInputStream extends FilterInputStream
{
	private ConnectionHandler connectionHandler;
	
    public LastActiveInputStream(InputStream in, ConnectionHandler connectionHandler)
    {
        super(in);
        this.connectionHandler = connectionHandler;
    }

	@Override
	public int read() throws IOException
	{
		int read = super.read();
		this.connectionHandler.refreshLastActiveTime();
		return read;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int read = super.read(b, off, len);
		this.connectionHandler.refreshLastActiveTime();
		return read;
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		int read = super.read(b);
		this.connectionHandler.refreshLastActiveTime();
		return read;
	}
}
