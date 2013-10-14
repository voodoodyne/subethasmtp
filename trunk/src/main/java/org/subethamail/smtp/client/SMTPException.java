package org.subethamail.smtp.client;

import java.io.IOException;

import org.subethamail.smtp.client.SMTPClient.Response;

/**
 * Thrown if a syntactically valid reply was received from the server, which
 * indicates an error via the status code.
 */
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
