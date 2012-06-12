package org.subethamail.smtp.server;

import java.util.Locale;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * TimeBasedSessionIdFactory is a very simple {@link SessionIdFactory}, which
 * assigns numeric identifiers based on the current milliseconds time, amending
 * it as necessary to make it unique.
 */
@ThreadSafe
public class TimeBasedSessionIdFactory implements SessionIdFactory {
	@GuardedBy("this")
	private long lastAllocatedId = 0;

	@Override
	public String create() {
		long id = System.currentTimeMillis();
		synchronized (this) {
			if (id <= lastAllocatedId)
				id = lastAllocatedId + 1;
			lastAllocatedId = id;
		}
		return Long.toString(id, 36).toUpperCase(Locale.ENGLISH);
	}
}
