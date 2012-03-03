package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussion;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topic;
import com.slobodastudio.discussions.tool.MyLog;

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
		for (OProperty property : entity.getProperties()) {
			if (LOGV) {
				MyLog.v(TAG, property.getName() + ":" + property.getType() + ":" + property.getValue());
			}
			// FIXME: this is brutal hack
			if (property.getName().equals(Point.Columns.GROUP_ID_SERVER)) {
				cv.put(Point.Columns.GROUP_ID, (Integer) property.getValue());
			} else {
				put(cv, property);
			}
		}
		return cv;
	}

	private static ContentValues put(final ContentValues cv, final OProperty property) {

		EdmSimpleType type = (EdmSimpleType) property.getType();
		Class classType = type.getCanonicalJavaType();
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

		downloadValues(Discussion.TABLE_NAME, Discussion.CONTENT_URI);
	}

	public void downloadTopics() {

		final ContentResolver provider = context.getContentResolver();
		for (OEntity entity : consumer.getEntities(Topic.TABLE_NAME).execute()) {
			ContentValues cv = OEntityToContentValue(entity);
			cv.put(Topic.Columns.DISCUSSION_ID, getNavigationPropertyId(entity, Topic.Columns.DISCUSSION_ID,
					Discussion.Columns.DISCUSSION_ID));
			// cv.put(Topic.Columns.PERSON_ID, getNavigationPropertyIds(entity, Topic.Columns.PERSON_ID,
			// Person.Columns.PERSON_ID).toString());
			if (LOGV) {
				MyLog.v(TAG, "Content value: " + cv.toString());
			}
			try {
				provider.insert(Topic.CONTENT_URI, cv);
			} catch (SQLiteException e) {
				throw new RuntimeException("Cant insert value: " + cv.toString(), e);
			}
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
}
