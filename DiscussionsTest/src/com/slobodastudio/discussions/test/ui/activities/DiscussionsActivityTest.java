package com.slobodastudio.discussions.test.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.ui.IntentExtrasKey;
import com.slobodastudio.discussions.ui.activities.DiscussionsActivity;
import com.slobodastudio.discussions.ui.activities.TopicsActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.jayway.android.robotium.solo.Solo;

public class DiscussionsActivityTest extends ActivityInstrumentationTestCase2 {

	private Solo solo;

	public DiscussionsActivityTest() {

		super(DiscussionsActivity.class);
		Uri uri = Persons.buildDiscussionUri(2);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.putExtra(IntentExtrasKey.PERSON_ID, 2);
		setActivityIntent(intent);
	}

	@Override
	public void tearDown() throws Exception {

		solo.finishOpenedActivities();
	}

	public void testOpenTopics() {

		solo.waitForActivity(DiscussionsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start discussion activity", DiscussionsActivity.class);
		solo.clickInList(1);
		solo.assertCurrentActivity("Failure to start activity", TopicsActivity.class);
	}

	public void testShowEmptyList() {

		Uri uri = Persons.buildDiscussionUri(1);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.putExtra(IntentExtrasKey.PERSON_ID, 1);
		solo.getCurrentActivity().startActivity(intent);
		solo.waitForActivity(DiscussionsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start discussions activity", DiscussionsActivity.class);
		assertTrue("List should be empty", solo.searchText(solo
				.getString(R.string.fragment_empty_discussions), true));
	}

	public void testShowList() {

		solo.waitForActivity(DiscussionsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start discussion activity", DiscussionsActivity.class);
		ListView list = solo.getCurrentListViews().get(0);
		assertEquals("Discussions count", 2, list.getCount());
		{
			Cursor cursor = (Cursor) list.getItemAtPosition(0);
			// assert id
			int idIndex = cursor.getColumnIndexOrThrow(Discussions.Columns.ID);
			int actualId = cursor.getInt(idIndex);
			assertEquals("Value id", 1, actualId);
			// assert name
			int nameIndex = cursor.getColumnIndexOrThrow(Discussions.Columns.SUBJECT);
			String actualName = cursor.getString(nameIndex);
			assertEquals("Value name", "Abortion", actualName);
		}
		Cursor cursor = (Cursor) list.getItemAtPosition(1);
		// assert id
		int idIndex = cursor.getColumnIndexOrThrow(Discussions.Columns.ID);
		int actualId = cursor.getInt(idIndex);
		assertEquals("Value id", 2, actualId);
		// assert name
		int nameIndex = cursor.getColumnIndexOrThrow(Discussions.Columns.SUBJECT);
		String actualName = cursor.getString(nameIndex);
		assertEquals("Value name", "News", actualName);
	}

	@Override
	protected void setUp() throws Exception {

		solo = new Solo(getInstrumentation(), getActivity());
	}
}