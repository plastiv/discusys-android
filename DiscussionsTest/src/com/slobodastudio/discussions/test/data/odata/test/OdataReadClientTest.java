package com.slobodastudio.discussions.test.data.odata.test;

import com.slobodastudio.discussions.data.odata.OdataReadClient;
import com.slobodastudio.discussions.data.provider.DiscussionsContract;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.data.provider.DiscussionsProvider;

import android.database.Cursor;
import android.test.ProviderTestCase2;
import android.util.Log;

public class OdataReadClientTest extends ProviderTestCase2<DiscussionsProvider> {

	private OdataReadClient mOdataClient;

	public OdataReadClientTest() {

		super(DiscussionsProvider.class, DiscussionsContract.CONTENT_AUTHORITY);
	}

	public void testDownloadAll() {

		long start = System.currentTimeMillis();
		// persons
		mOdataClient.refreshPersons();
		Cursor cursor = getProvider().query(Persons.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
		mOdataClient.refreshDiscussions();
		cursor = getProvider().query(Discussions.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
		mOdataClient.refreshTopics();
		cursor = getProvider().query(Topics.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
		mOdataClient.refreshPoints();
		cursor = getProvider().query(Points.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
		mOdataClient.refreshDescriptions();
		cursor = getProvider().query(Descriptions.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
		mOdataClient.refreshComments();
		cursor = getProvider().query(Comments.CONTENT_URI, null, null, null, null);
		assertEquals(true, cursor.getCount() > 0);
		Log.d("download time: ", String.valueOf((System.currentTimeMillis() - start)));
	}

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		mOdataClient = new OdataReadClient(getMockContext());
		assertLocalDatabaseIsEmpty();
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	private void assertLocalDatabaseIsEmpty() {

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
}
