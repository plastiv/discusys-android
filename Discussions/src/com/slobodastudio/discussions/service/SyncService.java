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
package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.data.ProviderTestData;
import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataSyncService;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. Reads data from both
 * local {@link Resources} and from remote sources, such as a spreadsheet. */
public class SyncService extends IntentService {

	public static final String EXTRA_STATUS_RECEIVER = "com.uniquie.name.STATUS_RECEIVER";
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;
	public static final int STATUS_RUNNING = 0x1;
	private static final String TAG = "SyncService";

	public SyncService() {

		super(TAG);
	}

	@Override
	public void onCreate() {

		super.onCreate();
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");
		final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		if (receiver != null) {
			receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		}
		try {
			// Bulk of sync work, performed by executing several fetches from
			// local and online sources.
			// TODO: download values here
			// ProviderTestData.generateData(this);
			ProviderTestData.deleteData(this);
			OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, this);
			service.downloadAllValues();
		} catch (Exception e) {
			Log.e(TAG, "Problem while syncing", e);
			if (receiver != null) {
				// Pass back error to surface listener
				final Bundle bundle = new Bundle();
				bundle.putString(Intent.EXTRA_TEXT, e.toString());
				receiver.send(STATUS_ERROR, bundle);
			}
		}
		// Announce success to any surface listener
		Log.d(TAG, "sync finished");
		if (receiver != null) {
			receiver.send(STATUS_FINISHED, Bundle.EMPTY);
		}
	}
}
