package org.subethamail.smtp.util;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Jeff Schnitzer
 */
public class TextUtils
{
	/**
	 * @return a delimited string containing the specified items
	 */
	public static String joinTogether(Collection<String> items, String delim)
	{
		StringBuffer ret = new StringBuffer();

		for (Iterator<String> it=items.iterator(); it.hasNext();)
		{
			ret.append(it.next());
			if (it.hasNext())
			{
				ret.append(delim);
			}
		}

		return ret.toString();
	}

	/**
	 * @return the value of str.getBytes() without the idiotic checked exception
	 */
	public static byte[] getBytes(String str, String charset)
	{
		try
		{
			return str.getBytes(charset);
		}
		catch (UnsupportedEncodingException ex) { throw new IllegalStateException(ex); }
	}
	
	/** @return the string as US-ASCII bytes */
	public static byte[] getAsciiBytes(String str)
	{
		return getBytes(str, "US-ASCII");
	}
}
