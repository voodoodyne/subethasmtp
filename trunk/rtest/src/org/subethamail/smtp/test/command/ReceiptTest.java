package org.subethamail.smtp.test.command;

import org.subethamail.smtp.test.ServerTestCase;

/**
 * @author Jon Stevens
 */
public class ReceiptTest extends ServerTestCase
{
	public ReceiptTest(String name)
	{
		super(name);
	}

	public void testReceiptBeforeMail() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("RCPT TO: bar@foo.com");
		expect("503 Error: need MAIL command");
	}

	public void testReceiptErrorInParams() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@subethamail.org");
		expect("250 Ok");

		send("RCPT");
		expect("501 Syntax: RCPT TO: <address>  Error in parameters:");
	}

	public void testReceiptAccept() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@subethamail.org");
		expect("250 Ok");

		send("RCPT TO: failure@subethamail.org");
		expect("553 <failure@subethamail.org> address unknown.");

		send("RCPT TO: success@subethamail.org");
		expect("250 Ok");
	}

	public void testReceiptNoWhiteSpace() throws Exception
	{
		expect("220");

		send("HELO foo.com");
		expect("250");

		send("MAIL FROM: success@subethamail.org");
		expect("250 Ok");

		send("RCPT TO:success@subethamail.org");
		expect("250 Ok");
	}

}
