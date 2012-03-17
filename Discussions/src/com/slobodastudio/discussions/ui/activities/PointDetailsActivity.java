package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.fragments.PointDetailFragment;

import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.MenuItem;

public class PointDetailsActivity extends BaseDetailActivity {

	private static final String TAG = PointDetailsActivity.class.getSimpleName();

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_save:
				((PointDetailFragment) mFragment).onActionSave();
				finish();
				return true;
			case R.id.menu_cancel:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Fragment onCreatePane() {

		PointDetailFragment fragment = new PointDetailFragment();
		fragment.setArguments(PointDetailFragment.intentToFragmentArguments(getIntent()));
		return fragment;
	}
}
