package com.slobodastudio.discussions.service;

public interface OdataSyncResultListener {

	void handleError(String message);

	void updateSyncStatus(boolean syncing);
}
