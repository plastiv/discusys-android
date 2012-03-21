package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.ui.IntentExtrasKey;

import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class DiscussionsListFragment extends BaseListFragment {

	public DiscussionsListFragment() {

		super(R.string.fragment_empty_discussions, Discussions.Columns.SUBJECT, Discussions.Columns.ID,
				Discussions.CONTENT_URI);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_topics: {
				// Otherwise we need to launch a new activity to display
				// the dialog fragment with selected text.
				Uri uri = Discussions.buildTopicUri(getItemId(item));
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
		Uri uri = Discussions.buildTopicUri(getItemId(position));
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		if (getActivity().getIntent().hasExtra(IntentExtrasKey.PERSON_ID)) {
			intent.putExtra(IntentExtrasKey.PERSON_ID, getActivity().getIntent().getExtras().getInt(
					IntentExtrasKey.PERSON_ID));
		} else {
			throw new IllegalStateException("intent was without person id");
		}
		intent.putExtra(IntentExtrasKey.DISCUSSION_ID, getItemId(position));
		startActivity(intent);
	}

	@Override
	protected BaseDetailFragment getDetailFragment() {

		return new DiscussionsDetailFragment();
	}
}
