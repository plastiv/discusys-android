package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.IntentExtrasKey;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

public class PointsListFragment extends BaseListFragment {

	public PointsListFragment() {

		super(R.string.fragment_empty_points, Points.Columns.NAME, Points.Columns.ID, Points.CONTENT_URI);
	}

	@Override
	public void actionAdd() {

		Intent intent = new Intent(Intent.ACTION_EDIT, Points.CONTENT_URI);
		if (getActivity().getIntent().hasExtra(IntentExtrasKey.PERSON_ID)) {
			intent.putExtra(IntentExtrasKey.PERSON_ID, getActivity().getIntent().getExtras().getInt(
					IntentExtrasKey.PERSON_ID));
		} else {
			throw new IllegalStateException("intent was without person id");
		}
		if (getActivity().getIntent().hasExtra(IntentExtrasKey.TOPIC_ID)) {
			intent.putExtra(IntentExtrasKey.TOPIC_ID, getActivity().getIntent().getExtras().getInt(
					IntentExtrasKey.TOPIC_ID));
		} else {
			throw new IllegalStateException("intent was without topic id");
		}
		startActivity(intent);
	}

	@Override
	public void actionEdit(final int valueId) {

		Intent intent = new Intent(Intent.ACTION_EDIT, Points.CONTENT_URI);
		if (getActivity().getIntent().hasExtra(IntentExtrasKey.PERSON_ID)) {
			intent.putExtra(IntentExtrasKey.PERSON_ID, getActivity().getIntent().getExtras().getInt(
					IntentExtrasKey.PERSON_ID));
		} else {
			throw new IllegalStateException("intent was without person id");
		}
		if (getActivity().getIntent().hasExtra(IntentExtrasKey.TOPIC_ID)) {
			intent.putExtra(IntentExtrasKey.TOPIC_ID, getActivity().getIntent().getExtras().getInt(
					IntentExtrasKey.TOPIC_ID));
		} else {
			throw new IllegalStateException("intent was without topic id");
		}
		intent.putExtra(IntentExtrasKey.ID, valueId);
		startActivity(intent);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		showDetails(position);
	}

	@Override
	protected BaseDetailFragment getDetailFragment() {

		return new PointsDetailFragment();
	}
}
