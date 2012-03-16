package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.service.SyncService;
import com.slobodastudio.discussions.ui.IntentExtrasKey;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class PointDetailFragment extends SherlockFragment {

	private static final boolean DEBUG = true && ApplicationConstants.DEBUG_MODE;
	private static final int INVALID_POINT_ID = Integer.MIN_VALUE;
	private static final String TAG = PointDetailFragment.class.getSimpleName();
	private static final int TYPE_DIR = 1;
	private static final int TYPE_ITEM = 0;
	private EditText mNameEditText;
	private CheckBox mSharedToPublicCheckBox;
	private Spinner mSideCodeSpinner;
	private int personId;
	private int pointId = INVALID_POINT_ID;
	private int topicId;
	private int typeId;

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
		Intent intent;
		if (pointId != INVALID_POINT_ID) {
			// update point
			Point point = new Point(expectedAgreementCode, expectedDrawing, expectedExpanded,
					expectedGroupId, pointId, expectedPointName, expectedNumberedPoint, expectedPersonId,
					expectedSharedToPublic, expectedSideCode, expectedTopicId);
			intent = new Intent(SyncService.ACTION_UPDATE);
			intent.putExtras(point.toBundle());
		} else {
			// new point
			Point point = new Point(expectedAgreementCode, expectedDrawing, expectedExpanded,
					expectedGroupId, 1, expectedPointName, expectedNumberedPoint, expectedPersonId,
					expectedSharedToPublic, expectedSideCode, expectedTopicId);
			intent = new Intent(SyncService.ACTION_INSERT);
			intent.putExtras(point.toBundle());
		}
		getActivity().startService(intent);
		Toast.makeText(getActivity(), getActivity().getString(R.string.toast_saved), Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		if (container == null) {
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
			Log.d(TAG, "[onCreateView] intent: " + getActivity().getIntent());
		}
		Intent intent = getActivity().getIntent();
		Uri uri = intent.getData();
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
		mSideCodeSpinner = (Spinner) layout.findViewById(R.id.spinner_point_agreement_code);
		mSharedToPublicCheckBox = (CheckBox) layout.findViewById(R.id.chb_share_to_public);
		// fill in data
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_EDIT)) {
			switch (typeId) {
				case TYPE_DIR:
					// leave empty fields to create new point
					if (getActivity().getIntent().hasExtra(IntentExtrasKey.PERSON_ID)) {
						personId = getActivity().getIntent().getExtras().getInt(IntentExtrasKey.PERSON_ID);
					} else {
						throw new IllegalStateException("intent was without person id");
					}
					if (getActivity().getIntent().hasExtra(IntentExtrasKey.TOPIC_ID)) {
						topicId = getActivity().getIntent().getExtras().getInt(IntentExtrasKey.TOPIC_ID);
					} else {
						throw new IllegalStateException("intent was without topic id");
					}
					pointId = INVALID_POINT_ID;
					break;
				case TYPE_ITEM: {
					Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
					if (cursor.getCount() == 1) {
						Point value = new Point(cursor);
						pointId = value.getId();
						personId = value.getPersonId();
						topicId = value.getTopicId();
						mNameEditText.setText(value.getName());
						mSideCodeSpinner.setSelection(value.getAgreementCode());
						mSharedToPublicCheckBox.setChecked(value.isSharedToPublic());
					} else {
						throw new IllegalStateException("Expected single value in cursor, was: "
								+ cursor.getCount());
					}
					break;
				}
				default:
					throw new IllegalArgumentException("Unknown type id: " + typeId);
			}
		} else if (action.equals(Intent.ACTION_VIEW)) {
			switch (typeId) {
				case TYPE_DIR:
					throw new IllegalStateException("Should not reach here. No sense to view empty point");
				case TYPE_ITEM: {
					Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
					if (cursor.getCount() == 1) {
						Point value = new Point(cursor);
						mNameEditText.setText(value.getName());
						mNameEditText.setEnabled(false);
						mSideCodeSpinner.setSelection(value.getAgreementCode());
						mSideCodeSpinner.setEnabled(false);
						mSharedToPublicCheckBox.setChecked(value.isSharedToPublic());
						mSharedToPublicCheckBox.setEnabled(false);
					} else {
						throw new IllegalStateException("Expected single value in cursor, was: "
								+ cursor.getCount());
					}
					break;
				}
				default:
					throw new IllegalArgumentException("Unknown type id: " + typeId);
			}
		} else {
			throw new IllegalArgumentException("Unknown action: " + action);
		}
		return layout;
	}
}
