package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.odata.OdataSyncService;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.observers.PointsObserver;
import com.slobodastudio.discussions.photon.DiscussionUser;
import com.slobodastudio.discussions.photon.PhotonService;
import com.slobodastudio.discussions.photon.PhotonServiceCallback;
import com.slobodastudio.discussions.service.SyncService;
import com.slobodastudio.discussions.ui.IntentExtrasKey;
import com.slobodastudio.discussions.ui.fragments.PointsFragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

public class PointsActivity extends BaseListActivity implements PhotonServiceCallback {

	private static final boolean DEBUG = true && ApplicationConstants.DEBUG_MODE;
	private static final String TAG = PointsActivity.class.getSimpleName();
	PhotonService serviceInstance;
	private int discussionId;
	private int personId;
	private final PointsObserver pointObserver = new PointsObserver(new Handler(), this);
	private final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName className, final IBinder service) {

			if (DEBUG) {
				Log.d(TAG, "[onServiceConnected] className: " + className);
			}
			serviceInstance = ((PhotonService.LocalBinder) service).getService();
			serviceInstance.getCallbackHandler().addCallbackListener(PointsActivity.this);
			connectPhoton();
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {

			if (DEBUG) {
				Log.d(TAG, "[onServiceDisconnected] className: " + className);
			}
			serviceInstance = null;
		}
	};
	private int topicId;

	@Override
	public void onArgPointChanged(final int pointId) {

		if (DEBUG) {
			Log.d(TAG, "[onArgPointChanged] point id: " + pointId);
		}
		OdataSyncService odata = new OdataSyncService(this);
		odata.downloadPoint(pointId);
	}

	@Override
	public void onConnect() {

		if (DEBUG) {
			Log.d(TAG, "[onConnect] ");
		}
		// OdataWriteClient odata = new OdataWriteClient(ODataConstants.SERVICE_URL_JAPAN);
		// odata.insertPoint(Points.ArgreementCode.UNSOLVED, null, false, null, null, 4,
		// "android second try point", true, Points.SideCode.NEUTRAL, 2);
		// serviceInstance.opSendNotifyStructureChanged(topicId);
	}

	@Override
	public void onErrorOccured(final String message) {

		Log.e(TAG, "[onErrorOccured] message: " + message);
	}

	@Override
	public void onEventJoin(final DiscussionUser newUser) {

		if (DEBUG) {
			Log.d(TAG, "[onEventJoin] user come: " + newUser.getUserName());
		}
	}

	@Override
	public void onEventLeave(final DiscussionUser leftUser) {

		if (DEBUG) {
			Log.d(TAG, "[onEventLeave] user left: " + leftUser.getUserName());
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (DEBUG) {
			Log.d(TAG, "[onOptionsItemSelected] item id: " + item.getItemId());
		}
		switch (item.getItemId()) {
			case R.id.menu_new:
				((PointsFragment) mFragment).onActionNew();
				return true;
			case R.id.menu_refresh:
				if (serviceInstance.isConnected()) {
					serviceInstance.opSendNotifyStructureChanged(topicId);
				} else {
					Toast.makeText(this, "Photon is not connected", Toast.LENGTH_SHORT).show();
				}
				Toast.makeText(this, "Fetching data", Toast.LENGTH_SHORT).show();
				setSupportProgressBarIndeterminateVisibility(true);
				Intent intent = new Intent(SyncService.ACTION_DOWNLOAD);
				intent.putExtra(SyncService.EXTRA_TOPIC_ID, topicId);
				startService(intent);
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
		OdataSyncService odata = new OdataSyncService(this);
		odata.downloadPoints(topicId);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		initFromIntentExtra();
	}

	@Override
	protected Fragment onCreatePane() {

		return new PointsFragment();
	}

	@Override
	protected void onPause() {

		serviceInstance.getCallbackHandler().removeCallbackListener(this);
		unbindService(serviceConnection);
		getContentResolver().unregisterContentObserver(pointObserver);
		super.onPause();
	}

	@Override
	protected void onResume() {

		super.onResume();
		bindService(new Intent(this, PhotonService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		getContentResolver().registerContentObserver(Points.CONTENT_URI, true, pointObserver);
	}

	private void connectPhoton() {

		// TODO need a discussion here
		serviceInstance.connect(discussionId, "tcp:123.108.5.30,8080", "Tamaki", personId);
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
		personId = getIntent().getExtras().getInt(IntentExtrasKey.PERSON_ID);
		topicId = getIntent().getExtras().getInt(IntentExtrasKey.TOPIC_ID);
		discussionId = getIntent().getExtras().getInt(IntentExtrasKey.DISCUSSION_ID);
		if (DEBUG) {
			Log.d(TAG, "[initFromIntentExtras] personId: " + personId + ", topicId: " + topicId
					+ ", discussionId: " + discussionId);
		}
	}
}
