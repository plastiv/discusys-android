package com.slobodastudio.discussions.data;

import com.slobodastudio.discussions.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {

	public static String getOdataUrl(final Context context) {

		return "http://" + getServerAddress(context) + "/DiscSvc/discsvc.svc/";
	}

	public static String getPhotonDbAddress(final Context context) {

		String serverAddress = getServerAddress(context);
		String localServer = context.getString(R.string.local_server_address);
		if (serverAddress.equals(localServer)) {
			return context.getString(R.string.local_database_address);
		}
		String developmentServer = context.getString(R.string.development_server_address);
		if (serverAddress.equals(developmentServer)) {
			return context.getString(R.string.development_database_address);
		}
		String newPublicDb = context.getString(R.string.new_public_server_address);
		if (serverAddress.equals(newPublicDb)) {
			return context.getString(R.string.new_public_database_address);
		}
		// default case
		return context.getString(R.string.public_database_address);
	}

	public static String getPhotonUrl(final Context context) {

		return getServerAddress(context) + ":5055";
	}

	public static String getServerAddress(final Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(PreferenceKey.SERVER_ADDRESS, context
				.getString(R.string.public_server_address));
	}
}
