package com.slobodastudio.discussions.data.provider.test;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussion;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topic;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.test.ProviderTestCase2;

public class TopicsTableTest extends ProviderTestCase2<DiscussionsProvider> {

	private static final Uri tableUri = Topic.CONTENT_URI;

	public TopicsTableTest() {

		super(DiscussionsProvider.class, DiscussionsProvider.class.getName());
	}

	private static ContentValues getTestValue() {

		final ContentValues cv = new ContentValues();
		cv.put(Topic.Columns.TOPIC_ID, Integer.valueOf(1));
		cv.put(Topic.Columns.NAME, "name");
		cv.put(Topic.Columns.DISCUSSION_ID, Integer.valueOf(1123));
		return cv;
	}

	private static ContentValues getTestValue2() {

		final ContentValues cv = new ContentValues();
		cv.put(Topic.Columns.TOPIC_ID, Integer.valueOf(2));
		cv.put(Topic.Columns.NAME, "second topic");
		cv.put(Topic.Columns.DISCUSSION_ID, Integer.valueOf(1123));
		return cv;
	}

	public void testDeleteCascadeFromDiscussion() {

		// insert valid value
		final ContentProvider provider = getProvider();
		final Uri uri = provider.insert(tableUri, getTestValue());
		final Cursor cursor = provider.query(uri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail();
		}
		// delete assosiated discussion
		getProvider().delete(Discussion.CONTENT_URI, null, null);
		// check if topic was deleted too
		final Cursor secondCursor = provider.query(uri, null, null, null, null);
		if (secondCursor.moveToFirst()) {
			fail();
		} else {
			assertTrue(true);
		}
	}

	public void testInsert() {

		final ContentProvider provider = getProvider();
		final Uri uri = provider.insert(tableUri, getTestValue());
		final Cursor cursor = provider.query(uri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail();
		}
	}

	public void testInsertValueTwice() {

		final ContentProvider provider = getProvider();
		final Uri uri = provider.insert(tableUri, getTestValue());
		final Cursor cursor = provider.query(uri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail();
		}
		final Uri secondUri = provider.insert(tableUri, getTestValue());
		final Cursor secondCursor = provider.query(secondUri, null, null, null, null);
		assertEquals(1, secondCursor.getCount());
	}

	public void testInsertWrongValue() {

		final ContentProvider provider = getProvider();
		final ContentValues cv = new ContentValues();
		cv.put(Topic.Columns.TOPIC_ID, Integer.valueOf(1));
		cv.put(Topic.Columns.NAME, "name");
		// not valid disscussion id
		cv.put(Topic.Columns.DISCUSSION_ID, Integer.valueOf(1));
		try {
			provider.insert(tableUri, cv);
			fail();
		} catch (SQLiteConstraintException e) {
			assertTrue(true);
		}
	}

	public void testIsEmpty() {

		final ContentProvider provider = getProvider();
		final Cursor cursor = provider.query(tableUri, null, null, null, null);
		assertEquals("Table should be empty, was rows: " + cursor.getCount(), 0, cursor.getCount());
	}

	public void testQuery() {

		ContentProvider provider = getProvider();
		Cursor cursor = provider.query(tableUri, null, null, null, null);
		assertNotNull(cursor);
		cursor = null;
		try {
			cursor = provider.query(Uri.parse("content://com.slobodastudio.discussions.wrong_content_uri"),
					null, null, null, null);
			// we're wrong if we get until here!
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	public void testQueryFromDiscussion() {

		ContentProvider provider = getProvider();
		Cursor cursor = provider.query(Discussion.buildTopicUri("1123"), null, null, null, null);
		assertNotNull(cursor);
		if (cursor.moveToFirst()) {
			ProviderUtil.logCursor(cursor);
			fail("Was able to fetch topic value, while topic table is empty");
		} else {
			assertTrue(true);
		}
		provider.insert(tableUri, getTestValue());
		provider.insert(tableUri, getTestValue2());
		cursor = provider.query(Discussion.buildTopicUri("1123"), new String[] { BaseColumns._ID,
				Topic.Columns.NAME, Topic.Columns.TOPIC_ID, Topic.Columns.DISCUSSION_ID }, null, null, null);
		assertNotNull(cursor);
		assertEquals("Should be two associated topics, was: " + cursor.getCount(), 2, cursor.getCount());
	}

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		getProvider().insert(Discussion.CONTENT_URI, DiscussionsTableTest.getTestValue());
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}
}
