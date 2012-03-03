package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussion;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Person;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topic;

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
		return consumer.createEntity(Discussion.TABLE_NAME)
				.properties(OProperties.string(Discussion.Columns.SUBJECT, subject))
				.execute();
		// @formatter:on
	}

	public OEntity insertPerson(final String name, final String email, final Integer color,
			final boolean online) {

		// @formatter:off
		return consumer.createEntity(Person.TABLE_NAME)
				.properties(OProperties.string(Person.Columns.NAME, name))
				.properties(OProperties.string(Person.Columns.EMAIL, email))
				.properties(OProperties.int32(Person.Columns.COLOR, color))
				.properties(OProperties.boolean_(Person.Columns.ONLINE, online))
				.execute();
		// @formatter:on
	}

	public OEntity insertPoint(final int agreementCode, final byte[] drawing, final boolean expanded,
			final int groupId, final String numberedPoint, final int personId, final String pointName,
			final boolean sharedToPublic, final int sideCode, final int topicId) {

		// @formatter:off
		return consumer.createEntity(Point.TABLE_NAME)
				.properties(OProperties.int32(Point.Columns.AGREEMENT_CODE, Integer.valueOf(agreementCode)))
				.properties(OProperties.binary(Point.Columns.DRAWING, drawing))
				.properties(OProperties.boolean_(Point.Columns.EXPANDED, Boolean.valueOf(expanded)))
				.link(Point.Columns.GROUP_ID_SERVER, OEntityKey.parse(String.valueOf(groupId)))
				.properties(OProperties.string(Point.Columns.NUMBERED_POINT, numberedPoint))
				.link(Point.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(personId)))
				.properties(OProperties.string(Point.Columns.POINT_NAME, pointName))
				.properties(OProperties.boolean_(Point.Columns.SHARED_TO_PUBLIC, Boolean.valueOf(sharedToPublic)))
				.properties(OProperties.int32(Point.Columns.SIDE_CODE, Integer.valueOf(sideCode)))		
				.link(Point.Columns.TOPIC_ID, OEntityKey.parse(String.valueOf(topicId)))
				.execute();
		// @formatter:on
	}

	public OEntity insertTopic(final String topicName, final int discussionId, final int personId) {

		// @formatter:off
		return consumer.createEntity(Topic.TABLE_NAME)
				.properties(OProperties.int32(Topic.Columns.DISCUSSION_ID, Integer.valueOf(discussionId)))
				.properties(OProperties.string(Topic.Columns.NAME, topicName))	
				.link(Topic.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(personId)))
				.execute();
		// @formatter:on
	}
}
