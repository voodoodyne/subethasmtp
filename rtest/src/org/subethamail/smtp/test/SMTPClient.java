package org.subethamail.smtp.test;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;

/**
 * NIO Test client
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SMTPClient
{
	private static final String HOSTNAME = "127.0.0.1";
	private static final int PORT = 25;
	private static final int CONNECT_TIMEOUT = 30; // seconds
	private static final int MAX_THREADS = 3000;

	public static void main(String[] args) throws Throwable
	{
		ByteBuffer.setUseDirectBuffers(false);
		ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

		SocketConnector connector =
			new SocketConnector(Runtime.getRuntime().availableProcessors() + 1, Executors.newCachedThreadPool());

		// Configure the service.
		SocketConnectorConfig config = connector.getDefaultConfig();
		config.setConnectTimeout(CONNECT_TIMEOUT);
		config.setThreadModel(ThreadModel.MANUAL);

		connector.getFilterChain().addLast("threadPool", new ExecutorFilter(Executors.newFixedThreadPool(MAX_THREADS)));
		connector.getFilterChain().addLast("codec",
											new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
		connector.getFilterChain().addLast("logger", new LoggingFilter());

		SMTPSessionHandler handler = new SMTPSessionHandler("localhost");
		while (true)
		{
			try
			{
				for (int i = 0; i < 10; i++)
					connector.connect(new InetSocketAddress(HOSTNAME, PORT), handler);
				Thread.sleep(100);
			}
			catch (RuntimeIOException e)
			{
				System.err.println("Failed to connect.");
				e.printStackTrace();
				Thread.sleep(1000);
			}
		}
	}
}
