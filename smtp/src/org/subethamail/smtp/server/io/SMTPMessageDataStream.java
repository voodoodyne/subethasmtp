package org.subethamail.smtp.server.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.util.SharedByteArrayInputStream;

import org.apache.mina.common.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides an {@link InputStream} backed by a {@link ByteBuffer} 
 * till the threshold is reached. It will then create a temporary file and store the 
 * previously buffered and incoming data into it to prevent OOM exceptions.
 *  
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SMTPMessageDataStream
{
	private final static Logger log = LoggerFactory.getLogger(SMTPMessageDataStream.class);
	
	private final static String TMPFILE_PREFIX = "subetha";
	private final static String TMPFILE_SUFFIX = ".eml";
	
	/**
	 * Initial size of the byte array buffer.  Better to make this
	 * large to start with so that we can avoid reallocs; mail
	 * messages are rarely tiny.
	 */
	private final static int BUF_SIZE = 8192;
	
	/** The memory buffer */
	ByteBuffer buffer;
	
	/** If we switch to file output, this is the file. */
	File outFile;

	/** If we switch to file output, this is the channel used to write to the file. */ 
	FileOutputStream stream;
	
	/** When to trigger */
	int threshold;
	
	boolean thresholdReached = false;
	
	/** When the output stream is closed, this becomes true */
	boolean closed;

	/**
	 * @param thresholdBytes The server will buffer
	 *        incoming messages to disk when they hit this limit in the
	 *        DATA received.
	 */
	public SMTPMessageDataStream(int thresholdBytes)
	{
		super();
		this.buffer = ByteBuffer.allocate(BUF_SIZE);
		this.buffer.setAutoExpand(true);
		this.threshold = thresholdBytes;
	}
	
	public void write(byte[] src) throws IOException
	{
		int predicted = this.buffer.position() + src.length;
		
		// Checks whether reading count bytes would cross the limit.
		if (predicted > this.threshold)
		{
			// If previously hit, then use the stream.
			if (this.thresholdReached)
			{
				this.stream.write(this.buffer.array());
			}
			else
			{
				thresholdReached(this.buffer.position(), predicted);
				this.thresholdReached = true;
			}
			
			this.buffer.clear();
		}
		
		this.buffer.put(src);
	}
	
	/**
	 * @return the current threshold value.
	 */
	public int getThreshold()
	{
		return this.threshold;
	}	
	
	/**
	 * Called when the threshold is about to be exceeded. Once called, 
	 * it won't be called again.
	 * 
	 * @param current is the current number of bytes that have been written
	 * @param predicted is the total number after the write completes
	 */
	private void thresholdReached(int current, int predicted) throws IOException
	{
		this.outFile = File.createTempFile(TMPFILE_PREFIX, TMPFILE_SUFFIX);
		log.debug("Writing message to file : "+outFile.getAbsoluteFile().getName());
		this.stream = new FileOutputStream(this.outFile);

		this.stream.write(this.buffer.array());
		log.debug("ByteBuffer written to stream");
		this.buffer.setAutoExpand(false);
	}
	
	public InputStream getInputStream() throws IOException
	{		
		flush();
		if (this.thresholdReached)
			return new SharedTmpFileInputStream(this.outFile);
		else
			return new SharedByteArrayInputStream(this.buffer.array());
	}

	public boolean isThresholdReached() 
	{
		return thresholdReached;
	}

	public void flush() throws IOException
	{
		if (this.thresholdReached)
		{
			this.stream.write(this.buffer.array());
			this.buffer.clear();
		}
	}
	
	public void close() throws IOException
	{
		if (this.closed)
			return;
		
		if (this.thresholdReached)
		{
			this.stream.write(this.buffer.array());
			this.stream.flush();
			this.stream.close();
			log.debug("Temp file writing achieved");
		}
		
		this.buffer.release();
		this.closed = true;
	}
}