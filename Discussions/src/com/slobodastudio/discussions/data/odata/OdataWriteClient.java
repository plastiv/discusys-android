package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OProperties;

public class OdataWriteClient {

	private final ODataConsumer consumer;

	// FIXME catch 404 errors from HTTP RESPONSE
	/** Create a new odata consumer pointing to the odata read-write service.
	 * 
	 * @param serviceRootUri
	 *            the service uri e.g. http://services.odata.org/Northwind/Northwind.svc/ */
	public OdataWriteClient(final String serviceRootUri) {

		// FIXME: check if network is accessible
		consumer = ODataConsumer.create(serviceRootUri);
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

	public OEntity insertPoint(final int agreementCode, final byte[] drawing, final boolean expanded,
			final int groupId, final String numberedPoint, final int personId, final String pointName,
			final boolean sharedToPublic, final int sideCode, final int topicId) {

		// @formatter:off
		return consumer.createEntity(Points.TABLE_NAME)
				.properties(OProperties.int32(Points.Columns.AGREEMENT_CODE, Integer.valueOf(agreementCode)))
				.properties(OProperties.binary(Points.Columns.DRAWING, drawing))
				.properties(OProperties.boolean_(Points.Columns.EXPANDED, Boolean.valueOf(expanded)))
				.link(Points.Columns.GROUP_ID_SERVER, OEntityKey.parse(String.valueOf(groupId)))
				.properties(OProperties.string(Points.Columns.NUMBERED_POINT, numberedPoint))
				.link(Points.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(personId)))
				.properties(OProperties.string(Points.Columns.POINT_NAME, pointName))
				.properties(OProperties.boolean_(Points.Columns.SHARED_TO_PUBLIC, Boolean.valueOf(sharedToPublic)))
				.properties(OProperties.int32(Points.Columns.SIDE_CODE, Integer.valueOf(sideCode)))		
				.link(Points.Columns.TOPIC_ID, OEntityKey.parse(String.valueOf(topicId)))
				.execute();
		// @formatter:on
	}

	public OEntity insertTopic(final String topicName, final int discussionId, final int personId) {

		// @formatter:off
		return consumer.createEntity(Topics.TABLE_NAME)
				.properties(OProperties.int32(Topics.Columns.DISCUSSION_ID, Integer.valueOf(discussionId)))
				.properties(OProperties.string(Topics.Columns.NAME, topicName))	
				.link(Topics.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(personId)))
				.execute();
		// @formatter:on
	}
}
