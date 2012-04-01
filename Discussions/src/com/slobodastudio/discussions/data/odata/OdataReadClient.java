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
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.core4j.Enumerable;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.edm.EdmSimpleType;

import java.util.ArrayList;
import java.util.List;

public class OdataReadClient extends BaseOdataClient {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final boolean LOGV = false && ApplicationConstants.DEV_MODE;
	private static final String TAG = OdataReadClient.class.getSimpleName();
	private final ContentResolver mContentResolver;

	public OdataReadClient(final Context context) {

		super(context);
		mContentResolver = mContext.getContentResolver();
	}

	public OdataReadClient(final String serviceRootUri, final Context context) {

		super(serviceRootUri, context);
		mContentResolver = mContext.getContentResolver();
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

	@Deprecated
	public void downloadAllValues() {

		downloadValuesWithoutNavigationIds(Persons.TABLE_NAME, Persons.CONTENT_URI);
		downloadValuesWithoutNavigationIds(Discussions.TABLE_NAME, Discussions.CONTENT_URI);
		downloadTopics();
		downloadPoints();
		// downloadDescriptions();
	}

	@Deprecated
	public void downloadDescriptions() {

		for (OEntity entity : mConsumer.getEntities(RichText.TABLE_NAME).execute()) {
			insertDescription(entity);
		}
	}

	@Deprecated
	public void downloadPoints() {

		for (OEntity entity : getPointsEntities()) {
			insertPoint(entity);
		}
	}

	@Deprecated
	public void downloadPoints(final int topicId) {

		for (OEntity entity : getPointsEntities(topicId)) {
			insertPoint(entity);
		}
	}

	@Deprecated
	public void downloadTopics() {

		for (OEntity entity : getTopicsEntities()) {
			insertTopic(entity);
			insertPersonsTopics(entity);
		}
	}

	@Deprecated
	public void downloadValuesWithoutNavigationIds(final String tableName, final Uri contentUri) {

		for (OEntity entity : mConsumer.getEntities(tableName).execute()) {
			ContentValues cv = OEntityToContentValue(entity);
			mContentResolver.insert(contentUri, cv);
		}
	}

	public void refreshDiscussions() {

		log("[refreshDiscussions]");
		Enumerable<OEntity> discussions = mConsumer.getEntities(Discussions.TABLE_NAME).execute();
		log("[refreshDiscussions] discussions entities count: " + discussions.count());
		List<Integer> serversIds = new ArrayList<Integer>(discussions.count());
		for (OEntity discussion : discussions) {
			serversIds.add(getAsInt(discussion, Discussions.Columns.ID));
			ContentValues cv = OEntityToContentValue(discussion);
			mContentResolver.insert(Discussions.CONTENT_URI, cv);
		}
		log("[refreshDiscussions] all discussions was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Discussions.CONTENT_URI, new String[] { Discussions.Columns.ID },
				null, null, null);
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Discussions.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int discussionId = cur.getInt(idIndex);
				if (!serversIds.contains(discussionId)) {
					// delete this row
					log("[refreshDiscussions] delete discussion: " + discussionId);
					Uri uri = Discussions.buildTableUri(discussionId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshPersons() {

		log("[refreshPersons] ");
		Enumerable<OEntity> persons = mConsumer.getEntities(Persons.TABLE_NAME).execute();
		log("[refreshPersons] topics entities count: " + persons.count());
		List<Integer> serversIds = new ArrayList<Integer>(persons.count());
		for (OEntity person : persons) {
			serversIds.add(getAsInt(person, Points.Columns.ID));
			ContentValues cv = OEntityToContentValue(person);
			mContentResolver.insert(Persons.CONTENT_URI, cv);
		}
		log("[refreshPersons] all persons was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Persons.CONTENT_URI, new String[] { Persons.Columns.ID }, null,
				null, null);
		log("[refreshPersons] db persons count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Points.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int personId = cur.getInt(idIndex);
				if (!serversIds.contains(personId)) {
					// delete this row
					log("[refreshPersons] delete person: " + personId);
					Uri uri = Persons.buildTableUri(personId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshPoint(final int pointId) {

		OEntity entity = mConsumer.getEntity(Points.TABLE_NAME, pointId).expand(
				Topics.TABLE_NAME + "," + Persons.TABLE_NAME).execute();
		insertPoint(entity);
	}

	public void refreshPoints() {

		Enumerable<OEntity> points = getPointsEntities();
		List<Integer> serversIds = new ArrayList<Integer>(points.count());
		for (OEntity point : points) {
			serversIds.add(getAsInt(point, Points.Columns.ID));
			insertPoint(point);
		}
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Points.CONTENT_URI, new String[] { Points.Columns.ID,
				BaseColumns._ID }, null, null, null);
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Points.Columns.ID);
			int localIdIndex = cur.getColumnIndexOrThrow(BaseColumns._ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int pointId = cur.getInt(idIndex);
				if (!serversIds.contains(pointId)) {
					// delete this row
					int rowId = cur.getInt(localIdIndex);
					Uri uri = Points.buildTableUri(rowId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshPoints(final int topicId) {

		log("[refreshPoints] topic id: " + topicId);
		Enumerable<OEntity> points = getPointsEntities(topicId);
		log("[refreshPoints] points entities count: " + points.count());
		List<Integer> serversIds = new ArrayList<Integer>(points.count());
		for (OEntity point : points) {
			serversIds.add(getAsInt(point, Points.Columns.ID));
			insertPoint(point);
		}
		log("[refreshPoints] all points was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Points.CONTENT_URI, new String[] { Points.Columns.ID,
				BaseColumns._ID }, Points.Columns.TOPIC_ID + "=?", new String[] { String.valueOf(topicId) },
				null);
		log("[refreshPoints] db points count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Points.Columns.ID);
			int localIdIndex = cur.getColumnIndexOrThrow(BaseColumns._ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int pointId = cur.getInt(idIndex);
				if (!serversIds.contains(pointId)) {
					// delete this row
					int rowId = cur.getInt(localIdIndex);
					log("[refreshPoints] delete point: " + rowId);
					Uri uri = Points.buildTableUri(rowId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshTopics() {

		log("[refreshTopics] ");
		Enumerable<OEntity> topics = getTopicsEntities();
		log("[refreshTopics] topics entities count: " + topics.count());
		List<Integer> serversIds = new ArrayList<Integer>(topics.count());
		for (OEntity topic : topics) {
			serversIds.add(getAsInt(topic, Topics.Columns.ID));
			insertTopic(topic);
			insertPersonsTopics(topic);
		}
		log("[refreshTopics] all topics was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Topics.CONTENT_URI, new String[] { Topics.Columns.ID }, null,
				null, null);
		log("[refreshTopics] db topics count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Topics.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int topicId = cur.getInt(idIndex);
				if (!serversIds.contains(topicId)) {
					// delete this row
					log("[refreshTopics] delete topic: " + topicId);
					Uri uri = Topics.buildTableUri(topicId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	@Deprecated
	private List<Integer> getNavigationPropertyIds(final OEntity entity, final String navPropertyName,
			final String originalPropertyName) {

		List<Integer> ids = new ArrayList<Integer>();
		for (OEntity person : getRelatedEntities(entity, navPropertyName)) {
			ids.add(getAsInt(person, originalPropertyName));
		}
		return ids;
	}

	private Enumerable<OEntity> getPointsEntities() {

		return mConsumer.getEntities(Points.TABLE_NAME).expand(Topics.TABLE_NAME + "," + Persons.TABLE_NAME)
				.execute();
	}

	private Enumerable<OEntity> getPointsEntities(final int topicId) {

		return mConsumer.getEntities(Points.TABLE_NAME).expand(Topics.TABLE_NAME + "," + Persons.TABLE_NAME)
				.filter("Topic/Id eq " + String.valueOf(topicId)).execute();
	}

	@Deprecated
	private Enumerable<OEntity> getRelatedEntities(final OEntity originEntity, final String linkColumn) {

		return mConsumer.getEntities(originEntity.getLink(linkColumn, ORelatedEntitiesLink.class)).execute();
	}

	@Deprecated
	private OEntity getRelatedEntity(final OEntity originEntity, final String linkColumn) {

		return mConsumer.getEntity(originEntity.getLink(linkColumn, ORelatedEntityLink.class)).execute();
	}

	@Deprecated
	private OEntity getTopicEntity(final int topicId) {

		return mConsumer.getEntity(Topics.Columns.ID, topicId).expand(Topics.Columns.POINT_ID).execute();
	}

	private Enumerable<OEntity> getTopicsEntities() {

		return mConsumer.getEntities(Topics.TABLE_NAME).expand(
				Discussions.TABLE_NAME + "," + Persons.TABLE_NAME).execute();
	}

	@Deprecated
	private Uri insertDescription(final OEntity entity) {

		// get properties
		ContentValues cv = OEntityToContentValue(entity);
		// get related discussion id
		OEntity discussion = entity.getLink(RichText.Columns.DISCUSSION_ID, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (discussion != null) {
			cv.put(RichText.Columns.DISCUSSION_ID, getAsInt(discussion, Discussions.Columns.ID));
		}
		// get related point id
		OEntity point = entity.getLink(RichText.Columns.POINT_ID, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (point != null) {
			cv.put(RichText.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
		}
		if ((discussion == null) && (point == null)) {
			// TODO: throw ex
			Log.e(TAG, "Both description foreign keys was null");
			if (ApplicationConstants.ODATA_SANITIZE) {
				mConsumer.deleteEntity(RichText.TABLE_NAME, getAsInt(entity, RichText.Columns.ID)).execute();;
			}
			return null;
		}
		return mContentResolver.insert(RichText.CONTENT_URI, cv);
	}

	private void insertPersonsTopics(final OEntity topic) {

		// get topic id
		int topicId = getAsInt(topic, Topics.Columns.ID);
		// get related persons
		List<OEntity> persons = topic.getLink(Topics.Columns.PERSON_ID, ORelatedEntitiesLinkInline.class)
				.getRelatedEntities();
		// insert many-to-many relationship
		for (OEntity person : persons) {
			ContentValues cv = new ContentValues();
			cv.put(PersonsTopics.Columns.TOPIC_ID, topicId);
			cv.put(PersonsTopics.Columns.PERSON_ID, getAsInt(person, Persons.Columns.ID));
			// TODO: provide special PersonsTopics uri for insert
			mContentResolver.insert(Persons.buildTopicUri(1231231), cv);
		}
	}

	private Uri insertPoint(final OEntity point) {

		// get properties
		ContentValues cv = OEntityToContentValue(point);
		// get related topic id
		OEntity topic = point.getLink(Topics.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		if (topic == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related topic link was null for point: " + getAsInt(point, Points.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete point: " + getAsInt(point, Points.Columns.ID));
				mConsumer.deleteEntity(Points.TABLE_NAME, getAsInt(point, Points.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Points.Columns.TOPIC_ID, getAsInt(topic, Topics.Columns.ID));
		// get related person id
		OEntity person = point.getLink(Persons.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		if (person == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related person link was null for point: " + getAsInt(point, Points.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete point: " + getAsInt(point, Points.Columns.ID));
				mConsumer.deleteEntity(Points.TABLE_NAME, getAsInt(point, Points.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Points.Columns.PERSON_ID, getAsInt(person, Persons.Columns.ID));
		// TODO no sync cloumn needed
		cv.put(Points.Columns.SYNC, false);
		return mContentResolver.insert(Points.CONTENT_URI, cv);
	}

	private Uri insertTopic(final OEntity entity) {

		// get properties
		ContentValues cv = OEntityToContentValue(entity);
		// get related discussion id
		OEntity discussion = entity.getLink(Topics.Columns.DISCUSSION_ID, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (discussion == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related discussion link is null for topic: " + getAsInt(entity, Topics.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				mConsumer.deleteEntity(Topics.TABLE_NAME, getAsInt(entity, Topics.Columns.ID)).execute();;
			}
			return null;
		}
		cv.put(Topics.Columns.DISCUSSION_ID, getAsInt(discussion, Discussions.Columns.ID));
		return mContentResolver.insert(Topics.CONTENT_URI, cv);
	}

	private void log(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}
}
