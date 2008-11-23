package org.subethamail.smtp.server.io;

import java.io.File;
import java.io.IOException;

import javax.mail.util.SharedFileInputStream;

/**
 * This class uses a temporary file to store big messages and asks JVM
 * to delete them when JVM is destroyed.
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SharedTmpFileInputStream 
	extends SharedFileInputStream 
{
	private File tempFile;
	
	public SharedTmpFileInputStream(File f) throws IOException 
	{
		super(f);
		this.tempFile = f;
		
		// Always mark file to be deleted on exit in case streams
		// are not closed properly.
		this.tempFile.deleteOnExit();
	}
}