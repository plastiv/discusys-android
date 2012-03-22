/*
 * Copyright 2012 sloboda-studio.com
 */
package com.slobodastudio.discussions;

import android.app.Application;
import android.os.StrictMode;

import com.bugsense.trace.BugSenseHandler;

/** The Class BackupApplication.
 * 
 * @author Sergii Pechenizkyi <pechenizkyi gmail com> */
public class DiscussionsApplication extends Application {

	@Override
	public void onCreate() {

		super.onCreate();
		if (ApplicationConstants.DEBUG_MODE) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectNetwork().penaltyDeath()
					.build());
		} else {
			BugSenseHandler.setup(this, ApplicationConstants.BUG_SENSE_API_KEY);
		}
	}
}
