package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.ui.IntentExtrasKey;

import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class TopicsListFragment extends BaseListFragment {

	public TopicsListFragment() {

		super(R.string.fragment_empty_topics, Topics.Columns.NAME, Topics.Columns.ID, Topics.CONTENT_URI);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_points: {
				// Otherwise we need to launch a new activity to display
				// the dialog fragment with selected text.
				Uri uri = Topics.buildPointUri(getItemId(item));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return true;
			}
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		Uri uri = Topics.buildPointUri(getItemId(position));
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		if (getActivity().getIntent().hasExtra(IntentExtrasKey.PERSON_ID)) {
			intent.putExtra(IntentExtrasKey.PERSON_ID, getActivity().getIntent().getExtras().getInt(
					IntentExtrasKey.PERSON_ID));
		} else {
			throw new IllegalStateException("intent was without person id");
		}
		if (getActivity().getIntent().hasExtra(IntentExtrasKey.DISCUSSION_ID)) {
			intent.putExtra(IntentExtrasKey.DISCUSSION_ID, getActivity().getIntent().getExtras().getInt(
					IntentExtrasKey.DISCUSSION_ID));
		} else {
			throw new IllegalStateException("intent was without discussion id");
		}
		if (getActivity().getIntent().hasExtra(IntentExtrasKey.PERSON_NAME)) {
			intent.putExtra(IntentExtrasKey.PERSON_NAME, getActivity().getIntent().getExtras().getString(
					IntentExtrasKey.PERSON_NAME));
		} else {
			throw new IllegalStateException("intent was without person name");
		}
		intent.putExtra(IntentExtrasKey.TOPIC_ID, getItemId(position));
		startActivity(intent);
	}

	@Override
	protected BaseDetailFragment getDetailFragment() {

		return new TopicsDetailFragment();
	}
}
