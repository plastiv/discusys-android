package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.fragments.PointsFragment;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.view.MenuItem;

public class PointsActivity extends BaseListActivity {

	private static final String TAG = PointsActivity.class.getSimpleName();

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		Log.d(TAG, "[onOptionsItemSelected] item id: " + item.getItemId());
		switch (item.getItemId()) {
			case R.id.menu_new:
				((PointsFragment) mFragment).onActionNew();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Fragment onCreatePane() {

		return new PointsFragment();
	}
}
