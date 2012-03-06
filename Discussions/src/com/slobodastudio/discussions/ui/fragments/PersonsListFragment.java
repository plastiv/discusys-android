package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.tools.MyLog;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

public class PersonsListFragment extends BaseListFragment {

	private static final String TAG = PersonsListFragment.class.getSimpleName();

	public PersonsListFragment() {

		super(R.string.fragment_empty_persons, Persons.Columns.NAME);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		getAdapter().getCursor().moveToPosition(position);
		int personIdIndex = getAdapter().getCursor().getColumnIndexOrThrow(Persons.Columns.ID);
		int personId = getAdapter().getCursor().getInt(personIdIndex);
		MyLog.v(TAG, String.valueOf(personId));
		showAssociatedDiscsussions(personId);
	}

	private void showAssociatedDiscsussions(final int personId) {

		Intent intent = new Intent(Intent.ACTION_VIEW, Persons.buildDiscussionsUri(personId));
		startActivity(intent);
	}
}
