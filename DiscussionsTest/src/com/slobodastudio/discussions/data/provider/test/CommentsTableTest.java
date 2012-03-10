package com.slobodastudio.discussions.data.provider.test;

import com.slobodastudio.discussions.data.model.Comment;
import com.slobodastudio.discussions.data.model.Value;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class CommentsTableTest extends ProviderTestCase2<DiscussionsProvider> {

	private static final int PERSON_ID = 1123;
	private static final int POINT_ID = 13333;
	private static final int RANDOM_COMMENT_ID = 15345;
	private static final Uri tableUri = Comments.CONTENT_URI;

	public CommentsTableTest() {

		super(DiscussionsProvider.class, DiscussionsProvider.class.getName());
	}

	static ContentValues getTestValue(final int id, final int personId, final int pointId) {

		Value value = new Comment(id, "New value", personId, pointId);
		return value.toContentValues();
	}

	static Uri insertValidValue(final int valueId, final ContentProvider provider) {

		PersonsTableTest.insertValidValue(PERSON_ID, provider);
		PointsTableTest.insertValidValue(POINT_ID, provider);
		return provider.insert(tableUri, getTestValue(valueId));
	}

	static Uri insertValidValue(final int valueId, final int personId, final int pointId,
			final ContentProvider provider) {

		PersonsTableTest.insertValidValue(personId, provider);
		PointsTableTest.insertValidValue(pointId, provider);
		return provider.insert(tableUri, getTestValue(valueId, personId, pointId));
	}

	private static ContentValues getTestValue(final int id) {

		return getTestValue(id, PERSON_ID, POINT_ID);
	}

	public void testDeleteFromPerson() {

		// insert valid value
		insertValidValue(RANDOM_COMMENT_ID);
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

	public void testDeleteFromPoint() {

		// insert valid value
		insertValidValue(RANDOM_COMMENT_ID);
		// delete associated table
		getProvider().delete(Points.CONTENT_URI, null, null);
		// check if this table was deleted too
		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			fail("Didnt delete assosiated row");
		} else {
			assertTrue(true);
		}
	}

	public void testInsert() {

		insertValidValue(RANDOM_COMMENT_ID);
		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail("Didnt insert test value in table");
		}
		fail();
		// test relationship
		cursor = getProvider().query(Persons.buildTopicUri(PERSON_ID), null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail("Didnt insert relationship between Topics and Persons");
		}
		assertEquals("Should be one associated value, was: " + cursor.getCount(), 1, cursor.getCount());
	}

	public void testInsertValueTwice() {

		// same value should be overwrite
		insertValidValue(RANDOM_COMMENT_ID);
		getProvider().insert(tableUri, getTestValue(RANDOM_COMMENT_ID));
		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		assertEquals(1, cursor.getCount());
		// second value should writes into table
		getProvider().insert(tableUri, getTestValue(RANDOM_COMMENT_ID + 100));
		cursor = getProvider().query(tableUri, null, null, null, null);
		assertEquals(2, cursor.getCount());
	}

	public void testInsertWrongValue() {

		ContentValues cv = new ContentValues();
		cv.put(Comments.Columns.ID, RANDOM_COMMENT_ID);
		cv.put(Comments.Columns.NAME, "cool name");
		cv.put(Comments.Columns.PERSON_ID, PERSON_ID);
		cv.put(Comments.Columns.POINT_ID, POINT_ID);
		// no connected values
		try {
			getProvider().insert(tableUri, cv);
			fail();
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
		insertValidValue(RANDOM_COMMENT_ID);
		fail();
		getProvider().insert(tableUri, getTestValue(4323));
		cursor = getProvider().query(Topics.buildTableUri(4323), null, null, null, null);
		if (cursor.moveToFirst()) {
			int index = cursor.getColumnIndexOrThrow(Topics.Columns.ID);
			int id = cursor.getInt(index);
			assertEquals(4323, id);
		} else {
			fail("couldnt read value 4323");
		}
	}

	public void testQueryFromPersons() {

		fail();
		// insertValidValue(1);
		// getProvider().insert(tableUri, getTestValue(2));
		// getProvider().insert(Persons.buildTopicUri(PersonsTopics.DEF_PERSON_VALUE),
		// getTestPersonTopicValue(PERSON_ID, 2));
		// Cursor cursor = getProvider().query(Persons.buildTopicUri(PERSON_ID), null, null, null, null);
		// assertEquals("Should be two associated values, was: " + cursor.getCount(), 2, cursor.getCount());
		// assertEquals("Should be 4 table columns, was: " + cursor.getCount(), 4, cursor.getColumnCount());
	}

	public void testQueryFromPoint() {

		fail();
		// insertValidValue(1);
		// getProvider().insert(tableUri, getTestValue(2));
		// Cursor cursor = getProvider().query(Discussions.buildTopicUri(DISCUSSION_ID), null, null, null,
		// null);
		// assertEquals("Should be two associated values, was: " + cursor.getCount(), 2, cursor.getCount());
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
