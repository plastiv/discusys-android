package com.slobodastudio.discussions.photon;

import com.slobodastudio.discussions.R;

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

import java.util.Random;

public class DiscussionBoardPhotonActivity extends Activity implements PhotonServiceCallback {

	private static final boolean LOGV = true;
	private static final String TAG = "MainActivity";
	private final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName className, final IBinder service) {

			Log.v(TAG, "onServiceConnected");
			serviceInstance = ((PhotonService.LocalBinder) service).getService();
			serviceInstance.getCallbackHandler().addCallbackListener(DiscussionBoardPhotonActivity.this);
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {

			Log.v(TAG, "onServiceDisconnected");
			serviceInstance = null;
		}
	};
	PhotonService serviceInstance;

	@Override
	public void onConnect() {

		Log.v(TAG, "login done");
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.photon_activity);
		if (LOGV) {
			Log.v(TAG, "onCreate()");
		}
		((Button) findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View arg0) {

				Log.v(TAG, "onClick");
				serviceInstance.connect(0, "localhost", "usr_" + (new Random()).nextInt(400), 1);
			}
		});
	}

	@Override
	public void onErrorOccured(final String message) {

		Log.v(TAG, "error occured: " + message);
	}

	@Override
	public void onEventJoin(DiscussionUser newUser) {

		// TODO Auto-generated method stub
	}

	@Override
	public void onEventLeave(final DiscussionUser leftUser) {

		// TODO Auto-generated method stub
	}

	@Override
	public void onStructureChanged(final int topicId) {

		Log.v(TAG, "on structure changed: " + topicId);
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