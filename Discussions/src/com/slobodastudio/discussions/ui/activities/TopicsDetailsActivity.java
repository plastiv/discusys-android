package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.BaseDetailFragment;
import com.slobodastudio.discussions.ui.fragments.TopicsDetailFragment;

import android.net.Uri;
import android.support.v4.app.Fragment;

public class TopicsDetailsActivity extends BaseDetailActivity {

	@Override
	protected Fragment onCreatePane() {

		BaseDetailFragment fragment = new TopicsDetailFragment();
		Uri uri = getIntent().getData();
		String id = uri.getLastPathSegment();
		fragment.setArgumentId(Integer.valueOf(id));
		return fragment;
	}
}
