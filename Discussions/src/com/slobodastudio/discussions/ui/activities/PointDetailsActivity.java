package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.IntentAction;
import com.slobodastudio.discussions.ui.fragments.PointDetailCommentsFragment;
import com.slobodastudio.discussions.ui.fragments.PointDetailFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PointDetailsActivity extends BaseActivity {

	private static final String TAG = PointDetailsActivity.class.getSimpleName();
	private PointDetailFragment mFragment;

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		String action = getIntent().getAction();
		if (Intent.ACTION_VIEW.equals(action)) {
			menuInflater.inflate(R.menu.actionbar_point_view, menu);
			return super.onCreateOptionsMenu(menu);
		}
		if (Intent.ACTION_EDIT.equals(action)) {
			menuInflater.inflate(R.menu.actionbar_point_edit, menu);
			return super.onCreateOptionsMenu(menu);
		}
		if (IntentAction.NEW.equals(action)) {
			menuInflater.inflate(R.menu.actionbar_point_new, menu);
			return super.onCreateOptionsMenu(menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_save:
				mFragment.onActionSave();
				finish();
				return true;
			case R.id.menu_cancel:
				finish();
				return true;
			case R.id.menu_delete:
				mFragment.onActionDelete();
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(final int arg0, final int arg1, final Intent arg2) {

		Log.d(TAG, "[onActivityResult] ");
		// TODO need to call super for a fragment handled
		super.onActivityResult(arg0, arg1, arg2);
		mFragment.onActivityResult(arg0, arg1, arg2);
	}

	@Override
	protected void onControlServiceConnected() {

		// No operation with service in this activity
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_details);
		// FragmentManager fm = getSupportFragmentManager();
		// boolean fragmentNotFound = fm.findFragmentById(R.id.frame_point_details) == null;
		// if (DEBUG) {
		// Log.d(TAG, "[onCreate] savedInstanceState: " + savedInstanceState + ", findFragmentById: "
		// + fragmentNotFound);
		// }
		// if (savedInstanceState == null) {
		// // Create the list fragment and add it as our sole content.
		// if (fragmentNotFound) {
		// mFragment = new PointDetailFragment();
		// mFragment.setArguments(PointDetailFragment.intentToFragmentArguments(getIntent()));
		// fm.beginTransaction().add(R.id.frame_point_details, mFragment).commit();
		// }
		// } else {
		// if (!fragmentNotFound) {
		// mFragment = (PointDetailFragment) fm.findFragmentById(R.id.frame_point_details);
		// } else {
		// throw new IllegalStateException("fragment should be created here");
		// }
		// }
		final ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		bar.addTab(bar.newTab().setText("Description").setTabListener(
				new TabListener<PointDetailFragment>(this, "description", PointDetailFragment.class,
						PointDetailFragment.intentToFragmentArguments(getIntent()))));
		addCommentsTab(bar);
		// bar.addTab(bar.newTab()
		// .setText("Apps")
		// .setTabListener(new TabListener<LoaderCustom.AppListFragment>(
		// this, "apps", LoaderCustom.AppListFragment.class)));
		// bar.addTab(bar.newTab()
		// .setText("Throttle")
		// .setTabListener(new TabListener<LoaderThrottle.ThrottledLoaderListFragment>(
		// this, "throttle", LoaderThrottle.ThrottledLoaderListFragment.class)));
		if (savedInstanceState != null) {
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {

		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}

	private void addCommentsTab(final ActionBar actionBar) {

		Tab commentsTab = actionBar.newTab();
		commentsTab.setText("Comments");
		Bundle fragmentArguments = PointDetailCommentsFragment.intentToFragmentArguments(getIntent());
		TabListener<PointDetailCommentsFragment> commentsTabListener = new TabListener<PointDetailCommentsFragment>(
				this, "comments", PointDetailCommentsFragment.class, fragmentArguments);
		commentsTab.setTabListener(commentsTabListener);
		actionBar.addTab(commentsTab);
	}

	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {

		private final SherlockFragmentActivity mActivity;
		private final Bundle mArgs;
		private final Class<T> mClass;
		private Fragment mFragment;
		private final String mTag;

		public TabListener(final SherlockFragmentActivity activity, final String tag, final Class<T> clz) {

			this(activity, tag, clz, null);
		}

		public TabListener(final SherlockFragmentActivity activity, final String tag, final Class<T> clz,
				final Bundle args) {

			mActivity = activity;
			mTag = tag;
			mClass = clz;
			mArgs = args;
			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state. If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
			if ((mFragment != null) && !mFragment.isDetached()) {
				FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
				ft.detach(mFragment);
				ft.commit();
			}
		}

		@Override
		public void onTabReselected(final Tab tab, final FragmentTransaction ft) {

			Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onTabSelected(final Tab tab, final FragmentTransaction ft) {

			if (mFragment == null) {
				mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				ft.attach(mFragment);
			}
		}

		@Override
		public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {

			if (mFragment != null) {
				ft.detach(mFragment);
			}
		}
	}
}
