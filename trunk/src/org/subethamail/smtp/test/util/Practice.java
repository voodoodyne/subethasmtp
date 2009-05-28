package org.subethamail.smtp.test.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;

/**
 * A simple command-line tool that lets us practice with the smtp library.
 * 
 * @author Jeff Schnitzer
 */
public class Practice
{
	/** */
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(Practice.class);
	
	/** */
	public static final int PORT = 2566;

	/** */
	public static void main(String[] args) throws Exception
	{
		Wiser wiser = new Wiser();
		wiser.setHostname("localhost");
		wiser.setPort(PORT);

		wiser.start();

		char ch;
		do
		{
			ch = (char)System.in.read();
			if (ch == ' ')
				wiser.dumpMessages(System.out);
		}
		while (ch != 'q');
		
		wiser.stop();
	}
}