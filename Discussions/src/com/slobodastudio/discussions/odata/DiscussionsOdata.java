package com.slobodastudio.discussions.odata;

import com.slobodastudio.discussions.odata.DiscussionsTableShema.Discussion;
import com.slobodastudio.discussions.odata.DiscussionsTableShema.Person;
import com.slobodastudio.discussions.odata.DiscussionsTableShema.Point;
import com.slobodastudio.discussions.odata.DiscussionsTableShema.Tables;
import com.slobodastudio.discussions.odata.DiscussionsTableShema.Topic;

import android.graphics.Color;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OProperties;

public class DiscussionsOdata extends ODataReportUtil {

	ODataConsumer consumer;

	public DiscussionsOdata() {

		// create a new odata consumer pointing to the odata test read-write service
		consumer = ODataConsumer.create(ODataConstants.DISCUSSIONS_JAPAN);
		// ODataConsumer.dump.all(true);
	}

	public OEntity insertDiscussion() {

		// create the new discussion
		return consumer.createEntity(Tables.DISCUSSION).properties(
				OProperties.string(Discussion.SUBJECT, "Android discussion")).execute();
	}

	public OEntity insertPerson() {

		// create the new person
		return consumer.createEntity(Tables.PERSON).properties(OProperties.string(Person.NAME, "Android"))
				.properties(OProperties.string(Person.EMAIL, "test@android.from")).properties(
						OProperties.int32(Person.COLOR, Integer.valueOf(Color.CYAN))).execute();
	}

	public OEntity insertPoint() {

		// create the new point
		return consumer.createEntity(Tables.POINT).properties(
				OProperties.string(Point.POINT, "Android point name finally")).properties(
				OProperties.int32(Point.AGREEMENT_CODE, Integer.valueOf(0))).properties(
				OProperties.int32(Point.SIDE_CODE, Integer.valueOf(1))).properties(
				OProperties.boolean_(Point.SHARED_TO_PUBLIC, Boolean.TRUE)).link(Point.TOPIC_ID,
				OEntityKey.parse("2")).link(Point.PERSON_ID, OEntityKey.parse("2")).execute();
	}

	public void insertTopic() {

		// create the new discussion
		// OEntity discussion = consumer.createEntity(Tables.DISCUSSION).properties(
		// OProperties.string(Discussion.SUBJECT, "Android discussion for connection with topic"))
		// .execute();
		// // create the new person
		// OEntity person = consumer.createEntity(Tables.PERSON).properties(
		// OProperties.string(Person.NAME, "Android person for topic")).properties(
		// OProperties.string(Person.EMAIL, "test@android.from")).properties(
		// OProperties.int32(Person.COLOR, Integer.valueOf(Color.CYAN))).execute();
		// create the new topic
		OEntity topic = consumer.createEntity(Tables.TOPIC).properties(
				OProperties.string(Topic.NAME, "Android topic link to person 2")).execute();
		consumer.mergeEntity(topic.getEntitySetName(), topic.getEntityKey()).link(Topic.PERSON_ID,
				OEntityKey.parse("3")).execute();
	}

	public void reportCurrentState() {

		// list all persons
		for (OEntity person : consumer.getEntities(Tables.PERSON).execute()) {
			reportEntity("Person: " + person.getProperty(Person.NAME).getValue(), person);
		}
		// list all discussions
		for (OEntity discussion : consumer.getEntities(Tables.DISCUSSION).execute()) {
			reportEntity("Discussion: " + discussion.getProperty(Discussion.SUBJECT).getValue(), discussion);
		}
		// list all topics
		for (OEntity topic : consumer.getEntities(Tables.TOPIC).execute()) {
			reportEntity("Topic: " + topic.getProperty(Topic.NAME).getValue(), topic);
		}
		// list all points
		for (OEntity point : consumer.getEntities(Tables.POINT).execute()) {
			reportEntity("Point: " + point.getProperty(Point.POINT).getValue(), point);
		}
	}
}
