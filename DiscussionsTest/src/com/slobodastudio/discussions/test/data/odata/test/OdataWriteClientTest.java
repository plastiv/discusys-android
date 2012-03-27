package com.slobodastudio.discussions.test.data.odata.test;

import android.test.AndroidTestCase;

public class OdataWriteClientTest extends AndroidTestCase {

	// public static void testInsertDiscussion() {
	//
	// OdataWriteClient odata = new OdataWriteClient(ODataConstants.SERVICE_URL);
	// String expectedSubject = "Android Subject";
	// OEntity entity = odata.insertDiscussion(expectedSubject);
	// String actualSubject = (String) entity.getProperty(Discussions.Columns.SUBJECT).getValue();
	// assertEquals(expectedSubject, actualSubject);
	// int discussionId = (Integer) entity.getProperty(Discussions.Columns.ID).getValue();
	// assertEquals(true, discussionId > 0);
	// }
	//
	// public static void testInsertPerson() {
	//
	// OdataWriteClient odata = new OdataWriteClient(ODataConstants.SERVICE_URL);
	// String expectedName = "Android";
	// String expectedMail = "androoid@test.com";
	// int expectedColor = Color.CYAN;
	// boolean expectedOnline = false;
	// OEntity entity = odata.insertPerson(expectedName, expectedMail, expectedColor, expectedOnline);
	// String actualName = (String) entity.getProperty(Persons.Columns.NAME).getValue();
	// assertEquals(expectedName, actualName);
	// String actualMail = (String) entity.getProperty(Persons.Columns.EMAIL).getValue();
	// assertEquals(expectedMail, actualMail);
	// int actualColor = (Integer) entity.getProperty(Persons.Columns.COLOR).getValue();
	// assertEquals(expectedColor, actualColor);
	// boolean actualOnline = (Boolean) entity.getProperty(Persons.Columns.ONLINE).getValue();
	// assertEquals(expectedOnline, actualOnline);
	// }
	//
	// public void testInsertPoint() {
	//
	// OdataWriteClient odataWrite = new OdataWriteClient(ODataConstants.SERVICE_URL);
	// OdataReadClient odataRead = new OdataReadClient(getContext());
	// int expectedAgreementCode = 0;
	// Byte[] expectedDrawing = new Byte[] { 0, 1 };
	// boolean expectedExpanded = false;
	// int expectedGroupId = 1;
	// String expectedNumberedPoint = "";
	// int expectedPersonId = 1;
	// String expectedPointName = "Android point";
	// boolean expectedSharedToPublic = true;
	// int expectedSideCode = 0;
	// int expectedTopicId = 1;
	// OEntity entity = odataWrite.insertPoint(expectedAgreementCode, expectedDrawing, expectedExpanded,
	// expectedGroupId, expectedNumberedPoint, expectedPersonId, expectedPointName,
	// expectedSharedToPublic, expectedSideCode, expectedTopicId);
	// // agreement code
	// int actualAgreementCode = (Integer) entity.getProperty(Points.Columns.AGREEMENT_CODE).getValue();
	// assertEquals(expectedAgreementCode, actualAgreementCode);
	// // drawing
	// Byte[] actualDrawing = (Byte[]) entity.getProperty(Points.Columns.DRAWING).getValue();
	// assertEquals(expectedDrawing[0], actualDrawing[0]);
	// assertEquals(expectedDrawing[1], actualDrawing[1]);
	// // expanded
	// boolean actualExpanded = (Boolean) entity.getProperty(Points.Columns.EXPANDED).getValue();
	// assertEquals(expectedExpanded, actualExpanded);
	// // group id
	// OEntity groupEntity = odataRead.getRelatedEntity(entity, Points.Columns.GROUP_ID_SERVER);
	// assertNotNull(groupEntity);
	// int actualGroupId = (Integer) groupEntity.getProperty(Group.Columns.GROUP_ID).getValue();
	// assertEquals(expectedGroupId, actualGroupId);
	// // numbered point
	// String actualNumberedPoint = (String) entity.getProperty(Points.Columns.NUMBERED_POINT).getValue();
	// assertEquals(expectedNumberedPoint, actualNumberedPoint);
	// // person id
	// OEntity personEntity = odataRead.getRelatedEntity(entity, Points.Columns.PERSON_ID);
	// int actualPersonId = (Integer) personEntity.getProperty(Persons.Columns.ID).getValue();
	// assertEquals(expectedPersonId, actualPersonId);
	// // point name
	// String actualPointName = (String) entity.getProperty(Points.Columns.NAME).getValue();
	// assertEquals(expectedPointName, actualPointName);
	// // shared to public
	// boolean actualSharedToPublic = (Boolean) entity.getProperty(Points.Columns.SHARED_TO_PUBLIC)
	// .getValue();
	// assertEquals(expectedSharedToPublic, actualSharedToPublic);
	// // side code
	// int actualSideCode = (Integer) entity.getProperty(Points.Columns.SIDE_CODE).getValue();
	// assertEquals(expectedSideCode, actualSideCode);
	// // topic id
	// OEntity topicEntity = odataRead.getRelatedEntity(entity, Points.Columns.TOPIC_ID);
	// int actualTopicId = (Integer) topicEntity.getProperty(Topics.Columns.ID).getValue();
	// assertEquals(expectedTopicId, actualTopicId);
	// }
	//
	// public void testInsertTopic() {
	//
	// fail("Not implemented many-to-many support yet");
	// OdataWriteClient odata = new OdataWriteClient(ODataConstants.SERVICE_URL);
	// OdataReadClient odataRead = new OdataReadClient(getContext());
	// String expectedName = "Android topic";
	// int expectedDiscussionId = 1;
	// int expectedPersonId = 1;
	// OEntity entity = odata.insertTopic(expectedName, expectedDiscussionId, expectedPersonId);
	// // name
	// String actualName = (String) entity.getProperty(Topics.Columns.NAME).getValue();
	// assertEquals(expectedName, actualName);
	// // discussion
	// OEntity discussionEntity = odataRead.getRelatedEntity(entity, Topics.Columns.DISCUSSION_ID);
	// int actualDiscussionId = (Integer) discussionEntity.getProperty(Discussions.Columns.ID).getValue();
	// assertEquals(expectedDiscussionId, actualDiscussionId);
	// // person
	// OEntity personEntity = odataRead.getRelatedEntity(entity, Topics.Columns.PERSON_ID);
	// int actualPersonId = (Integer) personEntity.getProperty(Persons.Columns.ID).getValue();
	// assertEquals(expectedPersonId, actualPersonId);
	// }
	@Override
	protected void setUp() throws Exception {

		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}
}
