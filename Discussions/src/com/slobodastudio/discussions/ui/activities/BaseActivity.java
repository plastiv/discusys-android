package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.service.ControlService;
import com.slobodastudio.discussions.service.ControlService.LocalBinder;
import com.slobodastudio.discussions.service.OdataSyncResultListener;
import com.slobodastudio.discussions.service.ServiceHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;

public abstract class BaseActivity extends SherlockFragmentActivity {

	public static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = BaseActivity.class.getSimpleName();
	protected boolean mBound = false;
	protected final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName className, final IBinder service) {

			if (DEBUG) {
				Log.d(TAG, "[onServiceConnected] className: " + className);
			}
			// We've bound to PhotonService, cast the IBinder and get PhotonService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mServiceHelper = mService.getServiceHelper();
			mBound = true;
			boolean syncing = mService.getServiceHelper().isSyncing();
			setSupportProgressBarIndeterminateVisibility(syncing);
			mServiceHelper.setOdataListener(mListener);
			onControlServiceConnected();
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {

			Log.e(TAG, "[onServiceDisconnected] className: " + className);
			mBound = false;
			mService = null;
			mServiceHelper = null;
		}
	};
	protected Fragment mFragment;
	protected ControlService mService;
	protected ServiceHelper mServiceHelper;
	private final OdataSyncResultListener mListener = new OdataSyncResultListener() {

		@Override
		public void handleError(final String message) {

			final String errorText = getString(R.string.toast_sync_error, message);
			Toast.makeText(BaseActivity.this, errorText, Toast.LENGTH_LONG).show();
		}

		@Override
		public void updateSyncStatus(final boolean syncing) {

			if (syncing) {
				// Toast.makeText(BaseActivity.this, "Syncing data...", Toast.LENGTH_LONG).show();
			} else {
				// Toast.makeText(BaseActivity.this, "Data synced", Toast.LENGTH_LONG).show();
			}
			setSupportProgressBarIndeterminateVisibility(syncing);
		}
	};

	public ServiceHelper getServiceHelper() {

		return mServiceHelper;
	}

	/** Invoke "home" action, returning to {@link HomeActivity}. */
	public void goHome() {

		if (this instanceof PersonsActivity) {
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/** Invoke "search" action, triggering a default search. */
	public void goSearch() {

		startSearch(null, false, Bundle.EMPTY, false);
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return false || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(final int keyCode, final KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goHome();
			return true;
		}
		return false || super.onKeyLongPress(keyCode, event);
	}

	protected abstract void onControlServiceConnected();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		// This has to be called before setContentView and you must use the
		// class in com.actionbarsherlock.view and NOT android.view
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(com.slobodastudio.discussions.R.layout.base_activity);
		FragmentManager fm = getSupportFragmentManager();
		if (DEBUG) {
			Log.d(TAG, "[onCreate] savedInstanceState: " + savedInstanceState + ", findFragmentById: "
					+ (fm.findFragmentById(R.id.frame_layout_list) == null));
		}
		if (savedInstanceState == null) {
			// Create the list fragment and add it as our sole content.
			if (fm.findFragmentById(R.id.frame_layout_list) == null) {
				mFragment = onCreatePane();
				if (mFragment == null) {
					return;
				}
				fm.beginTransaction().add(R.id.frame_layout_list, mFragment).commit();
			}
		} else {
			if (fm.findFragmentById(R.id.frame_layout_list) != null) {
				mFragment = fm.findFragmentById(R.id.frame_layout_list);
				//
				// fm.beginTransaction().add(R.id.frame_layout_list, mFragment).commit();
			} else {
				throw new IllegalStateException("fragment should be created here");
			}
		}
		setSupportProgressBarIndeterminateVisibility(false);
	}

	/** Called in <code>onCreate</code> when the fragment constituting this activity is needed. The returned
	 * fragment's arguments will be set to the intent used to invoke this activity. */
	protected abstract Fragment onCreatePane();

	@Override
	protected void onPause() {

		if (mBound) {
			mServiceHelper.setOdataListener(null);
		}
		super.onPause();
	}

	@Override
	protected void onStart() {

		super.onStart();
		Intent intent = new Intent(this, ControlService.class);
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
}
