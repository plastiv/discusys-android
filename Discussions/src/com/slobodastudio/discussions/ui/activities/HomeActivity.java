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
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.service.SyncService;
import com.slobodastudio.discussions.utils.AnalyticsUtils;
import com.slobodastudio.discussions.utils.DetachableResultReceiver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.widget.Toast;

/** Front-door {@link Activity} that displays high-level features the schedule application offers to users.
 * Depending on whether the device is a phone or an Android 3.0+ tablet, different layouts will be used. For
 * example, on a phone, the primary content is a {@link DashboardFragment}, whereas on a tablet, both a
 * {@link DashboardFragment} and a {@link TagStreamFragment} are displayed. */
public class HomeActivity extends BaseActivity {

	private static final String TAG = "HomeActivity";
	private SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (item.getItemId() == R.id.menu_refresh) {
			triggerRefresh();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		AnalyticsUtils.getInstance(this).trackPageView("/Home");
		setContentView(R.layout.base_activity);
		FragmentManager fm = getSupportFragmentManager();
		mSyncStatusUpdaterFragment = (SyncStatusUpdaterFragment) fm
				.findFragmentByTag(SyncStatusUpdaterFragment.TAG);
		if (mSyncStatusUpdaterFragment == null) {
			mSyncStatusUpdaterFragment = new SyncStatusUpdaterFragment();
			fm.beginTransaction().add(mSyncStatusUpdaterFragment, SyncStatusUpdaterFragment.TAG).commit();
			triggerRefresh();
		}
		Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
		startActivity(intent);
		finish();
	}

	private void triggerRefresh() {

		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, SyncService.class);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mSyncStatusUpdaterFragment.mReceiver);
		startService(intent);
	}

	private void updateRefreshStatus(final boolean refreshing) {

		getActionBarHelper().setRefreshActionItemState(refreshing);
	}

	/** A non-UI fragment, retained across configuration changes, that updates its activity's UI when sync
	 * status changes. */
	public static class SyncStatusUpdaterFragment extends Fragment implements
			DetachableResultReceiver.Receiver {

		public static final String TAG = SyncStatusUpdaterFragment.class.getName();
		private DetachableResultReceiver mReceiver;
		private boolean mSyncing = false;

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {

			super.onActivityCreated(savedInstanceState);
			((HomeActivity) getActivity()).updateRefreshStatus(mSyncing);
		}

		@Override
		public void onCreate(final Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
			setRetainInstance(true);
			mReceiver = new DetachableResultReceiver(new Handler());
			mReceiver.setReceiver(this);
		}

		/** {@inheritDoc} */
		@Override
		public void onReceiveResult(final int resultCode, final Bundle resultData) {

			HomeActivity activity = (HomeActivity) getActivity();
			if (activity == null) {
				return;
			}
			switch (resultCode) {
				case SyncService.STATUS_RUNNING: {
					mSyncing = true;
					break;
				}
				case SyncService.STATUS_FINISHED: {
					mSyncing = false;
					break;
				}
				case SyncService.STATUS_ERROR: {
					// Error happened down in SyncService, show as toast.
					mSyncing = false;
					final String errorText = getString(R.string.toast_sync_error, resultData
							.getString(Intent.EXTRA_TEXT));
					Toast.makeText(activity, errorText, Toast.LENGTH_LONG).show();
					break;
				}
			}
			activity.updateRefreshStatus(mSyncing);
		}
	}
}
