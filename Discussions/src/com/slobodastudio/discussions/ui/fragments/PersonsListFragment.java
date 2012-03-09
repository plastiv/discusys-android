package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;

public class PersonsListFragment extends BaseListFragment {

	private static final String TAG = PersonsListFragment.class.getSimpleName();

	public PersonsListFragment() {

		super(R.string.fragment_empty_persons, Persons.Columns.NAME, Persons.Columns.ID, Persons.CONTENT_URI);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_points: {
				// Otherwise we need to launch a new activity to display
				// the dialog fragment with selected text.
				Uri uri = Persons.buildPointUri(getItemId(item));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return true;
			}
			case R.id.menu_topics: {
				// Otherwise we need to launch a new activity to display
				// the dialog fragment with selected text.
				Uri uri = Persons.buildTopicUri(getItemId(item));
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return true;
			}
			case R.id.menu_discussions: {
				// Otherwise we need to launch a new activity to display
				// the dialog fragment with selected text.
				Uri uri = Persons.buildDiscussionsUri(getItemId(item));
				Log.v(TAG, "Uri=" + uri);
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

		return new PersonsDetailFragment();
	}
}
