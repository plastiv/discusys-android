package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.service.ServiceHelper.OdataSyncResultReceiver;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

public class ActivityResultHelper {

	public static void sendStatusError(final ResultReceiver activityReceiver, final String errorMessage) {

		if (activityReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putString(Intent.EXTRA_TEXT, errorMessage);
			activityReceiver.send(OdataSyncResultReceiver.STATUS_ERROR, bundle);
		}
	}

	public static void sendStatusFinished(final ResultReceiver activityReceiver) {

		if (activityReceiver != null) {
			activityReceiver.send(OdataSyncResultReceiver.STATUS_FINISHED, Bundle.EMPTY);
		}
	}

	public static void sendStatusStart(final ResultReceiver activityReceiver) {

		if (activityReceiver != null) {
			activityReceiver.send(OdataSyncResultReceiver.STATUS_RUNNING, Bundle.EMPTY);
		}
	}
}
