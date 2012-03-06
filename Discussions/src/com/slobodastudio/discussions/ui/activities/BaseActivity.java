/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.ui.actionbar.ActionBarFragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/** A base activity that defers common functionality across app activities. This class shouldn't be used
 * directly; instead, activities should inherit from {@link BaseSinglePaneActivity} or
 * {@link BaseMultiPaneActivity}. */
public abstract class BaseActivity extends ActionBarFragmentActivity {

	/** Converts a fragment arguments bundle into an intent. */
	public static Intent fragmentArgumentsToIntent(final Bundle arguments) {

		Intent intent = new Intent();
		if (arguments == null) {
			return intent;
		}
		final Uri data = arguments.getParcelable("_uri");
		if (data != null) {
			intent.setData(data);
		}
		intent.putExtras(arguments);
		intent.removeExtra("_uri");
		return intent;
	}

	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
	public static Bundle intentToFragmentArguments(final Intent intent) {

		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}
		final Uri data = intent.getData();
		if (data != null) {
			arguments.putParcelable("_uri", data);
		}
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			arguments.putAll(intent.getExtras());
		}
		return arguments;
	}

	/** Invoke "home" action, returning to {@link HomeActivity}. */
	public void goHome() {

		// FIXME : handle if this is HomeActivity actually
		// if ( instanceof HomeActivity) {
		// return;
		// }
		final Intent intent = new Intent(this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/** Invoke "search" action, triggering a default search. */
	public void goSearch() {

		startSearch(null, false, Bundle.EMPTY, false);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {

		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.actionbar_menu, menu);
		// Calling super after populating the menu is necessary here to ensure that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return false || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(final int keyCode, final KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goHome();
			return true;
		}
		return false || super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
				break;
			case R.id.menu_refresh:
				Toast.makeText(this, "Fake refreshing...", Toast.LENGTH_SHORT).show();
				getActionBarHelper().setRefreshActionItemState(true);
				getWindow().getDecorView().postDelayed(new Runnable() {

					@Override
					public void run() {

						getActionBarHelper().setRefreshActionItemState(false);
					}
				}, 1000);
				break;
			case R.id.menu_new:
				Toast.makeText(this, "Tapped new", Toast.LENGTH_SHORT).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Takes a given intent and either starts a new activity to handle it (the default behavior), or
	 * creates/updates a fragment (in the case of a multi-pane activity) that can handle the intent.
	 * 
	 * Must be called from the main (UI) thread. */
	public void openActivityOrFragment(final Intent intent) {

		// Default implementation simply calls startActivity
		startActivity(intent);
	}
}
