package com.slobodastudio.discussions.test.data.odata.test;

import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataReadClient;
import com.slobodastudio.discussions.data.odata.OdataSyncService;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.RichText;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.database.Cursor;
import android.test.ProviderTestCase2;

public class OdataReadClientTest extends ProviderTestCase2<DiscussionsProvider> {

	public OdataReadClientTest() {

		super(DiscussionsProvider.class, DiscussionsProvider.class.getName());
	}

	public void testDescription() {

		OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, getContext());
		service.downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		service.downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		service.downloadTopics();
		service.downloadPoints();
		service.downloadDescriptions();
		Cursor cursor = getContext().getContentResolver().query(RichText.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
		assertEquals(true, cursor.getColumnCount() > 2);
		if (cursor.moveToFirst()) {
			int discussionIdIndex = cursor.getColumnIndexOrThrow(RichText.Columns.DISCUSSION_ID);
			int pointIdIndex = cursor.getColumnIndexOrThrow(RichText.Columns.POINT_ID);
			if (cursor.isNull(discussionIdIndex) && cursor.isNull(pointIdIndex)) {
				fail("both foreigh keys are null");
			}
		}
	}

	public void testDiscussions() {

		OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, getContext());
		service.downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		Cursor cursor = getContext().getContentResolver().query(Discussions.CONTENT_URI, null, null, null,
				null);
		assertEquals(true, cursor.getCount() > 0);
	}

	public void testPersons() {

		OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, getContext());
		service.downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		Cursor cursor = getContext().getContentResolver().query(Persons.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
	}

	public void testPoints() {

		OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, getContext());
		service.downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		service.downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		service.downloadTopics();
		service.downloadPoints();
		Cursor cursor = getContext().getContentResolver().query(Points.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
	}

	public void testServersMetadata() {

		OdataReadClient odata = new OdataReadClient(ODataConstants.SERVICE_URL_JAPAN);
		odata.logServerMetaData();
	}

	public void testTopics() {

		OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, getContext());
		service.downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		service.downloadTopics();
		Cursor cursor = getContext().getContentResolver().query(Topics.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
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
