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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;

public class PointDetailsActivity extends BaseActivity {

	private static final String EXTRA_KEY_TAB = "extra_key_tab";
	private static final String TAG = PointDetailsActivity.class.getSimpleName();
	private int discussionId;
	private TabHost mTabHost;
	private TabsAdapter mTabsAdapter;
	private ViewPager mViewPager;
	private int personId;
	private String personName;

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

		PointDescriptionTabFragment descriptionTabFragment = (PointDescriptionTabFragment) mTabsAdapter
				.getActiveFragment(0);
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
			Fragment sourceTabFragment = mTabsAdapter.getActiveFragment(2);
			// Fragment sourceTabFragment = getSupportFragmentManager().findFragmentByTag(
			// FragmentTag.POINT_SOURCE);
			if ((sourceTabFragment != null)) {
				sourceTabFragment.onActivityResult(requestCode, resultCode, data);
			}
			Fragment mediaTabFragment = mTabsAdapter.getActiveFragment(1);
			// Fragment mediaTabFragment = getSupportFragmentManager()
			// .findFragmentByTag(FragmentTag.POINT_MEDIA);
			if ((mediaTabFragment != null)) {
				mediaTabFragment.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	@Override
	protected void onControlServiceConnected() {

		connectPhoton();
		PointMediaTabFragment mediaTabFragment = (PointMediaTabFragment) mTabsAdapter.getActiveFragment(1);
		// PointMediaTabFragment mediaTabFragment = (PointMediaTabFragment) getSupportFragmentManager()
		// .findFragmentByTag(FragmentTag.POINT_MEDIA);
		if ((mediaTabFragment != null)) {
			mediaTabFragment.onServiceConnected();
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_details);
		initFromIntentExtra(getIntent());
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		setubTabs();
		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString(EXTRA_KEY_TAB));
		} else if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			mTabHost.setCurrentTabByTag(FragmentTag.POINT_COMMENTS);
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {

		super.onSaveInstanceState(outState);
		outState.putString(EXTRA_KEY_TAB, mTabHost.getCurrentTabTag());
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

	private void setubTabs() {

		Bundle arguments = PointDescriptionTabFragment.intentToFragmentArguments(getIntent());
		mTabsAdapter.addTab(mTabHost.newTabSpec(FragmentTag.POINT_DESCRIPTION).setIndicator(
				getString(R.string.tab_title_description),
				getResources().getDrawable(R.drawable.ic_tab_description)),
				PointDescriptionTabFragment.class, arguments);
		if (!IntentAction.NEW.equals(getIntent().getAction())) {
			arguments = PointMediaTabFragment.intentToFragmentArguments(getIntent());
			mTabsAdapter.addTab(mTabHost.newTabSpec(FragmentTag.POINT_MEDIA).setIndicator(
					getString(R.string.tab_title_media),
					getResources().getDrawable(R.drawable.ic_tab_attachments)), PointMediaTabFragment.class,
					arguments);
			arguments = PointSourcesTabFragment.intentToFragmentArguments(getIntent());
			mTabsAdapter.addTab(mTabHost.newTabSpec(FragmentTag.POINT_SOURCE).setIndicator(
					getString(R.string.tab_title_source),
					getResources().getDrawable(R.drawable.ic_tab_sources)), PointSourcesTabFragment.class,
					arguments);
			arguments = PointCommentsTabFragment.intentToFragmentArguments(getIntent());
			mTabsAdapter.addTab(mTabHost.newTabSpec(FragmentTag.POINT_COMMENTS).setIndicator(
					getString(R.string.tab_title_comments),
					getResources().getDrawable(R.drawable.ic_tab_comments)), PointCommentsTabFragment.class,
					arguments);
		}
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
	public static class TabsAdapter extends FragmentPagerAdapter implements TabHost.OnTabChangeListener,
			ViewPager.OnPageChangeListener {

		private final Context mContext;
		private final FragmentManager mFragmentManager;
		private final TabHost mTabHost;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
		private final ViewPager mViewPager;

		public TabsAdapter(final FragmentActivity activity, final TabHost tabHost, final ViewPager pager) {

			super(activity.getSupportFragmentManager());
			mFragmentManager = activity.getSupportFragmentManager();
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		private static String makeFragmentName(final int viewId, final int index) {

			return "android:switcher:" + viewId + ":" + index;
		}

		public void addTab(final TabHost.TabSpec tabSpec, final Class<?> clss, final Bundle args) {

			tabSpec.setContent(new DummyTabFactory(mContext));
			String tag = tabSpec.getTag();
			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		public Fragment getActiveFragment(final int position) {

			String name = makeFragmentName(mViewPager.getId(), position);
			return mFragmentManager.findFragmentByTag(name);
		}

		@Override
		public int getCount() {

			return mTabs.size();
		}

		@Override
		public Fragment getItem(final int position) {

			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(), info.args);
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

			// Unfortunately when TabHost changes the current tab, it kindly
			// also takes care of putting focus on it when not in touch mode.
			// The jerk.
			// This hack tries to prevent this from pulling focus out of our
			// ViewPager.
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);
		}

		@Override
		public void onTabChanged(final String tabId) {

			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {

			private final Context mContext;

			public DummyTabFactory(final Context context) {

				mContext = context;
			}

			@Override
			public View createTabContent(final String tag) {

				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
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
