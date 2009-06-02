package org.subethamail.smtp.client;

import java.io.IOException;

import org.subethamail.smtp.client.SMTPClient.Response;

@SuppressWarnings("serial")
public class SMTPException extends IOException
{
	Response response;

	public SMTPException(Response resp)
	{
		super(resp.toString());
		
		this.response = resp;
	}

	public Response getResponse() { return this.response; }
	
}
