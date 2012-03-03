package com.slobodastudio.discussions.data.provider.test;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussion;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class DiscussionsTableTest extends ProviderTestCase2<DiscussionsProvider> {

	public DiscussionsTableTest() {

		super(DiscussionsProvider.class, DiscussionsProvider.class.getName());
	}

	public static ContentValues getTestValue() {

		final ContentValues cv = new ContentValues();
		cv.put(Discussion.Columns.DISCUSSION_ID, Integer.valueOf(1123));
		cv.put(Discussion.Columns.SUBJECT, "Discussion subject");
		return cv;
	}

	public void testInsert() {

		final ContentProvider provider = getProvider();
		final Uri uri = provider.insert(Discussion.CONTENT_URI, getTestValue());
		final Cursor cursor = provider.query(uri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail();
		}
	}

	public void testInsertSameValue() {

		final ContentProvider provider = getProvider();
		final Uri uri = provider.insert(Discussion.CONTENT_URI, getTestValue());
		final Cursor cursor = provider.query(uri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail();
		}
		final Uri secondUri = provider.insert(Discussion.CONTENT_URI, getTestValue());
		final Cursor secondCursor = provider.query(secondUri, null, null, null, null);
		assertEquals(1, secondCursor.getCount());
	}

	public void testIsEmpty() {

		final ContentProvider provider = getProvider();
		final Cursor cursor = provider.query(Discussion.CONTENT_URI, null, null, null, null);
		assertEquals("Table should be empty, was rows: " + cursor.getCount(), 0, cursor.getCount());
	}

	public void testQuery() {

		ContentProvider provider = getProvider();
		Cursor cursor = provider.query(Discussion.CONTENT_URI, null, null, null, null);
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

	@Override
	protected void setUp() throws Exception {

		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}
}
