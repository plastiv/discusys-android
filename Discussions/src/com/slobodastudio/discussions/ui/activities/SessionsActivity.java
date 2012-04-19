package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sessions;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class SessionsActivity extends BaseActivity {

	// TODO: separate same with PersonsActivity code to one BaseMainActivity
	private static final String TAG = SessionsActivity.class.getSimpleName();
	private boolean mIsActivityCreated;

	public SessionsActivity() {

		// default values
		mIsActivityCreated = false;
	}

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_list_refresh, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_refresh:
				mServiceHelper.downloadAll();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onControlServiceConnected() {

		if (DEBUG) {
			Log.d(TAG, "[onControlServiceConnected] action main: " + mIsActivityCreated + ", isBound: "
					+ mBound);
		}
		if (mIsActivityCreated && mBound) {
			// when app first run
			mServiceHelper.downloadAll();
			mIsActivityCreated = false;
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		if (DEBUG) {
			Log.d(TAG, "[onCreate] action main: " + getIntent().getAction().equals(Intent.ACTION_MAIN));
		}
		if (getIntent().getAction().equals(Intent.ACTION_MAIN)) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Sessions.CONTENT_URI);
			setIntent(intent);
			if (savedInstanceState == null) {
				// first time activity created
				showCurrentVersionInToast();
				mIsActivityCreated = true;
			}
		}
		super.onCreate(savedInstanceState);
		setTitle(R.string.activity_title_sessions);
		setContentView(R.layout.activity_sessions);
		// AnalyticsUtils.getInstance(this).trackPageView("/Home");
	}

	private void showCurrentVersionInToast() {

		String versionName;
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			throw new RuntimeException();
		}
		Toast.makeText(this, getString(R.string.toast_version, versionName), Toast.LENGTH_SHORT).show();
	}
}
