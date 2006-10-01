/*
 * $Id$
 * $Source: /cvsroot/Similarity4/src/java/com/similarity/mbean/BindStatisticsManagerMBean.java,v $
 */
package org.subethamail.smtp;

import java.io.IOException;

/**
 * Thrown to reject an SMTP command with a specific code.
 * 
 * @author Jeff Schnitzer
 */
@SuppressWarnings("serial")
public class RejectException extends IOException
{
	int code;
	
	/** */
	public RejectException()
	{
		this(554, "Transaction failed");
	}

	/** */
	public RejectException(int code, String message)
	{
		super(code + " " + message);
		
		this.code = code;
	}

	/** */
	public int getCode()
	{
		return this.code;
	}
}
