package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;

import android.os.Bundle;

import com.actionbarsherlock.view.MenuItem;

public class DiscussionInfoActivity extends BaseActivity {

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
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
		setContentView(R.layout.activity_discussion_description);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
	}
}