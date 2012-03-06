package com.slobodastudio.discussions.data.provider.test;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class DiscussionsTableTest extends ProviderTestCase2<DiscussionsProvider> {

	private static final Uri tableUri = Discussions.CONTENT_URI;

	public DiscussionsTableTest() {

		super(DiscussionsProvider.class, DiscussionsProvider.class.getName());
	}

	static Uri insertValidValue(final int valueId, final ContentProvider provider) {

		return provider.insert(tableUri, getTestValue(valueId));
	}

	private static ContentValues getTestValue(final int discussionId) {

		final ContentValues cv = new ContentValues();
		cv.put(Discussions.Columns.DISCUSSION_ID, Integer.valueOf(discussionId));
		cv.put(Discussions.Columns.SUBJECT, "Discussion subject");
		return cv;
	}

	public void testInsert() {

		insertValidValue(1);
		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail("Didnt insert test value in table");
		}
		assertEquals("Should be one associated value, was: " + cursor.getCount(), 1, cursor.getCount());
	}

	public void testInsertValueTwice() {

		// same value should be overwrite
		insertValidValue(1);
		insertValidValue(1);
		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		assertEquals(1, cursor.getCount());
		// second value should writes into table
		insertValidValue(2);
		cursor = getProvider().query(tableUri, null, null, null, null);
		assertEquals(2, cursor.getCount());
	}

	public void testInsertWrongValue() {

		final ContentValues cv = new ContentValues();
		cv.put(Discussions.Columns.DISCUSSION_ID, Integer.valueOf(1));
		// subject required
		try {
			getProvider().insert(tableUri, cv);
			fail();
		} catch (SQLiteConstraintException e) {
			assertTrue(true);
		}
	}

	public void testIsEmpty() {

		final Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		assertEquals("Table should be empty, was rows: " + cursor.getCount(), 0, cursor.getCount());
	}

	public void testQuery() {

		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		assertNotNull(cursor);
		cursor = null;
		try {
			cursor = getProvider().query(
					Uri.parse("content://com.slobodastudio.discussions.wrong_content_uri"), null, null, null,
					null);
			// we're wrong if we get until here!
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	public void testQueryFromPersons() {

		// insert valid value
		getProvider().insert(tableUri, getTestValue(100));
		getProvider().insert(Persons.CONTENT_URI, PersonsTableTest.getTestValue(123));
		getProvider().insert(Topics.CONTENT_URI, TopicsTableTest.getTestValue(400, 100));
		getProvider().insert(Persons.buildTopicUri("not important"),
				TopicsTableTest.getTestPersonTopicValue(123, 400));
		// insert 2nd valid value
		getProvider().insert(tableUri, getTestValue(200));
		getProvider().insert(Topics.CONTENT_URI, TopicsTableTest.getTestValue(500, 200));
		getProvider().insert(Persons.buildTopicUri("not important"),
				TopicsTableTest.getTestPersonTopicValue(123, 500));
		// queue values
		Cursor cursor = getProvider().query(Persons.buildDiscussionsUri(123), null, null, null, null);
		assertEquals("Should be two associated values, was: " + cursor.getCount(), 2, cursor.getCount());
		assertEquals("Should be 3 table columns, was: " + cursor.getCount(), 3, cursor.getColumnCount());
	}

	@Override
	protected void setUp() throws Exception {

		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	private Uri insertValidValue(final int valueId) {

		return insertValidValue(valueId, getProvider());
	}
}
