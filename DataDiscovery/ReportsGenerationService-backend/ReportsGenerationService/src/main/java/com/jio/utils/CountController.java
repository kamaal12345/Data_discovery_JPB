package com.jio.utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CountController {
	private final int limit;
	private final AtomicInteger counter = new AtomicInteger(0);
	private final AtomicBoolean shouldStop = new AtomicBoolean(false);

	public CountController(int limit) {
		this.limit = limit;
	}

	public boolean increment(int delta) {
		if (shouldStop.get())
			return true;
		int current = counter.addAndGet(delta);
		if (current >= limit) {
			shouldStop.set(true);
			return true;
		}
		return false;
	}

	public boolean isStopped() {
		return shouldStop.get();
	}

	public int getCount() {
		return counter.get();
	}
}
