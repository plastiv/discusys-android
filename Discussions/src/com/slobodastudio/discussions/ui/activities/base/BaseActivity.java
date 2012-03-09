package com.slobodastudio.discussions.ui.activities.base;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public abstract class BaseActivity extends SherlockFragmentActivity {

	static final boolean DEBUG = true | ApplicationConstants.DEBUG_MODE;
	private static final String TAG = BaseActivity.class.getSimpleName();
	private Fragment mFragment;

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
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_menu, menu);
		// Calling super after populating the menu is necessary here to ensure that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
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
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
				break;
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
			case R.id.menu_new:
				Toast.makeText(this, "Tapped new", Toast.LENGTH_SHORT).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		// This has to be called before setContentView and you must use the
		// class in com.actionbarsherlock.view and NOT android.view
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);
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
		}
	}

	/** Called in <code>onCreate</code> when the fragment constituting this activity is needed. The returned
	 * fragment's arguments will be set to the intent used to invoke this activity. */
	protected abstract Fragment onCreatePane();
}
