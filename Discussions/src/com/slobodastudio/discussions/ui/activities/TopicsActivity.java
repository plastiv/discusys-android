package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.TopicsListFragment;

import android.support.v4.app.Fragment;

public class TopicsActivity extends BaseActivity {

	private static final String TAG = TopicsActivity.class.getSimpleName();

	@Override
	protected void onControlServiceConnected() {

		// TODO Auto-generated method stub
	}

	@Override
	protected Fragment onCreatePane() {

		return new TopicsListFragment();
	}
}
