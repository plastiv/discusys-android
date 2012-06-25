package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.ExtraKey;
import com.slobodastudio.discussions.ui.IntentAction;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
	private int mCurPosition;
	private int mDiscussionId;
	private boolean mDualPane;
	private boolean mEmptyFragmentShownOnActionBarClose;
	private SimpleCursorAdapter mOtherPointsAdapter;
	private ListView mOtherPointsList;
	private int mPersonId;
	private int mSelectedList;
	private int mTopicId;
	private SimpleCursorAdapter mUserPointsAdapter;
	private ListView mUserPointsList;

	public PointsFragment() {

		// initialize default values
		mEmptyFragmentShownOnActionBarClose = true;
		mSelectedList = SELECTED_NONE;
	}

	public void onActionNew() {

		if (mDualPane) {
			// We can display everything in-place with fragments
			// Check what fragment is currently shown, replace if needed.
			PointDescriptionTabFragment details = (PointDescriptionTabFragment) getFragmentManager()
					.findFragmentById(R.id.frame_layout_details);
			if ((details == null) || (details.isEmpty())) {
				// Make new fragment to show this selection.
				Log.d(TAG, "hellp");
				details = new PointDescriptionTabFragment();
				Intent intent = createNewPointIntent();
				details.setArguments(PointDescriptionTabFragment.intentToFragmentArguments(intent));
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_layout_details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
			mActionMode = getSherlockActivity().startActionMode(new EditDetailsActionMode(false));
		} else {
			// Otherwise we need to launch a new activity to display details
			Intent intent = createNewPointIntent();
			startActivity(intent);
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		if (DEBUG) {
			Log.d(TAG, "[onActivityCreared] saved state: " + savedInstanceState);
		}
		initFromIntentExtra();
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
		mUserPointsList.setAdapter(mUserPointsAdapter);
		// mUserPointsList.setActivated(true);
		// Create an empty adapter we will use to display the loaded data.
		mOtherPointsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_base, null,
				new String[] { Points.Columns.NAME, Persons.Columns.COLOR }, new int[] { R.id.list_item_text,
						R.id.image_person_color }, 0);
		mOtherPointsAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

				int viewId = view.getId();
				switch (viewId) {
					case R.id.image_person_color:
						ImageView colorView = (ImageView) view;
						colorView.setBackgroundColor(cursor.getInt(columnIndex));
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
		mOtherPointsList.setAdapter(mOtherPointsAdapter);
		// mOtherPointsList.setActivated(true);
		// set up click listener
		mUserPointsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> arg0, final View v, final int position, final long id) {

				mSelectedList = SELECTED_USERS;
				mCurPosition = position;
				mUserPointsList.setItemChecked(position, true);
				mOtherPointsList.clearChoices();
				if (mActionMode != null) {
					mEmptyFragmentShownOnActionBarClose = false;
					mActionMode.finish();
				}
				onActionEdit(position);
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
					mEmptyFragmentShownOnActionBarClose = false;
					mActionMode.finish();
				}
				onActionView(position);
			}
		});
		// Prepare the loader. Either re-connect with an existing one, or start a new one.
		getLoaderManager().initLoader(LOADER_USER_POINTS_ID, null, this);
		getLoaderManager().initLoader(LOADER_OTHER_POINTS_ID, null, this);
		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View detailsFrame = getActivity().findViewById(R.id.frame_layout_details);
		// mDualPane = (detailsFrame != null) && (detailsFrame.getVisibility() == View.VISIBLE);
		mDualPane = false;
		if (savedInstanceState != null) {
			// Restore last state for checked position in correct list
			mCurPosition = savedInstanceState.getInt(EXTRA_POSITION, 0);
			mSelectedList = savedInstanceState.getInt(EXTRA_SELECTED, SELECTED_NONE);
			// if (mDualPane) {
			// Fragment detaildFragment = getFragmentManager().findFragmentById(R.id.frame_layout_details);
			// if (detaildFragment != null) {
			// FragmentTransaction ft = getFragmentManager().beginTransaction();
			// ft.replace(R.id.frame_layout_details, detaildFragment);
			// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// ft.commit();
			// }
			// }
		}
		if (mDualPane) {
			// In dual-pane mode, the list view highlights the selected item.
			mUserPointsList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
			mOtherPointsList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
			// TODO: Make sure our UI is in the correct state.
			switch (mSelectedList) {
				case SELECTED_NONE:
					// nothing to do
					break;
				case SELECTED_USERS:
					mUserPointsList.setItemChecked(mCurPosition, true);
					break;
				case SELECTED_OTHERS:
					mOtherPointsList.setItemChecked(mCurPosition, true);
					break;
				default:
					throw new IllegalStateException("Unknown selected list id: " + mSelectedList);
			}
			// show empty details "select point to see"
			showEmtyDetails();
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
				String[] args = { String.valueOf(mTopicId), String.valueOf(mPersonId) };
				String sortOrder = BaseColumns._ID + " DESC";
				return new CursorLoader(getActivity(), URI, null, where, args, sortOrder);
			}
			case LOADER_OTHER_POINTS_ID: {
				// String where = Points.Columns.TOPIC_ID + "=? AND " + Points.Columns.PERSON_ID + "!=? ";
				String[] args = { String.valueOf(mTopicId), String.valueOf(mPersonId) };
				// String sortOrder = Points.TABLE_NAME + "." + BaseColumns._ID + " DESC";
				String sortOrder = Persons.TABLE_NAME + "." + Persons.Columns.ID + " ASC";
				return new CursorLoader(getActivity(), Points.CONTENT_AND_PERSON_URI, null, null, args,
						sortOrder);
			}
			default:
				throw new IllegalArgumentException("Unknown loader id: " + id);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_points, null);
		mUserPointsList = (ListView) layout.findViewById(R.id.user_points_listview);
		mOtherPointsList = (ListView) layout.findViewById(R.id.other_user_points_listview);
		// mUserPointsList.setEmptyView(layout.findViewById(R.id.user_points_listview_empty));
		// mOtherPointsList.setEmptyView(layout.findViewById(R.id.other_user_points_listview_empty));
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

	public void showEmtyDetails() {

		mUserPointsList.clearChoices();
		mOtherPointsList.clearChoices();
		if (mDualPane) {
			// Make new fragment to show this selection.
			PointDescriptionTabFragment details = new PointDescriptionTabFragment();
			details.setEmpty(true);
			// Execute a transaction, replacing any existing fragment
			// with this one inside the frame.
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.frame_layout_details, details);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
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
		int columnIndex = cursor.getColumnIndexOrThrow(Points.Columns.ID);
		return cursor.getInt(columnIndex);
	}

	private Intent createEditPointIntent(final int pointId) {

		Intent intent = new Intent(Intent.ACTION_EDIT, Points.buildTableUri(pointId));
		intent.putExtra(ExtraKey.DISCUSSION_ID, mDiscussionId);
		intent.putExtra(ExtraKey.POINT_ID, pointId);
		intent.putExtra(ExtraKey.PERSON_ID, mPersonId);
		intent.putExtra(ExtraKey.TOPIC_ID, mTopicId);
		return intent;
	}

	private Intent createNewPointIntent() {

		Intent intent = new Intent(IntentAction.NEW, Points.CONTENT_URI);
		intent.putExtra(ExtraKey.PERSON_ID, mPersonId);
		intent.putExtra(ExtraKey.TOPIC_ID, mTopicId);
		intent.putExtra(ExtraKey.DISCUSSION_ID, mDiscussionId);
		intent.putExtra(ExtraKey.POINT_ID, Integer.MIN_VALUE);
		return intent;
	}

	private Intent createViewPointIntent(final int pointId) {

		Intent intent = new Intent(Intent.ACTION_VIEW, Points.buildTableUri(pointId));
		intent.putExtra(ExtraKey.POINT_ID, pointId);
		intent.putExtra(ExtraKey.ORIGIN_PERSON_ID, mPersonId);
		intent.putExtra(ExtraKey.PERSON_ID, mPersonId);
		intent.putExtra(ExtraKey.TOPIC_ID, mTopicId);
		intent.putExtra(ExtraKey.DISCUSSION_ID, mDiscussionId);
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

		int pointId;
		if ((mUserPointsAdapter.getCursor() != null)
				&& mUserPointsAdapter.getCursor().moveToPosition(position)) {
			int valueIdIndex = mUserPointsAdapter.getCursor().getColumnIndexOrThrow(Points.Columns.ID);
			pointId = mUserPointsAdapter.getCursor().getInt(valueIdIndex);
		} else {
			pointId = PointDescriptionTabFragment.INVALID_POINT_ID;
			return;
		}
		if (mDualPane) {
			// We can display everything in-place with fragments
			// Check what fragment is currently shown, replace if needed.
			PointDescriptionTabFragment details = (PointDescriptionTabFragment) getFragmentManager()
					.findFragmentById(R.id.frame_layout_details);
			if ((details == null) || (details.getPointId() != pointId)) {
				// Make new fragment to show this selection.
				details = new PointDescriptionTabFragment();
				Intent intent = createEditPointIntent(pointId);
				details.setArguments(PointDescriptionTabFragment.intentToFragmentArguments(intent));
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_layout_details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
			mActionMode = getSherlockActivity().startActionMode(new EditDetailsActionMode(true));
		} else {
			// Otherwise we need to launch a new activity to display details
			Intent intent = createEditPointIntent(pointId);
			startActivity(intent);
		}
	}

	private void onActionView(final int position) {

		int valueId;
		if ((mOtherPointsAdapter.getCursor() != null)
				&& mOtherPointsAdapter.getCursor().moveToPosition(position)) {
			int valueIdIndex = mOtherPointsAdapter.getCursor().getColumnIndexOrThrow(Points.Columns.ID);
			valueId = mOtherPointsAdapter.getCursor().getInt(valueIdIndex);
		} else {
			valueId = PointDescriptionTabFragment.INVALID_POINT_ID;
			return;
		}
		if (mDualPane) {
			// We can display everything in-place with fragments
			// Check what fragment is currently shown, replace if needed.
			PointDescriptionTabFragment details = (PointDescriptionTabFragment) getFragmentManager()
					.findFragmentById(R.id.frame_layout_details);
			if ((details == null) || (details.getPointId() != valueId)) {
				// Make new fragment to show this selection.
				details = new PointDescriptionTabFragment();
				Intent intent = createViewPointIntent(valueId);
				details.setArguments(PointDescriptionTabFragment.intentToFragmentArguments(intent));
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_layout_details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
			mActionMode = getSherlockActivity().startActionMode(new ViewDetailsActionMode());
		} else {
			// Otherwise we need to launch a new activity to display details
			Intent intent = createViewPointIntent(valueId);
			startActivity(intent);
		}
	}

	private final class EditDetailsActionMode implements ActionMode.Callback {

		private final boolean showDeleteAction;

		public EditDetailsActionMode(final boolean showDeleteAction) {

			this.showDeleteAction = showDeleteAction;
		}

		@Override
		public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {

			switch (item.getItemId()) {
				case R.id.menu_save:
					((PointDescriptionTabFragment) getFragmentManager().findFragmentById(
							R.id.frame_layout_details)).onActionSave();
					mode.finish();
					return true;
				case R.id.menu_cancel:
					// PointDetailFragment.onActionCancel();
					mode.finish();
					return true;
				case R.id.menu_delete:
					((PointDescriptionTabFragment) getFragmentManager().findFragmentById(
							R.id.frame_layout_details)).onActionDelete();
					mode.finish();
					return true;
				default:
					throw new IllegalArgumentException("Unknown menuitem id: " + item.getItemId());
			}
		}

		@Override
		public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {

			mode.setTitle(R.string.action_mode_title_points);
			MenuInflater inflater = mode.getMenuInflater();
			if (showDeleteAction) {
				inflater.inflate(R.menu.actionbar_point_edit, menu);
			} else {
				inflater.inflate(R.menu.actionbar_point_new, menu);
			}
			mEmptyFragmentShownOnActionBarClose = true;
			return true;
		}

		@Override
		public void onDestroyActionMode(final ActionMode mode) {

			Log.d(TAG, "[onDestroyActionMode] showEmptyOnClose: " + mEmptyFragmentShownOnActionBarClose);
			if (mEmptyFragmentShownOnActionBarClose) {
				showEmtyDetails();
			}
			mActionMode = null;
		}

		@Override
		public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {

			return false;
		}
	}

	private final class ViewDetailsActionMode implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {

			switch (item.getItemId()) {
				case R.id.menu_cancel:
					// PointDetailFragment.onActionCancel();
					mode.finish();
					return true;
				default:
					throw new IllegalArgumentException("Unknown menuitem id: " + item.getItemId());
			}
		}

		@Override
		public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {

			mode.setTitle(R.string.action_mode_title_view_points);
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.actionbar_point_view, menu);
			mEmptyFragmentShownOnActionBarClose = true;
			return true;
		}

		@Override
		public void onDestroyActionMode(final ActionMode mode) {

			Log.d(TAG, "[onDestroyActionMode] showEmptyOnClose: " + mEmptyFragmentShownOnActionBarClose);
			if (mEmptyFragmentShownOnActionBarClose) {
				showEmtyDetails();
			}
			mActionMode = null;
		}

		@Override
		public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {

			return false;
		}
	}
}