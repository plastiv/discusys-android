package com.slobodastudio.discussions.test.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.ui.IntentExtrasKey;
import com.slobodastudio.discussions.ui.activities.PointsActivity;
import com.slobodastudio.discussions.ui.activities.TopicsActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.jayway.android.robotium.solo.Solo;

public class TopicsActivityTest extends ActivityInstrumentationTestCase2 {

	private Solo solo;

	public TopicsActivityTest() {

		super(TopicsActivity.class);
		Uri uri = Discussions.buildTopicUri(1);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.putExtra(IntentExtrasKey.PERSON_ID, 2);
		setActivityIntent(intent);
	}

	@Override
	public void tearDown() throws Exception {

		solo.finishOpenedActivities();
	}

	public void testOpenPoints() {

		solo.waitForActivity(TopicsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", TopicsActivity.class);
		solo.clickInList(1);
		solo.assertCurrentActivity("Failure to start activity", PointsActivity.class);
	}

	public void testShowEmptyList() {

		Uri uri = Discussions.buildTopicUri(3);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.putExtra(IntentExtrasKey.PERSON_ID, 2);
		setActivityIntent(intent);
		solo.getCurrentActivity().startActivity(intent);
		solo.waitForActivity(TopicsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", TopicsActivity.class);
		assertTrue("List should be empty", solo.searchText(solo.getString(R.string.fragment_empty_topics),
				true));
	}

	public void testShowList() {

		solo.waitForActivity(TopicsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", TopicsActivity.class);
		ListView list = solo.getCurrentListViews().get(0);
		assertEquals("Topics count", 1, list.getCount());
		Cursor cursor = (Cursor) list.getItemAtPosition(0);
		// assert id
		int idIndex = cursor.getColumnIndexOrThrow(Topics.Columns.ID);
		int actualId = cursor.getInt(idIndex);
		assertEquals("Value id", 1, actualId);
		// assert name
		int nameIndex = cursor.getColumnIndexOrThrow(Topics.Columns.NAME);
		String actualName = cursor.getString(nameIndex);
		assertEquals("Value name", "Abortion pro and cons", actualName);
	}

	@Override
	protected void setUp() throws Exception {

		solo = new Solo(getInstrumentation(), getActivity());
	}
}