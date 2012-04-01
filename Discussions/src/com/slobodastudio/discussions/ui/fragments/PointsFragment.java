package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.IntentExtrasKey;
import com.slobodastudio.discussions.ui.activities.BaseActivity;
import com.slobodastudio.discussions.utils.MyLog;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PointsFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String EXTRA_POSITION = "extra_key_position";
	private static final String EXTRA_SELECTED = "extra_key_selected_list_id";
	private static final int LOADER_OTHER_POINTS_ID = 1;
	private static final int LOADER_USER_POINTS_ID = 0;
	private static final int SELECTED_NONE = 0;
	private static final int SELECTED_OTHERS = 2;
	private static final int SELECTED_USERS = 1;
	private static final String TAG = PointsFragment.class.getSimpleName();
	private static final Uri URI = Points.CONTENT_URI;
	private ActionMode mActionMode;
	private final String mColumnId = Points.Columns.ID;
	private final String mColumnName = Points.Columns.NAME;
	private int mCurPosition;
	private boolean mDualPane;
	private SimpleCursorAdapter mOtherPointsAdapter;
	private ListView mOtherPointsList;
	private int mSelectedList = SELECTED_NONE;
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

				mSelectedList = SELECTED_USERS;
				mCurPosition = position;
				mUserPointsList.setItemChecked(position, true);
				mOtherPointsList.clearChoices();
				onActionEdit(id, position);
			}
		});
		// set up click listener
		mOtherPointsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> arg0, final View v, final int position, final long id) {

				mSelectedList = SELECTED_OTHERS;
				mCurPosition = position;
				mOtherPointsList.setItemChecked(position, true);
				mUserPointsList.clearChoices();
				if (mActionMode != null) {
					mActionMode.finish();
				}
				onActionView(id, position);
			}
		});
		// Prepare the loader. Either re-connect with an existing one, or start a new one.
		getLoaderManager().initLoader(LOADER_USER_POINTS_ID, null, this);
		getLoaderManager().initLoader(LOADER_OTHER_POINTS_ID, null, this);
		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View detailsFrame = getActivity().findViewById(R.id.frame_layout_details);
		mDualPane = (detailsFrame != null) && (detailsFrame.getVisibility() == View.VISIBLE);
		if (savedInstanceState != null) {
			// Restore last state for checked position in correct list
			mCurPosition = savedInstanceState.getInt(EXTRA_POSITION, 0);
			mSelectedList = savedInstanceState.getInt(EXTRA_SELECTED, SELECTED_NONE);
		}
		if (mDualPane) {
			// In dual-pane mode, the list view highlights the selected item.
			mUserPointsList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
			mOtherPointsList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
			// TODO: Make sure our UI is in the correct state.
			if (mSelectedList != SELECTED_NONE) {
				if (mSelectedList == SELECTED_USERS) {
					mUserPointsList.setItemChecked(mCurPosition, true);
				} else if (mSelectedList == SELECTED_OTHERS) {
					mOtherPointsList.setItemChecked(mCurPosition, true);
				} else {
					throw new IllegalStateException("Unknown selected list id: " + mSelectedList);
				}
			}
			// show empty details "select point to see"
			showEmtyDetails();
		}
	}

	@Override
	public boolean onContextItemSelected(final android.view.MenuItem item) {

		MyLog.v(TAG, (String) item.getTitle());
		switch (item.getItemId()) {
			case R.id.menu_delete:
				int pointId = getItemId(item);
				((BaseActivity) getActivity()).getServiceHelper().deletePoint(pointId);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.equals(mUserPointsList)) {
			AdapterView.AdapterContextMenuInfo info;
			try {
				// Casts the incoming data object into the type for AdapterView objects.
				info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			} catch (ClassCastException e) {
				// If the menu object can't be cast, logs an error.
				throw new RuntimeException("bad menuInfo: " + menuInfo, e);
			}
			Cursor cursor = (Cursor) mUserPointsAdapter.getItem(info.position);
			if (cursor == null) {
				// For some reason the requested item isn't available, do nothing
				return;
			}
			int columnIndex = cursor.getColumnIndexOrThrow(mColumnName);
			menu.setHeaderTitle(cursor.getString(columnIndex));// if your table name is name
			android.view.MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.list_context_delete_menu, menu);
		}
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
		mUserPointsList.setEmptyView(layout.findViewById(R.id.user_points_listview_empty));
		mOtherPointsList.setEmptyView(layout.findViewById(R.id.other_user_points_listview_empty));
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
			Log.d(TAG, "[onLoadFinished] cursor count: " + data.getCount() + ", id: " + loader.getId());
		}
		switch (loader.getId()) {
			case LOADER_USER_POINTS_ID:
				mUserPointsAdapter.swapCursor(data);
				break;
			case LOADER_OTHER_POINTS_ID:
				mOtherPointsAdapter.swapCursor(data);
				break;
			default:
				throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {

		super.onSaveInstanceState(outState);
		outState.putInt(EXTRA_POSITION, mCurPosition);
		outState.putInt(EXTRA_SELECTED, mSelectedList);
	}

	void onActionEdit(final long id, final int position) {

		if (mDualPane) {
			// We can display everything in-place with fragments
			// Check what fragment is currently shown, replace if needed.
			int valueId;
			if ((mUserPointsAdapter.getCursor() != null)
					&& mUserPointsAdapter.getCursor().moveToPosition(position)) {
				int valueIdIndex = mUserPointsAdapter.getCursor().getColumnIndexOrThrow(mColumnId);
				valueId = mUserPointsAdapter.getCursor().getInt(valueIdIndex);
			} else {
				valueId = PointDetailFragment.INVALID_POINT_ID;
				return;
			}
			PointDetailFragment details = (PointDetailFragment) getFragmentManager().findFragmentById(
					R.id.frame_layout_details);
			if ((details == null) || (details.getPointId() != valueId)) {
				// Make new fragment to show this selection.
				details = new PointDetailFragment();
				Intent intent = new Intent(Intent.ACTION_EDIT, Points.buildTableUri(id));
				details.setArguments(PointDetailFragment.intentToFragmentArguments(intent));
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_layout_details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
			mActionMode = getSherlockActivity().startActionMode(new EditDetailsActionMode());
		} else {
			// Otherwise we need to launch a new activity to display details
			Intent intent = new Intent(Intent.ACTION_EDIT, Points.buildTableUri(id));
			startActivity(intent);
		}
	}

	void onActionView(final long id, final int position) {

		if (mDualPane) {
			// We can display everything in-place with fragments
			// Check what fragment is currently shown, replace if needed.
			int valueId;
			if ((mOtherPointsAdapter.getCursor() != null)
					&& mOtherPointsAdapter.getCursor().moveToPosition(position)) {
				int valueIdIndex = mOtherPointsAdapter.getCursor().getColumnIndexOrThrow(mColumnId);
				valueId = mOtherPointsAdapter.getCursor().getInt(valueIdIndex);
			} else {
				valueId = PointDetailFragment.INVALID_POINT_ID;
				return;
			}
			PointDetailFragment details = (PointDetailFragment) getFragmentManager().findFragmentById(
					R.id.frame_layout_details);
			if ((details == null) || (details.getPointId() != valueId)) {
				// Make new fragment to show this selection.
				details = new PointDetailFragment();
				Intent intent = new Intent(Intent.ACTION_VIEW, Points.buildTableUri(id));
				details.setArguments(PointDetailFragment.intentToFragmentArguments(intent));
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_layout_details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
		} else {
			// Otherwise we need to launch a new activity to display details
			Intent intent = new Intent(Intent.ACTION_VIEW, Points.buildTableUri(id));
			startActivity(intent);
		}
	}

	protected int getItemId(final android.view.MenuItem item) {

		AdapterView.AdapterContextMenuInfo info;
		try {
			// Casts the incoming data object into the type for AdapterView objects.
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			// If the menu object can't be cast, logs an error.
			throw new RuntimeException("bad menuInfo: " + item.getMenuInfo(), e);
		}
		Cursor cursor = (Cursor) mUserPointsAdapter.getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			throw new NullPointerException("Cursor was null, cant get a value id");
		}
		int columnIndex = cursor.getColumnIndexOrThrow(mColumnId);
		return cursor.getInt(columnIndex);
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

	private void showEmtyDetails() {

		PointDetailFragment details = (PointDetailFragment) getFragmentManager().findFragmentById(
				R.id.frame_layout_details);
		// Make new fragment to show this selection.
		details = new PointDetailFragment();
		details.setEmpty(true);
		// Execute a transaction, replacing any existing fragment
		// with this one inside the frame.
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.frame_layout_details, details);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	private final class EditDetailsActionMode implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {

			switch (item.getItemId()) {
				case R.id.menu_save:
					((PointDetailFragment) getFragmentManager().findFragmentById(R.id.frame_layout_details))
							.onActionSave();
					mode.finish();
					return true;
				case R.id.menu_cancel:
					((PointDetailFragment) getFragmentManager().findFragmentById(R.id.frame_layout_details))
							.onActionCancel();
					showEmtyDetails();
					mUserPointsList.clearChoices();
					mode.finish();
					return true;
				default:
					throw new IllegalArgumentException("Unknown menuitem id: " + item.getItemId());
			}
		}

		@Override
		public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {

			mode.setTitle(R.string.action_mode_name_points);
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.actionbar_details_menu, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(final ActionMode mode) {

			mActionMode = null;
		}

		@Override
		public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {

			return false;
		}
	}
}