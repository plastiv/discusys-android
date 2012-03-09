package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class BasePanelActivity extends BaseActivity {

	private static final String TAG = BasePanelActivity.class.getSimpleName();
	private Fragment mFragment;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(com.slobodastudio.discussions.R.layout.base_activity);
		// TODO: how to set custom title? onTitleChanged(getTitle(), 0);
		// final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
		// getActivityHelper().setActionBarTitle(customTitle != null ? customTitle : getTitle());
		Log.v(TAG, "Oncreate");
		if (savedInstanceState == null) {
			mFragment = onCreatePane();
			mFragment.setArguments(intentToFragmentArguments(getIntent()));
			getSupportFragmentManager().beginTransaction().add(R.id.frame_layout_list, mFragment).commit();
		}
		Log.v(TAG, "onCreate done");
	}

	/** Called in <code>onCreate</code> when the fragment constituting this activity is needed. The returned
	 * fragment's arguments will be set to the intent used to invoke this activity. */
	protected abstract Fragment onCreatePane();
}
