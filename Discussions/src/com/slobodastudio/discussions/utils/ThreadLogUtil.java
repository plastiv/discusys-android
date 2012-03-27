package com.slobodastudio.discussions.utils;

import android.os.Looper;
import android.util.Log;

public class ThreadLogUtil {

	/** A private Constructor prevents class from instantiating. */
	private ThreadLogUtil() throws UnsupportedOperationException {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	public static String getThreadSignature() {

		final Thread t = Thread.currentThread();
		StringBuilder sb = new StringBuilder();
		sb.append("Thread name: ").append(t.getName());
		sb.append(" id: ").append(t.getId());
		sb.append(" priority: ").append(t.getPriority());
		sb.append(" group: ").append(t.getThreadGroup().getName());
		return sb.toString();
	}

	public static void logIsCurrentThreadUiThread(final String tag) {

		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
			Log.d(tag, "UI thread true");
		} else {
			Log.d(tag, "UI thread false");
		}
	}

	public static void logThreadSignature(final String tag) {

		Log.d(tag, getThreadSignature());
	}
}
