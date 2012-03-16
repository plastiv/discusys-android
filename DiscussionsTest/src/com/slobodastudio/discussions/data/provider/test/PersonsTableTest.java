package com.slobodastudio.discussions.data.provider.test;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class PersonsTableTest extends ProviderTestCase2<DiscussionsProvider> {

	private static final Uri tableUri = Persons.CONTENT_URI;

	public PersonsTableTest() {

		super(DiscussionsProvider.class, DiscussionsProvider.class.getName());
	}

	static ContentValues getTestValue(final int valueId) {

		final ContentValues cv = new ContentValues();
		cv.put(Persons.Columns.AVATAR, new byte[] {});
		cv.put(Persons.Columns.ID, Integer.valueOf(valueId));
		cv.put(Persons.Columns.NAME, "person name");
		cv.put(Persons.Columns.EMAIL, "person@mail");
		cv.put(Persons.Columns.COLOR, Color.CYAN);
		cv.put(Persons.Columns.ONLINE, false);
		return cv;
	}

	static Uri insertValidValue(final int valueId, final ContentProvider provider) {

		return provider.insert(tableUri, getTestValue(valueId));
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
		cv.put(Persons.Columns.ID, Integer.valueOf(1));
		// name required
		// cv.put(Persons.Columns.NAME, "person name");
		cv.put(Persons.Columns.EMAIL, "person@mail");
		cv.put(Persons.Columns.COLOR, Color.CYAN);
		cv.put(Persons.Columns.ONLINE, false);
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
		insertValidValue(1);
		getProvider().insert(tableUri, getTestValue(4323));
		cursor = getProvider().query(Persons.buildTableUri(4323), null, null, null, null);
		if (cursor.moveToFirst()) {
			int index = cursor.getColumnIndexOrThrow(Persons.Columns.ID);
			int id = cursor.getInt(index);
			assertEquals(4323, id);
		} else {
			fail("couldnt read value 4323");
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

	private Uri insertValidValue(final int valueId) {

		return insertValidValue(valueId, getProvider());
	}
}
