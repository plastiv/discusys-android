package com.slobodastudio.discussions.test.ui.activities.readonly;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.ProviderTestData;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.ui.activities.DiscussionsActivity;
import com.slobodastudio.discussions.ui.activities.PersonsActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.jayway.android.robotium.solo.Solo;

public class PersonsActivityTest extends ActivityInstrumentationTestCase2<PersonsActivity> {

	private Solo solo;

	public PersonsActivityTest() {

		super(PersonsActivity.class);
		Uri uri = Persons.CONTENT_URI;
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		setActivityIntent(intent);
	}

	@Override
	public void tearDown() throws Exception {

		solo.finishOpenedActivities();
	}

	public void testOpenDiscussions() {

		solo.waitForActivity(PersonsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", PersonsActivity.class);
		solo.clickInList(2);
		solo.assertCurrentActivity("Failure to start activity", DiscussionsActivity.class);
	}

	public void testShowEmptyList() {

		solo.waitForActivity(PersonsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", PersonsActivity.class);
		getActivity().getContentResolver().delete(Persons.CONTENT_URI, null, null);
		assertTrue("List should be empty", solo.searchText(solo.getString(R.string.fragment_empty_persons),
				true));
		ProviderTestData.generatePersons(getActivity().getContentResolver());
	}

	public void testShowList() {

		solo.waitForActivity(PersonsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", PersonsActivity.class);
		ListView list = solo.getCurrentListViews().get(0);
		assertEquals("Items count", 13, list.getCount());
		Cursor cursor = (Cursor) list.getItemAtPosition(1);
		// assert id
		int idIndex = cursor.getColumnIndexOrThrow(Persons.Columns.ID);
		int actualId = cursor.getInt(idIndex);
		assertEquals("Value id", 2, actualId);
		// assert name
		int nameIndex = cursor.getColumnIndexOrThrow(Persons.Columns.NAME);
		String actualName = cursor.getString(nameIndex);
		assertEquals("Value name", "Muhammad", actualName);
	}

	@Override
	protected void setUp() throws Exception {

		solo = new Solo(getInstrumentation(), getActivity());
	}
}