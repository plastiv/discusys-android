package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.photon.DiscussionUser;
import com.slobodastudio.discussions.photon.PhotonServiceCallback;

import android.util.Log;

public class ResolverPhotonServiceCallback implements PhotonServiceCallback {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = ResolverPhotonServiceCallback.class.getSimpleName();
	private final ServiceHelper mServiceHelper;

	public ResolverPhotonServiceCallback(final ServiceHelper serviceHelper) {

		super();
		mServiceHelper = serviceHelper;
	}

	@Override
	public void onArgPointChanged(final int pointId) {

		if (DEBUG) {
			Log.d(TAG, "[onArgPointChanged] point id: " + pointId);
		}
		mServiceHelper.updatePoint(pointId);
	}

	@Override
	public void onConnect() {

		if (DEBUG) {
			Log.d(TAG, "[onConnect] empty");
		}
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
	public void onRefreshCurrentTopic() {

		if (DEBUG) {
			Log.d(TAG, "[onRefreshCurrentTopic] Empty. ");
		}
	}

	@Override
	public void onStructureChanged(final int topicId) {

		if (DEBUG) {
			Log.d(TAG, "[onStructureChanged] topic id: " + topicId);
		}
		mServiceHelper.downloadPointsFromTopic(topicId);
	}
}
