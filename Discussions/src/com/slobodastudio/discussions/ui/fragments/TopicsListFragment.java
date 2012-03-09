package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;

import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;

public class TopicsListFragment extends BaseListFragment {

	private static final String TAG = TopicsListFragment.class.getSimpleName();

	public TopicsListFragment() {

		super(R.string.fragment_empty_topics, Topics.Columns.NAME, Topics.Columns.ID, Topics.CONTENT_URI);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_points:
				// Otherwise we need to launch a new activity to display
				// the dialog fragment with selected text.
				Uri uri = Topics.buildPointUri(getItemId(item));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected BaseDetailsFragment getDetailFragment() {

		return new TopicsDetailFragment();
	}
}
