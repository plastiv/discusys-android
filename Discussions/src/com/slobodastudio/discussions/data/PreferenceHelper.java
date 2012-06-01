package com.slobodastudio.discussions.data;

import com.slobodastudio.discussions.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {

	public static String getOdataUrl(final Context context) {

		return "http://" + getServerAddress(context) + "/DiscSvc/discsvc.svc/";
	}

	public static String getPhotonUrl(final Context context) {

		return getServerAddress(context) + ":5055";
	}

	public static String getServerAddress(final Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(PreferenceKey.SERVER_ADDRESS, context.getString(R.string.public_server_url));
	}
}
