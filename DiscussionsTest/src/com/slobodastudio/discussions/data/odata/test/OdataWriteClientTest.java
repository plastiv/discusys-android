package com.slobodastudio.discussions.data.odata.test;

import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataReadClient;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussion;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Group;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Person;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topic;

import android.graphics.Color;
import android.test.AndroidTestCase;

import org.odata4j.core.OEntity;

public class OdataWriteClientTest extends AndroidTestCase {

	public static void testInsertDiscussion() {

		OdataWriteClient odata = new OdataWriteClient(ODataConstants.DISCUSSIONS_JAPAN);
		String expectedSubject = "Android Subject";
		OEntity entity = odata.insertDiscussion(expectedSubject);
		String actualSubject = (String) entity.getProperty(Discussion.Columns.SUBJECT).getValue();
		assertEquals(expectedSubject, actualSubject);
	}

	public static void testInsertPerson() {

		OdataWriteClient odata = new OdataWriteClient(ODataConstants.DISCUSSIONS_JAPAN);
		String expectedName = "Android";
		String expectedMail = "androoid@test.com";
		int expectedColor = Color.CYAN;
		boolean expectedOnline = false;
		OEntity entity = odata.insertPerson(expectedName, expectedMail, expectedColor, expectedOnline);
		String actualName = (String) entity.getProperty(Person.Columns.NAME).getValue();
		assertEquals(expectedName, actualName);
		String actualMail = (String) entity.getProperty(Person.Columns.EMAIL).getValue();
		assertEquals(expectedMail, actualMail);
		int actualColor = (Integer) entity.getProperty(Person.Columns.COLOR).getValue();
		assertEquals(expectedColor, actualColor);
		boolean actualOnline = (Boolean) entity.getProperty(Person.Columns.ONLINE).getValue();
		assertEquals(expectedOnline, actualOnline);
	}

	public static void testInsertPoint() {

		OdataWriteClient odataWrite = new OdataWriteClient(ODataConstants.DISCUSSIONS_JAPAN);
		OdataReadClient odataRead = new OdataReadClient(ODataConstants.DISCUSSIONS_JAPAN);
		int expectedAgreementCode = 0;
		byte[] expectedDrawing = new byte[] { 0, 1 };
		boolean expectedExpanded = false;
		int expectedGroupId = 1;
		String expectedNumberedPoint = "";
		int expectedPersonId = 1;
		String expectedPointName = "Android point";
		boolean expectedSharedToPublic = true;
		int expectedSideCode = 0;
		int expectedTopicId = 1;
		OEntity entity = odataWrite.insertPoint(expectedAgreementCode, expectedDrawing, expectedExpanded,
				expectedGroupId, expectedNumberedPoint, expectedPersonId, expectedPointName,
				expectedSharedToPublic, expectedSideCode, expectedTopicId);
		// agreement code
		int actualAgreementCode = (Integer) entity.getProperty(Point.Columns.AGREEMENT_CODE).getValue();
		assertEquals(expectedAgreementCode, actualAgreementCode);
		// drawing
		byte[] actualDrawing = (byte[]) entity.getProperty(Point.Columns.DRAWING).getValue();
		assertEquals(expectedDrawing[0], actualDrawing[0]);
		assertEquals(expectedDrawing[1], actualDrawing[1]);
		// expanded
		boolean actualExpanded = (Boolean) entity.getProperty(Point.Columns.EXPANDED).getValue();
		assertEquals(expectedExpanded, actualExpanded);
		// group id
		OEntity groupEntity = odataRead.getRelatedEntity(entity, Point.Columns.GROUP_ID_SERVER);
		int actualGroupId = (Integer) groupEntity.getProperty(Group.Columns.GROUP_ID).getValue();
		assertEquals(expectedGroupId, actualGroupId);
		// numbered point
		String actualNumberedPoint = (String) entity.getProperty(Point.Columns.NUMBERED_POINT).getValue();
		assertEquals(expectedNumberedPoint, actualNumberedPoint);
		// person id
		OEntity personEntity = odataRead.getRelatedEntity(entity, Point.Columns.PERSON_ID);
		int actualPersonId = (Integer) personEntity.getProperty(Person.Columns.PERSON_ID).getValue();
		assertEquals(expectedPersonId, actualPersonId);
		// point name
		String actualPointName = (String) entity.getProperty(Point.Columns.POINT_NAME).getValue();
		assertEquals(expectedPointName, actualPointName);
		// shared to public
		boolean actualSharedToPublic = (Boolean) entity.getProperty(Point.Columns.SHARED_TO_PUBLIC)
				.getValue();
		assertEquals(expectedSharedToPublic, actualSharedToPublic);
		// side code
		int actualSideCode = (Integer) entity.getProperty(Point.Columns.SIDE_CODE).getValue();
		assertEquals(expectedSideCode, actualSideCode);
		// topic id
		OEntity topicEntity = odataRead.getRelatedEntity(entity, Point.Columns.TOPIC_ID);
		int actualTopicId = (Integer) topicEntity.getProperty(Topic.Columns.TOPIC_ID).getValue();
		assertEquals(expectedTopicId, actualTopicId);
	}

	public static void testInsertTopic() {

		fail("Not implemented many-to-many support yet");
		OdataWriteClient odata = new OdataWriteClient(ODataConstants.DISCUSSIONS_JAPAN);
		OdataReadClient odataRead = new OdataReadClient(ODataConstants.DISCUSSIONS_JAPAN);
		String expectedName = "Android topic";
		int expectedDiscussionId = 1;
		int expectedPersonId = 1;
		OEntity entity = odata.insertTopic(expectedName, expectedDiscussionId, expectedPersonId);
		// name
		String actualName = (String) entity.getProperty(Topic.Columns.NAME).getValue();
		assertEquals(expectedName, actualName);
		// discussion
		OEntity discussionEntity = odataRead.getRelatedEntity(entity, Topic.Columns.DISCUSSION_ID);
		int actualDiscussionId = (Integer) discussionEntity.getProperty(Discussion.Columns.DISCUSSION_ID)
				.getValue();
		assertEquals(expectedDiscussionId, actualDiscussionId);
		// person
		OEntity personEntity = odataRead.getRelatedEntity(entity, Topic.Columns.PERSON_ID);
		int actualPersonId = (Integer) personEntity.getProperty(Person.Columns.PERSON_ID).getValue();
		assertEquals(expectedPersonId, actualPersonId);
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
