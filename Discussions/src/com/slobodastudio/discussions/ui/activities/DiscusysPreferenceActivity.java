package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class DiscusysPreferenceActivity extends SherlockPreferenceActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.discusys_preference);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);
	}
}
