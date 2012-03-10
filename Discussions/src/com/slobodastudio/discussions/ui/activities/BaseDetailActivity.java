package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;

import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public abstract class BaseDetailActivity extends BaseActivity {

	private static final String TAG = BaseDetailActivity.class.getSimpleName();

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_details_menu, menu);
		// Calling super after populating the menu is necessary here to ensure that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_save:
				Toast.makeText(this, "Save", Toast.LENGTH_SHORT).show();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
