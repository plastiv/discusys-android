package com.slobodastudio.discussions.test.data.odata.test;

import com.slobodastudio.discussions.data.odata.BaseOdataClient;
import com.slobodastudio.discussions.data.odata.OdataReadClient;
import com.slobodastudio.discussions.data.provider.DiscussionsContract;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;
import com.slobodastudio.discussions.test.data.provider.test.ProviderUtil;

import android.database.Cursor;
import android.test.ProviderTestCase2;
import android.util.Log;

public class OdataReadClientTest extends ProviderTestCase2<DiscussionsProvider> {

	private OdataReadClient mOdataClient;

	public OdataReadClientTest() {

		super(DiscussionsProvider.class, DiscussionsContract.CONTENT_AUTHORITY);
	}

	public void testDatabaseIsEmpty() {

		Cursor cursor = getProvider().query(Topics.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() == 0);
		cursor = getProvider().query(Persons.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() == 0);
		cursor = getProvider().query(Discussions.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() == 0);
		cursor = getProvider().query(Points.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() == 0);
		cursor = getProvider().query(Descriptions.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() == 0);
	}

	public void testDescription() {

		fail("Doesnt need right now");
		mOdataClient.downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		mOdataClient.downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		mOdataClient.downloadTopics();
		mOdataClient.downloadPoints();
		mOdataClient.downloadDescriptions();
		Cursor cursor = getProvider().query(Descriptions.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
		assertEquals(true, cursor.getColumnCount() > 2);
		if (cursor.moveToFirst()) {
			int discussionIdIndex = cursor.getColumnIndexOrThrow(Descriptions.Columns.DISCUSSION_ID);
			int pointIdIndex = cursor.getColumnIndexOrThrow(Descriptions.Columns.POINT_ID);
			if (cursor.isNull(discussionIdIndex) && cursor.isNull(pointIdIndex)) {
				fail("both foreigh keys are null");
			}
		}
	}

	public void testDiscussions() {

		mOdataClient.downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		Cursor cursor = getProvider().query(Discussions.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
	}

	public void testPersons() {

		mOdataClient.downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		Cursor cursor = getProvider().query(Persons.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
	}

	public void testPoints() {

		long start = System.currentTimeMillis();
		mOdataClient.downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		mOdataClient.downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		mOdataClient.downloadTopics();
		mOdataClient.downloadPoints();
		Log.d("download time: ", String.valueOf((System.currentTimeMillis() - start)));
		Cursor cursor = getProvider().query(Points.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
	}

	public void testPointsFromTopic() {

		mOdataClient.downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		mOdataClient.downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		mOdataClient.downloadTopics();
		mOdataClient.downloadPoints(2);
		Cursor cursor = getProvider().query(Points.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
	}

	public void testServersMetadata() {

		BaseOdataClient odataClient = new BaseOdataClient(getMockContext());
		odataClient.logServerMetaData();
	}

	public void testSinglePoint() {

		mOdataClient.downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		mOdataClient.downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		mOdataClient.downloadTopics();
		mOdataClient.refreshPoint(32);
		Cursor cursor = getProvider().query(Points.CONTENT_URI, null, null, null, null);
		ProviderUtil.logCursor(cursor);
		assertEquals(true, cursor.getCount() == 1);
	}

	public void testTopics() {

		mOdataClient.downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		mOdataClient.downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		mOdataClient.downloadTopics();
		Cursor cursor = getProvider().query(Topics.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
	}

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		mOdataClient = new OdataReadClient(getMockContext());
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}
}
