package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.photon.DiscussionUser;
import com.slobodastudio.discussions.photon.PhotonServiceCallback;
import com.slobodastudio.discussions.photon.constants.PhotonConstants;
import com.slobodastudio.discussions.ui.IntentExtrasKey;
import com.slobodastudio.discussions.ui.fragments.PointsFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PointsActivity extends BaseActivity implements PhotonServiceCallback {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = PointsActivity.class.getSimpleName();
	private int discussionId;
	private int personId;
	private String personName;
	private int topicId;

	@Override
	public void onArgPointChanged(final int pointId) {

		if (DEBUG) {
			Log.d(TAG, "[onArgPointChanged] point id: " + pointId);
		}
		((PointsFragment) mFragment).showEmtyDetails();
	}

	@Override
	public void onConnect() {

		if (DEBUG) {
			Log.d(TAG, "[onConnect] Empty. ");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_points, menu);
		// Calling super after populating the menu is necessary here to ensure that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onErrorOccured(final String message) {

		Log.e(TAG, "[onErrorOccured] Empty. message: " + message);
	}

	@Override
	public void onEventJoin(final DiscussionUser newUser) {

		if (DEBUG) {
			Log.d(TAG, "[onEventJoin] Empty. user come: " + newUser.getUserName());
		}
	}

	@Override
	public void onEventLeave(final DiscussionUser leftUser) {

		if (DEBUG) {
			Log.d(TAG, "[onEventLeave] Empty. user left: " + leftUser.getUserName());
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (DEBUG) {
			Log.d(TAG, "[onOptionsItemSelected] item id: " + item.getItemId());
		}
		switch (item.getItemId()) {
			case R.id.menu_new:
				if (mFragment != null) {
					((PointsFragment) mFragment).onActionNew();
				} else {
					Toast.makeText(this, "New button press was skipped", Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.menu_refresh:
				onRefreshCurrentTopic();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onRefreshCurrentTopic() {

		if (DEBUG) {
			Log.d(TAG, "[onRefreshCurrentTopic] topic id: " + topicId);
		}
		mServiceHelper.downloadPointsFromTopic(topicId);
	}

	@Override
	public void onStructureChanged(final int topicId) {

		if (DEBUG) {
			Log.d(TAG, "[onStructureChanged] Empty. topic id: " + topicId);
		}
	}

	@Override
	protected void onControlServiceConnected() {

		connectPhoton();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		initFromIntentExtra();
	}

	@Override
	protected Fragment onCreatePane() {

		return new PointsFragment();
	}

	private void connectPhoton() {

		if (mBound && !mService.getPhotonController().isConnected()) {
			mService.getPhotonController().connect(discussionId, PhotonConstants.DB_SERVER_ADDRESS,
					personName, personId);
			mService.getPhotonController().getCallbackHandler().addCallbackListener(PointsActivity.this);
		}
	}

	private void initFromIntentExtra() {

		if (!getIntent().hasExtra(IntentExtrasKey.PERSON_ID)) {
			throw new IllegalStateException("Activity intent was without person id");
		}
		if (!getIntent().hasExtra(IntentExtrasKey.TOPIC_ID)) {
			throw new IllegalStateException("Activity intent was without topic id");
		}
		if (!getIntent().hasExtra(IntentExtrasKey.DISCUSSION_ID)) {
			throw new IllegalStateException("Activity intent was without discussion id");
		}
		if (!getIntent().hasExtra(IntentExtrasKey.PERSON_NAME)) {
			throw new IllegalStateException("Activity intent was without person name");
		}
		personName = getIntent().getExtras().getString(IntentExtrasKey.PERSON_NAME);
		personId = getIntent().getExtras().getInt(IntentExtrasKey.PERSON_ID);
		topicId = getIntent().getExtras().getInt(IntentExtrasKey.TOPIC_ID);
		if (topicId == -1) {
			throw new IllegalStateException("Activity intent has illegal topic id -1");
		}
		discussionId = getIntent().getExtras().getInt(IntentExtrasKey.DISCUSSION_ID);
		if (DEBUG) {
			Log.d(TAG, "[initFromIntentExtras] personId: " + personId + ", topicId: " + topicId
					+ ", discussionId: " + discussionId + ", personName: " + personName);
		}
	}
}
