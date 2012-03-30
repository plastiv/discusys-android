package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.BaseDetailFragment;
import com.slobodastudio.discussions.ui.fragments.DiscussionsDetailFragment;

import android.net.Uri;
import android.support.v4.app.Fragment;

public abstract class DiscussionsDetailsActivity extends BaseDetailActivity {

	@Override
	protected Fragment onCreatePane() {

		BaseDetailFragment fragment = new DiscussionsDetailFragment();
		Uri uri = getIntent().getData();
		String id = uri.getLastPathSegment();
		fragment.setArgumentId(Integer.valueOf(id));
		return fragment;
	}
}
