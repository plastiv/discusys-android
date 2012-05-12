package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.IntentAction;
import com.slobodastudio.discussions.ui.fragments.PointCommentsTabFragment;
import com.slobodastudio.discussions.ui.fragments.PointDescriptionTabFragment;
import com.slobodastudio.discussions.ui.fragments.PointMediaTabFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PointDetailsActivity extends BaseActivity {

	private static final String EXTRA_KEY_TAB_INDEX = "extra_key_tab_index";
	private static final String TAG = PointDetailsActivity.class.getSimpleName();

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

		PointDescriptionTabFragment descriptionTabFragment = (PointDescriptionTabFragment) getSupportFragmentManager()
				.findFragmentByTag(FragmentTag.POINT_DESCRIPTION);
		switch (item.getItemId()) {
			case R.id.menu_save:
				descriptionTabFragment.onActionSave();
				finish();
				return true;
			case R.id.menu_cancel:
				finish();
				return true;
			case R.id.menu_delete:
				descriptionTabFragment.onActionDelete();
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		Log.d(TAG, "[onActivityResult]");
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			Fragment mediaTabFragment = getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.POINT_MEDIA);
			if ((mediaTabFragment != null)) {
				mediaTabFragment.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	@Override
	protected void onControlServiceConnected() {

		// No operation with service in this activity
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_details);
		String action = getIntent().getAction();
		if (IntentAction.NEW.equals(action)) {
			addDescripitionFragmentOnly();
		} else {
			setupActionBarTabs(savedInstanceState);
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {

		super.onSaveInstanceState(outState);
		outState.putInt(EXTRA_KEY_TAB_INDEX, getSupportActionBar().getSelectedNavigationIndex());
	}

	private void addCommentsTab() {

		Tab commentsTab = getSupportActionBar().newTab();
		commentsTab.setText(R.string.tab_title_comments);
		Bundle fragmentArguments = PointCommentsTabFragment.intentToFragmentArguments(getIntent());
		TabListener<PointCommentsTabFragment> commentsTabListener = new TabListener<PointCommentsTabFragment>(
				this, FragmentTag.POINT_COMMENTS, PointCommentsTabFragment.class, fragmentArguments);
		commentsTab.setTabListener(commentsTabListener);
		commentsTab.setIcon(R.drawable.ic_tab_comments);
		getSupportActionBar().addTab(commentsTab);
	}

	private void addDescripitionFragmentOnly() {

		Fragment descriptionTabFragment = getSupportFragmentManager().findFragmentByTag(
				FragmentTag.POINT_DESCRIPTION);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (descriptionTabFragment == null) {
			Bundle fragmentArguments = PointDescriptionTabFragment.intentToFragmentArguments(getIntent());
			descriptionTabFragment = Fragment.instantiate(this, PointDescriptionTabFragment.class.getName(),
					fragmentArguments);
			ft.add(android.R.id.content, descriptionTabFragment, FragmentTag.POINT_DESCRIPTION);
		} else {
			ft.attach(descriptionTabFragment);
		}
		ft.commit();
	}

	private void addDescriptionTab() {

		Tab desctiptionTab = getSupportActionBar().newTab();
		desctiptionTab.setText(R.string.tab_title_description);
		Bundle fragmentArguments = PointDescriptionTabFragment.intentToFragmentArguments(getIntent());
		TabListener<PointDescriptionTabFragment> commentsTabListener = new TabListener<PointDescriptionTabFragment>(
				this, FragmentTag.POINT_DESCRIPTION, PointDescriptionTabFragment.class, fragmentArguments);
		desctiptionTab.setTabListener(commentsTabListener);
		desctiptionTab.setIcon(R.drawable.ic_tab_description);
		getSupportActionBar().addTab(desctiptionTab);
	}

	private void addMediaTab() {

		Tab mediaTab = getSupportActionBar().newTab();
		mediaTab.setText(R.string.tab_title_media);
		Bundle fragmentArguments = PointMediaTabFragment.intentToFragmentArguments(getIntent());
		TabListener<PointMediaTabFragment> mediaTabListener = new TabListener<PointMediaTabFragment>(this,
				FragmentTag.POINT_MEDIA, PointMediaTabFragment.class, fragmentArguments);
		mediaTab.setTabListener(mediaTabListener);
		mediaTab.setIcon(R.drawable.ic_tab_attachments);
		getSupportActionBar().addTab(mediaTab);
	}

	private void setupActionBarTabs(final Bundle savedInstanceState) {

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		addDescriptionTab();
		addCommentsTab();
		addMediaTab();
		if (savedInstanceState != null) {
			getSupportActionBar()
					.setSelectedNavigationItem(savedInstanceState.getInt(EXTRA_KEY_TAB_INDEX, 0));
		}
	}

	private final class FragmentTag {

		private static final String POINT_COMMENTS = "point_comments_tag";
		private static final String POINT_DESCRIPTION = "point_description_tag";
		private static final String POINT_MEDIA = "point_media_tag";
	}

	private class TabListener<T extends Fragment> implements ActionBar.TabListener {

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
				Log.d(TAG, "[TabListener] detach fragment " + mTag);
				FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
				ft.detach(mFragment);
				ft.commit();
			}
		}

		@Override
		public void onTabReselected(final Tab tab, final FragmentTransaction ft) {

			// do nothing. Tab already present
		}

		@Override
		public void onTabSelected(final Tab tab, final FragmentTransaction ft) {

			if (mFragment == null) {
				Log.d(TAG, "[TabListener] create fragment " + mTag);
				mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				Log.d(TAG, "[TabListener] attach fragment " + mTag);
				ft.attach(mFragment);
			}
		}

		@Override
		public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {

			if (mFragment != null) {
				Log.d(TAG, "[onTabUnselected] detach fragment " + mTag);
				ft.detach(mFragment);
			}
		}
	}
}
