package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.ExtraKey;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class UserPointListFragment extends SherlockListFragment {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = UserPointListFragment.class.getSimpleName();
	private int mDiscussionId;
	private int mPersonId;
	private int mTopicId;
	private SimpleCursorAdapter mUserPointsAdapter;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		initFromIntentExtra();
		addListHeader();
		// Create an empty adapter we will use to display the loaded data.
		mUserPointsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_base, null,
				new String[] { Points.Columns.NAME, Points.Columns.ID }, new int[] { R.id.list_item_text,
						R.id.image_person_color }, 0);
		mUserPointsAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

				int viewId = view.getId();
				switch (viewId) {
					case R.id.image_person_color:
						ImageView colorView = (ImageView) view;
						int color = getActivity().getIntent().getExtras().getInt(ExtraKey.PERSON_COLOR);
						colorView.setBackgroundColor(color);
						return true;
					case R.id.list_item_text:
						TextView itemText = (TextView) view;
						itemText.setText(cursor.getString(columnIndex));
						return true;
					default:
						return false;
				}
			}
		});
		setListAdapter(mUserPointsAdapter);
		// Prepare the loader. Either re-connect with an existing one, or start a new one.
		getLoaderManager().initLoader(UserPointsCursorLoader.LOADER_USER_POINTS_ID, null,
				new UserPointsCursorLoader());
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		onActionEdit(position);
	}

	private void addListHeader() {

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View headerView = layoutInflater.inflate(R.layout.view_point_list_header, null, false);
		TextView mPointListTitleTextView = (TextView) headerView.findViewById(R.id.points_listview_header);
		mPointListTitleTextView.setText(R.string.text_current_user_points);
		getListView().addHeaderView(headerView);
	}

	private Intent createEditPointIntent(final int pointId) {

		Intent intent = new Intent(Intent.ACTION_EDIT, Points.buildTableUri(pointId));
		intent.putExtra(ExtraKey.DISCUSSION_ID, mDiscussionId);
		intent.putExtra(ExtraKey.POINT_ID, pointId);
		intent.putExtra(ExtraKey.PERSON_ID, mPersonId);
		intent.putExtra(ExtraKey.TOPIC_ID, mTopicId);
		return intent;
	}

	private void initFromIntentExtra() {

		if (!getActivity().getIntent().hasExtra(ExtraKey.PERSON_ID)) {
			throw new IllegalStateException("Activity intent was without person id");
		}
		if (!getActivity().getIntent().hasExtra(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("Activity intent was without topic id");
		}
		if (!getActivity().getIntent().hasExtra(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalStateException("Activity intent was without discussion id");
		}
		mDiscussionId = getActivity().getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID);
		mPersonId = getActivity().getIntent().getExtras().getInt(ExtraKey.PERSON_ID);
		mTopicId = getActivity().getIntent().getExtras().getInt(ExtraKey.TOPIC_ID);
		if (DEBUG) {
			Log.d(TAG, "[initFromIntentExtras] personId: " + mPersonId + ", topicId: " + mTopicId);
		}
	}

	private void onActionEdit(final int position) {

		// position - 1 because of header
		if ((mUserPointsAdapter.getCursor() != null)
				&& mUserPointsAdapter.getCursor().moveToPosition(position - 1)) {
			int valueIdIndex = mUserPointsAdapter.getCursor().getColumnIndexOrThrow(Points.Columns.ID);
			int pointId = mUserPointsAdapter.getCursor().getInt(valueIdIndex);
			Intent intent = createEditPointIntent(pointId);
			startActivity(intent);
		}
	}

	private class UserPointsCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {

		private static final int LOADER_USER_POINTS_ID = 0;

		@Override
		public Loader<Cursor> onCreateLoader(final int id, final Bundle arguments) {

			switch (id) {
				case LOADER_USER_POINTS_ID: {
					String where = Points.Columns.TOPIC_ID + "=? AND " + Points.Columns.PERSON_ID + "=? ";
					String[] args = { String.valueOf(mTopicId), String.valueOf(mPersonId) };
					String sortOrder = BaseColumns._ID + " DESC";
					return new CursorLoader(getActivity(), Points.CONTENT_URI, null, where, args, sortOrder);
				}
				default:
					throw new IllegalArgumentException("Unknown loader id: " + id);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case LOADER_USER_POINTS_ID:
					mUserPointsAdapter.swapCursor(null);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			switch (loader.getId()) {
				case LOADER_USER_POINTS_ID:
					mUserPointsAdapter.swapCursor(data);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}
	}
}
