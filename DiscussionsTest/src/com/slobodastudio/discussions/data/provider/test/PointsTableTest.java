package com.slobodastudio.discussions.data.provider.test;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class PointsTableTest extends ProviderTestCase2<DiscussionsProvider> {

	private static final int GROUP_ID = 343;
	private static final int PERSON_ID = 1123;
	private static final Uri tableUri = Points.CONTENT_URI;
	private static final int TOPIC_ID = 105;

	public PointsTableTest() {

		super(DiscussionsProvider.class, DiscussionsProvider.class.getName());
	}

	static Uri insertValidValue(final int valueId, final ContentProvider provider) {

		TopicsTableTest.insertValidValue(TOPIC_ID, provider);
		return provider.insert(tableUri, getTestValue(valueId));
	}

	private static ContentValues getTestValue(final int valueId) {

		final ContentValues cv = new ContentValues();
		cv.put(Points.Columns.ID, Integer.valueOf(valueId));
		cv.put(Points.Columns.AGREEMENT_CODE, 0);
		cv.put(Points.Columns.EXPANDED, false);
		cv.put(Points.Columns.GROUP_ID, GROUP_ID);
		cv.put(Points.Columns.NUMBERED_POINT, "112");
		cv.put(Points.Columns.PERSON_ID, Integer.valueOf(PERSON_ID));
		cv.put(Points.Columns.NAME, "My point");
		cv.put(Points.Columns.SHARED_TO_PUBLIC, true);
		cv.put(Points.Columns.SIDE_CODE, 1);
		cv.put(Points.Columns.TOPIC_ID, Integer.valueOf(TOPIC_ID));
		cv.put(Points.Columns.DRAWING, new byte[] {});
		return cv;
	}

	public void testDeleteFromPerson() {

		// insert valid value
		insertValidValue(1);
		// delete associated table
		getProvider().delete(Persons.CONTENT_URI, null, null);
		// check if this table was deleted too
		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			fail("Didnt delete assosiated row");
		} else {
			assertTrue(true);
		}
	}

	public void testDeleteFromTopic() {

		insertValidValue(1);
		// delete associated discussion
		getProvider().delete(Topics.CONTENT_URI, null, null);
		// check if topic was deleted too
		final Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			fail("Didnt delete assosiated row");
		} else {
			assertTrue(true);
		}
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
		getProvider().insert(tableUri, getTestValue(1));
		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		assertEquals(1, cursor.getCount());
		// second value should writes into table
		getProvider().insert(tableUri, getTestValue(2));
		cursor = getProvider().query(tableUri, null, null, null, null);
		assertEquals(2, cursor.getCount());
	}

	public void testInsertWrongValue() {

		final ContentValues cv = new ContentValues();
		cv.put(Points.Columns.ID, Integer.valueOf(1));
		cv.put(Points.Columns.AGREEMENT_CODE, 0);
		cv.put(Points.Columns.EXPANDED, false);
		cv.put(Points.Columns.GROUP_ID, 1);
		cv.put(Points.Columns.NUMBERED_POINT, "");
		// wrong person_id
		cv.put(Points.Columns.PERSON_ID, Integer.valueOf(2));
		cv.put(Points.Columns.NAME, "My point");
		cv.put(Points.Columns.SHARED_TO_PUBLIC, true);
		cv.put(Points.Columns.SIDE_CODE, 1);
		cv.put(Points.Columns.TOPIC_ID, Integer.valueOf(1));
		cv.put(Points.Columns.DRAWING, new byte[] {});
		try {
			getProvider().insert(tableUri, cv);
			fail("Wrong value as inserted");
		} catch (RuntimeException e) {
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
		insertValidValue(1);
		getProvider().insert(tableUri, getTestValue(4323));
		cursor = getProvider().query(Points.buildTableUri(4323), null, null, null, null);
		if (cursor.moveToFirst()) {
			int index = cursor.getColumnIndexOrThrow(Points.Columns.ID);
			int id = cursor.getInt(index);
			assertEquals(4323, id);
		} else {
			fail("couldnt read value 4323");
		}
	}

	public void testQueryFromPersons() {

		insertValidValue(1);
		getProvider().insert(tableUri, getTestValue(2));
		Cursor cursor = getProvider().query(Persons.buildPointUri(PERSON_ID), null, null, null, null);
		assertEquals("Should be two associated values, was: " + cursor.getCount(), 2, cursor.getCount());
	}

	public void testQueryFromTopics() {

		insertValidValue(1);
		getProvider().insert(tableUri, getTestValue(2));
		Cursor cursor = getProvider().query(Topics.buildPointUri(TOPIC_ID), null, null, null, null);
		assertEquals("Should be two associated values, was: " + cursor.getCount(), 2, cursor.getCount());
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
