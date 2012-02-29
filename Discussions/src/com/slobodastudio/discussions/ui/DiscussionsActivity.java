package com.slobodastudio.discussions.ui;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.odata.DiscussionsTableShema.Discussion;
import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataReadClient;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import org.odata4j.core.OEntity;

import java.util.ArrayList;
import java.util.Map;

public class DiscussionsActivity extends BaseListActivity implements OnItemClickListener {

	private static final String TAG = "DiscussionsActivity";
	ArrayList<Map<String, Object>> discussions;

	static OEntity addNewValue(final String value) {

		return new OdataWriteClient(ODataConstants.DISCUSSIONS_JAPAN).insertDiscussion(value);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getListView().setOnItemClickListener(this);
		updateListValues();
	}

	@Override
	public void onItemClick(final AdapterView<?> adView, final View target, final int position, final long id) {

		Log.v(TAG, "in onItemClick with " + " Position = " + position + ". Id = "
				+ discussions.get(position).get(Discussion._ID));
		// Uri selectedPerson = ContentUris.withAppendedId(People.CONTENT_URI, id);
		// Intent intent = new Intent(Intent.ACTION_VIEW, selectedPerson);
		// startActivity(intent);
		Intent intent = new Intent(this, TopicsActivity.class);
		intent.putExtra("id", ((Integer) discussions.get(position).get(Discussion._ID)).intValue());
		intent.putExtra(UriParameterKey.PERSON_ID, getIntent().getIntExtra("id", -1));
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				updateListValues();
				return true;
			case R.id.menu_addNew:
				onAddNewClick();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void onAddNewClick() {

		final CharSequence[] items = { "New discussion", "One more", "Same old discussion" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a discussion name");
		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int item) {

				Toast.makeText(getApplicationContext(), "Adding new discussion: " + items[item],
						Toast.LENGTH_SHORT).show();
				addNewValue((String) items[item]);
				updateListValues();
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void updateListValues() {

		if (getIntent().hasExtra("id")) {
			int userId = getIntent().getIntExtra("id", -1);
			discussions = new OdataReadClient(ODataConstants.DISCUSSIONS_JAPAN).getDiscussions(userId);
		} else {
			discussions = new OdataReadClient(ODataConstants.DISCUSSIONS_JAPAN).getDiscussions();
		}
		if (discussions.size() > 0) {
			updateListValues(discussions, Discussion.SUBJECT);
		} else {
			Toast.makeText(this,
					"No assosiated discussions for this userId: " + getIntent().getIntExtra("id", -1),
					Toast.LENGTH_LONG).show();
			finish();
		}
	}
}
