package com.slobodastudio.discussions.photon;

public interface PhotonServiceCallback {

	void onConnect();

	void onErrorOccured(String message);

	void onEventJoin(DiscussionUser newUser);

	void onEventLeave(DiscussionUser leftUser);

	void onStructureChanged(int topicId);
}
