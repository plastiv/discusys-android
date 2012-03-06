package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.utils.MyLog;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

public class TopicsListFragment extends BaseListFragment {

	private static final String TAG = TopicsListFragment.class.getSimpleName();

	public TopicsListFragment() {

		super(R.string.fragment_empty_topics, Topics.Columns.NAME);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		getAdapter().getCursor().moveToPosition(position);
		int valueIdIndex = getAdapter().getCursor().getColumnIndexOrThrow(Topics.Columns.ID);
		int valueId = getAdapter().getCursor().getInt(valueIdIndex);
		MyLog.v(TAG, String.valueOf(valueId));
		showAssociatedTable(valueId);
	}

	private void showAssociatedTable(final int valueId) {

		Intent intent = new Intent(Intent.ACTION_VIEW, Topics.buildPointUri(valueId));
		startActivity(intent);
	}
}
