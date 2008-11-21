/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.subethamail.smtp.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import javax.mail.util.SharedByteArrayInputStream;

import org.apache.mina.common.BufferDataException;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.io.SharedTmpFileInputStream;

/**
 * A {@link ProtocolDecoder} which decodes incoming SMTP data based on session context.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SMTPCodecDecoder implements ProtocolDecoder 
{
	private final static Logger log = LoggerFactory.getLogger(SMTPCodecDecoder.class);

    private final static String CONTEXT = SMTPCodecDecoder.class.getName()+ ".context";

	private final static String TMPFILE_PREFIX = "subetha";
	private final static String TMPFILE_SUFFIX = ".eml";

    private final static byte[] SMTP_CMD_DELIMITER = new byte[] {'\r','\n'};
    private final static byte[] SMTP_DATA_DELIMITER = new byte[] {'\r','\n', '.', '\r','\n'};

    private final Charset charset;

    /**
     * <a href="http://rfc.net/rfc2822.html#s2.1.1.">RFC 2822</a>
     */
    private int maxLineLength = 998;
    
    /** When to trigger */
    private int threshold;    

    /**
	 * Creates a new instance with the specified <tt>charset</tt> and the
	 * specified <tt>thresholdBytes</tt> deferring size.
	 */
    public SMTPCodecDecoder(Charset charset, int thresholdBytes) 
    {
        if (charset == null) 
        {
            throw new NullPointerException("charset");
        }

        this.charset = charset;
        this.threshold = thresholdBytes;       
    }

    /** */
	public void setDataDeferredSize(int dataDeferredSize) 
	{
		this.threshold = dataDeferredSize;
	}
	
	/** */
    public static byte[] asArray(ByteBuffer b) 
    {
    	int l = b.remaining();
    	byte[] array = new byte[l];
    	b.get(array, 0, l);
	    
	    return array;
    }
    
    /**
	 * Returns the allowed maximum size of the line to be decoded. If the size
	 * of the line to be decoded exceeds this value, the decoder will throw a
	 * {@link BufferDataException}. The default value is <tt>1024</tt> (1KB).
	 */
    public int getMaxLineLength() 
    {
        return maxLineLength;
    }

    /**
	 * Sets the allowed maximum size of the line to be decoded. If the size of
	 * the line to be decoded exceeds this value, the decoder will throw a
	 * {@link BufferDataException}. The default value is <tt>1024</tt> (1KB).
	 */
    public void setMaxLineLength(int maxLineLength) 
    {
        if (maxLineLength <= 0) 
        {
            throw new IllegalArgumentException("maxLineLength: "+ maxLineLength);
        }

        this.maxLineLength = maxLineLength;
    }

    /** */
    private DecoderContext getContext(IoSession session) 
    {
        DecoderContext ctx = (DecoderContext) session.getAttribute(CONTEXT);
        if (ctx == null) 
        {
            ctx = new DecoderContext();
            session.setAttribute(CONTEXT, ctx);
        }
        return ctx;
    }

    /** */
    public void finishDecode(IoSession session, ProtocolDecoderOutput out)
            throws Exception 
    {
    }

    /** */
    public void dispose(IoSession session) 
    	throws Exception 
    {
        DecoderContext ctx = (DecoderContext) session.getAttribute(CONTEXT);
        if (ctx != null) 
        {
            ctx.getBuffer().release();
            ctx.closeOutputStream();
            session.removeAttribute(CONTEXT);
        }
    }

    /** */
    public void decode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out)
            throws Exception 
    {
    	DecoderContext ctx = getContext(session);
        int matchCount = ctx.getMatchCount();
        
        ConnectionHandler.Context minaCtx = (ConnectionHandler.Context) 
        	session.getAttribute(ConnectionHandler.CONTEXT_ATTRIBUTE);

        boolean dataMode = minaCtx.getSession().isDataMode();
        byte[] delimBuf;
        
		if (dataMode) 
		{
			delimBuf = SMTP_DATA_DELIMITER;
		}
		else 
		{
            delimBuf = SMTP_CMD_DELIMITER;
        }

        // Try to find a match
        int oldPos = in.position();
        int oldLimit = in.limit();
        
        if (matchCount == delimBuf.length)
        	matchCount = 0;
        
        while (in.remaining() > 0) 
        {
            byte b = in.get();
            if (delimBuf[matchCount] == b) 
            {
                matchCount++;
                if (matchCount == delimBuf.length) 
                {
                    // Found a match.
                    int pos = in.position();
                    in.limit(pos);
                    in.position(oldPos);

                   	ctx.write(dataMode, in);
                    
                    in.limit(oldLimit);
                    in.position(pos);

                    if (ctx.getOverflowPosition() == 0) 
                    {
                    	ByteBuffer buf = ctx.getBuffer();
                		buf.flip();
                		
                		if (!dataMode)
                		{
                			buf.limit(buf.limit() - matchCount);
                		}
                		
                        try 
                        {
                        	if (dataMode)
                        	{
                        		delimBuf = SMTP_CMD_DELIMITER;
                        		out.write(ctx.getInputStream());                        		
                        		ctx.reset();
                        	}
                        	else
                        	{
                        		out.write(buf.getString(ctx.getDecoder()));
                        	}
                        }
                        catch (IOException ioex) 
                        {
                        	throw new CharacterCodingException();
                        } 
                        finally 
                        {                       	
                            buf.clear();
                        }
                    } 
                    else 
                    {
                        int overflowPosition = ctx.getOverflowPosition();
                        ctx.reset();
                        throw new BufferDataException("Line is too long: " + overflowPosition);
                    }

                    oldPos = pos;
                    matchCount = 0;
                }
            } 
            else 
            {
				// fix for DIRMINA-506
				in.position(Math.max(0, in.position() - matchCount));
                matchCount = 0;
            }
        }

        // Put remainder to buf.
        in.position(oldPos);
        ctx.write(dataMode, in);

        ctx.setMatchCount(matchCount);
    }

    private class DecoderContext 
    {
        private final CharsetDecoder decoder;
        private ByteBuffer buf;
        private int matchCount = 0;
        private int overflowPosition = 0;
    	private boolean thresholdReached = false;        

    	/** If we switch to file output, this is the file. */
    	File outFile;

    	/** If we switch to file output, this is the stream to write to the file. */ 
    	FileOutputStream stream;
    	
        private DecoderContext() 
        {
            decoder = charset.newDecoder();
            buf = ByteBuffer.allocate(80).setAutoExpand(true);
        }

        /** */
        public CharsetDecoder getDecoder() 
        {
            return decoder;
        }

        /** */
        public ByteBuffer getBuffer() 
        {
            return buf;
        }
        
        /** */
        private void compactBuffer() 
        {
        	buf.clear();
        	if (buf.capacity() > getMaxLineLength()) 
        	{
        		buf.release();
        		buf = ByteBuffer.allocate(80).setAutoExpand(true);
        	}        	
        }
        
        /** */
        public int getOverflowPosition() 
        {
            return overflowPosition;
        }
        
        /** */
        public int getMatchCount() 
        {
            return matchCount;
        }

        /** */
        public void setMatchCount(int matchCount) 
        {
            this.matchCount = matchCount;
        }
        
        /** */
        protected void reset() 
        {
            overflowPosition = 0;
            matchCount = 0;
            decoder.reset();
            thresholdReached = false;
            compactBuffer();
        }
        
        /** */
        public void write(boolean dataMode, ByteBuffer b) 
        	throws IOException
        {
    		if (dataMode)
    		{
    			write(asArray(b));
    		}
    		else
    		{
    			append(b);
    		}
        }
        
        /** */
    	private void write(byte[] src) 
    		throws IOException
    	{
    		int predicted = this.thresholdReached ? 0 : this.buf.position() + src.length;
    		
    		// Checks whether reading count bytes would cross the limit.
    		if (this.thresholdReached || predicted > threshold)
    		{
    			// If previously hit, then use the stream.
    			if (!this.thresholdReached)
    			{
    				thresholdReached(this.buf.position(), predicted);
    				this.thresholdReached = true;
    				compactBuffer();
    			}
    			
				this.stream.write(src);
    		}
    		else
    		{
    			this.buf.put(src);
    		}
    	}
    	
    	/**
		 * Called when the threshold is about to be exceeded. Once called, it
		 * won't be called again.
		 * 
		 * @param current
		 *            is the current number of bytes that have been written
		 * @param predicted
		 *            is the total number after the write completes
		 */
    	private void thresholdReached(int current, int predicted) 
    		throws IOException
    	{
    		this.outFile = File.createTempFile(TMPFILE_PREFIX, TMPFILE_SUFFIX);
    		if (log.isDebugEnabled())
    		{
    			log.debug("Writing message to file : " + outFile.getAbsolutePath());
    		}
    		
    		this.stream = new FileOutputStream(this.outFile);
    		this.buf.flip();
    		this.stream.write(asArray(this.buf));
    		log.debug("ByteBuffer written to stream");
    	}
    	
    	/** */
    	protected void closeOutputStream() throws IOException
    	{
    		if (this.stream != null)
    		{
    			this.stream.flush();
    			this.stream.close();
    			log.debug("Temp file writing achieved");
    		}
    	}
    	
    	/** */
    	protected InputStream getInputStream() throws IOException
    	{		
    		if (this.thresholdReached)
    		{
    			return new SharedTmpFileInputStream(this.outFile);
    		}
    		else
    		{
    			return new SharedByteArrayInputStream(asArray(this.buf));
    		}
    	}
    	
    	/** */
        private void append(ByteBuffer in) throws CharacterCodingException 
        {
            if (overflowPosition != 0) 
            {
                discard(in);
            }
            else 
            {
            	int pos = buf.position();
            	if ( pos > maxLineLength - in.remaining()) 
            	{
                    overflowPosition = pos;
                    buf.clear();
                    discard(in);
            	} 
            	else 
            	{
            		this.buf.put(in);
            	}
            }
        }

        /** */
        private void discard(ByteBuffer in) 
        {
            if (Integer.MAX_VALUE - in.remaining() < overflowPosition) 
            {
                overflowPosition = Integer.MAX_VALUE;
            } 
            else 
            {
                overflowPosition += in.remaining();
            }
            in.position(in.limit());
        }
    }
}