package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.TopicsListFragment;

import android.support.v4.app.Fragment;

public class TopicsActivity extends BaseListActivity {

	private static final String TAG = TopicsActivity.class.getSimpleName();

	@Override
	protected Fragment onCreatePane() {

		return new TopicsListFragment();
	}
}
