package com.slobodastudio.discussions.ui;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.odata.DiscussionsTableShema.Person;
import com.slobodastudio.discussions.odata.ODataConstants;
import com.slobodastudio.discussions.odata.OdataReadClient;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Map;

/** Simple list view class to show available users on server. */
public class UsersActivity extends Activity {

	private ListView lvSimple;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_list_activity);
		// FIXME: check if network is accessible
		// FIXME: move network sync off main thread!
		ArrayList<Map<String, String>> users = new OdataReadClient(ODataConstants.DISCUSSIONS_JAPAN)
				.getUsers();
		String[] from = { Person.NAME, Person.EMAIL };
		int[] to = { android.R.id.text1, android.R.id.text2 };
		SimpleAdapter sAdapter = new SimpleAdapter(this, users, android.R.layout.simple_list_item_2, from, to);
		lvSimple = (ListView) findViewById(android.R.id.list);
		lvSimple.setAdapter(sAdapter);
	}
}
