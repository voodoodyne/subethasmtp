package org.subethamail.smtp.test.command;

import org.subethamail.smtp.test.util.ServerTestCase;

/**
 * @author Jon Stevens
 */
public class MailTest extends ServerTestCase
{
	/** */
	public MailTest(String name)
	{
		super(name);
	}

	/** */
	public void testMailNoHello() throws Exception
	{
		this.expect("220");

		this.send("MAIL FROM: test@example.com");
		this.expect("250");
	}

	/** */
	public void testAlreadySpecified() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("MAIL FROM: test@example.com");
		this.expect("250 Ok");

		this.send("MAIL FROM: another@example.com");
		this.expect("503 Sender already specified.");
	}

	/** */
	public void testInvalidSenders() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");
		
		// added <> because without them "lkjk" is a parameter
		// to the MAIL command. (Postfix responds accordingly)
		this.send("MAIL FROM: <test@lkjsd lkjk>");
		this.expect("553 <test@lkjsd lkjk> Invalid email address.");
	}

	/** */
	public void testMalformedMailCommand() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("MAIL");
		this.expect("501 Syntax: MAIL FROM: <address>  Error in parameters:");
	}

	/** */
	public void testEmptyFromCommand() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("MAIL FROM: <>");
		this.expect("250");
	}

	/** */
	public void testEmptyEmailFromCommand() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("MAIL FROM:");
		this.expect("501 Syntax: MAIL FROM: <address>");
	}

	/** */
	public void testMailWithoutWhitespace() throws Exception
	{
		this.expect("220");

		this.send("HELO foo.com");
		this.expect("250");

		this.send("MAIL FROM:<validuser@subethamail.org>");
		this.expect("250 Ok");
	}
	
	/** */
	public void testSize() throws Exception
	{
	    this.wiser.getServer().setMaxMessageSize(1000);
	    this.expect("220");

	    this.send("EHLO foo.com");
	    this.expectContains("250-SIZE 1000");

	    this.send("MAIL FROM:<validuser@subethamail.org> SIZE=100");
	    this.expect("250 Ok");
	}

	/** */
	public void testSizeWithoutSize() throws Exception
	{
	    this.wiser.getServer().setMaxMessageSize(1000);
	    this.expect("220");

	    this.send("EHLO foo.com");
	    this.expectContains("250-SIZE 1000");

	    this.send("MAIL FROM:<validuser@subethamail.org>");
	    this.expect("250 Ok");
	}

	/** */
	public void testSizeTooLarge() throws Exception
	{
	    this.wiser.getServer().setMaxMessageSize(1000);
	    this.expect("220");

	    this.send("EHLO foo.com");
	    this.expectContains("250-SIZE 1000");

	    this.send("MAIL FROM:<validuser@subethamail.org> SIZE=1001");
	    this.expect("552");
	}

}

