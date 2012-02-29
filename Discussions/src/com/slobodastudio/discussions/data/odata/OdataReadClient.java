package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.odata.DiscussionsTableShema.Discussion;
import com.slobodastudio.discussions.data.odata.DiscussionsTableShema.Person;
import com.slobodastudio.discussions.data.odata.DiscussionsTableShema.Tables;
import com.slobodastudio.discussions.data.odata.DiscussionsTableShema.Topic;
import com.slobodastudio.discussions.tool.MyLog;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmSimpleType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdataReadClient {

	// FIXME catch 404 errors from HTTP RESPONSE
	private static final String TAG = "OdataReadClient";
	private final ODataConsumer consumer;

	/** Create a new odata consumer pointing to the odata read-write service.
	 * 
	 * @param serviceRootUri
	 *            the service uri e.g. http://services.odata.org/Northwind/Northwind.svc/ */
	public OdataReadClient(final String serviceRootUri) {

		// FIXME: check if network is accessible
		consumer = ODataConsumer.create(serviceRootUri);
	}

	public ArrayList<Map<String, Object>> getDiscussions() {

		ArrayList<Map<String, Object>> discussions = new ArrayList<Map<String, Object>>();
		Map<String, Object> m;
		for (OEntity discussion : consumer.getEntities(Tables.DISCUSSION).execute()) {
			m = new HashMap<String, Object>();
			m.put(Discussion._ID, discussion.getProperty(Discussion._ID).getValue());
			m.put(Discussion.SUBJECT, discussion.getProperty(Discussion.SUBJECT).getValue());
			discussions.add(m);
		}
		return discussions;
	}

	public ArrayList<Map<String, Object>> getDiscussions(final int personId) {

		ArrayList<Map<String, Object>> discussions = new ArrayList<Map<String, Object>>();
		Map<String, Object> m;
		List<Integer> discId = new ArrayList<Integer>();
		try {
			for (OEntity topic : consumer.getEntities(Tables.PERSON + "(" + personId + ")/" + Tables.TOPIC)
					.execute()) {
				try {
					for (OEntity discussion : consumer.getEntities(
							Tables.TOPIC + "(" + topic.getProperty(Topic._ID).getValue() + ")/"
									+ Tables.DISCUSSION).execute()) {
						m = new HashMap<String, Object>();
						if (!discId.contains(discussion.getProperty(Discussion._ID).getValue())) {
							discId.add((Integer) discussion.getProperty(Discussion._ID).getValue());
							m.put(Discussion._ID, discussion.getProperty(Discussion._ID).getValue());
							m.put(Discussion.SUBJECT, discussion.getProperty(Discussion.SUBJECT).getValue());
							discussions.add(m);
						}
					}
				} catch (RuntimeException e) {
					MyLog.e(TAG, "Can't query discussions for person id: " + personId, e);
				}
			}
		} catch (RuntimeException e) {
			MyLog.e(TAG, "Can't query discussions for person id: " + personId, e);
		}
		return discussions;
	}

	public ArrayList<Map<String, Object>> getPoints() {

		ArrayList<Map<String, Object>> points = new ArrayList<Map<String, Object>>();
		Map<String, Object> m;
		for (OEntity point : consumer.getEntities(Tables.POINT).execute()) {
			m = new HashMap<String, Object>();
			for (OProperty<?> p : point.getProperties()) {
				Object v = p.getValue();
				if (p.getType().equals(EdmSimpleType.BINARY) && (v != null)) {
					v = org.odata4j.repack.org.apache.commons.codec.binary.Base64.encodeBase64String(
							(byte[]) v).trim();
				}
				m.put(p.getName(), v);
			}
			points.add(m);
		}
		return points;
	}

	public ArrayList<Map<String, Object>> getPoints(final int topicId) {

		ArrayList<Map<String, Object>> points = new ArrayList<Map<String, Object>>();
		Map<String, Object> m;
		for (OEntity point : consumer.getEntities(Tables.TOPIC + "(" + topicId + ")/" + Tables.POINT)
				.execute()) {
			m = new HashMap<String, Object>();
			for (OProperty<?> p : point.getProperties()) {
				Object v = p.getValue();
				if (p.getType().equals(EdmSimpleType.BINARY) && (v != null)) {
					v = org.odata4j.repack.org.apache.commons.codec.binary.Base64.encodeBase64String(
							(byte[]) v).trim();
				}
				m.put(p.getName(), v);
			}
			points.add(m);
		}
		return points;
	}

	public ArrayList<Map<String, Object>> getTopics() {

		ArrayList<Map<String, Object>> topics = new ArrayList<Map<String, Object>>();
		Map<String, Object> m;
		for (OEntity topic : consumer.getEntities(Tables.TOPIC).execute()) {
			m = new HashMap<String, Object>();
			m.put(Topic._ID, topic.getProperty(Topic._ID).getValue());
			m.put(Topic.NAME, topic.getProperty(Topic.NAME).getValue());
			topics.add(m);
		}
		return topics;
	}

	public ArrayList<Map<String, Object>> getTopics(final int discussionId) {

		ArrayList<Map<String, Object>> topics = new ArrayList<Map<String, Object>>();
		Map<String, Object> m;
		for (OEntity topic : consumer.getEntities(
				Tables.DISCUSSION + "(" + discussionId + ")/" + Tables.TOPIC).execute()) {
			m = new HashMap<String, Object>();
			m.put(Topic._ID, topic.getProperty(Topic._ID).getValue());
			m.put(Topic.NAME, topic.getProperty(Topic.NAME).getValue());
			topics.add(m);
		}
		return topics;
	}

	public ArrayList<Map<String, Object>> getUsers() {

		ArrayList<Map<String, Object>> users = new ArrayList<Map<String, Object>>();
		Map<String, Object> m;
		for (OEntity person : consumer.getEntities(Tables.PERSON).execute()) {
			m = new HashMap<String, Object>();
			m.put(Person._ID, person.getProperty(Person._ID).getValue());
			m.put(Person.COLOR, person.getProperty(Person.COLOR).getValue());
			m.put(Person.NAME, person.getProperty(Person.NAME).getValue());
			m.put(Person.EMAIL, person.getProperty(Person.EMAIL).getValue());
			users.add(m);
		}
		return users;
	}
}
