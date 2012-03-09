package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.TopicsDetailFragment;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class TopicsDetailsActivity extends BasePanelActivity {

	@Override
	protected Fragment onCreatePane() {

		return new TopicsDetailFragment();
	}

	public static class DetailsActivity extends Activity {

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
}
