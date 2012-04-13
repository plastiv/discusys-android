package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.IntentAction;
import com.slobodastudio.discussions.ui.fragments.PointDetailFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;

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
	protected void onControlServiceConnected() {

		// No operation with service in this activity
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_details);
		FragmentManager fm = getSupportFragmentManager();
		boolean fragmentNotFound = fm.findFragmentById(R.id.frame_point_details) == null;
		if (DEBUG) {
			Log.d(TAG, "[onCreate] savedInstanceState: " + savedInstanceState + ", findFragmentById: "
					+ fragmentNotFound);
		}
		if (savedInstanceState == null) {
			// Create the list fragment and add it as our sole content.
			if (fragmentNotFound) {
				mFragment = new PointDetailFragment();
				mFragment.setArguments(PointDetailFragment.intentToFragmentArguments(getIntent()));
				fm.beginTransaction().add(R.id.frame_point_details, mFragment).commit();
			}
		} else {
			if (!fragmentNotFound) {
				mFragment = (PointDetailFragment) fm.findFragmentById(R.id.frame_point_details);
			} else {
				throw new IllegalStateException("fragment should be created here");
			}
		}
	}
}
