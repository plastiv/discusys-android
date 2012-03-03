package com.slobodastudio.discussions.ui.old;

import com.slobodastudio.discussions.R;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class TopicsActivity extends BaseListActivity implements OnItemClickListener {

	private static final String TAG = "TopicsActivity";
	private ArrayList<Map<String, Object>> topics;

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getListView().setOnItemClickListener(this);
		updateTopicsList();
	}

	@Override
	public void onItemClick(final AdapterView<?> adView, final View target, final int position, final long id) {

		// Log.v(TAG, "in onItemClick with " + " Position = " + position + ". Id = "
		// + topics.get(position).get(Topic._ID));
		// Uri selectedPerson = ContentUris.withAppendedId(People.CONTENT_URI, id);
		// Intent intent = new Intent(Intent.ACTION_VIEW, selectedPerson);
		// startActivity(intent);
		// Intent intent = new Intent(this, PointsActivity.class);
		// intent.putExtra("id", ((Integer) topics.get(position).get(Topic._ID)).intValue());
		// intent.putExtra(IntentParameterKeys.PERSON_ID,
		// getIntent().getIntExtra(IntentParameterKeys.PERSON_ID,
		// -1));
		// startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				updateTopicsList();
				return true;
			case R.id.menu_addNew:
				Toast.makeText(this, "Didnt implemented yet because of many-to-many relation ship ",
						Toast.LENGTH_LONG).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void updateTopicsList() {

		// if (getIntent().hasExtra("id")) {
		// int discussionId = getIntent().getIntExtra("id", -1);
		// Log.v(TAG, "discussionId: " + discussionId);
		// topics = new OdataReadClient(ODataConstants.DISCUSSIONS_JAPAN).getTopics(discussionId);
		// } else {
		// topics = new OdataReadClient(ODataConstants.DISCUSSIONS_JAPAN).getTopics();
		// }
		// if (topics.size() > 0) {
		// updateListValues(topics, Topic.NAME);
		// } else {
		// Toast.makeText(this,
		// "No assosiated topics for this disscussionId: " + getIntent().getIntExtra("id", -1),
		// Toast.LENGTH_LONG).show();
		// finish();
		// }
	}
}
