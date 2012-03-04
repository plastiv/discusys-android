package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataSyncService;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, this);
		service.downloadValues(Persons.TABLE_NAME, Persons.CONTENT_URI);
		// service.downloadTopics();
		Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
		startActivity(intent);
		finish();
	}
}
