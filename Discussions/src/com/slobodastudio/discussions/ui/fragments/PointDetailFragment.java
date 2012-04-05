package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.IntentExtrasKey;
import com.slobodastudio.discussions.ui.activities.BaseActivity;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PointDetailFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final int INVALID_POINT_ID = Integer.MIN_VALUE;
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String EXTRA_ACTION = "extra_key_action";
	private static final String EXTRA_DESCRIPTION_ID = "extra_description_id";
	private static final String EXTRA_DESCRIPTION_TEXT = "extra_description_text";
	private static final String EXTRA_PERSON_ID = IntentExtrasKey.PERSON_ID;
	private static final String EXTRA_POINT_ID = "extra_point_id";
	private static final String EXTRA_POINT_NAME = "extra_point_name";
	private static final String EXTRA_SHARED_TO_PUBLIC = "extra_shared_to_public";
	private static final String EXTRA_SIDE_CODE = "extra_side_code";
	private static final String EXTRA_TOPIC_ID = IntentExtrasKey.TOPIC_ID;
	private static final String EXTRA_URI = "extra_key_uri";
	private static final int LOADER_COMMENTS_ID = 2;
	private static final int LOADER_DESCRIPTION_ID = 1;
	private static final int LOADER_POINT_ID = 0;
	private static final String TAG = PointDetailFragment.class.getSimpleName();
	private static final int TYPE_DIR = 1;
	private static final int TYPE_ITEM = 0;
	private int descriptionId = Integer.MIN_VALUE;
	private boolean empty = false;
	private SimpleCursorAdapter mCommentsAdapter;
	private ListView mCommentsList;
	private Cursor mDescriptionCursor;
	private EditText mDesctiptionEditText;
	private EditText mNameEditText;
	private Cursor mPointCursor;
	private CheckBox mSharedToPublicCheckBox;
	private Spinner mSideCodeSpinner;
	private int personId;
	private int pointId = INVALID_POINT_ID;
	private int topicId;
	private int typeId;

	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
	public static Bundle intentToFragmentArguments(final Intent intent) {

		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}
		final Uri data = intent.getData();
		if (data != null) {
			arguments.putParcelable(EXTRA_URI, data);
		}
		final String action = intent.getAction();
		if (action != null) {
			arguments.putString(EXTRA_ACTION, action);
		}
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			arguments.putAll(intent.getExtras());
		}
		return arguments;
	}

	public int getPointId() {

		return pointId;
	}

	public boolean isEmpty() {

		return empty;
	}

	public void onActionCancel() {

		// discard changes
		String action = getArguments().getString(EXTRA_ACTION);
		if (action.equals(Intent.ACTION_EDIT)) {
			switch (typeId) {
				case TYPE_DIR:
					throw new UnsupportedOperationException();
				case TYPE_ITEM: {
					if (mPointCursor.getCount() == 1) {
						Point value = new Point(mPointCursor);
						pointId = value.getId();
						personId = value.getPersonId();
						topicId = value.getTopicId();
						mNameEditText.setText(value.getName());
						mSideCodeSpinner.setSelection(value.getAgreementCode());
						mSharedToPublicCheckBox.setChecked(value.isSharedToPublic());
					} else {
						throw new IllegalStateException("Expected single value in cursor, was: "
								+ mPointCursor.getCount());
					}
					break;
				}
				default:
					throw new IllegalArgumentException("Unknown type id: " + typeId);
			}
		} else {
			throw new IllegalArgumentException("Unknown action: " + action);
		}
	}

	public void onActionSave() {

		// description is first because notify server by point change
		if (descriptionId != Integer.MIN_VALUE) {
			// update description
			Description description = new Description(descriptionId, mDesctiptionEditText.getText()
					.toString(), null, pointId);
			((BaseActivity) getActivity()).getServiceHelper().updateDescription(description.toBundle());
		}
		// save point
		int expectedAgreementCode = Points.ArgreementCode.UNSOLVED;
		byte[] expectedDrawing = new byte[] { 0, 1 };
		boolean expectedExpanded = false;
		int expectedGroupId = 1;
		String expectedNumberedPoint = "";
		int expectedPersonId = personId;
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
		int expectedTopicId = topicId;
		if (pointId != INVALID_POINT_ID) {
			// update point
			Point point = new Point(expectedAgreementCode, expectedDrawing, expectedExpanded,
					expectedGroupId, pointId, expectedPointName, expectedNumberedPoint, expectedPersonId,
					expectedSharedToPublic, expectedSideCode, expectedTopicId);
			((BaseActivity) getActivity()).getServiceHelper().updatePoint(point.toBundle());
		} else {
			// new point
			Bundle values;
			Point point = new Point(expectedAgreementCode, expectedDrawing, expectedExpanded,
					expectedGroupId, INVALID_POINT_ID, expectedPointName, expectedNumberedPoint,
					expectedPersonId, expectedSharedToPublic, expectedSideCode, expectedTopicId);
			// ((BaseActivity) getActivity()).getServiceHelper().insertPoint(point.toBundle());
			values = point.toBundle();
			// with new description
			if (descriptionId != Integer.MIN_VALUE) {
				throw new IllegalStateException("Cant be new point without new description");
			}
			// new description
			Description description = new Description(descriptionId, mDesctiptionEditText.getText()
					.toString(), null, pointId);
			// ((BaseActivity) getActivity()).getServiceHelper().insertDescription(description.toBundle());
			values.putAll(description.toBundle());
			((BaseActivity) getActivity()).getServiceHelper().insertPointAndDescription(values);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

		switch (loaderId) {
			case LOADER_POINT_ID: {
				Uri uri = getArguments().getParcelable(EXTRA_URI);
				if (DEBUG) {
					Log.d(TAG, "[onCreateLoader] point uri: " + uri);
				}
				String id = Points.getValueId(uri);
				String where = Points.Columns.ID + "=?";
				String[] args = new String[] { id };
				return new CursorLoader(getActivity(), Points.CONTENT_URI, null, where, args, null);
			}
			case LOADER_DESCRIPTION_ID: {
				String where = Descriptions.Columns.POINT_ID + "=?";
				String[] args = new String[] { String.valueOf(pointId) };
				return new CursorLoader(getActivity(), Descriptions.CONTENT_URI, null, where, args, null);
			}
			case LOADER_COMMENTS_ID: {
				String where = Comments.Columns.POINT_ID + "=?";
				String[] args = new String[] { String.valueOf(pointId) };
				return new CursorLoader(getActivity(), Comments.CONTENT_URI, null, where, args, null);
			}
			default:
				throw new IllegalArgumentException("Unknown loader id: " + loaderId);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		if (isEmpty()) {
			if (DEBUG) {
				Log.d(TAG, "[onCreateView] show empty fragment");
			}
			TextView text = (TextView) inflater.inflate(R.layout.details_item, null);
			text.setText(getActivity().getString(R.string.fragment_select_point));
			return text;
		}
		if ((container == null) || (getArguments() == null)) {
			if (DEBUG) {
				Log.d(TAG, "[onCreateView] container and arguments was null");
			}
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.
			return null;
		}
		if (DEBUG) {
			Log.d(TAG, "[onCreateView] arguments: " + getArguments().toString());
		}
		// set type
		Uri uri = getArguments().getParcelable(EXTRA_URI);
		String type = getActivity().getContentResolver().getType(uri);
		if (type.equals(Points.CONTENT_DIR_TYPE)) {
			typeId = TYPE_DIR;
		} else if (type.equals(Points.CONTENT_ITEM_TYPE)) {
			typeId = TYPE_ITEM;
		} else {
			throw new IllegalArgumentException("Unknown type: " + type);
		}
		// setup layout
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.point_desctiption_item, null);
		mNameEditText = (EditText) layout.findViewById(R.id.et_point_name);
		mDesctiptionEditText = (EditText) layout.findViewById(R.id.et_point_description);
		mSideCodeSpinner = (Spinner) layout.findViewById(R.id.spinner_point_agreement_code);
		mSharedToPublicCheckBox = (CheckBox) layout.findViewById(R.id.chb_share_to_public);
		mCommentsList = (ListView) layout.findViewById(R.id.comments_listview);
		mCommentsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.comments_list_item, null,
				new String[] { Persons.Columns.NAME, Comments.Columns.TEXT }, new int[] {
						R.id.text_comment_person_name, R.id.text_comment }, 0);
		mCommentsList.setAdapter(mCommentsAdapter);
		mCommentsList.setEmptyView(layout.findViewById(R.id.comments_listview_empty));
		// fill in data
		String action = getArguments().getString(EXTRA_ACTION);
		if (action.equals(Intent.ACTION_EDIT)) {
			onActionEdit(savedInstanceState);
		} else if (action.equals(Intent.ACTION_VIEW)) {
			onActionView();
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
				mPointCursor = data;
				if (data.getCount() > 0) {
					Point value = new Point(mPointCursor);
					pointId = value.getId();
					personId = value.getPersonId();
					topicId = value.getTopicId();
					mNameEditText.setText(value.getName());
					mSideCodeSpinner.setSelection(value.getSideCode(), true);
					mSharedToPublicCheckBox.setChecked(value.isSharedToPublic());
					getLoaderManager().initLoader(LOADER_DESCRIPTION_ID, null, this);
					getLoaderManager().initLoader(LOADER_COMMENTS_ID, null, this);
				}
				break;
			}
			case LOADER_DESCRIPTION_ID:
				mDescriptionCursor = data;
				if (data.getCount() > 0) {
					Description description = new Description(mDescriptionCursor);
					mDesctiptionEditText.setText(description.getText());
					descriptionId = description.getId();
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
			outState.putBoolean(EXTRA_SHARED_TO_PUBLIC, mSharedToPublicCheckBox.isChecked());
			outState.putString(EXTRA_POINT_NAME, mNameEditText.getText().toString());
			outState.putInt(EXTRA_SIDE_CODE, getSelectedSideCodeId());
			outState.putInt(EXTRA_PERSON_ID, personId);
			outState.putInt(EXTRA_TOPIC_ID, topicId);
			outState.putInt(EXTRA_POINT_ID, pointId);
			outState.putInt(EXTRA_DESCRIPTION_ID, descriptionId);
			outState.putString(EXTRA_DESCRIPTION_TEXT, mDesctiptionEditText.getText().toString());
		}
	}

	public void setEmpty(final boolean empty) {

		this.empty = empty;
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

	private void onActionEdit(final Bundle savedInstanceState) {

		switch (typeId) {
			case TYPE_DIR:
				// leave empty fields to create new point
				if (getArguments().containsKey(EXTRA_PERSON_ID)) {
					personId = getArguments().getInt(EXTRA_PERSON_ID);
				} else {
					throw new IllegalStateException("intent was without person id");
				}
				if (getArguments().containsKey(EXTRA_TOPIC_ID)) {
					topicId = getArguments().getInt(EXTRA_TOPIC_ID);
				} else {
					throw new IllegalStateException("intent was without topic id");
				}
				pointId = INVALID_POINT_ID;
				break;
			case TYPE_ITEM:
				if (savedInstanceState == null) {
					getLoaderManager().initLoader(LOADER_POINT_ID, null, this);
				} else {
					pointId = savedInstanceState.getInt(EXTRA_POINT_ID, Integer.MIN_VALUE);
					personId = savedInstanceState.getInt(EXTRA_PERSON_ID, Integer.MIN_VALUE);
					topicId = savedInstanceState.getInt(EXTRA_TOPIC_ID, Integer.MIN_VALUE);
					mNameEditText.setText(savedInstanceState.getString(EXTRA_POINT_NAME));
					mDesctiptionEditText.setText(savedInstanceState.getString(EXTRA_DESCRIPTION_TEXT));
					mSideCodeSpinner.setSelection(savedInstanceState.getInt(EXTRA_SIDE_CODE,
							Integer.MIN_VALUE));
					mSharedToPublicCheckBox.setChecked(savedInstanceState.getBoolean(EXTRA_SHARED_TO_PUBLIC));
				}
				setupView(true);
				break;
			default:
				throw new IllegalArgumentException("Unknown type id: " + typeId);
		}
	}

	private void onActionView() {

		switch (typeId) {
			case TYPE_DIR:
				throw new IllegalStateException("Should not reach here. No sense to view empty point");
			case TYPE_ITEM:
				getLoaderManager().initLoader(LOADER_POINT_ID, null, this);
				setupView(false);
				break;
			default:
				throw new IllegalArgumentException("Unknown type id: " + typeId);
		}
	}

	private void setupView(final boolean editable) {

		if (!editable) {
			mDesctiptionEditText.setEnabled(false);
			mNameEditText.setEnabled(false);
			mDesctiptionEditText.setEnabled(false);
			mSideCodeSpinner.setEnabled(false);
			mSharedToPublicCheckBox.setEnabled(false);
		}
	}
}
