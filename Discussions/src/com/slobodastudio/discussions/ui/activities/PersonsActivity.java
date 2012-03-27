package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.service.DownloadService;
import com.slobodastudio.discussions.ui.fragments.PersonsListFragment;
import com.slobodastudio.discussions.utils.AnalyticsUtils;
import com.slobodastudio.discussions.utils.DetachableResultReceiver;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PersonsActivity extends BaseActivity {

	private static final String TAG = PersonsActivity.class.getSimpleName();
	private SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;

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
	protected void onCreate(final Bundle savedInstanceState) {

		if (getIntent().getAction().equals(Intent.ACTION_MAIN)) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
			setIntent(intent);
		}
		super.onCreate(savedInstanceState);
		setTitle(R.string.activity_name_persons);
		AnalyticsUtils.getInstance(this).trackPageView("/Home");
		setContentView(R.layout.base_activity);
		FragmentManager fm = getSupportFragmentManager();
		mSyncStatusUpdaterFragment = (SyncStatusUpdaterFragment) fm
				.findFragmentByTag(SyncStatusUpdaterFragment.TAG);
		if (mSyncStatusUpdaterFragment == null) {
			mSyncStatusUpdaterFragment = new SyncStatusUpdaterFragment();
			fm.beginTransaction().add(mSyncStatusUpdaterFragment, SyncStatusUpdaterFragment.TAG).commit();
		}
		showCurrentVersionInToast();
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

	private void triggerRefresh() {

		updateRefreshStatus(true);
		final Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_ALL);
		intent.putExtra(DownloadService.EXTRA_STATUS_RECEIVER, mSyncStatusUpdaterFragment.mReceiver);
		startService(intent);
	}

	private void updateRefreshStatus(final boolean refreshing) {

		setSupportProgressBarIndeterminateVisibility(refreshing);
	}

	/** A non-UI fragment, retained across configuration changes, that updates its activity's UI when sync
	 * status changes. */
	public static class SyncStatusUpdaterFragment extends Fragment implements
			DetachableResultReceiver.Receiver {

		public static final String TAG = SyncStatusUpdaterFragment.class.getSimpleName();
		private DetachableResultReceiver mReceiver;
		private boolean mSyncing = false;

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {

			if (DEBUG) {
				Log.d(TAG, "[onActivityCreated] savedInstanceState: " + savedInstanceState);
			}
			super.onActivityCreated(savedInstanceState);
			((PersonsActivity) getActivity()).updateRefreshStatus(mSyncing);
		}

		@Override
		public void onCreate(final Bundle savedInstanceState) {

			if (DEBUG) {
				Log.d(TAG, "[onCreate] savedInstanceState: " + savedInstanceState);
			}
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
			mReceiver = new DetachableResultReceiver(new Handler());
			mReceiver.setReceiver(this);
			((PersonsActivity) getActivity()).triggerRefresh();
		}

		/** {@inheritDoc} */
		@Override
		public void onReceiveResult(final int resultCode, final Bundle resultData) {

			if (DEBUG) {
				Log.d(TAG, "[onReceiveResult] resultCode: " + resultCode + ", resultData: " + resultData);
			}
			PersonsActivity activity = (PersonsActivity) getActivity();
			if (activity == null) {
				return;
			}
			switch (resultCode) {
				case DownloadService.STATUS_RUNNING: {
					mSyncing = true;
					activity.updateRefreshStatus(mSyncing);
					Toast.makeText(activity, "Syncing data...", Toast.LENGTH_LONG).show();
					break;
				}
				case DownloadService.STATUS_FINISHED: {
					mSyncing = false;
					activity.updateRefreshStatus(mSyncing);
					Toast.makeText(activity, "Data synced", Toast.LENGTH_LONG).show();
					break;
				}
				case DownloadService.STATUS_ERROR: {
					// Error happened down in SyncService, show as toast.
					mSyncing = false;
					activity.updateRefreshStatus(mSyncing);
					final String errorText = getString(R.string.toast_sync_error, resultData
							.getString(Intent.EXTRA_TEXT));
					Toast.makeText(activity, errorText, Toast.LENGTH_LONG).show();
					break;
				}
			}
		}
	}
}
