package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.DiscussionsListFragment;

import android.support.v4.app.Fragment;

public class DiscussionsActivity extends BaseActivity {

	private static final String TAG = DiscussionsActivity.class.getSimpleName();

	@Override
	protected void onControlServiceConnected() {

		// TODO Auto-generated method stub
	}

	@Override
	protected Fragment onCreatePane() {

		return new DiscussionsListFragment();
	}
}
