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

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. Reads data from both
 * local {@link Resources} and from remote sources, such as a spreadsheet. */
public class UpdateService extends IntentService {

	private static final boolean DEBUG = true && ApplicationConstants.DEBUG_MODE;
	private static final String TAG = UpdateService.class.getSimpleName();

	public UpdateService() {

		super(TAG);
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		if (DEBUG) {
			Log.d(TAG, "[onHandleIntent] intent: " + intent.toString());
		}
		try {
			// Bulk of sync work, performed by executing several fetches from
			// local and online sources.
			OdataWriteClient odataWrite = new OdataWriteClient(ODataConstants.SERVICE_URL_JAPAN);
			odataWrite.updatePoint(new Point(intent.getExtras()));
		} catch (Exception e) {
			Log.e(TAG, "Problem while syncing", e);
		}
	}
}
