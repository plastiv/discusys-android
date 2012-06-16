package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.PreferenceHelper;
import com.slobodastudio.discussions.data.PreferenceKey;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class DiscusysPreferenceActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private ListPreference mServerAddressListPreference;

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

		if (PreferenceKey.SERVER_ADDRESS.equals(key)) {
			mServerAddressListPreference.setSummary(PreferenceHelper.getServerAddress(this));
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.discusys_preference);
		mServerAddressListPreference = (ListPreference) getPreferenceScreen().findPreference(
				PreferenceKey.SERVER_ADDRESS);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);
	}

	@Override
	protected void onResume() {

		super.onResume();
		mServerAddressListPreference.setSummary(PreferenceHelper.getServerAddress(this));
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop() {

		super.onStop();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
}
