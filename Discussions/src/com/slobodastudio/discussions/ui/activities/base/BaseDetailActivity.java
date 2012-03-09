package com.slobodastudio.discussions.ui.activities.base;

import android.content.res.Configuration;
import android.os.Bundle;

public abstract class BaseDetailActivity extends BaseActivity {

	private static final String TAG = BaseDetailActivity.class.getSimpleName();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// If the screen is now in landscape mode, we can show the
			// dialog in-line with the list so we don't need this activity.
			finish();
			return;
		}
		super.onCreate(savedInstanceState);
	}
}
