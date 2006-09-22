package org.subethamail.smtp.test.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * A crude telnet client that can be used to send SMTP messages and test
 * the responses.
 * 
 * @author Jeff Schnitzer
 * @author Jon Stevens
 */
public class Client
{
	Socket socket;
	BufferedReader reader;
	PrintWriter writer;

	/**
	 * Establishes a connection to host and port.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public Client(String host, int port) throws UnknownHostException, IOException
	{
		socket = new Socket(host, port);
		writer = new PrintWriter(socket.getOutputStream(), true);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
    /**
	 * Sends a message to the server, ie "HELO foo.example.com". A newline will
	 * be appended to the message.
	 * 
	 * @throws an exception if the method cannot send for any reason
	 */
	public void send(String msg) throws Exception
	{
		writer.write(msg);
	}

	/**
	 * Throws an exception if the response does not start with
	 * the specified string.
	 */
	public void expect(String expect) throws Exception
	{
		assert(this.readResponse().startsWith(expect));
	}
	
	/**
	 * Get the complete response, including a multiline response.
	 * Newlines are included.
	 */
	protected String readResponse() throws Exception
	{
		StringBuilder builder = new StringBuilder();
		boolean done = false;
		while (!done)
		{
			String line = this.reader.readLine();
			if (line.charAt(3) != '-')
				done = true;
				
			builder.append(line);
			builder.append('\n');
		}
		
		return builder.toString();
	}

	/** */
	public void close() throws Exception
	{
		if (!socket.isClosed())
			socket.close();
	}
}
