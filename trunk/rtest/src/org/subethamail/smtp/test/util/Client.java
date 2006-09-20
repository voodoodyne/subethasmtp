package org.subethamail.smtp.test.util;


/**
 * A crude telnet client that can be used to send SMTP messages and test
 * the responses.
 * 
 * TODO:  implement me!
 * 
 * @author Jeff Schnitzer
 */
public class Client
{
	/**
	 * Establishes a connection to host and port.
	 */
	public Client(String host, int port)
	{
		
	}
	
	/**
	 * Sends a message to the server, ie "HELO foo.example.com".  A newline
	 * will be appended to the message.
	 * 
	 * @throws an exception if the method cannot send for any reason
	 */
	public void send(String msg) throws Exception
	{
		
	}
	
	/**
	 * Gets a complete response from the server, expecting that the response
	 * will start with the given string (ie "553").  An exception will be
	 * thrown if the read times out or if the response does not begin with
	 * the expected value.
	 */
	public void expect(String start) throws Exception
	{
		
	}
}
