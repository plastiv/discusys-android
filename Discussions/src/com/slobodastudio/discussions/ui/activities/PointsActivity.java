package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.PointsListFragment;

import android.support.v4.app.Fragment;

public class PointsActivity extends BasePanelActivity {

	@Override
	protected Fragment onCreatePane() {

		return new PointsListFragment();
	}
}
