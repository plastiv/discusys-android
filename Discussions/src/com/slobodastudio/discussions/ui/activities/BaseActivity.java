package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;

public abstract class BaseActivity extends SherlockFragmentActivity {

	public static final boolean DEBUG = true && ApplicationConstants.DEBUG_MODE;
	private static final String TAG = BaseActivity.class.getSimpleName();
	protected Fragment mFragment;

	/** Invoke "home" action, returning to {@link HomeActivity}. */
	public static void goHome() {

		throw new UnsupportedOperationException("goHome() is not supported yet");
		// FIXME : handle if this is HomeActivity actually
		// if ( instanceof HomeActivity) {
		// return;
		// }
		// final Intent intent = new Intent(this, HomeActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// startActivity(intent);
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
}
