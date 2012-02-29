package com.slobodastudio.discussions.tool;

import android.util.Log;

public class MyLog {

	public static void e(final String tag, final String message, final Throwable e) {

		Log.e(tag, message, e);
	}
}
