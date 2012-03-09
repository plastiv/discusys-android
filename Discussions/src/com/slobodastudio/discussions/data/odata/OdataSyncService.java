package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.PersonsTopics;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.utils.MyLog;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import org.core4j.Enumerable;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.edm.EdmSimpleType;

import java.util.ArrayList;
import java.util.List;

public class OdataSyncService {

	private static final boolean LOGV = true;
	private static final String TAG = OdataSyncService.class.getSimpleName();
	private final ODataConsumer consumer;
	private final Context context;

	public OdataSyncService(final String serviceRootUri, final Context context) {

		// FIXME: check if network is accessible
		consumer = ODataConsumer.create(serviceRootUri);
		this.context = context;
	}

	private static ContentValues OEntityToContentValue(final OEntity entity) {

		final ContentValues cv = new ContentValues();
		Log.v(TAG, entity.getProperties().toString());
		for (OProperty<?> property : entity.getProperties()) {
			if (LOGV) {
				MyLog.v(TAG, property.getName() + ":" + property.getType() + ":" + property.getValue());
			}
			// FIXME: this is brutal hack
			if (property.getName().equals(Points.Columns.GROUP_ID_SERVER)) {
				cv.put(Points.Columns.GROUP_ID, (Integer) property.getValue());
			} else {
				put(cv, property);
			}
		}
		return cv;
	}

	private static ContentValues put(final ContentValues cv, final OProperty<?> property) {

		EdmSimpleType<?> type = (EdmSimpleType<?>) property.getType();
		Class<? extends Object> classType = type.getCanonicalJavaType();
		if (classType.equals(Integer.class)) {
			cv.put(property.getName(), (Integer) property.getValue());
		} else if (classType.equals(String.class)) {
			cv.put(property.getName(), (String) property.getValue());
		} else if (classType.equals(Boolean.class)) {
			cv.put(property.getName(), (Boolean) property.getValue());
		} else if (classType.equals(byte[].class)) {
			cv.put(property.getName(), (byte[]) property.getValue());
		} else {
			throw new IllegalAccessError("Unknown property name: " + property.getName() + " type: "
					+ property.getType() + " value: " + property.getValue() + "javaType: "
					+ classType.getCanonicalName());
		}
		return cv;
	}

	public void downloadAllValues() {

		downloadValues(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		downloadValues(Persons.TABLE_NAME, Persons.CONTENT_URI);
		downloadTopics();
		downloadPoints();
	}

	public void downloadPoints() {

		for (OEntity entity : consumer.getEntities(Points.TABLE_NAME).execute()) {
			insertPoint(entity);
		}
	}

	public void downloadTopics() {

		for (OEntity entity : consumer.getEntities(Topics.TABLE_NAME).execute()) {
			insertTopic(entity);
			insertPersonsTopics(entity);
		}
	}

	public void downloadValues(final String tableName, final Uri contentUri) {

		final ContentResolver provider = context.getContentResolver();
		for (OEntity entity : consumer.getEntities(tableName).execute()) {
			ContentValues cv = OEntityToContentValue(entity);
			try {
				provider.insert(contentUri, cv);
			} catch (SQLiteException e) {
				throw new RuntimeException("Cant insert value: " + cv.toString(), e);
			}
		}
	}

	private int getNavigationPropertyId(final OEntity entity, final String navPropertyName,
			final String originalPropertyName) {

		OEntity linkedEntity = consumer.getEntity(entity.getLink(navPropertyName, ORelatedEntityLink.class))
				.execute();
		if (linkedEntity == null) {
			// FIXME: bad behavior
			return 1;
		}
		return (Integer) linkedEntity.getProperty(originalPropertyName).getValue();
	}

	private List<Integer> getNavigationPropertyIds(final OEntity entity, final String navPropertyName,
			final String originalPropertyName) {

		List<Integer> ids = new ArrayList<Integer>();
		Enumerable<OEntity> linkedPersons = consumer.getEntities(
				entity.getLink(navPropertyName, ORelatedEntitiesLink.class)).execute();
		for (OEntity person : linkedPersons) {
			ids.add((Integer) person.getProperty(originalPropertyName).getValue());
		}
		return ids;
	}

	private void insertPersonsTopics(final OEntity topic) {

		// get accociated persons ids
		int topicId = (Integer) topic.getProperty(Topics.Columns.ID).getValue();
		List<Integer> personsIds = getNavigationPropertyIds(topic, Topics.Columns.PERSON_ID,
				Persons.Columns.ID);
		for (int personId : personsIds) {
			ContentValues cv = new ContentValues();
			cv.put(PersonsTopics.Columns.TOPIC_ID, topicId);
			cv.put(PersonsTopics.Columns.PERSON_ID, personId);
			if (LOGV) {
				MyLog.v(TAG, "Content value: " + cv.toString());
			}
			try {
				context.getContentResolver().insert(Persons.buildTopicUri(1231231), cv);
			} catch (SQLiteException e) {
				throw new RuntimeException("Cant insert value: " + cv.toString(), e);
			}
		}
	}

	private Uri insertPoint(final OEntity point) {

		// get properties
		ContentValues cv = OEntityToContentValue(point);
		// get navigation id
		cv.put(Points.Columns.PERSON_ID, getNavigationPropertyId(point, Points.Columns.PERSON_ID,
				Persons.Columns.ID));
		cv.put(Points.Columns.TOPIC_ID, getNavigationPropertyId(point, Points.Columns.TOPIC_ID,
				Topics.Columns.ID));
		// insert into topic table
		// if (!cv.containsKey(Points.Columns.NUMBERED_POINT)) {
		// cv.put(Points.Columns.NUMBERED_POINT, "default numbered point");
		// }
		// if (!cv.containsKey(Points.Columns.DRAWING)) {
		// cv.put(Points.Columns.DRAWING, new byte[] { 2, 3 });
		// }
		cv.put(Points.Columns.NUMBERED_POINT, "default numbered point");
		cv.put(Points.Columns.DRAWING, new byte[] { 2, 3 });
		cv.put(Points.Columns.GROUP_ID, 1);
		if (LOGV) {
			MyLog.v(TAG, "Content value: " + cv.toString());
		}
		try {
			return context.getContentResolver().insert(Points.CONTENT_URI, cv);
		} catch (SQLiteException e) {
			throw new RuntimeException("Cant insert value: " + cv.toString(), e);
		}
	}

	private Uri insertTopic(final OEntity topic) {

		// get properties
		ContentValues cv = OEntityToContentValue(topic);
		// get navigation id
		cv.put(Topics.Columns.DISCUSSION_ID, getNavigationPropertyId(topic, Topics.Columns.DISCUSSION_ID,
				Discussions.Columns.ID));
		// insert into topic table
		if (LOGV) {
			MyLog.v(TAG, "Content value: " + cv.toString());
		}
		try {
			return context.getContentResolver().insert(Topics.CONTENT_URI, cv);
		} catch (SQLiteException e) {
			throw new RuntimeException("Cant insert value: " + cv.toString(), e);
		}
	}
}
