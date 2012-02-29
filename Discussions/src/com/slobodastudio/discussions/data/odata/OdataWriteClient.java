package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.odata.DiscussionsTableShema.Discussion;
import com.slobodastudio.discussions.data.odata.DiscussionsTableShema.Person;
import com.slobodastudio.discussions.data.odata.DiscussionsTableShema.Point;
import com.slobodastudio.discussions.data.odata.DiscussionsTableShema.Tables;

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

		return consumer.createEntity(Tables.DISCUSSION).properties(
				OProperties.string(Discussion.SUBJECT, subject)).execute();
	}

	public OEntity insertPerson(final String name, final String email, final Integer color) {

		return consumer.createEntity(Tables.PERSON).properties(OProperties.string(Person.NAME, name))
				.properties(OProperties.string(Person.EMAIL, email)).properties(
						OProperties.int32(Person.COLOR, color)).execute();
	}

	public OEntity insertPoint(final String point, final int agreementCode, final int sideCode,
			final boolean sharedToPublic, final int topicId, final int personId) {

		return consumer.createEntity(Tables.POINT).properties(OProperties.string(Point.POINT, point))
				.properties(OProperties.int32(Point.AGREEMENT_CODE, Integer.valueOf(agreementCode)))
				.properties(OProperties.int32(Point.SIDE_CODE, Integer.valueOf(sideCode))).properties(
						OProperties.boolean_(Point.SHARED_TO_PUBLIC, Boolean.valueOf(sharedToPublic))).link(
						Point.TOPIC_ID, OEntityKey.parse(String.valueOf(topicId))).link(Point.PERSON_ID,
						OEntityKey.parse(String.valueOf(personId))).execute();
	}
}
