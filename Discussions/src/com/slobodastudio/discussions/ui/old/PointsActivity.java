package com.slobodastudio.discussions.ui.old;

import com.slobodastudio.discussions.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class PointsActivity extends BaseListActivity implements OnItemClickListener {

	private static final String TAG = "PointsActivity";
	private ArrayList<Map<String, Object>> points;

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getListView().setOnItemClickListener(this);
		updateListValues();
	}

	@Override
	public void onItemClick(final AdapterView<?> adView, final View target, final int position, final long id) {

		// Log.v(TAG, "in onItemClick with " + " Position = " + position + ". Id = "
		// + points.get(position).get(Point._ID));
		// Uri selectedPerson = ContentUris.withAppendedId(People.CONTENT_URI, id);
		// Intent intent = new Intent(Intent.ACTION_VIEW, selectedPerson);
		// startActivity(intent);
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

		final CharSequence[] items = { "New point", "One more point", "Same old point" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Pick a point name");
		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int item) {

				Toast.makeText(getApplicationContext(), "Adding new point: " + items[item],
						Toast.LENGTH_SHORT).show();
				addNewValue((String) items[item]);
				updateListValues();
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	void addNewValue(final String value) {

		// FIXME : return new OdataWriteClient(ODataConstants.DISCUSSIONS_JAPAN).insertPoint(value, 0, 1,
		// true,
		// getIntent().getIntExtra("id", -1), getIntent().getIntExtra(IntentParameterKeys.PERSON_ID, -1));
	}

	void updateListValues() {

		// if (getIntent().hasExtra("id")) {
		// int topicId = getIntent().getIntExtra("id", -1);
		// points = new OdataReadClient(ODataConstants.DISCUSSIONS_JAPAN).getPoints(topicId);
		// } else {
		// points = new OdataReadClient(ODataConstants.DISCUSSIONS_JAPAN).getPoints();
		// }
		// if (points.size() > 0) {
		// updateListValues(points, Point.Columns.POINT_NAME);
		// } else {
		// Toast.makeText(this,
		// "No assosiated arg points for this topicId: " + getIntent().getIntExtra("id", -1),
		// Toast.LENGTH_LONG).show();
		// finish();
		// }
	}
}
