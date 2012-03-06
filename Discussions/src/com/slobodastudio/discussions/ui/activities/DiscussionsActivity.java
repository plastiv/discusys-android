package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.DiscussionsListFragment;

import android.support.v4.app.Fragment;

public class DiscussionsActivity extends BasePanelActivity {

	@Override
	protected Fragment onCreatePane() {

		return new DiscussionsListFragment();
	}
}
