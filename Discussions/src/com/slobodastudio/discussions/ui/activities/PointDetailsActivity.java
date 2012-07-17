package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.PreferenceHelper;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.ui.ExtraKey;
import com.slobodastudio.discussions.ui.IntentAction;
import com.slobodastudio.discussions.ui.fragments.PointCommentsTabFragment;
import com.slobodastudio.discussions.ui.fragments.PointDescriptionTabFragment;
import com.slobodastudio.discussions.ui.fragments.PointMediaTabFragment;
import com.slobodastudio.discussions.ui.fragments.PointSourcesTabFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PointDetailsActivity extends BaseActivity {

	private static final int COMMENT_TAB_POSITION = 1;
	private static final int DESCRIPTION_TAB_POSITION = 0;
	private static final String EXTRA_KEY_TAB_INDEX = "extra_key_tab_index";
	private static final int MEDIA_TAB_POSITION = 2;
	private static final int SOURCE_TAB_POSITION = 3;
	private static final String TAG = PointDetailsActivity.class.getSimpleName();
	private int discussionId;
	private TabsAdapter mTabsAdapter;
	private ViewPager mViewPager;
	private int personId;
	private String personName;

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (isLandscape()) {
			if (isScreenSizeNormal() || isScreenSizeSmall()) {
				getSupportActionBar().setDisplayShowHomeEnabled(false);
			}
		}
	}

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

		PointDescriptionTabFragment descriptionTabFragment;
		if (mTabsAdapter == null) {
			descriptionTabFragment = (PointDescriptionTabFragment) getSupportFragmentManager()
					.findFragmentByTag(FragmentTag.POINT_DESCRIPTION);
		} else {
			descriptionTabFragment = (PointDescriptionTabFragment) mTabsAdapter
					.getItem(DESCRIPTION_TAB_POSITION);
		}
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
			case R.id.menu_change_topic:
				showChangeTopicDialog(descriptionTabFragment);
				return true;
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			Fragment sourceTabFragment = mTabsAdapter.getItem(SOURCE_TAB_POSITION);
			Log.d(TAG, "[onActivityResult] source tab fragment not null " + (sourceTabFragment != null));
			if ((sourceTabFragment != null)) {
				sourceTabFragment.onActivityResult(requestCode, resultCode, data);
			}
			Fragment mediaTabFragment = mTabsAdapter.getItem(MEDIA_TAB_POSITION);
			Log.d(TAG, "[onActivityResult] media tab fragment not null " + (mediaTabFragment != null));
			if ((mediaTabFragment != null)) {
				mediaTabFragment.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	@Override
	protected void onControlServiceConnected() {

		connectPhoton();
		PointMediaTabFragment mediaTabFragment;
		if (mTabsAdapter == null) {
			mediaTabFragment = (PointMediaTabFragment) getSupportFragmentManager().findFragmentByTag(
					FragmentTag.POINT_MEDIA);
		} else {
			mediaTabFragment = (PointMediaTabFragment) mTabsAdapter.getItem(MEDIA_TAB_POSITION);
		}
		if ((mediaTabFragment != null)) {
			mediaTabFragment.onServiceConnected();
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_details);
		initFromIntentExtra(getIntent());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(final int position) {

				Log.d(TAG, "[onPageSelected] position: " + position);
				// When swiping between pages, select the
				// corresponding tab.
				getSupportActionBar().setSelectedNavigationItem(position);
			}
		});
		String action = getIntent().getAction();
		if (IntentAction.NEW.equals(action)) {
			addDescripitionFragmentOnly();
		} else {
			setupActionBarTabs();
		}
		if (savedInstanceState != null) {
			getSupportActionBar()
					.setSelectedNavigationItem(savedInstanceState.getInt(EXTRA_KEY_TAB_INDEX, 0));
		} else if (Intent.ACTION_VIEW.equals(action)) {
			getSupportActionBar().setSelectedNavigationItem(COMMENT_TAB_POSITION);
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {

		super.onSaveInstanceState(outState);
		outState.putInt(EXTRA_KEY_TAB_INDEX, getSupportActionBar().getSelectedNavigationIndex());
	}

	private void addCommentsTab() {

		Tab commentsTab = getSupportActionBar().newTab();
		// commentsTab.setText(R.string.tab_title_comments);
		Bundle commentArguments = PointCommentsTabFragment.intentToFragmentArguments(getIntent());
		commentsTab.setIcon(R.drawable.ic_tab_comments);
		mTabsAdapter.addTab(commentsTab, FragmentTag.POINT_COMMENTS, PointCommentsTabFragment.class,
				commentArguments);
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
		// desctiptionTab.setText(R.string.tab_title_description);
		Bundle descriptionArguments = PointDescriptionTabFragment.intentToFragmentArguments(getIntent());
		desctiptionTab.setIcon(R.drawable.ic_tab_description);
		mTabsAdapter.addTab(desctiptionTab, FragmentTag.POINT_DESCRIPTION, PointDescriptionTabFragment.class,
				descriptionArguments);
	}

	private void addMediaTab() {

		Tab mediaTab = getSupportActionBar().newTab();
		// mediaTab.setText(R.string.tab_title_media);
		Bundle mediaArguments = PointMediaTabFragment.intentToFragmentArguments(getIntent());
		mediaTab.setIcon(R.drawable.ic_tab_attachments);
		mTabsAdapter.addTab(mediaTab, FragmentTag.POINT_MEDIA, PointMediaTabFragment.class, mediaArguments);
	}

	private void addSourceTab() {

		Tab sourceTab = getSupportActionBar().newTab();
		// sourceTab.setText(R.string.tab_title_source);
		Bundle sourceArguments = PointSourcesTabFragment.intentToFragmentArguments(getIntent());
		sourceTab.setIcon(R.drawable.ic_tab_sources);
		mTabsAdapter.addTab(sourceTab, FragmentTag.POINT_SOURCE, PointSourcesTabFragment.class,
				sourceArguments);
	}

	private void connectPhoton() {

		if (mBound && !mService.getPhotonController().isConnected()) {
			mService.getPhotonController().connect(this, discussionId,
					PreferenceHelper.getPhotonDbAddress(this), personName, personId);
		}
	}

	private void initFromIntentExtra(final Intent intent) {

		if (!intent.hasExtra(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalStateException("intent was without discussion id");
		}
		if (!intent.hasExtra(ExtraKey.PERSON_ID)) {
			throw new IllegalStateException("intent was without person id");
		}
		if (!intent.hasExtra(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("intent was without topic id");
		}
		personName = intent.getStringExtra(ExtraKey.PERSON_NAME);
		discussionId = intent.getIntExtra(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
		personId = intent.getIntExtra(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
	}

	private boolean isLandscape() {

		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	private boolean isScreenSizeNormal() {

		int screenLayout = getResources().getConfiguration().screenLayout;
		return (screenLayout & Configuration.SCREENLAYOUT_SIZE_NORMAL) == Configuration.SCREENLAYOUT_SIZE_NORMAL;
	}

	private boolean isScreenSizeSmall() {

		int screenLayout = getResources().getConfiguration().screenLayout;
		return (screenLayout & Configuration.SCREENLAYOUT_SIZE_SMALL) == Configuration.SCREENLAYOUT_SIZE_SMALL;
	}

	private void setupActionBarTabs() {

		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		bar.setDisplayHomeAsUpEnabled(true);
		if (isLandscape()) {
			if (isScreenSizeNormal() || isScreenSizeSmall()) {
				bar.setDisplayShowHomeEnabled(false);
			}
		}
		mTabsAdapter = new TabsAdapter(this, bar, mViewPager);
		addDescriptionTab();
		addCommentsTab();
		addMediaTab();
		addSourceTab();
	}

	private void showChangeTopicDialog(final PointDescriptionTabFragment descriptionTabFragment) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_title_attach);
		final Cursor cursor = getContentResolver().query(Topics.CONTENT_URI, null, null, null, null);
		builder.setCursor(cursor, new OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int which) {

				int nameColumeIndex = cursor.getColumnIndexOrThrow(Topics.Columns.NAME);
				int idColumeIndex = cursor.getColumnIndexOrThrow(Topics.Columns.ID);
				if (cursor.moveToPosition(which)) {
					Log.d(TAG, "Selected topic name: " + cursor.getString(nameColumeIndex) + " , id: "
							+ cursor.getInt(idColumeIndex));
					descriptionTabFragment.onActionSave(cursor.getInt(idColumeIndex));
				}
			}
		}, Topics.Columns.NAME);
		builder.create().show();
	}

	/** This is a helper class that implements the management of tabs and all details of connecting a ViewPager
	 * with associated TabHost. It relies on a trick. Normally a tab host has a simple API for supplying a
	 * View or Intent that each tab will show. This is not sufficient for switching between pages. So instead
	 * we make the content part of the tab host 0dp high (it is not shown) and the TabsAdapter supplies its
	 * own dummy view to show as the tab content. It listens to changes in tabs, and takes care of switch to
	 * the correct paged in the ViewPager whenever the selected tab changes. */
	public static class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,
			ViewPager.OnPageChangeListener {

		private final Map<String, Fragment> fragments = new HashMap<String, Fragment>();
		private final ActionBar mActionBar;
		private final Context mContext;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
		private final ViewPager mViewPager;

		public TabsAdapter(final FragmentActivity activity, final ActionBar actionBar, final ViewPager pager) {

			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = actionBar;
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(final Tab tab, final String tag, final Class<?> clss, final Bundle args) {

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			mActionBar.addTab(tab.setTabListener(this));
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {

			return mTabs.size();
		}

		@Override
		public Fragment getItem(final int position) {

			TabInfo info = mTabs.get(position);
			Fragment fragment = fragments.get(info.tag);
			if (fragment == null) {
				fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
				fragments.put(info.tag, fragment);
			}
			return fragment;
		}

		@Override
		public void onPageScrolled(final int position, final float positionOffset,
				final int positionOffsetPixels) {

		}

		@Override
		public void onPageScrollStateChanged(final int state) {

		}

		@Override
		public void onPageSelected(final int position) {

			mActionBar.setSelectedNavigationItem(position);
		}

		@Override
		public void onTabReselected(final Tab tab, final FragmentTransaction ft) {

		}

		@Override
		public void onTabSelected(final Tab tab, final FragmentTransaction ft) {

			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {

		}

		static final class TabInfo {

			private final Bundle args;
			private final Class<?> clss;
			private final String tag;

			TabInfo(final String _tag, final Class<?> _class, final Bundle _args) {

				tag = _tag;
				clss = _class;
				args = _args;
			}
		}
	}

	private final class FragmentTag {

		private static final String POINT_COMMENTS = "point_comments_tag";
		private static final String POINT_DESCRIPTION = "point_description_tag";
		private static final String POINT_MEDIA = "point_media_tag";
		private static final String POINT_SOURCE = "point_source_tag";
	}
}
