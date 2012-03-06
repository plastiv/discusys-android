package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.TablesTestData;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		TablesTestData.generateData(this);
		Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
		startActivity(intent);
		finish();
	}
}
