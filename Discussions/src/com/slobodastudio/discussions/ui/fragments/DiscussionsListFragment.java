package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.tools.MyLog;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

public class DiscussionsListFragment extends BaseListFragment {

	private static final String TAG = DiscussionsListFragment.class.getSimpleName();

	public DiscussionsListFragment() {

		super(R.string.fragment_empty_discussions, Discussions.Columns.SUBJECT);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		getAdapter().getCursor().moveToPosition(position);
		int valueIdIndex = getAdapter().getCursor().getColumnIndexOrThrow(Discussions.Columns.DISCUSSION_ID);
		int valueId = getAdapter().getCursor().getInt(valueIdIndex);
		MyLog.v(TAG, String.valueOf(valueId));
		showAssociatedTable(valueId);
	}

	private void showAssociatedTable(final int personId) {

		Intent intent = new Intent(Intent.ACTION_VIEW, Discussions.buildTopicUri(personId));
		startActivity(intent);
	}
}
