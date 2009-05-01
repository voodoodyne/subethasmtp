package org.subethamail.smtp.test.command;

import org.subethamail.smtp.test.util.ServerTestCase;

/**
 * @author Jon Stevens
 */
public class DataTest extends ServerTestCase
{
	public DataTest(String name)
	{
		super(name);
	}

	public void testNeedMail() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("DATA");
		expect("503 Error: need MAIL command");
	}

	public void testNeedRcpt() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@subethamail.org");
		expect("250");

		send("DATA");
		expect("503 Error: need RCPT command");
	}

	public void testData() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@subethamail.org");
		expect("250");

		send("RCPT TO: success@subethamail.org");
		expect("250");

		send("DATA");
		expect("354 End data with <CR><LF>.<CR><LF>");
	}

	public void testRsetAfterData() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@subethamail.org");
		expect("250");

		send("RCPT TO: success@subethamail.org");
		expect("250");

		send("DATA");
		expect("354 End data with <CR><LF>.<CR><LF>");

		send("alsdkfj \r\n.");

		send("RSET");
		expect("250 Ok");
		
		send("HELO foo.com");
		expect("250");
	}
}
