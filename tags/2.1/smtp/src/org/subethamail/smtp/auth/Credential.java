package org.subethamail.smtp.auth;

/**
 * Holds the identity of a logged in user.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class Credential 
{
	private String id;

	/** Creates a new instance of Credential */
	public Credential(String id)
	{
		this.id = id;
	}
	
	public String getId() 
	{
		return id;
	}

	public void setId(String id) 
	{
		this.id = id;
	}
}
