package com.slobodastudio.discussions.utils;

import com.slobodastudio.discussions.ApplicationConstants;

import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

import java.util.HashMap;
import java.util.Map;

/** Custom wrapper on {@link Log}. Overrides some methods to make more robust control on logging, like
 * BugSense, exclude verbose messages from release. */
public class MyLog {

	private static final boolean GLOBAL_LOGD = true && ApplicationConstants.DEV_MODE;
	private static final boolean GLOBAL_LOGV = true && ApplicationConstants.DEV_MODE;

	/** Custom wrapper on system {@link Log#DEBUG} log. Blocks to print log on release version.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually identifies the class or activity
	 *            where the log call occurs.
	 * @param message
	 *            The message you would like logged. */
	public static void d(final String tag, final String message) {

		if (GLOBAL_LOGD) {
			Log.d(tag, message);
		}
	}

	/** Custom wrapper on system {@link Log#ERROR} log. Prints error in logcat while debugging and sends error
	 * report on release mode.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually identifies the class or activity
	 *            where the log call occurs.
	 * @param message
	 *            The message you would like logged.
	 * @param e
	 *            An exception to log */
	public static void e(final String tag, final String message, final Exception e) {

		if (ApplicationConstants.DEV_MODE) {
			Log.e(tag, message, e);
		} else {
			Map<String, String> extraData = new HashMap<String, String>();
			extraData.put("Description", message);
			BugSenseHandler.log(tag, extraData, e);
		}
	}

	/** Custom wrapper on system {@link Log#VERBOSE} log. Blocks to print log on release version.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually identifies the class or activity
	 *            where the log call occurs.
	 * @param message
	 *            The message you would like logged. */
	public static void v(final String tag, final String message) {

		if (GLOBAL_LOGV) {
			Log.v(tag, message);
		}
	}
}
