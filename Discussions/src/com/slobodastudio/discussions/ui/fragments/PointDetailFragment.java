package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.ExtraKey;
import com.slobodastudio.discussions.ui.IntentAction;
import com.slobodastudio.discussions.ui.activities.BaseActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PointDetailFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final int INVALID_POINT_ID = Integer.MIN_VALUE;
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final int LOADER_COMMENTS_ID = 2;
	private static final int LOADER_DESCRIPTION_ID = 1;
	private static final int LOADER_POINT_ID = 0;
	private static final String TAG = PointDetailFragment.class.getSimpleName();
	private SimpleCursorAdapter mCommentsAdapter;
	private ListView mCommentsList;
	private Cursor mDescriptionCursor;
	private int mDescriptionId;
	private EditText mDesctiptionEditText;
	private int mDiscussionId;
	private boolean mIsEmpty;
	private EditText mNameEditText;
	private int mPersonId;
	private Cursor mPointCursor;
	private int mPointId;
	private CheckBox mSharedToPublicCheckBox;
	private Spinner mSideCodeSpinner;
	private int mTopicId;

	public PointDetailFragment() {

		// initialize default values
		mPointId = INVALID_POINT_ID;
		mIsEmpty = false;
		mDescriptionId = Integer.MIN_VALUE;
	}

	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
	public static Bundle intentToFragmentArguments(final Intent intent) {

		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}
		final String action = intent.getAction();
		if (action != null) {
			arguments.putString(ExtraKey.ACTION, action);
		}
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			arguments.putAll(intent.getExtras());
		}
		return arguments;
	}

	public int getPointId() {

		return mPointId;
	}

	public boolean isEmpty() {

		return mIsEmpty;
	}

	public void onActionAttachLink() {

		SharedPreferences sp = getActivity().getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editorSp = sp.edit();
		// editorSp.putString(ExtraKey., value)
		// return editorSp.commit();
	}

	public void onActionCancel() {

		// TODO: what can i do on Cancel?
		// discard changes
		// if (getArguments() != null) {
		// String action = getArguments().getString(ExtraKey.ACTION);
		// if (!Intent.ACTION_EDIT.equals(action) && !IntentAction.NEW.equals(action)) {
		// throw new UnsupportedOperationException(
		// "Operation [onActionCancel] doesnt support this intent: " + action);
		// }
		// }
		return;
	}

	public void onActionComment() {

		final EditText commentEditText = new EditText(getActivity());
		final String dialorTitle = getActivity().getString(R.string.dialog_title_comment);
		final String saveButtonTitle = getActivity().getString(R.string.menu_action_save);
		final String cancelButtonTitle = getActivity().getString(R.string.menu_action_cancel);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(dialorTitle).setCancelable(false).setPositiveButton(saveButtonTitle,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog, final int id) {

						String comment = commentEditText.getText().toString();
						Bundle commentValues = new Bundle();
						int personId;
						if (getArguments().containsKey(ExtraKey.ORIGIN_PERSON_ID)) {
							personId = getArguments().getInt(ExtraKey.ORIGIN_PERSON_ID, Integer.MIN_VALUE);
						} else {
							personId = mPersonId;
						}
						commentValues.putString(Comments.Columns.TEXT, comment);
						commentValues.putInt(Comments.Columns.POINT_ID, mPointId);
						commentValues.putInt(Comments.Columns.PERSON_ID, personId);
						((BaseActivity) getActivity()).getServiceHelper().insertComment(commentValues,
								mDiscussionId, mTopicId);
					}
				}).setNegativeButton(cancelButtonTitle, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int id) {

				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.setView(commentEditText);
		alert.show();
	}

	public void onActionDelete() {

		String action = getArguments().getString(ExtraKey.ACTION);
		if (!Intent.ACTION_EDIT.equals(action)) {
			throw new IllegalArgumentException("[onActionDelete] was called with incorrect action: " + action);
		}
		if (mPointId == INVALID_POINT_ID) {
			throw new IllegalArgumentException("[onActionDelete] was called with incorrect point id: "
					+ mPointId);
		}
		((BaseActivity) getActivity()).getServiceHelper().deletePoint(mPointId);
	}

	public void onActionSave() {

		// description is first because notify server by point change
		if (mDescriptionId != Integer.MIN_VALUE) {
			// update description
			Description description = new Description(mDescriptionId, mDesctiptionEditText.getText()
					.toString(), null, mPointId);
			((BaseActivity) getActivity()).getServiceHelper().updateDescription(description.toBundle());
		}
		// save point
		int expectedAgreementCode = Points.ArgreementCode.UNSOLVED;
		byte[] expectedDrawing = new byte[] { 0, 1 };
		boolean expectedExpanded = false;
		int expectedGroupId = 1;
		String expectedNumberedPoint = "";
		int expectedPersonId = mPersonId;
		String expectedPointName = mNameEditText.getText().toString();
		boolean expectedSharedToPublic = mSharedToPublicCheckBox.isChecked();
		int expectedSideCode;
		switch ((int) mSideCodeSpinner.getSelectedItemId()) {
			case Points.SideCode.CONS:
				expectedSideCode = Points.SideCode.CONS;
				break;
			case Points.SideCode.NEUTRAL:
				expectedSideCode = Points.SideCode.NEUTRAL;
				break;
			case Points.SideCode.PROS:
				expectedSideCode = Points.SideCode.PROS;
				break;
			default:
				throw new IllegalArgumentException("Unknown side code: "
						+ (int) mSideCodeSpinner.getSelectedItemId());
		}
		int expectedTopicId = mTopicId;
		if (mPointId != INVALID_POINT_ID) {
			// update point
			Point point = new Point(expectedAgreementCode, expectedDrawing, expectedExpanded,
					expectedGroupId, mPointId, expectedPointName, expectedNumberedPoint, expectedPersonId,
					expectedSharedToPublic, expectedSideCode, expectedTopicId);
			((BaseActivity) getActivity()).getServiceHelper().updatePoint(point.toBundle(), mDiscussionId);
		} else {
			// new point
			Bundle values;
			Point point = new Point(expectedAgreementCode, expectedDrawing, expectedExpanded,
					expectedGroupId, INVALID_POINT_ID, expectedPointName, expectedNumberedPoint,
					expectedPersonId, expectedSharedToPublic, expectedSideCode, expectedTopicId);
			// ((BaseActivity) getActivity()).getServiceHelper().insertPoint(point.toBundle());
			values = point.toBundle();
			// with new description
			if (mDescriptionId != Integer.MIN_VALUE) {
				throw new IllegalStateException("Cant be new point without new description");
			}
			// new description
			Description description = new Description(mDescriptionId, mDesctiptionEditText.getText()
					.toString(), null, mPointId);
			// ((BaseActivity) getActivity()).getServiceHelper().insertDescription(description.toBundle());
			values.putAll(description.toBundle());
			((BaseActivity) getActivity()).getServiceHelper()
					.insertPointAndDescription(values, mDiscussionId);
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_delete:
				onActionDeleteComment(item);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
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
		Cursor cursor = (Cursor) mCommentsAdapter.getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}
		int textIndex = cursor.getColumnIndexOrThrow(Comments.Columns.TEXT);
		int personIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.PERSON_ID);
		int personId = cursor.getInt(personIdIndex);
		int authorPersonId;
		if (getArguments().containsKey(ExtraKey.ORIGIN_PERSON_ID)) {
			authorPersonId = getArguments().getInt(ExtraKey.ORIGIN_PERSON_ID, Integer.MIN_VALUE);
		} else {
			authorPersonId = mPersonId;
		}
		if (personId == authorPersonId) {
			menu.setHeaderTitle(cursor.getString(textIndex));// if your table name is name
			android.view.MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.context_comments, menu);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

		if (!arguments.containsKey(ExtraKey.POINT_ID)) {
			throw new IllegalArgumentException("Loader was called without point id");
		}
		int myPointId = arguments.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
		if (DEBUG) {
			Log.d(TAG, "[onCreateLoader] point id: " + myPointId);
		}
		switch (loaderId) {
			case LOADER_POINT_ID: {
				String where = Points.Columns.ID + "=?";
				String[] args = new String[] { String.valueOf(myPointId) };
				return new CursorLoader(getActivity(), Points.CONTENT_URI, null, where, args, null);
			}
			case LOADER_DESCRIPTION_ID: {
				String where = Descriptions.Columns.POINT_ID + "=?";
				String[] args = new String[] { String.valueOf(myPointId) };
				return new CursorLoader(getActivity(), Descriptions.CONTENT_URI, null, where, args, null);
			}
			case LOADER_COMMENTS_ID: {
				String where = Comments.Columns.POINT_ID + "=?";
				String[] args = new String[] { String.valueOf(myPointId) };
				return new CursorLoader(getActivity(), Comments.CONTENT_URI, null, where, args, null);
			}
			default:
				throw new IllegalArgumentException("Unknown loader id: " + loaderId);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		if (isEmpty() || (container == null) || (getArguments() == null)) {
			if (DEBUG) {
				Log.d(TAG, "[onCreateView] show empty fragment");
			}
			TextView text = (TextView) inflater.inflate(R.layout.fragment_empty, null);
			text.setText(getActivity().getString(R.string.text_select_point));
			return text;
		}
		// setup layout
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_point_description, container,
				false);
		mNameEditText = (EditText) layout.findViewById(R.id.et_point_name);
		mDesctiptionEditText = (EditText) layout.findViewById(R.id.et_point_description);
		mSideCodeSpinner = (Spinner) layout.findViewById(R.id.spinner_point_agreement_code);
		mSharedToPublicCheckBox = (CheckBox) layout.findViewById(R.id.chb_share_to_public);
		mCommentsList = (ListView) layout.findViewById(R.id.comments_listview);
		registerForContextMenu(mCommentsList);
		// Button btn = new Button(getActivity());
		// btn.setText("Add comment");
		// LayoutParams lp = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
		// android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		//
		// btn.setLayoutParams(lp);
		// btn.setl
		// btn.setGravity(Gravity.CENTER_HORIZONTAL);
		// btn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(final View v) {
		//
		// onActionComment();
		// }
		// });
		// mCommentsList.addFooterView(btn);
		mCommentsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_comments, null,
				new String[] { Persons.Columns.NAME, Comments.Columns.TEXT, Persons.Columns.COLOR },
				new int[] { R.id.text_comment_person_name, R.id.text_comment, R.id.image_person_color }, 0);
		mCommentsAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

				int viewId = view.getId();
				switch (viewId) {
					case R.id.image_person_color:
						ImageView colorView = (ImageView) view;
						colorView.setBackgroundColor(cursor.getInt(columnIndex));
						return true;
					case R.id.text_comment:
						TextView itemText = (TextView) view;
						itemText.setText(cursor.getString(columnIndex));
						return true;
					case R.id.text_comment_person_name:
						TextView itemName = (TextView) view;
						itemName.setText(cursor.getString(columnIndex));
						return true;
					default:
						return false;
				}
			}
		});
		mCommentsList.setAdapter(mCommentsAdapter);
		mCommentsList.setEmptyView(layout.findViewById(R.id.comments_listview_empty));
		if (getArguments() == null) {
			// at this point we are expected to show point details
			throw new IllegalArgumentException("Fragment was called without arguments");
		}
		if (DEBUG) {
			Log.d(TAG, "[onCreateView] arguments: " + getArguments().toString());
		}
		// fill in data
		String action = getArguments().getString(ExtraKey.ACTION);
		if (Intent.ACTION_EDIT.equals(action)) {
			onActionEdit(savedInstanceState);
		} else if (Intent.ACTION_VIEW.equals(action)) {
			onActionView(savedInstanceState);
		} else if (IntentAction.NEW.equals(action)) {
			onActionNew(savedInstanceState);
		} else {
			throw new IllegalArgumentException("Unknown action: " + action);
		}
		return layout;
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {

		switch (loader.getId()) {
			case LOADER_POINT_ID:
				mPointCursor = null;
				break;
			case LOADER_DESCRIPTION_ID:
				mDescriptionCursor = null;
				break;
			case LOADER_COMMENTS_ID:
				mCommentsAdapter.swapCursor(null);
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
			case LOADER_POINT_ID: {
				if (data.getCount() == 1) {
					mPointCursor = data;
					Point value = new Point(mPointCursor);
					mPointId = value.getId();
					mPersonId = value.getPersonId();
					mTopicId = value.getTopicId();
					mNameEditText.setText(value.getName());
					mSideCodeSpinner.setSelection(value.getSideCode(), true);
					mSharedToPublicCheckBox.setChecked(value.isSharedToPublic());
					Bundle args = new Bundle();
					args.putInt(ExtraKey.POINT_ID, mPointId);
					getLoaderManager().initLoader(LOADER_DESCRIPTION_ID, args, this);
					getLoaderManager().initLoader(LOADER_COMMENTS_ID, args, this);
				} else {
					Log.w(TAG, "[onLoadFinished] LOADER_POINT_ID count was: " + data.getCount());
				}
				break;
			}
			case LOADER_DESCRIPTION_ID:
				if (data.getCount() == 1) {
					mDescriptionCursor = data;
					Description description = new Description(mDescriptionCursor);
					mDesctiptionEditText.setText(description.getText());
					mDescriptionId = description.getId();
				} else {
					Log.w(TAG, "[onLoadFinished] LOADER_DESCRIPTION_ID count was: " + data.getCount());
				}
				break;
			case LOADER_COMMENTS_ID:
				mCommentsAdapter.swapCursor(data);
				break;
			default:
				throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {

		super.onSaveInstanceState(outState);
		if (!isEmpty()) {
			outState.putBoolean(ExtraKey.SHARED_TO_PUBLIC, mSharedToPublicCheckBox.isChecked());
			outState.putString(ExtraKey.POINT_NAME, mNameEditText.getText().toString());
			outState.putInt(ExtraKey.AGREEMENT_CODE, getSelectedSideCodeId());
			outState.putInt(ExtraKey.PERSON_ID, mPersonId);
			outState.putInt(ExtraKey.TOPIC_ID, mTopicId);
			outState.putInt(ExtraKey.POINT_ID, mPointId);
			outState.putInt(ExtraKey.DESCRIPTION_ID, mDescriptionId);
			outState.putString(ExtraKey.DESCRIPTION_TEXT, mDesctiptionEditText.getText().toString());
		}
	}

	public void setEmpty(final boolean empty) {

		mIsEmpty = empty;
	}

	private int getSelectedSideCodeId() {

		int sideCode;
		switch ((int) mSideCodeSpinner.getSelectedItemId()) {
			case Points.SideCode.CONS:
				sideCode = Points.SideCode.CONS;
				break;
			case Points.SideCode.NEUTRAL:
				sideCode = Points.SideCode.NEUTRAL;
				break;
			case Points.SideCode.PROS:
				sideCode = Points.SideCode.PROS;
				break;
			default:
				throw new IllegalArgumentException("Unknown side code: "
						+ (int) mSideCodeSpinner.getSelectedItemId());
		}
		return sideCode;
	}

	private void onActionDeleteComment(final MenuItem item) {

		AdapterView.AdapterContextMenuInfo info;
		try {
			// Casts the incoming data object into the type for AdapterView objects.
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			// If the menu object can't be cast, logs an error.
			throw new RuntimeException("bad menuInfo: " + item.getMenuInfo(), e);
		}
		Cursor cursor = (Cursor) mCommentsAdapter.getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}
		int columnIndex = cursor.getColumnIndexOrThrow(Comments.Columns.ID);
		int pointIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.POINT_ID);
		int personIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.PERSON_ID);
		int commentId = cursor.getInt(columnIndex);
		int pointId = cursor.getInt(pointIdIndex);
		int personId = cursor.getInt(personIdIndex);
		((BaseActivity) getActivity()).getServiceHelper().deleteComment(commentId, pointId, mDiscussionId,
				mTopicId, personId);
	}

	private void onActionEdit(final Bundle savedInstanceState) {

		if (!getArguments().containsKey(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalStateException("intent was without discussion id");
		}
		if (!getArguments().containsKey(ExtraKey.POINT_ID)) {
			throw new IllegalStateException("Fragment was called without point id in arguments");
		}
		mDiscussionId = getArguments().getInt(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
		if (savedInstanceState == null) {
			int initialPointId = getArguments().getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
			Bundle args = new Bundle();
			args.putInt(ExtraKey.POINT_ID, initialPointId);
			getLoaderManager().initLoader(LOADER_POINT_ID, args, this);
		} else {
			populateFromSavedInstanceState(savedInstanceState);
		}
		setViewsEnabled(true);
	}

	private void onActionNew(final Bundle savedInstanceState) {

		if (!getArguments().containsKey(ExtraKey.PERSON_ID)) {
			throw new IllegalStateException("intent was without person id");
		}
		if (!getArguments().containsKey(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("intent was without topic id");
		}
		if (!getArguments().containsKey(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalStateException("intent was without discussion id");
		}
		if (savedInstanceState == null) {
			// leave empty fields to create new point
			mPersonId = getArguments().getInt(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
			mTopicId = getArguments().getInt(ExtraKey.TOPIC_ID, Integer.MIN_VALUE);
			mDiscussionId = getArguments().getInt(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
			mPointId = INVALID_POINT_ID;
		} else {
			populateFromSavedInstanceState(savedInstanceState);
		}
		setViewsEnabled(true);
	}

	private void onActionView(final Bundle savedInstanceState) {

		if (!getArguments().containsKey(ExtraKey.POINT_ID)) {
			throw new IllegalStateException("Fragment was called without point id in arguments");
		}
		if (savedInstanceState == null) {
			int initialPointId = getArguments().getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
			Bundle args = new Bundle();
			args.putInt(ExtraKey.POINT_ID, initialPointId);
			getLoaderManager().initLoader(LOADER_POINT_ID, args, this);
		} else {
			populateFromSavedInstanceState(savedInstanceState);
		}
		setViewsEnabled(false);
	}

	private void populateFromSavedInstanceState(final Bundle savedInstanceState) {

		if (!savedInstanceState.containsKey(ExtraKey.POINT_ID)) {
			throw new IllegalStateException("SavedInstanceState doesnt contain point id");
		}
		if (!savedInstanceState.containsKey(ExtraKey.PERSON_ID)) {
			throw new IllegalStateException("SavedInstanceState doesnt contain person id");
		}
		if (!savedInstanceState.containsKey(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("SavedInstanceState doesnt contain topic id");
		}
		if (!savedInstanceState.containsKey(ExtraKey.POINT_NAME)) {
			throw new IllegalStateException("SavedInstanceState doesnt contain point name");
		}
		if (!savedInstanceState.containsKey(ExtraKey.DESCRIPTION_TEXT)) {
			throw new IllegalStateException("SavedInstanceState doesnt contain description text");
		}
		if (!savedInstanceState.containsKey(ExtraKey.AGREEMENT_CODE)) {
			throw new IllegalStateException("SavedInstanceState doesnt contain agreement code");
		}
		if (!savedInstanceState.containsKey(ExtraKey.SHARED_TO_PUBLIC)) {
			throw new IllegalStateException("SavedInstanceState doesnt contain shared to public");
		}
		mPointId = savedInstanceState.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
		mPersonId = savedInstanceState.getInt(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
		mTopicId = savedInstanceState.getInt(ExtraKey.TOPIC_ID, Integer.MIN_VALUE);
		mNameEditText.setText(savedInstanceState.getString(ExtraKey.POINT_NAME));
		mDesctiptionEditText.setText(savedInstanceState.getString(ExtraKey.DESCRIPTION_TEXT));
		mSideCodeSpinner.setSelection(savedInstanceState.getInt(ExtraKey.AGREEMENT_CODE, Integer.MIN_VALUE));
		mSharedToPublicCheckBox.setChecked(savedInstanceState.getBoolean(ExtraKey.SHARED_TO_PUBLIC));
		Bundle args = new Bundle();
		args.putInt(ExtraKey.POINT_ID, mPointId);
		getLoaderManager().initLoader(LOADER_COMMENTS_ID, args, this);
	}

	private void setViewsEnabled(final boolean enabled) {

		if (!enabled) {
			mDesctiptionEditText.setEnabled(false);
			mNameEditText.setEnabled(false);
			mDesctiptionEditText.setEnabled(false);
			mSideCodeSpinner.setEnabled(false);
			mSharedToPublicCheckBox.setEnabled(false);
		}
	}
}
