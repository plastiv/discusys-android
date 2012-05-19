package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.photon.DiscussionUser;
import com.slobodastudio.discussions.photon.PhotonServiceCallback;
import com.slobodastudio.discussions.photon.constants.PhotonConstants;
import com.slobodastudio.discussions.ui.ExtraKey;
import com.slobodastudio.discussions.ui.fragments.PointsFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PointsActivity extends BaseActivity implements PhotonServiceCallback {

	private static final String TAG = PointsActivity.class.getSimpleName();
	PointsFragment mFragment;
	private int mDiscussionId;
	private int mPersonId;
	private String mPersonName;
	private int mTopicId;

	@Override
	public void onArgPointChanged(final int pointId) {

		if (DEBUG) {
			Log.d(TAG, "[onArgPointChanged] point id: " + pointId);
		}
		mFragment.showEmtyDetails();
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
					mFragment.onActionNew();
				} else {
					Toast.makeText(this, "New button press was skipped", Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.menu_refresh:
				onRefreshCurrentTopic();
				return true;
			case R.id.menu_discussion_info:
				startDiscussionInfoActivity();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onRefreshCurrentTopic() {

		if (DEBUG) {
			Log.d(TAG, "[onRefreshCurrentTopic] topic id: " + mTopicId);
		}
		mServiceHelper.downloadPointsFromTopic(mTopicId);
	}

	@Override
	public void onStructureChanged(final int changedTopicId) {

		if (DEBUG) {
			Log.d(TAG, "[onStructureChanged] Empty. topic id: " + changedTopicId);
		}
	}

	@Override
	protected void onActivityResult(final int arg0, final int arg1, final Intent arg2) {

		Log.d(TAG, "[onActivityResult] ");
		// TODO need to call super for a fragment handled
		super.onActivityResult(arg0, arg1, arg2);
	}

	@Override
	protected void onControlServiceConnected() {

		connectPhoton();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		initFromIntentExtra();
		setContentView(R.layout.activity_points);
		FragmentManager fm = getSupportFragmentManager();
		mFragment = (PointsFragment) fm.findFragmentById(R.id.fragment_points);
	}

	private void connectPhoton() {

		if (mBound && !mService.getPhotonController().isConnected()) {
			mService.getPhotonController().connect(mDiscussionId, PhotonConstants.DB_SERVER_ADDRESS,
					mPersonName, mPersonId);
			mService.getPhotonController().getCallbackHandler().addCallbackListener(PointsActivity.this);
		}
	}

	private void initFromIntentExtra() {

		if (!getIntent().hasExtra(ExtraKey.PERSON_ID)) {
			throw new IllegalStateException("Activity intent was without person id");
		}
		if (!getIntent().hasExtra(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("Activity intent was without topic id");
		}
		if (!getIntent().hasExtra(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalStateException("Activity intent was without discussion id");
		}
		if (!getIntent().hasExtra(ExtraKey.PERSON_NAME)) {
			throw new IllegalStateException("Activity intent was without person name");
		}
		mPersonName = getIntent().getExtras().getString(ExtraKey.PERSON_NAME);
		mPersonId = getIntent().getExtras().getInt(ExtraKey.PERSON_ID);
		mTopicId = getIntent().getExtras().getInt(ExtraKey.TOPIC_ID);
		if (mTopicId == -1) {
			throw new IllegalStateException("Activity intent has illegal topic id -1");
		}
		mDiscussionId = getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID);
		if (DEBUG) {
			Log.d(TAG, "[initFromIntentExtras] personId: " + mPersonId + ", topicId: " + mTopicId
					+ ", discussionId: " + mDiscussionId + ", personName: " + mPersonName);
		}
	}

	private void startDiscussionInfoActivity() {

		int discussionId = getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
		Uri discussionUri = Discussions.buildTableUri(discussionId);
		Intent discussionInfoIntent = new Intent(Intent.ACTION_VIEW, discussionUri, this,
				DiscussionInfoActivity.class);
		startActivity(discussionInfoIntent);
	}
}
