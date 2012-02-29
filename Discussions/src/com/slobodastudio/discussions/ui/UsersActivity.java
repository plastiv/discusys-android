package com.slobodastudio.discussions.ui;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.odata.DiscussionsTableShema.Person;
import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataReadClient;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

/** Simple list view class to show available users on server. */
public class UsersActivity extends BaseListActivity implements OnItemClickListener {

	private static final String TAG = "UsersActivity";
	ArrayList<Map<String, Object>> users;

	static OEntity addNewValue(final String value) {

		return new OdataWriteClient(ODataConstants.DISCUSSIONS_JAPAN).insertPerson(value,
				"test@from.android", Integer.valueOf(Color.CYAN));
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
				+ users.get(position).get(Person._ID));
		// Uri selectedPerson = ContentUris.withAppendedId(People.CONTENT_URI, id);
		// Intent intent = new Intent(Intent.ACTION_VIEW, selectedPerson);
		// startActivity(intent);
		Intent intent = new Intent(this, DiscussionsActivity.class);
		intent.putExtra("id", ((Integer) users.get(position).get(Person._ID)).intValue());
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

		final CharSequence[] items = { "Android", "New value", "Same old" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a user name");
		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int item) {

				Toast.makeText(getApplicationContext(), "Adding new user: " + items[item], Toast.LENGTH_SHORT)
						.show();
				addNewValue((String) items[item]);
				updateListValues();
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	void updateListValues() {

		users = new OdataReadClient(ODataConstants.DISCUSSIONS_JAPAN).getUsers();
		if (users.size() > 0) {
			updateListValues(users, Person.NAME);
		} else {
			Toast.makeText(this, "No users on server. You can create a new one", Toast.LENGTH_LONG).show();
		}
	}
}
