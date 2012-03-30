package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.fragments.TopicsListFragment;

import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TopicsActivity extends BaseActivity {

	private static final String TAG = TopicsActivity.class.getSimpleName();

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_menu_refresh, menu);
		// Calling super after populating the menu is necessary here to ensure that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
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
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onControlServiceConnected() {

		// TODO Auto-generated method stub
	}

	@Override
	protected Fragment onCreatePane() {

		return new TopicsListFragment();
	}
}
