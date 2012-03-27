package com.slobodastudio.discussions.photon;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class DiscussionBoardPhotonActivity extends Activity implements PhotonServiceCallback {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = DiscussionBoardPhotonActivity.class.getSimpleName();
	PhotonService serviceInstance;
	private final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName className, final IBinder service) {

			if (DEBUG) {
				Log.d(TAG, "[onServiceConnected] className: " + className);
			}
			serviceInstance = ((PhotonService.LocalBinder) service).getService();
			serviceInstance.getCallbackHandler().addCallbackListener(DiscussionBoardPhotonActivity.this);
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {

			if (DEBUG) {
				Log.d(TAG, "[onServiceDisconnected] className: " + className);
			}
			serviceInstance = null;
		}
	};

	@Override
	public void onArgPointChanged(final int pointId) {

		if (DEBUG) {
			Log.d(TAG, "[onArgPointChanged] point id: " + pointId);
		}
	}

	@Override
	public void onConnect() {

		if (DEBUG) {
			Log.d(TAG, "[onConnect] ");
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.photon_activity);
		if (DEBUG) {
			Log.d(TAG, "[onCreate] savedInstanceState: " + savedInstanceState);
		}
		((Button) findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View arg0) {

				if (DEBUG) {
					Log.d(TAG, "[onClick] connectButton");
				}
				// serviceInstance.connect(0, "localhost", "usr_" + (new Random()).nextInt(400), 1);
				serviceInstance.connect(1, "tcp:123.108.5.30,8080", "Tamaki", 4);
			}
		});
		((Button) findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View arg0) {

				if (DEBUG) {
					Log.d(TAG, "[onClick] notify button");
				}
				OdataWriteClient odata = new OdataWriteClient(ODataConstants.SERVICE_URL);
				odata.insertPoint(Points.ArgreementCode.UNSOLVED, null, false, null, null, 4,
						"android second try point", true, Points.SideCode.NEUTRAL, 2);
				serviceInstance.opSendNotifyStructureChanged(2);
			}
		});
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
	public void onStructureChanged(final int topicId) {

		if (DEBUG) {
			Log.d(TAG, "[onStructureChanged] topic id: " + topicId);
		}
	}

	@Override
	protected void onPause() {

		serviceInstance.getCallbackHandler().removeCallbackListener(this);
		unbindService(serviceConnection);
		super.onPause();
	}

	@Override
	protected void onResume() {

		super.onResume();
		bindService(new Intent(this, PhotonService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}
}