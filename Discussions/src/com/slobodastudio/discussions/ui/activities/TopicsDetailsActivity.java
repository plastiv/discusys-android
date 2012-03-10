package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.activities.base.BaseActivity;

import android.support.v4.app.Fragment;

public class TopicsDetailsActivity extends BaseActivity {

	@Override
	protected Fragment onCreatePane() {

		return new TopicsDetailFragment();
	}
}
