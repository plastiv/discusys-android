package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.tools.MyLog;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class BaseListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	// This is the Adapter being used to display the list's data.
	SimpleCursorAdapter mAdapter;
	int mEmptyListStringId;
	String mShownColumnName;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		// Give some text to display if there is no data. In a real
		// application this would come from a resource.
		setTableSpecificValues(getActivity().getIntent().getData());
		String emptyText = getResources().getString(mEmptyListStringId);
		setEmptyText(emptyText);
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);
		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
				new String[] { mShownColumnName }, new int[] { android.R.id.text1 }, 0);
		setListAdapter(mAdapter);
		// Start out with a progress indicator.
		setListShown(false);
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		MyLog.v("Fragment", (String) item.getTitle());
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info;
		try {
			// Casts the incoming data object into the type for AdapterView objects.
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			// If the menu object can't be cast, logs an error.
			throw new RuntimeException("bad menuInfo: " + menuInfo, e);
		}
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}
		int columnIndex = cursor.getColumnIndexOrThrow(mShownColumnName);
		menu.setHeaderTitle(cursor.getString(columnIndex));// if your table name is name
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.list_context_menu, menu);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		Uri baseUri = getActivity().getIntent().getData();
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(getActivity(), baseUri, null, null, null, null);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		// Insert desired behavior here.
		Log.i("FragmentComplexList", "Item clicked: " + id);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {

		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mAdapter.swapCursor(data);
		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	private void setTableSpecificValues(final Uri uri) {

		String mimeType = getActivity().getContentResolver().getType(uri);
		if (mimeType.equals(Persons.CONTENT_DIR_TYPE)) {
			mEmptyListStringId = R.string.fragment_empty_persons;
			mShownColumnName = Persons.Columns.NAME;
		} else if (mimeType.equals(Discussions.CONTENT_DIR_TYPE)) {
			mEmptyListStringId = R.string.fragment_empty_discussions;
			mShownColumnName = Discussions.Columns.SUBJECT;
		} else if (mimeType.equals(Topics.CONTENT_DIR_TYPE)) {
			mEmptyListStringId = R.string.fragment_empty_topics;
			mShownColumnName = Topics.Columns.NAME;
		} else if (mimeType.equals(Points.CONTENT_DIR_TYPE)) {
			mEmptyListStringId = R.string.fragment_empty_points;
			mShownColumnName = Points.Columns.POINT_NAME;
		} else {
			throw new IllegalAccessError("Unknown uri: " + uri);
		}
	}
}
