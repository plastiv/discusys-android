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

public class DiscussionBoardPhotonActivity extends Activity {

	private static final String TAG = "MainActivity";
	private final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName className, final IBinder service) {

			Log.v(TAG, "onServiceConnected");
			serviceInstance = ((PhotonService.LocalBinder) service).getService();
			// TODO: serviceInstance.addCallbackListener(RealtimeDemo.this);
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {

			Log.v(TAG, "onServiceDisconnected");
			serviceInstance = null;
		}
	};
	PhotonService serviceInstance;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
		setContentView(R.layout.photon_activity);
		bindService(new Intent(this, PhotonService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		((Button) findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View arg0) {

				Log.v(TAG, "onClick");
				serviceInstance.connect(0, PhotonConstants.SERVER_URL, "usr_" + (new Random()).nextInt(400),
						-1);
			}
		});
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		Log.v(TAG, "onDestroy");
		unbindService(serviceConnection);
	}
}