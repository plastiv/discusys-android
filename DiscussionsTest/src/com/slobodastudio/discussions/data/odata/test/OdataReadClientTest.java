package com.slobodastudio.discussions.data.odata.test;

import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataSyncService;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;

import android.test.AndroidTestCase;

public class OdataReadClientTest extends AndroidTestCase {

	public void testDiscussions() {

		OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, getContext());
		service.downloadValues(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
	}

	public void testPersons() {

		OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, getContext());
		service.downloadValues(Persons.TABLE_NAME, Persons.CONTENT_URI);
	}

	public void testPoints() {

		OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, getContext());
		service.downloadValues(Points.TABLE_NAME, Points.CONTENT_URI);
	}

	public void testTopics() {

		OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, getContext());
		service.downloadTopics();
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
