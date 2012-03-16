package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.service.SyncService;
import com.slobodastudio.discussions.utils.AnalyticsUtils;
import com.slobodastudio.discussions.utils.DetachableResultReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

public class HomeActivity extends BaseListActivity {

	private SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;

	private static void startNextActivity(final Context context) {

		Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
		context.startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (item.getItemId() == R.id.menu_refresh) {
			updateRefreshStatus(true);
			triggerRefresh();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		AnalyticsUtils.getInstance(this).trackPageView("/Home");
		setContentView(R.layout.base_activity);
		FragmentManager fm = getSupportFragmentManager();
		mSyncStatusUpdaterFragment = (SyncStatusUpdaterFragment) fm
				.findFragmentByTag(SyncStatusUpdaterFragment.TAG);
		if (mSyncStatusUpdaterFragment == null) {
			mSyncStatusUpdaterFragment = new SyncStatusUpdaterFragment();
			fm.beginTransaction().add(mSyncStatusUpdaterFragment, SyncStatusUpdaterFragment.TAG).commit();
		}
		String versionName;
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			throw new RuntimeException();
		}
		Toast.makeText(this, "version " + versionName, Toast.LENGTH_LONG).show();
	}

	@Override
	protected Fragment onCreatePane() {

		return null;
	}

	private void triggerRefresh() {

		updateRefreshStatus(true);
		final Intent intent = new Intent(SyncService.ACTION_SYNC);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mSyncStatusUpdaterFragment.mReceiver);
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
			((HomeActivity) getActivity()).updateRefreshStatus(mSyncing);
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
			((HomeActivity) getActivity()).triggerRefresh();
		}

		/** {@inheritDoc} */
		@Override
		public void onReceiveResult(final int resultCode, final Bundle resultData) {

			if (DEBUG) {
				Log.d(TAG, "[onReceiveResult] resultCode: " + resultCode + ", resultData: " + resultData);
			}
			HomeActivity activity = (HomeActivity) getActivity();
			if (activity == null) {
				return;
			}
			switch (resultCode) {
				case SyncService.STATUS_RUNNING: {
					mSyncing = true;
					activity.updateRefreshStatus(mSyncing);
					break;
				}
				case SyncService.STATUS_FINISHED: {
					mSyncing = false;
					activity.updateRefreshStatus(mSyncing);
					HomeActivity.startNextActivity(activity);
					break;
				}
				case SyncService.STATUS_ERROR: {
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
