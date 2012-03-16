package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.IntentExtrasKey;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PointsFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final boolean DEBUG = true && ApplicationConstants.DEBUG_MODE;
	private static final int LOADER_OTHER_POINTS_ID = 1;
	private static final int LOADER_USER_POINTS_ID = 0;
	private static final String TAG = PointsFragment.class.getSimpleName();
	private static final Uri URI = Points.CONTENT_URI;
	private final String mColumnName = Points.Columns.NAME;
	private SimpleCursorAdapter mOtherPointsAdapter;
	private ListView mOtherPointsList;
	private SimpleCursorAdapter mUserPointsAdapter;
	private ListView mUserPointsList;
	private int personId;
	private int topicId;

	public void onActionNew() {

		Intent intent = new Intent(Intent.ACTION_EDIT, Points.CONTENT_URI);
		intent.putExtra(IntentExtrasKey.PERSON_ID, personId);
		intent.putExtra(IntentExtrasKey.TOPIC_ID, topicId);
		startActivity(intent);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		if (DEBUG) {
			Log.d(TAG, "[onActivityCreared] saved state: " + savedInstanceState);
		}
		initFromIntentExtra();
		registerForContextMenu(mUserPointsList);
		registerForContextMenu(mOtherPointsList);
		// Create an empty adapter we will use to display the loaded data.
		mUserPointsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.simple_list_item, null,
				new String[] { mColumnName }, new int[] { R.id.list_item_text }, 0);
		mUserPointsList.setAdapter(mUserPointsAdapter);
		// Create an empty adapter we will use to display the loaded data.
		mOtherPointsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.simple_list_item, null,
				new String[] { mColumnName }, new int[] { R.id.list_item_text }, 0);
		mOtherPointsList.setAdapter(mOtherPointsAdapter);
		// set up click listener
		mUserPointsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> arg0, final View v, final int position, final long id) {

				onActionEdit(id);
			}
		});
		// set up click listener
		mOtherPointsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> arg0, final View v, final int position, final long id) {

				onActionView(id);
			}
		});
		// Prepare the loader. Either re-connect with an existing one, or start a new one.
		getLoaderManager().initLoader(LOADER_USER_POINTS_ID, null, this);
		getLoaderManager().initLoader(LOADER_OTHER_POINTS_ID, null, this);
		// TODO: Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle arguments) {

		// This is called when a new Loader needs to be created.
		// First, pick the base URI to use depending on whether we are currently filtering.
		// Now create and return a CursorLoader that will take care of creating a Cursor for the data being
		// displayed.
		switch (id) {
			case LOADER_USER_POINTS_ID: {
				String where = Points.Columns.TOPIC_ID + "=? AND " + Points.Columns.PERSON_ID + "=? ";
				String[] args = { String.valueOf(topicId), String.valueOf(personId) };
				String sortOrder = Points.Columns.ID + " DESC";
				return new CursorLoader(getActivity(), URI, null, where, args, sortOrder);
			}
			case LOADER_OTHER_POINTS_ID: {
				String where = Points.Columns.TOPIC_ID + "=? AND " + Points.Columns.PERSON_ID + "!=? ";
				String[] args = { String.valueOf(topicId), String.valueOf(personId) };
				String sortOrder = Points.Columns.ID + " DESC";
				return new CursorLoader(getActivity(), URI, null, where, args, sortOrder);
			}
			default:
				throw new IllegalArgumentException("Unknown loader id: " + id);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.points_list, null);
		mUserPointsList = (ListView) layout.findViewById(R.id.user_points_listview);
		mOtherPointsList = (ListView) layout.findViewById(R.id.other_user_points_listview);
		TextView empty = (TextView) inflater.inflate(R.layout.empty_list, null);
		mUserPointsList.setEmptyView(empty);
		mOtherPointsList.setEmptyView(empty);
		return layout;
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {

		switch (loader.getId()) {
			case LOADER_USER_POINTS_ID:
				mUserPointsAdapter.swapCursor(null);
				break;
			case LOADER_OTHER_POINTS_ID:
				mOtherPointsAdapter.swapCursor(null);
				break;
			default:
				throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
		}
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

		if (DEBUG) {
			Log.d(TAG, "[onLoadFinished] cursor count: " + data.getCount());
		}
		switch (loader.getId()) {
			case LOADER_USER_POINTS_ID:
				mUserPointsAdapter.swapCursor(data);
				mUserPointsList.invalidate();
				break;
			case LOADER_OTHER_POINTS_ID:
				mOtherPointsAdapter.swapCursor(data);
				break;
			default:
				throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
		}
	}

	void onActionEdit(final long id) {

		Intent intent = new Intent(Intent.ACTION_EDIT, Points.buildTableUri(id));
		startActivity(intent);
	}

	void onActionView(final long id) {

		Intent intent = new Intent(Intent.ACTION_VIEW, Points.buildTableUri(id));
		startActivity(intent);
	}

	private void initFromIntentExtra() {

		if (!getActivity().getIntent().hasExtra(IntentExtrasKey.PERSON_ID)) {
			throw new IllegalStateException("Activity intent was without person id");
		}
		if (!getActivity().getIntent().hasExtra(IntentExtrasKey.TOPIC_ID)) {
			throw new IllegalStateException("Activity intent was without topic id");
		}
		personId = getActivity().getIntent().getExtras().getInt(IntentExtrasKey.PERSON_ID);
		topicId = getActivity().getIntent().getExtras().getInt(IntentExtrasKey.TOPIC_ID);
		if (DEBUG) {
			Log.d(TAG, "[initFromIntentExtras] personId: " + personId + ", topicId: " + topicId);
		}
	}
}