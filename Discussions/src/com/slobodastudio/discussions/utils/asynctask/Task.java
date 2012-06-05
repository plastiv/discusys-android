package com.slobodastudio.discussions.utils.asynctask;

import android.content.res.Resources;
import android.os.AsyncTask;

public final class Task extends AsyncTask<Void, String, Boolean> {

	protected final Resources mResources;
	private String mProgressMessage;
	private ProgressTracker mProgressTracker;
	private Boolean mResult;

	/** UI Thread */
	public Task(final Resources resources) {

		// Keep reference to resources
		mResources = resources;
		// Initialise initial pre-execute message
		// mProgressMessage = resources.getString(com.mnm.asynctaskmanager.R.string.task_starting);
	}

	/** UI Thread */
	public void setProgressTracker(final ProgressTracker progressTracker) {

		// Attach to progress tracker
		mProgressTracker = progressTracker;
		// Initialise progress tracker with current task state
		if (mProgressTracker != null) {
			mProgressTracker.onProgress(mProgressMessage);
			if (mResult != null) {
				mProgressTracker.onComplete();
			}
		}
	}

	/** Separate Thread */
	@Override
	protected Boolean doInBackground(final Void... arg0) {

		// Working in separate thread
		for (int i = 10; i > 0; --i) {
			// Check if task is cancelled
			if (isCancelled()) {
				// This return causes onPostExecute call on UI thread
				return false;
			}
			try {
				// This call causes onProgressUpdate call on UI thread
				// publishProgress(mResources.getString(com.mnm.asynctaskmanager.R.string.task_working, i));
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				// This return causes onPostExecute call on UI thread
				return false;
			}
		}
		// This return causes onPostExecute call on UI thread
		return true;
	}

	/** UI Thread */
	@Override
	protected void onCancelled() {

		// Detach from progress tracker
		mProgressTracker = null;
	}

	/** UI Thread */
	@Override
	protected void onPostExecute(final Boolean result) {

		// Update result
		mResult = result;
		// And send it to progress tracker
		if (mProgressTracker != null) {
			mProgressTracker.onComplete();
		}
		// Detach from progress tracker
		mProgressTracker = null;
	}

	/** UI Thread */
	@Override
	protected void onProgressUpdate(final String... values) {

		// Update progress message
		mProgressMessage = values[0];
		// And send it to progress tracker
		if (mProgressTracker != null) {
			mProgressTracker.onProgress(mProgressMessage);
		}
	}
}