package com.slobodastudio.discussions.data.odata.test;

import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataSyncService;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussion;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Person;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Point;

import android.test.AndroidTestCase;

public class OdataReadClientTest extends AndroidTestCase {

	public void testDiscussions() {

		OdataSyncService service = new OdataSyncService(ODataConstants.DISCUSSIONS_JAPAN, getContext());
		service.downloadValues(Discussion.TABLE_NAME, Discussion.CONTENT_URI);
	}

	public void testPersons() {

		OdataSyncService service = new OdataSyncService(ODataConstants.DISCUSSIONS_JAPAN, getContext());
		service.downloadValues(Person.TABLE_NAME, Person.CONTENT_URI);
	}

	public void testPoints() {

		OdataSyncService service = new OdataSyncService(ODataConstants.DISCUSSIONS_JAPAN, getContext());
		service.downloadValues(Point.TABLE_NAME, Point.CONTENT_URI);
	}

	public void testTopics() {

		OdataSyncService service = new OdataSyncService(ODataConstants.DISCUSSIONS_JAPAN, getContext());
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
