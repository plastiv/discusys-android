package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;

public class PointsListFragment extends BaseListFragment {

	private static final String TAG = PointsListFragment.class.getSimpleName();

	public PointsListFragment() {

		super(R.string.fragment_empty_points, Points.Columns.NAME);
	}
}
