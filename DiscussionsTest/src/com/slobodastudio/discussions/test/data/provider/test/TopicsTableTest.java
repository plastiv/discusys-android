package com.slobodastudio.discussions.test.data.provider.test;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.PersonsTopics;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class TopicsTableTest extends ProviderTestCase2<DiscussionsProvider> {

	private static final int DISCUSSION_ID = 13333;
	private static final int PERSON_ID = 1123;
	private static final Uri tableUri = Topics.CONTENT_URI;

	public TopicsTableTest() {

		super(DiscussionsProvider.class, DiscussionsProvider.class.getName());
	}

	static ContentValues getTestPersonTopicValue(final int personId, final int topicId) {

		final ContentValues cv = new ContentValues();
		cv.put(PersonsTopics.Columns.TOPIC_ID, Integer.valueOf(topicId));
		cv.put(PersonsTopics.Columns.PERSON_ID, Integer.valueOf(personId));
		return cv;
	}

	static ContentValues getTestValue(final int topicId, final int discussionId) {

		final ContentValues cv = new ContentValues();
		cv.put(Topics.Columns.ID, Integer.valueOf(topicId));
		cv.put(Topics.Columns.NAME, "name");
		cv.put(Topics.Columns.DISCUSSION_ID, Integer.valueOf(discussionId));
		return cv;
	}

	static Uri insertValidValue(final int valueId, final ContentProvider provider) {

		DiscussionsTableTest.insertValidValue(DISCUSSION_ID, provider);
		PersonsTableTest.insertValidValue(PERSON_ID, provider);
		provider.insert(Persons.buildTopicUri(PersonsTopics.DEF_PERSON_VALUE), getTestPersonTopicValue(
				PERSON_ID, valueId));
		return provider.insert(tableUri, getTestValue(valueId));
	}

	static Uri insertValidValue(final int topicId, final int discussionId, final int personId,
			final ContentProvider provider) {

		DiscussionsTableTest.insertValidValue(discussionId, provider);
		PersonsTableTest.insertValidValue(personId, provider);
		provider.insert(Persons.buildTopicUri(PersonsTopics.DEF_PERSON_VALUE), getTestPersonTopicValue(
				personId, topicId));
		return provider.insert(tableUri, getTestValue(topicId, discussionId));
	}

	private static ContentValues getTestValue(final int topicId) {

		final ContentValues cv = new ContentValues();
		cv.put(Topics.Columns.ID, Integer.valueOf(topicId));
		cv.put(Topics.Columns.NAME, "name");
		cv.put(Topics.Columns.DISCUSSION_ID, Integer.valueOf(DISCUSSION_ID));
		return cv;
	}

	public void testDeleteFromDiscussion() {

		insertValidValue(1);
		// delete associated discussion
		getProvider().delete(Discussions.CONTENT_URI, null, null);
		// check if topic was deleted too
		final Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			fail("Didnt delete assosiated row");
		} else {
			assertTrue(true);
		}
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

	public void testInsert() {

		insertValidValue(1);
		Cursor cursor = getProvider().query(tableUri, null, null, null, null);
		if (cursor.moveToFirst()) {
			assertTrue(true);
		} else {
			fail("Didnt insert test value in table");
		}
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
		cv.put(Topics.Columns.ID, Integer.valueOf(1));
		cv.put(Topics.Columns.NAME, "name");
		// not valid disscussion id
		cv.put(Topics.Columns.DISCUSSION_ID, Integer.valueOf(DISCUSSION_ID - 1));
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
		cursor = getProvider().query(Topics.buildTableUri(4323), null, null, null, null);
		if (cursor.moveToFirst()) {
			int index = cursor.getColumnIndexOrThrow(Topics.Columns.ID);
			int id = cursor.getInt(index);
			assertEquals(4323, id);
		} else {
			fail("couldnt read value 4323");
		}
	}

	public void testQueryFromDiscussion() {

		insertValidValue(1);
		getProvider().insert(tableUri, getTestValue(2));
		Cursor cursor = getProvider().query(Discussions.buildTopicUri(DISCUSSION_ID), null, null, null, null);
		assertEquals("Should be two associated values, was: " + cursor.getCount(), 2, cursor.getCount());
	}

	public void testQueryFromPersons() {

		insertValidValue(1);
		getProvider().insert(tableUri, getTestValue(2));
		getProvider().insert(Persons.buildTopicUri(PersonsTopics.DEF_PERSON_VALUE),
				getTestPersonTopicValue(PERSON_ID, 2));
		Cursor cursor = getProvider().query(Persons.buildTopicUri(PERSON_ID), null, null, null, null);
		assertEquals("Should be two associated values, was: " + cursor.getCount(), 2, cursor.getCount());
		assertEquals("Should be 4 table columns, was: " + cursor.getCount(), 4, cursor.getColumnCount());
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
