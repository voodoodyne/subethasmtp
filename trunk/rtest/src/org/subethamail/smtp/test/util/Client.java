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

	String expect;
	boolean startsWith;
	
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

		// initiate reading from server...
//		Thread t = new Thread(this);
//		t.start(); // will call run method of this class
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

//	public void run()
//	{
//		try
//		{
//			while (true)
//			{
//				String read = reader.readLine();
//				System.out.println("Read: " + read);
//				if (read.equals("221 Bye"))
//					break;
//				doExpect(read);
//			}
//		}
//		catch (IOException ioe)
//		{
//		}
//		catch (Exception e)
//		{
//			throw new RuntimeException(e);
//		}
//	}

	public void expect(String expect, boolean startsWith)
		throws Exception
	{
		System.out.println("Setting Expect: " + expect + " startsWith: " + startsWith);
		this.expect = expect;
		this.startsWith = startsWith;
		doExpect(reader.readLine());
//		boolean done = false;
//		while(!done)
//		{
//			done = true;
//		}
	}

	void doExpect(String got) throws Exception
	{
		System.out.println("doExpect: " + got);
		if (startsWith)
		{
			if (!got.startsWith(expect))
				throw new Exception("Starts with: " + expect + " Got: " + got);
		}
		else
		{
			if (!got.equals(expect))
				throw new Exception("Expected: " + expect + " Got: " + got);
		}
		expect = null;
		startsWith = false;
	}

	public void close() throws Exception
	{
		if (!socket.isClosed())
			socket.close();
	}
}
