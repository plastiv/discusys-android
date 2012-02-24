package com.slobodastudio.discussions.ui;

import com.slobodastudio.discussions.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class PointsActivity extends ListActivity {

	String[] listItems = { "item 1", "item 2 ", "list", "android", "item 3", "foobar", "bar", };

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_list_activity);
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems));
	}
}
