package com.slobodastudio.discussions.data.provider.test;

import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.test.ProviderTestCase2;

// TODO: made some tests for update
public class TasksTableTest extends ProviderTestCase2<DiscussionsProvider> {

	public TasksTableTest() {

		super(DiscussionsProvider.class, DiscussionsProvider.class.getName());
	}
	// private static ContentValues getTestValue() {
	//
	// final ContentValues cv = new ContentValues();
	// cv.put(Tasks.TASK_DATE, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_START, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_END, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_STATE, Integer.valueOf(0));
	// cv.put(Tasks.TASK_TITLE, "Task title");
	// return cv;
	// }
	//
	// public void testQueryTasks() {
	//
	// ContentProvider provider = getProvider();
	// Cursor cursor = provider.query(Tasks.CONTENT_URI, null, null, null, null);
	// assertNotNull(cursor);
	// cursor = null;
	// try {
	// cursor = provider.query(Uri.parse("definitelywrong"), null, null, null, null);
	// // we're wrong if we get until here!
	// fail();
	// } catch (IllegalArgumentException e) {
	// assertTrue(true);
	// }
	// }
	//
	// public void testTasksDeleteAllTableRows() {
	//
	// final ContentProvider provider = getProvider();
	// final ContentValues cv = getTestValue();
	// provider.insert(Tasks.CONTENT_URI, cv);
	// cv.clear();
	// cv.put(Tasks.TASK_DATE, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_STATE, Integer.valueOf(0));
	// cv.put(Tasks.TASK_TITLE, "Task title2");
	// provider.insert(Tasks.CONTENT_URI, cv);
	// cv.clear();
	// cv.put(Tasks.TASK_DATE, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_STATE, Integer.valueOf(0));
	// cv.put(Tasks.TASK_TITLE, "Task title3");
	// provider.insert(Tasks.CONTENT_URI, cv);
	// final Cursor cursor = provider.query(Tasks.CONTENT_URI, null, null, null, null);
	// assertEquals("Should be 3 rows inserted, was : " + cursor.getCount(), 3, cursor.getCount());
	// int rowCount = provider.delete(Tasks.CONTENT_URI, "1", null);
	// assertEquals("Should be 3 rows deleted, was : " + rowCount, 3, rowCount);
	// }
	//
	// public void testTasksDeleteRowByUri() {
	//
	// final ContentProvider provider = getProvider();
	// final Uri uri = provider.insert(Tasks.CONTENT_URI, getTestValue());
	// int rowCount = provider.delete(uri, null, null);
	// assertEquals("Should delete one row, was: " + rowCount, 1, rowCount);
	// }
	//
	// public void testTasksInsertAllValues() {
	//
	// final ContentProvider provider = getProvider();
	// final Uri uri = provider.insert(Tasks.CONTENT_URI, getTestValue());
	// assertNotNull(uri);
	// }
	//
	// public void testTasksRequiredData() {
	//
	// final ContentProvider provider = getProvider();
	// final ContentValues cv = new ContentValues();
	// cv.put(Tasks.TASK_START, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_END, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_STATE, Integer.valueOf(0));
	// cv.put(Tasks.TASK_TITLE, "Task title");
	// try {
	// provider.insert(Tasks.CONTENT_URI, cv);
	// // should not reach here, because required TASK_DATA missed
	// fail();
	// } catch (SQLException e) {
	// assertTrue(true);
	// }
	// }
	//
	// public void testTasksRequiredTitle() {
	//
	// final ContentProvider provider = getProvider();
	// final ContentValues cv = new ContentValues();
	// cv.put(Tasks.TASK_DATE, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_START, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_END, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_STATE, Integer.valueOf(0));
	// try {
	// provider.insert(Tasks.CONTENT_URI, cv);
	// // should not reach here, because required TASK_TITLE missed
	// fail();
	// } catch (SQLException e) {
	// assertTrue(true);
	// }
	// }
	//
	// public void testTasksStateIsZeroByDefault() {
	//
	// final ContentProvider provider = getProvider();
	// final ContentValues cv = new ContentValues();
	// cv.put(Tasks.TASK_DATE, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_START, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_END, Long.valueOf(System.currentTimeMillis()));
	// cv.put(Tasks.TASK_TITLE, "Task title");
	// final Uri uri = provider.insert(Tasks.CONTENT_URI, cv);
	// final Cursor cursor = provider.query(uri, null, null, null, null);
	// if (cursor.moveToFirst()) {
	// int stateIndex = cursor.getColumnIndexOrThrow(Tasks.TASK_STATE);
	// int state = cursor.getInt(stateIndex);
	// assertEquals("Task state should be zero by default and was: " + state, 0, state);
	// } else {
	// fail("Cant get row for inserted task uri");
	// }
	// }
	//
	// public void testTasksUniqueTitle() {
	//
	// final ContentProvider provider = getProvider();
	// provider.insert(Tasks.CONTENT_URI, getTestValue());
	// provider.insert(Tasks.CONTENT_URI, getTestValue());
	// // should be only one row in table, not two, because TASK_TITLE is the same
	// // and ON CONFLICT REPLACE
	// final Cursor cursor = provider.query(Tasks.CONTENT_URI, null, null, null, null);
	// int rowCount = cursor.getCount();
	// assertEquals("Rows count should be one, was : " + rowCount, 1, rowCount);
	// }
	//
	// @Override
	// protected void setUp() throws Exception {
	//
	// super.setUp();
	// }
	//
	// @Override
	// protected void tearDown() throws Exception {
	//
	// super.tearDown();
	// }
}
