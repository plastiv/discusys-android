package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.RichText;
import com.slobodastudio.discussions.ui.IntentExtrasKey;
import com.slobodastudio.discussions.ui.activities.BaseActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PointDetailFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final int INVALID_POINT_ID = Integer.MIN_VALUE;
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String EXTRA_ACTION = "extra_key_action";
	private static final String EXTRA_PERSON_ID = IntentExtrasKey.PERSON_ID;
	private static final String EXTRA_TOPIC_ID = IntentExtrasKey.TOPIC_ID;
	private static final String EXTRA_URI = "extra_key_uri";
	private static final String TAG = PointDetailFragment.class.getSimpleName();
	private static final int TYPE_DIR = 1;
	private static final int TYPE_ITEM = 0;
	Cursor mCursor;
	private boolean empty = false;
	private EditText mDesctiptionEditText;
	private EditText mNameEditText;
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
					if (mCursor.getCount() == 1) {
						Point value = new Point(mCursor);
						pointId = value.getId();
						personId = value.getPersonId();
						topicId = value.getTopicId();
						mNameEditText.setText(value.getName());
						mSideCodeSpinner.setSelection(value.getAgreementCode());
						mSharedToPublicCheckBox.setChecked(value.isSharedToPublic());
					} else {
						throw new IllegalStateException("Expected single value in cursor, was: "
								+ mCursor.getCount());
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
			Point point = new Point(expectedAgreementCode, expectedDrawing, expectedExpanded,
					expectedGroupId, INVALID_POINT_ID, expectedPointName, expectedNumberedPoint,
					expectedPersonId, expectedSharedToPublic, expectedSideCode, expectedTopicId);
			((BaseActivity) getActivity()).getServiceHelper().insertPoint(point.toBundle());
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int arg0, final Bundle arg1) {

		Uri uri = getArguments().getParcelable(EXTRA_URI);
		if (DEBUG) {
			Log.d(TAG, "[onCreateLoader] uri: " + uri);
		}
		return new CursorLoader(getActivity(), uri, null, null, null, null);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		if ((container == null) || (getArguments() == null)) {
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.
			return null;
		}
		if (isEmpty()) {
			TextView text = (TextView) inflater.inflate(R.layout.details_item, null);
			text.setText(getActivity().getString(R.string.fragment_select_point));
			return text;
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
		// fill in data
		String action = getArguments().getString(EXTRA_ACTION);
		if (action.equals(Intent.ACTION_EDIT)) {
			onActionEdit();
		} else if (action.equals(Intent.ACTION_VIEW)) {
			onActionView();
		} else {
			throw new IllegalArgumentException("Unknown action: " + action);
		}
		return layout;
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> arg0) {

		mCursor = null;
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> arg0, final Cursor data) {

		mCursor = data;
		if (data.getCount() > 0) {
			Point value = new Point(mCursor);
			pointId = value.getId();
			personId = value.getPersonId();
			topicId = value.getTopicId();
			mNameEditText.setText(value.getName());
			mSideCodeSpinner.setSelection(value.getAgreementCode());
			mSharedToPublicCheckBox.setChecked(value.isSharedToPublic());
		}
	}

	public void setEmpty(final boolean empty) {

		this.empty = empty;
	}

	private void onActionEdit() {

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
				getLoaderManager().initLoader(0, null, this);
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
				getLoaderManager().initLoader(0, null, this);
				setupView(false);
				break;
			default:
				throw new IllegalArgumentException("Unknown type id: " + typeId);
		}
	}

	private void setDescription() {

		Uri uri = RichText.CONTENT_URI;
		String selection = RichText.Columns.POINT_ID + "=?";
		String[] selectionArgs = new String[] { String.valueOf(pointId) };
		Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, selectionArgs, null);
		if (cursor.getCount() > 0) {
			Description description = new Description(cursor);
			mDesctiptionEditText.setText(description.getText());
		} else if (DEBUG) {
			Log.d(TAG, "NO associated description for point id: " + pointId);
		}
		// TODO: set empty description message
		cursor.close();
	}

	private void setupView(final boolean editable) {

		// TODO: unchecked when description are enabled
		// setDescription();
		mDesctiptionEditText.setEnabled(false);
		if (!editable) {
			mNameEditText.setEnabled(false);
			mDesctiptionEditText.setEnabled(false);
			mSideCodeSpinner.setEnabled(false);
			mSharedToPublicCheckBox.setEnabled(false);
		}
	}
}
