package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.PersonsListFragment;

import android.support.v4.app.Fragment;

public class PersonsActivity extends BaseListActivity {

	private static final String TAG = PersonsActivity.class.getSimpleName();

	@Override
	protected Fragment onCreatePane() {

		return new PersonsListFragment();
	}
}
