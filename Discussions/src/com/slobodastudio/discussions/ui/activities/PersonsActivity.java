package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PersonsActivity extends BaseActivity {

	private static final String TAG = PersonsActivity.class.getSimpleName();
	ProgressDialog dialog;
	private boolean mIsActivityCreated;

	public PersonsActivity() {

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
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
			Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
			setIntent(intent);
			if (savedInstanceState == null) {
				// first time activity created
				showCurrentVersionInToast();
				mIsActivityCreated = true;
			}
		}
		super.onCreate(savedInstanceState);
		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		setTitle(R.string.activity_title_persons);
		setContentView(R.layout.activity_persons);
		// AnalyticsUtils.getInstance(this).trackPageView("/Home");
	}

	@Override
	protected void showProgressDialog(final boolean shown) {

		if (shown) {
			dialog = ProgressDialog.show(PersonsActivity.this, null,
					getString(R.string.dialog_title_downloading_database), true);
			dialog.show();
		} else {
			dialog.dismiss();
		}
		super.showProgressDialog(shown);
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
