package org.subethamail.smtp.util;

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

}
