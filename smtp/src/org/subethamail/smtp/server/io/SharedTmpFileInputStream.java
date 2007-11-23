package org.subethamail.smtp.server.io;

import java.io.File;
import java.io.IOException;

import javax.mail.util.SharedFileInputStream;

public class SharedTmpFileInputStream 
	extends SharedFileInputStream 
{
	private File tempFile;
	
	public SharedTmpFileInputStream(File f) throws IOException 
	{
		super(f);
		this.tempFile = f;
	}

	public void close() throws IOException 
	{
		super.close();
		if (in == null)
			this.tempFile.deleteOnExit();
	}
}
