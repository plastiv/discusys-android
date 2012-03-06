package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.ui.fragments.PersonsListFragment;

import android.support.v4.app.Fragment;

public class PersonsActivity extends BasePanelActivity {

	@Override
	protected Fragment onCreatePane() {

		return new PersonsListFragment();
	}
}
