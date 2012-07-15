package com.slobodastudio.discussions.photon;

import com.slobodastudio.discussions.data.model.ArgPointChanged;

public interface PhotonServiceCallback {

	void onArgPointChanged(ArgPointChanged argPointChanged);

	void onConnect();

	void onErrorOccured(String message);

	void onEventJoin(DiscussionUser newUser);

	void onEventLeave(DiscussionUser leftUser);

	void onRefreshCurrentTopic();

	void onStructureChanged(int topicId);
}
