package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Discussion;
import com.slobodastudio.discussions.data.model.Value;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.ui.activities.base.BaseDetailFragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DiscussionsDetailFragment extends BaseDetailFragment {

	public DiscussionsDetailFragment() {

		super(Discussions.CONTENT_URI);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		if (container == null) {
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.
			return null;
		}
		TextView text = (TextView) inflater.inflate(R.layout.details_item, null);
		if (getShownId() == BaseDetailFragment.NO_SELECTION_ID) {
			// TODO: move string to resources
			text.setText("Select item to show details");
		} else {
			Cursor cursor = getActivity().getContentResolver().query(getDetailsUri(), null, null, null, null);
			Value value = new Discussion(cursor);
			text.setText(value.toMyString());
		}
		return text;
	}
}
