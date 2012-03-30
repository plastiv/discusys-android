package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.ui.fragments.PersonsListFragment;
import com.slobodastudio.discussions.utils.AnalyticsUtils;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PersonsActivity extends BaseActivity {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = PersonsActivity.class.getSimpleName();
	private boolean isActionMain = false;

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_menu_refresh, menu);
		// Calling super after populating the menu is necessary here to ensure that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_refresh:
				Toast.makeText(this, "Fake refreshing...", Toast.LENGTH_SHORT).show();
				setSupportProgressBarIndeterminateVisibility(true);
				getWindow().getDecorView().postDelayed(new Runnable() {

					@Override
					public void run() {

						setSupportProgressBarIndeterminateVisibility(false);
					}
				}, 1000);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onControlServiceConnected() {

		if (DEBUG) {
			Log.d(TAG, "[onControlServiceConnected] action main: " + isActionMain + ", isBound: " + mBound);
		}
		if (isActionMain && mBound) {
			// when app first run
			mServiceHelper.downloadAll();
			isActionMain = false;
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		if (getIntent().getAction().equals(Intent.ACTION_MAIN)) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
			setIntent(intent);
			showCurrentVersionInToast();
			isActionMain = true;
		}
		super.onCreate(savedInstanceState);
		setTitle(R.string.activity_name_persons);
		AnalyticsUtils.getInstance(this).trackPageView("/Home");
	}

	@Override
	protected Fragment onCreatePane() {

		return new PersonsListFragment();
	}

	private void showCurrentVersionInToast() {

		String versionName;
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			throw new RuntimeException();
		}
		Toast.makeText(this, "Version " + versionName, Toast.LENGTH_LONG).show();
	}
}
