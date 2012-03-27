package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.photon.DiscussionUser;
import com.slobodastudio.discussions.photon.PhotonService;
import com.slobodastudio.discussions.photon.PhotonService.LocalBinder;
import com.slobodastudio.discussions.photon.PhotonServiceCallback;
import com.slobodastudio.discussions.photon.constants.PhotonConstants;
import com.slobodastudio.discussions.service.DownloadService;
import com.slobodastudio.discussions.ui.IntentExtrasKey;
import com.slobodastudio.discussions.ui.activities.HomeActivity.SyncStatusUpdaterFragment;
import com.slobodastudio.discussions.ui.fragments.PointsFragment;
import com.slobodastudio.discussions.utils.DetachableResultReceiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class PointsActivity extends BaseListActivity implements PhotonServiceCallback {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = PointsActivity.class.getSimpleName();
	private int discussionId;
	private boolean mBound = false;
	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName className, final IBinder service) {

			if (DEBUG) {
				Log.d(TAG, "[onServiceConnected] className: " + className);
			}
			// We've bound to PhotonService, cast the IBinder and get PhotonService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
			connectPhoton();
			mService.getCallbackHandler().addCallbackListener(PointsActivity.this);
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {

			if (DEBUG) {
				Log.d(TAG, "[onServiceDisconnected] className: " + className);
			}
			Log.e(TAG, "onServiceDisconnected");
			mBound = false;
			mService = null;
		}
	};
	private PhotonService mService;
	private OdataSyncStatusUpdaterFragment mSyncStatusUpdaterFragment;
	private int personId;
	private String personName;
	private int topicId;

	@Override
	public void onArgPointChanged(final int pointId) {

		if (DEBUG) {
			Log.d(TAG, "[onArgPointChanged] point id: " + pointId);
		}
		// showToast("Point changed: " + pointId);
		downloadPoint(pointId);
	}

	@Override
	public void onConnect() {

		if (DEBUG) {
			Log.d(TAG, "[onConnect] ");
		}
	}

	@Override
	public void onErrorOccured(final String message) {

		Log.e(TAG, "[onErrorOccured] message: " + message);
		// showToast("[onErrorOccured] message: " + message);
	}

	@Override
	public void onEventJoin(final DiscussionUser newUser) {

		if (DEBUG) {
			Log.d(TAG, "[onEventJoin] user come: " + newUser.getUserName());
		}
		// showToast("User online: " + newUser.getUserName());
	}

	@Override
	public void onEventLeave(final DiscussionUser leftUser) {

		if (DEBUG) {
			Log.d(TAG, "[onEventLeave] user left: " + leftUser.getUserName());
		}
		// showToast("User offline: " + leftUser.getUserName());
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (DEBUG) {
			Log.d(TAG, "[onOptionsItemSelected] item id: " + item.getItemId());
		}
		switch (item.getItemId()) {
			case R.id.menu_new:
				if (mFragment != null) {
					((PointsFragment) mFragment).onActionNew();
				} else {
					Toast.makeText(this, "New button press was skipped", Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.menu_refresh:
				downloadPointsFromTopic(topicId);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStructureChanged(final int topicId) {

		if (DEBUG) {
			Log.d(TAG, "[onStructureChanged] topic id: " + topicId);
		}
		// showToast("[onStructureChanged] topic id: " + topicId);
		downloadPointsFromTopic(topicId);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		initFromIntentExtra();
		FragmentManager fm = getSupportFragmentManager();
		mSyncStatusUpdaterFragment = (OdataSyncStatusUpdaterFragment) fm
				.findFragmentByTag(OdataSyncStatusUpdaterFragment.TAG);
		if (mSyncStatusUpdaterFragment == null) {
			mSyncStatusUpdaterFragment = new OdataSyncStatusUpdaterFragment();
			fm.beginTransaction().add(mSyncStatusUpdaterFragment, SyncStatusUpdaterFragment.TAG).commit();
		}
	}

	@Override
	protected Fragment onCreatePane() {

		return new PointsFragment();
	}

	@Override
	protected void onStart() {

		super.onStart();
		// Bind to LocalService (create in new thread)
		Intent intent = new Intent(this, PhotonService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {

		super.onStop();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	private void connectPhoton() {

		if (mBound && !mService.isConnected()) {
			mService.connect(discussionId, PhotonConstants.DB_SERVER_ADDRESS, personName, personId);
		}
	}

	private void downloadPoint(final int pointId) {

		Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_POINT);
		intent.putExtra(DownloadService.EXTRA_VALUE_ID, pointId);
		intent.putExtra(DownloadService.EXTRA_STATUS_RECEIVER, mSyncStatusUpdaterFragment.mReceiver);
		startService(intent);
	}

	private void downloadPointsFromTopic(final int topicId) {

		Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_POINT_FROM_TOPIC);
		intent.putExtra(DownloadService.EXTRA_VALUE_ID, topicId);
		intent.putExtra(DownloadService.EXTRA_STATUS_RECEIVER, mSyncStatusUpdaterFragment.mReceiver);
		startService(intent);
	}

	private void initFromIntentExtra() {

		if (!getIntent().hasExtra(IntentExtrasKey.PERSON_ID)) {
			throw new IllegalStateException("Activity intent was without person id");
		}
		if (!getIntent().hasExtra(IntentExtrasKey.TOPIC_ID)) {
			throw new IllegalStateException("Activity intent was without topic id");
		}
		if (!getIntent().hasExtra(IntentExtrasKey.DISCUSSION_ID)) {
			throw new IllegalStateException("Activity intent was without discussion id");
		}
		if (!getIntent().hasExtra(IntentExtrasKey.PERSON_NAME)) {
			throw new IllegalStateException("Activity intent was without person name");
		}
		personName = getIntent().getExtras().getString(IntentExtrasKey.PERSON_NAME);
		personId = getIntent().getExtras().getInt(IntentExtrasKey.PERSON_ID);
		topicId = getIntent().getExtras().getInt(IntentExtrasKey.TOPIC_ID);
		discussionId = getIntent().getExtras().getInt(IntentExtrasKey.DISCUSSION_ID);
		if (DEBUG) {
			Log.d(TAG, "[initFromIntentExtras] personId: " + personId + ", topicId: " + topicId
					+ ", discussionId: " + discussionId + ", personName: " + personName);
		}
	}

	private void showToast(final String message) {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				Toast.makeText(PointsActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void updateRefreshStatus(final boolean refreshing) {

		setSupportProgressBarIndeterminateVisibility(refreshing);
	}

	/** A non-UI fragment, retained across configuration changes, that updates its activity's UI when sync
	 * status changes. */
	public static class OdataSyncStatusUpdaterFragment extends Fragment implements
			DetachableResultReceiver.Receiver {

		public static final String TAG = OdataSyncStatusUpdaterFragment.class.getSimpleName();
		private DetachableResultReceiver mReceiver;
		private boolean mSyncing = false;

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {

			if (DEBUG) {
				Log.d(TAG, "[onActivityCreated] savedInstanceState: " + savedInstanceState);
			}
			super.onActivityCreated(savedInstanceState);
			((SherlockFragmentActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(mSyncing);
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
		}

		/** {@inheritDoc} */
		@Override
		public void onReceiveResult(final int resultCode, final Bundle resultData) {

			if (DEBUG) {
				Log.d(TAG, "[onReceiveResult] resultCode: " + resultCode + ", resultData: " + resultData);
			}
			SherlockFragmentActivity activity = (SherlockFragmentActivity) getActivity();
			if (activity == null) {
				return;
			}
			switch (resultCode) {
				case DownloadService.STATUS_RUNNING:
					mSyncing = true;
					break;
				case DownloadService.STATUS_FINISHED:
					mSyncing = false;
					break;
				case DownloadService.STATUS_ERROR:
					// Error happened down in SyncService, show as toast.
					mSyncing = false;
					final String errorText = getString(R.string.toast_sync_error, resultData
							.getString(Intent.EXTRA_TEXT));
					Toast.makeText(activity, errorText, Toast.LENGTH_LONG).show();
					break;
				default:
					throw new IllegalArgumentException("Unknown result code: " + resultCode);
			}
			activity.setSupportProgressBarIndeterminateVisibility(mSyncing);
		}
	}
}
