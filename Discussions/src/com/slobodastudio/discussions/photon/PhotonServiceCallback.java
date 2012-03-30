package com.slobodastudio.discussions.photon;

public interface PhotonServiceCallback {

	void onArgPointChanged(int pointId);

	void onConnect();

	void onErrorOccured(String message);

	void onEventJoin(DiscussionUser newUser);

	void onEventLeave(DiscussionUser leftUser);

	void onRefreshCurrentTopic();

	void onStructureChanged(int topicId);
}
