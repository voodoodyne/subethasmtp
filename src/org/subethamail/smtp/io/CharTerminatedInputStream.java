/***********************************************************************
 * Copyright (c) 1999-2006 The Apache Software Foundation.             *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/

package org.subethamail.smtp.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;

/**
 * An InputStream class that terminates the stream when it encounters a
 * particular byte sequence.
 *
 * @version 1.0.0, 24/04/1999
 */
public class CharTerminatedInputStream
    extends InputStream {

    /**
     * The wrapped input stream
     */
    private InputStream in;

    /**
     * The terminating character array
     */
    private int[] match;

    /**
     * An array containing the last N characters read from the stream, where
     * N is the length of the terminating character array
     */
    private int[] buffer;

    /**
     * The number of bytes that have been read that have not been placed
     * in the internal buffer.
     */
    private int pos = 0;

    /**
     * Whether the terminating sequence has been read from the stream
     */
    private boolean endFound = false;

    /**
     * A constructor for this object that takes a stream to be wrapped
     * and a terminating character sequence.
     *
     * @param in the <code>InputStream</code> to be wrapped
     * @param terminator the array of characters that will terminate the stream.
     *
     * @throws IllegalArgumentException if the terminator array is null or empty
     */
    public CharTerminatedInputStream(InputStream in, char[] terminator) {
        if (terminator == null) {
            throw new IllegalArgumentException("The terminating character array cannot be null.");
        }
        if (terminator.length == 0) {
            throw new IllegalArgumentException("The terminating character array cannot be of zero length.");
        }
        this.match = new int[terminator.length];
        this.buffer = new int[terminator.length];
        for (int i = 0; i < terminator.length; i++) {
            this.match[i] = terminator[i];
            this.buffer[i] = terminator[i];
        }
        this.in = in;
    }

    /**
     * Read a byte off this stream.
     *
     * @return the byte read off the stream
     * @throws IOException if an IOException is encountered while reading off the stream
     * @throws ProtocolException if the underlying stream returns -1 before the terminator is seen.
     */
    @Override
	public int read() throws IOException {
        if (this.endFound) {
            //We've found the match to the terminator
            return -1;
        }
        if (this.pos == 0) {
            //We have no data... read in a record
            int b = this.in.read();
            if (b == -1) {
                //End of stream reached without seeing the terminator
                throw new java.net.ProtocolException("pre-mature end of data");
            }
            if (b != this.match[0]) {
                //this char is not the first char of the match
                return b;
            }
            //this is a match...put this in the first byte of the buffer,
            // and fall through to matching logic
            this.buffer[0] = b;
            this.pos++;
        } else {
            if (this.buffer[0] != this.match[0]) {
                //Maybe from a previous scan, there is existing data,
                // and the first available char does not match the
                // beginning of the terminating string.
                return this.topChar();
            }
            //we have a match... fall through to matching logic.
        }
        //MATCHING LOGIC

        //The first character is a match... scan for complete match,
        // reading extra chars as needed, until complete match is found
        for (int i = 0; i < this.match.length; i++) {
            if (i >= this.pos) {
                int b = this.in.read();
                if (b == -1) {
                    //end of stream found, so match cannot be fulfilled.
                    // note we don't set endFound, because otherwise
                    // remaining part of buffer won't be returned.
                    return this.topChar();
                }
                //put the read char in the buffer
                this.buffer[this.pos] = b;
                this.pos++;
            }
            if (this.buffer[i] != this.match[i]) {
                //we did not find a match... return the top char
                return this.topChar();
            }
        }
        //A complete match was made...
        this.endFound = true;
        return -1;
    }

    /**
     * Private helper method to update the internal buffer of last read characters
     *
     * @return the byte that was previously at the front of the internal buffer
     */
    private int topChar() {
        int b = this.buffer[0];
        if (this.pos > 1) {
            //copy down the buffer to keep the fresh data at top
            System.arraycopy(this.buffer, 1, this.buffer, 0, this.pos - 1);
        }
        this.pos--;
        return b;
    }

    /**
     * Provide access to the base input stream.
     */
    public InputStream getBaseStream() {
    	return this.in;
    }
}