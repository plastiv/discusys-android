package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;

import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;

public class DiscussionsListFragment extends BaseListFragment {

	private static final String TAG = DiscussionsListFragment.class.getSimpleName();

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
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected BaseDetailsFragment getDetailFragment() {

		return new DiscussionsDetailFragment();
	}
}
