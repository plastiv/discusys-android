package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.fragments.PointDetailFragment;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PointDetailsActivity extends BaseActivity {

	private static final String TAG = PointDetailsActivity.class.getSimpleName();

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
			menuInflater.inflate(R.menu.actionbar_point_view, menu);
			return super.onCreateOptionsMenu(menu);
		}
		if (getIntent().getAction().equals(Intent.ACTION_EDIT)) {
			menuInflater.inflate(R.menu.actionbar_point_edit, menu);
			return super.onCreateOptionsMenu(menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

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
			case R.id.menu_delete:
				((PointDetailFragment) mFragment).onActionDelete();
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onControlServiceConnected() {

		// TODO Auto-generated method stub
	}

	@Override
	protected Fragment onCreatePane() {

		PointDetailFragment fragment = new PointDetailFragment();
		fragment.setArguments(PointDetailFragment.intentToFragmentArguments(getIntent()));
		return fragment;
	}
}
