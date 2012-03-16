package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.service.InsertService;
import com.slobodastudio.discussions.service.UpdateService;
import com.slobodastudio.discussions.ui.IntentExtrasKey;
import com.slobodastudio.discussions.ui.fragments.BaseDetailFragment;
import com.slobodastudio.discussions.ui.fragments.PointsDetailFragment;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

public class PointsDetailsActivity extends BaseDetailActivity {

	private static final String TAG = PointsDetailsActivity.class.getSimpleName();

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_save:
				actionSave();
				return true;
			case R.id.menu_cancel:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Fragment onCreatePane() {

		BaseDetailFragment fragment = new PointsDetailFragment();
		Log.d(TAG, "[onCreatePane] intent=" + getIntent() + ", has id: "
				+ getIntent().hasExtra(IntentExtrasKey.ID));
		if (getIntent().getAction().equals(Intent.ACTION_EDIT)) {
			if (getIntent().hasExtra(IntentExtrasKey.ID)) {
				fragment.setArgumentId(getIntent().getExtras().getInt(IntentExtrasKey.ID));
			}
			return fragment;
		} else if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
			Uri uri = getIntent().getData();
			String id = uri.getLastPathSegment();
			fragment.setArgumentId(Integer.valueOf(id));
			return fragment;
		}
		throw new IllegalArgumentException("Unknown intent action: " + getIntent().getAction());
	}

	private void actionSave() {

		Log.d(TAG, "[actionSave]");
		// name
		EditText editText = (EditText) mFragment.getView().findViewById(R.id.et_point_name);
		// agreement code
		Spinner s = (Spinner) mFragment.getView().findViewById(R.id.spinner_point_agreement_code);
		// shared to public
		CheckBox checkBox = (CheckBox) mFragment.getView().findViewById(R.id.chb_share_to_public);
		int expectedAgreementCode = Points.ArgreementCode.UNSOLVED;
		byte[] expectedDrawing = new byte[] { 0, 1 };
		boolean expectedExpanded = false;
		int expectedGroupId = 1;
		String expectedNumberedPoint = "";
		int expectedPersonId = getIntent().getExtras().getInt(IntentExtrasKey.PERSON_ID);
		String expectedPointName = editText.getText().toString();
		boolean expectedSharedToPublic = checkBox.isChecked();
		int expectedSideCode;
		switch ((int) s.getSelectedItemId()) {
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
				throw new IllegalArgumentException("Unknown side code: " + (int) s.getSelectedItemId());
		}
		int expectedTopicId = getIntent().getExtras().getInt(IntentExtrasKey.TOPIC_ID);
		Intent intent;
		if (getIntent().hasExtra(IntentExtrasKey.ID)) {
			// update point
			int pointId = getIntent().getExtras().getInt(IntentExtrasKey.ID);
			Point point = new Point(expectedAgreementCode, expectedDrawing, expectedExpanded,
					expectedGroupId, pointId, expectedPointName, expectedNumberedPoint, expectedPersonId,
					expectedSharedToPublic, expectedSideCode, expectedTopicId);
			intent = new Intent(Intent.ACTION_SYNC, null, this, UpdateService.class);
			intent.putExtras(point.toBundle());
		} else {
			// new point
			Point point = new Point(expectedAgreementCode, expectedDrawing, expectedExpanded,
					expectedGroupId, 1, expectedPointName, expectedNumberedPoint, expectedPersonId,
					expectedSharedToPublic, expectedSideCode, expectedTopicId);
			intent = new Intent(Intent.ACTION_SYNC, null, this, InsertService.class);
			intent.putExtras(point.toBundle());
		}
		startService(intent);
		Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
		finish();
	}
}
