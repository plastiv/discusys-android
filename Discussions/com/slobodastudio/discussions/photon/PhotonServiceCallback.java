package com.slobodastudio.discussions.photon;

public interface PhotonServiceCallback {

	void errorOccurred(String message);

	void loginDone(boolean ok);

	void onStructureChanged(final int topicId);
}
