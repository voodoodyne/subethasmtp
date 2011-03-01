package org.subethamail.smtp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.io.DotTerminatedOutputStream;
import org.subethamail.smtp.io.ExtraDotOutputStream;


/**
 * A very low level abstraction of the STMP stream which knows how to handle
 * the raw protocol for lines, whitespace, etc.
 *
 * @author Jeff Schnitzer
 */
public class SMTPClient
{
	/** 5 minutes */
	private static final int CONNECT_TIMEOUT = 300 * 1000;

	/** 10 minutes */
	private static final int REPLY_TIMEOUT = 600 * 1000;

	/** */
	private static Logger log = LoggerFactory.getLogger(SMTPClient.class);

	/** Just for display purposes */
	String hostPort;

	/** The raw socket */
	Socket socket;

	/** */
	BufferedReader reader;

	/** Output streams used for data */
	OutputStream rawOutput;
	/**
	 * A stream which wraps {@link #rawOutput} and is used to write out the DOT
	 * CR LF terminating sequence in the DATA command, if necessary
	 * complementing the message content with a closing CR LF.
	 */
	DotTerminatedOutputStream dotTerminatedOutput;
	/**
	 * This stream wraps {@link #dotTerminatedOutput} and it does the dot
	 * stuffing for the SMTP DATA command.
	 */
	ExtraDotOutputStream dataOutput;

	/** Note we bypass this during DATA */
	PrintWriter writer;

	/**
	 * Result of an SMTP exchange.
	 */
	public static class Response
	{
		int code;
		String message;

		public Response(int code, String text)
		{
			this.code = code;
			this.message = text;
		}

		public int getCode() { return this.code; }
		public String getMessage() { return this.message; }

		public boolean isSuccess()
		{
			return this.code >= 100 && this.code < 400;
		}

		@Override
		public String toString() { return this.code + " " + this.message; }
	}

	/**
	 * Establishes a connection to host and port and negotiate the initial EHLO
	 * exchange.
	 *
	 * @throws UnknownHostException if the hostname cannot be resolved
	 * @throws IOException if there is a problem connecting to the port
	 */
	public SMTPClient(String host, int port) throws UnknownHostException, IOException
	{
		this(host, port, null);
	}

	/**
	 * Establishes a connection to host and port from the specified local socket
	 * address and negotiate the initial EHLO exchange.
	 *
	 * @param bindpoint the local socket address. If null, the system will pick
	 *            up an ephemeral port and a valid local address.
	 *
	 * @throws UnknownHostException if the hostname cannot be resolved
	 * @throws IOException if there is a problem connecting to the port
	 */
	public SMTPClient(String host, int port, SocketAddress bindpoint) throws UnknownHostException, IOException
	{
		this.hostPort = host + ":" + port;

		if (log.isDebugEnabled())
			log.debug("Connecting to " + this.hostPort);

		this.socket = new Socket();
		this.socket.bind(bindpoint);
		this.socket.setSoTimeout(REPLY_TIMEOUT);
		this.socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT);
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

		this.rawOutput = this.socket.getOutputStream();
		this.dotTerminatedOutput = new DotTerminatedOutputStream(this.rawOutput);
		this.dataOutput = new ExtraDotOutputStream(this.dotTerminatedOutput);
		this.writer = new PrintWriter(this.rawOutput, true);
	}

	/**
	 * @return a nice pretty description of who we are connected to
	 */
	public String getHostPort()
	{
		return this.hostPort;
	}

	/**
	 * Sends a message to the server, ie "HELO foo.example.com". A newline will
	 * be appended to the message.
	 *
	 * @param msg should not have any newlines
	 */
	protected void send(String msg) throws IOException
	{
		if (log.isDebugEnabled())
			log.debug("Client: " + msg);

		// Force \r\n since println() behaves differently on different platforms
		this.writer.print(msg + "\r\n");
		this.writer.flush();
	}

	/**
	 * Note that the response text comes back without trailing newlines.
	 */
	protected Response receive() throws IOException
	{
		StringBuilder builder = new StringBuilder();
		String line = null;

		boolean done = false;
		while (!done)
		{
			line = this.reader.readLine();

			if (log.isDebugEnabled())
				log.debug("Server: " + line);

			builder.append(line.substring(4));

			if (line.charAt(3) == '-')
				builder.append('\n');
			else
				done = true;
		}

		String code = line.substring(0, 3);

		return new Response(Integer.parseInt(code), builder.toString());
	}

	/**
	 * Sends a message to the server, ie "HELO foo.example.com". A newline will
	 * be appended to the message.
	 *
	 * @param msg should not have any newlines
	 * @return the response from the server
	 */
	public Response sendReceive(String msg) throws IOException
	{
		this.send(msg);
		return this.receive();
	}

	/** If response is not success, throw an exception */
	public void receiveAndCheck() throws IOException, SMTPException
	{
		Response resp = this.receive();
		if (!resp.isSuccess())
			throw new SMTPException(resp);
	}

	/** If response is not success, throw an exception */
	public void sendAndCheck(String msg) throws IOException, SMTPException
	{
		this.send(msg);
		this.receiveAndCheck();
	}

	/** Logs but otherwise ignores errors */
	public void close()
	{
		if (!this.socket.isClosed())
		{
			try
			{
				this.socket.close();

				if (log.isDebugEnabled())
					log.debug("Closed connection to " + this.hostPort);
			}
			catch (IOException ex)
			{
				log.error("Problem closing connection to " + this.hostPort, ex);
			}
		}
	}

	/** */
	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " { " + this.hostPort + "}";
	}
}
