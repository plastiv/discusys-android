package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OCreateRequest;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OProperties;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class OdataWriteClient {

	private final ODataConsumer consumer;

	/** Create a new odata consumer pointing to the odata read-write service.
	 * 
	 * @param serviceRootUri
	 *            the service uri e.g. http://services.odata.org/Northwind/Northwind.svc/ */
	public OdataWriteClient() {

		// FIXME: check if network is accessible
		consumer = ODataJerseyConsumer.newBuilder(ODataConstants.SERVICE_URL).build();
	}

	// FIXME catch 404 errors from HTTP RESPONSE
	/** Create a new odata consumer pointing to the odata read-write service.
	 * 
	 * @param serviceRootUri
	 *            the service uri e.g. http://services.odata.org/Northwind/Northwind.svc/ */
	public OdataWriteClient(final String serviceRootUri) {

		// FIXME: check if network is accessible
		consumer = ODataJerseyConsumer.newBuilder(serviceRootUri).build();
	}

	public void deletePoint(final int pointId) {

		consumer.deleteEntity(Points.TABLE_NAME, pointId).execute();
	}

	public OEntity insertDiscussion(final String subject) {

		// @formatter:off
		return consumer.createEntity(Discussions.TABLE_NAME)
				.properties(OProperties.string(Discussions.Columns.SUBJECT, subject))
				.execute();
		// @formatter:on
	}

	public OEntity insertPerson(final String name, final String email, final Integer color,
			final boolean online) {

		// @formatter:off
		return consumer.createEntity(Persons.TABLE_NAME)
				.properties(OProperties.string(Persons.Columns.NAME, name))
				.properties(OProperties.string(Persons.Columns.EMAIL, email))
				.properties(OProperties.int32(Persons.Columns.COLOR, color))
				.properties(OProperties.boolean_(Persons.Columns.ONLINE, online))
				.execute();
		// @formatter:on
	}

	public OEntity insertPoint(final int agreementCode, final Byte[] drawing, final boolean expanded,
			final Integer groupId, final String numberedPoint, final int personId, final String pointName,
			final boolean sharedToPublic, final int sideCode, final int topicId) {

		// @formatter:off
		OCreateRequest<OEntity> request = consumer.createEntity(Points.TABLE_NAME)
				.properties(OProperties.int32(Points.Columns.AGREEMENT_CODE, Integer.valueOf(agreementCode)))				
				.properties(OProperties.boolean_(Points.Columns.EXPANDED, Boolean.valueOf(expanded)))	
				.link(Points.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(personId)))
				.properties(OProperties.string(Points.Columns.NAME, pointName))
				.properties(OProperties.boolean_(Points.Columns.SHARED_TO_PUBLIC, Boolean.valueOf(sharedToPublic)))
				.properties(OProperties.int32(Points.Columns.SIDE_CODE, Integer.valueOf(sideCode)))		
				.link(Points.Columns.TOPIC_ID, OEntityKey.parse(String.valueOf(topicId)));
		
		if(drawing != null){
			//request.properties(OProperties.binary(Points.Columns.DRAWING, drawing));
		}
		if(groupId != null){
			
			request.link(Points.Columns.GROUP_ID_SERVER, OEntityKey.parse(String.valueOf(groupId)));
		}
		if(numberedPoint != null){
			request.properties(OProperties.string(Points.Columns.NUMBERED_POINT, numberedPoint));
		}
		return request.execute();
		// @formatter:on
	}

	public OEntity insertPoint(final Point point) {

		// @formatter:off
		return consumer.createEntity(Points.TABLE_NAME)
				.properties(OProperties.int32(Points.Columns.AGREEMENT_CODE, point.getAgreementCode()))
				.properties(OProperties.binary(Points.Columns.DRAWING, point.getDrawing()))
				.properties(OProperties.boolean_(Points.Columns.EXPANDED, point.isExpanded()))
				//.link(Points.Columns.GROUP_ID_SERVER, OEntityKey.parse(String.valueOf(point.getGroupId())))
				.properties(OProperties.string(Points.Columns.NUMBERED_POINT, point.getNumberedPoint()))
				.link(Points.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(point.getPersonId())))
				.properties(OProperties.string(Points.Columns.NAME, point.getName()))
				.properties(OProperties.boolean_(Points.Columns.SHARED_TO_PUBLIC, Boolean.valueOf(point.isSharedToPublic())))
				.properties(OProperties.int32(Points.Columns.SIDE_CODE, Integer.valueOf(point.getSideCode())))		
				.link(Points.Columns.TOPIC_ID, OEntityKey.parse(String.valueOf(point.getTopicId())))
				.execute();
		// @formatter:on
	}

	public OEntity insertTopic(final String topicName, final int discussionId, final int personId) {

		// @formatter:off
		return consumer.createEntity(Topics.TABLE_NAME)
				.link(Topics.Columns.DISCUSSION_ID, OEntityKey.parse(String.valueOf(discussionId)))
				.properties(OProperties.string(Topics.Columns.NAME, topicName))	
				.link(Topics.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(personId)))
				.execute();
		// @formatter:on
	}

	public boolean updatePoint(final Point point) {

		// @formatter:off
		return consumer.mergeEntity(Points.TABLE_NAME, point.getId())
				.properties(OProperties.int32(Points.Columns.AGREEMENT_CODE, point.getAgreementCode()))
				.properties(OProperties.binary(Points.Columns.DRAWING, point.getDrawing()))
				.properties(OProperties.boolean_(Points.Columns.EXPANDED, point.isExpanded()))
				//.link(Points.Columns.GROUP_ID_SERVER, OEntityKey.parse(String.valueOf(point.getGroupId())))
				.properties(OProperties.string(Points.Columns.NUMBERED_POINT, point.getNumberedPoint()))
				.link(Points.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(point.getPersonId())))
				.properties(OProperties.string(Points.Columns.NAME, point.getName()))
				.properties(OProperties.boolean_(Points.Columns.SHARED_TO_PUBLIC, Boolean.valueOf(point.isSharedToPublic())))
				.properties(OProperties.int32(Points.Columns.SIDE_CODE, Integer.valueOf(point.getSideCode())))		
				.link(Points.Columns.TOPIC_ID, OEntityKey.parse(String.valueOf(point.getTopicId())))
				.execute();
		// @formatter:on
	}
}
