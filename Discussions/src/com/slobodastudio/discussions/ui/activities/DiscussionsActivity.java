package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.DiscussionsListFragment;

import android.support.v4.app.Fragment;

public class DiscussionsActivity extends BaseListActivity {

	private static final String TAG = DiscussionsActivity.class.getSimpleName();

	@Override
	protected Fragment onCreatePane() {

		return new DiscussionsListFragment();
	}
}
