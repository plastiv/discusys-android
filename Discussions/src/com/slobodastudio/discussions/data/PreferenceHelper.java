package com.slobodastudio.discussions.data;

import com.slobodastudio.discussions.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class PreferenceHelper {

	public static String getOdataUrl(final Context context) {

		return "http://" + getServerAddress(context) + "/DiscSvc/discsvc.svc/";
	}

	public static String getPhotonDbAddress(final Context context) {

		String serverAddress = getServerAddress(context);
		String localServer = context.getString(R.string.local_server_address);
		if (TextUtils.equals(serverAddress, localServer)) {
			return context.getString(R.string.local_database_address);
		}
		String developmentServer = context.getString(R.string.development_server_address);
		if (TextUtils.equals(serverAddress, developmentServer)) {
			return context.getString(R.string.development_database_address);
		}
		String publicServer = context.getString(R.string.public_server_address);
		if (TextUtils.equals(serverAddress, publicServer)) {
			return context.getString(R.string.public_database_address);
		}
		throw new IllegalStateException("Could not find database connection string for this server: "
				+ serverAddress);
	}

	public static String getPhotonUrl(final Context context) {

		return getServerAddress(context) + ":5055";
	}

	public static String getServerAddress(final Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String defaultServer = context.getString(R.string.local_server_address);
		String serverAddress = prefs.getString(PreferenceKey.SERVER_ADDRESS, defaultServer);
		return serverAddress;
	}
}
