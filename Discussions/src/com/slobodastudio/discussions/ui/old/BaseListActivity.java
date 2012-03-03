package com.slobodastudio.discussions.ui.old;

import com.slobodastudio.discussions.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseListActivity extends ListActivity {

	private static final String TAG = "BaseListActivity";
	private ArrayAdapter<String> mAdapter;
	private List<String> mListValues;

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_list_activity);
		// FIXME: check if network is accessible
		// FIXME: move network sync off main thread!
		mListValues = new ArrayList<String>();
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListValues);
		setListAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {

		MenuInflater inflater = getMenuInflater(); // from activity
		inflater.inflate(R.menu.operation_menu, menu);
		// It is important to return true to see the menu
		return true;
	}

	protected void updateListValues(final ArrayList<Map<String, Object>> mEntityValues, final String valueKey) {

		mListValues.clear();
		for (Map<String, Object> map : mEntityValues) {
			mListValues.add((String) map.get(valueKey));
		}
		mAdapter.notifyDataSetChanged();
	}
}
