package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;

public class PointsListFragment extends BaseListFragment {

	public PointsListFragment() {

		super(R.string.fragment_empty_points, Points.Columns.NAME, Points.Columns.ID, Points.CONTENT_URI);
	}

	@Override
	protected BaseDetailsFragment getDetailFragment() {

		return new PointsDetailsFragment();
	}
}
