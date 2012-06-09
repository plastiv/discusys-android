package com.slobodastudio.discussions.data;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {

	public static String getOdataUrl(final Context context) {

		if (ApplicationConstants.ODATA_LOCAL) {
			return "http://192.168.1.122/DiscSvc/discsvc.svc/";
		}
		return "http://" + getServerAddress(context) + "/DiscSvc/discsvc.svc/";
	}

	public static String getPhotonUrl(final Context context) {

		if (ApplicationConstants.PHOTON_LOCAL) {
			return "192.168.1.122:5555";
		}
		return getServerAddress(context) + ":5055";
	}

	public static String getServerAddress(final Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(PreferenceKey.SERVER_ADDRESS, context.getString(R.string.public_server_url));
	}
}
