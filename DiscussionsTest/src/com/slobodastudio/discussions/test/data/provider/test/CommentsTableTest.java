package com.slobodastudio.discussions.test.data.provider.test;

import com.slobodastudio.discussions.data.DataIoException;
import com.slobodastudio.discussions.data.model.Comment;
import com.slobodastudio.discussions.data.model.Value;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
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

	static ContentValues getTestValue(final int id, final Integer personId, final Integer pointId) {

		Value value = new Comment(id, "New value", personId, pointId);
		return value.toContentValues();
	}

	static Uri insertValidValue(final int valueId, final ContentProvider provider) {

		PersonsTableTest.insertValidValue(PERSON_ID, provider);
		PointsTableTest.insertValidValue(POINT_ID, provider);
		return provider.insert(tableUri, getTestValue(valueId));
	}

	static Uri insertValidValue(final int valueId, final Integer personId, final Integer pointId,
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
		// insert only one foreign key - person
		getProvider().delete(Comments.CONTENT_URI, null, null);
		getProvider().delete(Persons.CONTENT_URI, null, null);
		getProvider().delete(Points.CONTENT_URI, null, null);
		PersonsTableTest.insertValidValue(PERSON_ID, getProvider());
		getProvider().insert(tableUri, getTestValue(RANDOM_COMMENT_ID, PERSON_ID, null));
		cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail("Didnt insert test value in table");
		}
		// insert only one foreign key - point
		getProvider().delete(Comments.CONTENT_URI, null, null);
		getProvider().delete(Persons.CONTENT_URI, null, null);
		getProvider().delete(Points.CONTENT_URI, null, null);
		PointsTableTest.insertValidValue(POINT_ID, getProvider());
		getProvider().insert(tableUri, getTestValue(RANDOM_COMMENT_ID, null, POINT_ID));
		cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail("Didnt insert test value in table");
		}
		// fail to insert both foreign key missing
		getProvider().delete(Comments.CONTENT_URI, null, null);
		getProvider().delete(Persons.CONTENT_URI, null, null);
		getProvider().delete(Points.CONTENT_URI, null, null);
		try {
			getProvider().insert(tableUri, getTestValue(RANDOM_COMMENT_ID, null, null));
			fail("Was able to insert value without any foreign key");
		} catch (DataIoException e) {
			assertTrue(true);
		}
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
		cv.put(Comments.Columns.TEXT, "cool name");
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
		int idOffset = 223;
		getProvider().insert(tableUri, getTestValue(RANDOM_COMMENT_ID + idOffset));
		cursor = getProvider().query(Comments.buildTableUri(RANDOM_COMMENT_ID + idOffset), null, null, null,
				null);
		if (cursor.moveToFirst()) {
			int index = cursor.getColumnIndexOrThrow(Comments.Columns.ID);
			int id = cursor.getInt(index);
			assertEquals(RANDOM_COMMENT_ID + idOffset, id);
		} else {
			fail("couldnt read value: " + (RANDOM_COMMENT_ID + idOffset));
		}
	}

	public void testQueryFromPersons() {

		PersonsTableTest.insertValidValue(PERSON_ID, getProvider());
		getProvider().insert(tableUri, getTestValue(RANDOM_COMMENT_ID, PERSON_ID, null));
		getProvider().insert(tableUri, getTestValue(RANDOM_COMMENT_ID + 123, PERSON_ID, null));
		String selection = Comments.Columns.PERSON_ID + "=?";
		String[] selectionArgs = new String[] { String.valueOf(PERSON_ID) };
		Cursor cursor = getProvider().query(Comments.CONTENT_URI, null, selection, selectionArgs, null);
		assertEquals("Should be two associated values, was: " + cursor.getCount(), 2, cursor.getCount());
	}

	public void testQueryFromPoint() {

		PointsTableTest.insertValidValue(POINT_ID, getProvider());
		getProvider().insert(tableUri, getTestValue(RANDOM_COMMENT_ID, null, POINT_ID));
		getProvider().insert(tableUri, getTestValue(RANDOM_COMMENT_ID + 133, null, POINT_ID));
		String selection = Comments.Columns.POINT_ID + "=?";
		String[] selectionArgs = new String[] { String.valueOf(POINT_ID) };
		Cursor cursor = getProvider().query(Comments.CONTENT_URI, null, selection, selectionArgs, null);
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
