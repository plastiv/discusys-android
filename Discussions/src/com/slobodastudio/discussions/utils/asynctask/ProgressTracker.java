package com.slobodastudio.discussions.utils.asynctask;

public interface ProgressTracker {

	// Notifies about task completeness
	void onComplete();

	// Updates progress message
	void onProgress(String message);
}