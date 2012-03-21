package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.PersonsTopics;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.RichText;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.utils.MyLog;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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

	private static final boolean LOGV = true && ApplicationConstants.DEBUG_MODE;
	private static final String TAG = OdataSyncService.class.getSimpleName();
	private final ODataConsumer consumer;
	private final Context context;

	/** Sets service root uri by default to japan server */
	public OdataSyncService(final Context context) {

		// FIXME: check if network is accessible
		consumer = ODataConsumer.create(ODataConstants.SERVICE_URL_JAPAN);
		this.context = context;
	}

	public OdataSyncService(final String serviceRootUri, final Context context) {

		// FIXME: check if network is accessible
		consumer = ODataConsumer.create(serviceRootUri);
		this.context = context;
	}

	private static int getAsInt(final OEntity entity, final String valueColumn) {

		return (Integer) entity.getProperty(valueColumn).getValue();
	}

	private static ContentValues OEntityToContentValue(final OEntity entity) {

		final ContentValues cv = new ContentValues();
		for (OProperty<?> property : entity.getProperties()) {
			if (LOGV) {
				MyLog.v(TAG, property.getName() + ":" + property.getType() + ":" + property.getValue());
			}
			put(cv, property);
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
			throw new IllegalArgumentException("Unknown property name: " + property.getName() + " type: "
					+ property.getType() + " value: " + property.getValue() + "javaType: "
					+ classType.getCanonicalName());
		}
		return cv;
	}

	public void downloadAllValues() {

		downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		downloadTopics();
		downloadPoints();
		downloadDescriptions();
	}

	public void downloadDescriptions() {

		for (OEntity entity : consumer.getEntities(RichText.TABLE_NAME).execute()) {
			insertDescription(entity);
		}
	}

	public void downloadPoint(final int pointId) {

		OEntity entity = consumer.getEntity(Points.TABLE_NAME, pointId).execute();
		insertPoint(entity);
	}

	public void downloadPoints() {

		for (OEntity entity : consumer.getEntities(Points.TABLE_NAME).execute()) {
			insertPoint(entity);
		}
	}

	public void downloadPoints(final int topicId) {

		OEntity topic = consumer.getEntity(Topics.TABLE_NAME, topicId).execute();
		for (OEntity entity : getRelatedEntities(topic, "ArgPoint")) {
			insertPoint(entity);
		}
	}

	public void downloadTopics() {

		for (OEntity entity : consumer.getEntities(Topics.TABLE_NAME).execute()) {
			insertTopic(entity);
			insertPersonsTopics(entity);
		}
	}

	public void downloadValuesWithoutNavigationIds(final String tableName, final Uri contentUri) {

		final ContentResolver provider = context.getContentResolver();
		for (OEntity entity : consumer.getEntities(tableName).execute()) {
			ContentValues cv = OEntityToContentValue(entity);
			provider.insert(contentUri, cv);
		}
	}

	public Uri insertPoint(final OEntity entity) {

		// get properties
		ContentValues cv = OEntityToContentValue(entity);
		// get navigation id
		final String linkName = Points.Columns.TOPIC_ID;
		final OEntity topicEntity = getRelatedEntity(entity, linkName);
		if (topicEntity == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related " + linkName + " link is null");
			return null;
		}
		cv.put(linkName, getAsInt(topicEntity, Topics.Columns.ID));
		final String linkName2 = Points.Columns.PERSON_ID;
		final OEntity personEntity = getRelatedEntity(entity, linkName2);
		if (personEntity == null) {
			// TODO throw exception here
			Log.e(TAG, "Related " + linkName2 + " link is null");
			return null;
		}
		cv.put(linkName2, getAsInt(personEntity, Persons.Columns.ID));
		cv.put(Points.Columns.SYNC, false);
		if (LOGV) {
			MyLog.v(TAG, "Content value: " + cv.toString());
		}
		return context.getContentResolver().insert(Points.CONTENT_URI, cv);
	}

	private List<Integer> getNavigationPropertyIds(final OEntity entity, final String navPropertyName,
			final String originalPropertyName) {

		List<Integer> ids = new ArrayList<Integer>();
		for (OEntity person : getRelatedEntities(entity, navPropertyName)) {
			ids.add(getAsInt(person, originalPropertyName));
		}
		return ids;
	}

	private Enumerable<OEntity> getRelatedEntities(final OEntity originEntity, final String linkColumn) {

		return consumer.getEntities(originEntity.getLink(linkColumn, ORelatedEntitiesLink.class)).execute();
	}

	private OEntity getRelatedEntity(final OEntity originEntity, final String linkColumn) {

		return consumer.getEntity(originEntity.getLink(linkColumn, ORelatedEntityLink.class)).execute();
	}

	private Uri insertDescription(final OEntity entity) {

		// get properties
		ContentValues cv = OEntityToContentValue(entity);
		// get navigation id
		final String linkName = RichText.Columns.DISCUSSION_ID;
		OEntity discussionEntity = getRelatedEntity(entity, linkName);
		if (discussionEntity != null) {
			cv.put(linkName, getAsInt(discussionEntity, Discussions.Columns.ID));
		}
		// get navigation id
		final String linkName2 = RichText.Columns.POINT_ID;
		OEntity pointEntity = getRelatedEntity(entity, linkName2);
		if (pointEntity != null) {
			cv.put(linkName2, getAsInt(pointEntity, Points.Columns.ID));
		}
		if ((discussionEntity == null) && (pointEntity == null)) {
			// TODO: throw ex
			Log.e(TAG, "Both description foreign keys was null");
			return null;
		}
		// insert into table
		if (LOGV) {
			MyLog.v(TAG, "Content value: " + cv.toString());
		}
		return context.getContentResolver().insert(RichText.CONTENT_URI, cv);
	}

	private void insertPersonsTopics(final OEntity topic) {

		// get associated persons ids
		int topicId = (Integer) topic.getProperty(Topics.Columns.ID).getValue();
		List<Integer> personsIds = getNavigationPropertyIds(topic, Topics.Columns.PERSON_ID,
				Persons.Columns.ID);
		// insert many-to-many relationship
		for (int personId : personsIds) {
			ContentValues cv = new ContentValues();
			cv.put(PersonsTopics.Columns.TOPIC_ID, topicId);
			cv.put(PersonsTopics.Columns.PERSON_ID, personId);
			if (LOGV) {
				MyLog.v(TAG, "Content value: " + cv.toString());
			}
			// TODO: provide special PersonsTopics uri for insert
			context.getContentResolver().insert(Persons.buildTopicUri(1231231), cv);
		}
	}

	private Uri insertTopic(final OEntity entity) {

		// get properties
		ContentValues cv = OEntityToContentValue(entity);
		// get navigation id
		final String linkName = Topics.Columns.DISCUSSION_ID;
		OEntity discussionEntity = getRelatedEntity(entity, linkName);
		if (discussionEntity == null) {
			// TODO: throw ex here
			Log.e(TAG, "Related " + linkName + " link is null");
			return null;
		}
		cv.put(linkName, getAsInt(discussionEntity, Discussions.Columns.ID));
		// insert into table
		if (LOGV) {
			MyLog.v(TAG, "Content value: " + cv.toString());
		}
		return context.getContentResolver().insert(Topics.CONTENT_URI, cv);
	}
}
