package com.slobodastudio.discussions.test.data.provider.test;

import com.slobodastudio.discussions.data.DataIoException;
import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.model.Value;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class DescriptionsTableTest extends ProviderTestCase2<DiscussionsProvider> {

	private static final int DISCUSSION_ID = 1123;
	private static final int POINT_ID = 13333;
	private static final int RANDOM_DESCRIPTION_ID = 15345;
	private static final Uri tableUri = Descriptions.CONTENT_URI;

	public DescriptionsTableTest() {

		super(DiscussionsProvider.class, DiscussionsProvider.class.getName());
	}

	static ContentValues getTestValue(final int id, final Integer discussionId, final Integer pointId) {

		Value value = new Description(id, "hey new description value", discussionId, pointId);
		return value.toContentValues();
	}

	static Uri insertValidValue(final int valueId, final ContentProvider provider) {

		return insertValidValue(valueId, DISCUSSION_ID, POINT_ID, provider);
	}

	static Uri insertValidValue(final int valueId, final int discussionId, final int pointId,
			final ContentProvider provider) {

		DiscussionsTableTest.insertValidValue(discussionId, provider);
		PointsTableTest.insertValidValue(pointId, provider);
		return provider.insert(tableUri, getTestValue(valueId, discussionId, pointId));
	}

	private static ContentValues getTestValue(final int id) {

		return getTestValue(id, DISCUSSION_ID, POINT_ID);
	}

	public void testDeleteFromDiscussion() {

		// insert valid value
		insertValidValue(RANDOM_DESCRIPTION_ID);
		// delete associated table
		getProvider().delete(Discussions.CONTENT_URI, null, null);
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
		insertValidValue(RANDOM_DESCRIPTION_ID);
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

		insertValidValue(RANDOM_DESCRIPTION_ID);
		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail("Didnt insert test value in table");
		}
		// insert only one foreign key - discussion
		getProvider().delete(Descriptions.CONTENT_URI, null, null);
		getProvider().delete(Discussions.CONTENT_URI, null, null);
		getProvider().delete(Points.CONTENT_URI, null, null);
		DiscussionsTableTest.insertValidValue(DISCUSSION_ID, getProvider());
		getProvider().insert(tableUri, getTestValue(RANDOM_DESCRIPTION_ID, DISCUSSION_ID, null));
		cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail("Didnt insert test value in table");
		}
		// insert only one foreign key - point
		getProvider().delete(Descriptions.CONTENT_URI, null, null);
		getProvider().delete(Discussions.CONTENT_URI, null, null);
		getProvider().delete(Points.CONTENT_URI, null, null);
		PointsTableTest.insertValidValue(POINT_ID, getProvider());
		getProvider().insert(tableUri, getTestValue(RANDOM_DESCRIPTION_ID, null, POINT_ID));
		cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail("Didnt insert test value in table");
		}
		// fail to insert both foreign key missing
		getProvider().delete(Descriptions.CONTENT_URI, null, null);
		getProvider().delete(Discussions.CONTENT_URI, null, null);
		getProvider().delete(Points.CONTENT_URI, null, null);
		try {
			getProvider().insert(tableUri, getTestValue(RANDOM_DESCRIPTION_ID, null, null));
			fail("Was able to insert value without any foreign key");
		} catch (DataIoException e) {
			assertTrue(true);
		}
	}

	public void testInsertValueTwice() {

		// same value should be overwrite
		insertValidValue(RANDOM_DESCRIPTION_ID);
		getProvider().insert(tableUri, getTestValue(RANDOM_DESCRIPTION_ID));
		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		assertEquals(1, cursor.getCount());
		// second value should writes into table
		getProvider().insert(tableUri, getTestValue(RANDOM_DESCRIPTION_ID + 100));
		cursor = getProvider().query(tableUri, null, null, null, null);
		assertEquals(2, cursor.getCount());
	}

	public void testInsertWrongValue() {

		ContentValues cv = new ContentValues();
		cv.put(Descriptions.Columns.ID, RANDOM_DESCRIPTION_ID);
		// missed text
		// cv.put(RichText.Columns.TEXT, text);
		cv.put(Descriptions.Columns.DISCUSSION_ID, DISCUSSION_ID);
		cv.put(Comments.Columns.POINT_ID, POINT_ID);
		// no connected values
		try {
			getProvider().insert(tableUri, cv);
			fail();
		} catch (DataIoException e) {
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
		insertValidValue(RANDOM_DESCRIPTION_ID);
		int idOffset = 123;
		getProvider().insert(tableUri, getTestValue(RANDOM_DESCRIPTION_ID + idOffset));
		cursor = getProvider().query(Descriptions.buildTableUri(RANDOM_DESCRIPTION_ID + idOffset), null, null,
				null, null);
		if (cursor.moveToFirst()) {
			int index = cursor.getColumnIndexOrThrow(Descriptions.Columns.ID);
			int id = cursor.getInt(index);
			assertEquals(RANDOM_DESCRIPTION_ID + idOffset, id);
		} else {
			fail("couldnt read value: " + (RANDOM_DESCRIPTION_ID + idOffset));
		}
	}

	public void testQueryFromDiscussions() {

		DiscussionsTableTest.insertValidValue(DISCUSSION_ID, getProvider());
		getProvider().insert(tableUri, getTestValue(RANDOM_DESCRIPTION_ID, DISCUSSION_ID, null));
		getProvider().insert(tableUri, getTestValue(RANDOM_DESCRIPTION_ID + 123, DISCUSSION_ID, null));
		String selection = Descriptions.Columns.DISCUSSION_ID + "=?";
		String[] selectionArgs = new String[] { String.valueOf(DISCUSSION_ID) };
		Cursor cursor = getProvider().query(Descriptions.CONTENT_URI, null, selection, selectionArgs, null);
		assertEquals("Should be two associated values, was: " + cursor.getCount(), 2, cursor.getCount());
	}

	public void testQueryFromPoint() {

		PointsTableTest.insertValidValue(POINT_ID, getProvider());
		getProvider().insert(tableUri, getTestValue(RANDOM_DESCRIPTION_ID, null, POINT_ID));
		getProvider().insert(tableUri, getTestValue(RANDOM_DESCRIPTION_ID + 123, null, POINT_ID));
		String selection = Descriptions.Columns.POINT_ID + "=?";
		String[] selectionArgs = new String[] { String.valueOf(POINT_ID) };
		Cursor cursor = getProvider().query(Descriptions.CONTENT_URI, null, selection, selectionArgs, null);
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
