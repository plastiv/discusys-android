package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.fragments.BaseListFragment;
import com.slobodastudio.discussions.ui.fragments.TopicsListFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class TopicsActivity extends BaseListActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// add fragment to activity
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		BaseListFragment fragment = new TopicsListFragment();
		fragmentTransaction.add(R.id.frament_frame_layout, fragment);
		fragmentTransaction.commit();
	}
}
