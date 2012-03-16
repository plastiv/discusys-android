package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.IntentExtrasKey;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class PointsListFragment extends BaseListFragment {

	private static final String TAG = PointsListFragment.class.getSimpleName();

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
		Log.d(TAG, "[actionAdd] intent: " + intent);
		startActivity(intent);
	}

	@Override
	public void actionEdit(final int position) {

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
		int valueId;
		if ((getAdapter().getCursor() != null) && getAdapter().getCursor().moveToPosition(position)) {
			int valueIdIndex = getAdapter().getCursor().getColumnIndexOrThrow(mColumnId);
			valueId = getAdapter().getCursor().getInt(valueIdIndex);
		} else {
			throw new IllegalArgumentException("Cant get value id for position: " + position);
		}
		intent.putExtra(IntentExtrasKey.ID, valueId);
		Log.d(TAG, "[actionEdit] id: " + valueId + ", intent: " + intent);
		startActivity(intent);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		actionEdit(position);
	}

	@Override
	protected BaseDetailFragment getDetailFragment() {

		return new PointsDetailFragment();
	}
}
