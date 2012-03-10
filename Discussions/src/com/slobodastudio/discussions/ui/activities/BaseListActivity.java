package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.fragments.BaseListFragment;

import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public abstract class BaseListActivity extends BaseActivity {

	private static final String TAG = BaseListActivity.class.getSimpleName();

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_menu, menu);
		// Calling super after populating the menu is necessary here to ensure that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
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
				((BaseListFragment) mFragment).actionAdd();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
