package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.data.DataIoException;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.PersonsTopics;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Seats;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sessions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sources;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.utils.MyLog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.core4j.Enumerable;
import org.joda.time.LocalDateTime;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

import java.util.ArrayList;
import java.util.List;

public class OdataReadClient extends BaseOdataClient {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final boolean LOGV = false && ApplicationConstants.DEV_MODE;
	private static final String TAG = OdataReadClient.class.getSimpleName();

	public OdataReadClient(final Context context) {

		super(context);
	}

	private static int getAsInt(final OEntity entity, final String valueColumn) {

		return (Integer) entity.getProperty(valueColumn).getValue();
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
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
		} else if (classType.equals(LocalDateTime.class)) {
			cv.put(property.getName(), property.getValue().toString());
		} else {
			throw new IllegalArgumentException("Unknown property name: " + property.getName() + " type: "
					+ property.getType() + " value: " + property.getValue() + "javaType: "
					+ classType.getCanonicalName());
		}
		return cv;
	}

	public void refreshAttachments() {

		logd("[refreshAttachments]");
		Enumerable<OEntity> attachments = getAttachmentsEntities();
		logd("[refreshAttachments] entities count: " + attachments.count());
		List<Integer> serversIds = new ArrayList<Integer>(attachments.count());
		for (OEntity attachment : attachments) {
			serversIds.add(getAsInt(attachment, Attachments.Columns.ID));
			// ContentValues cv = OEntityToContentValue(attachment);
			// mContentResolver.insert(Attachments.CONTENT_URI, cv);
			insertAttachment(attachment);
		}
		logd("[refreshAttachments] all attachments was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Attachments.CONTENT_URI, new String[] { Attachments.Columns.ID },
				null, null, null);
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Attachments.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int attachmentId = cur.getInt(idIndex);
				if (!serversIds.contains(attachmentId)) {
					// delete this row
					logd("[refreshAttachments] delete attachment: " + attachmentId);
					Uri uri = Attachments.buildTableUri(attachmentId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshComments() {

		logd("[refreshComments]");
		Enumerable<OEntity> comments = getCommentsEntities();
		logd("[refreshComments] comment entities count: " + comments.count());
		List<Integer> serversIds = new ArrayList<Integer>(comments.count());
		for (OEntity comment : comments) {
			serversIds.add(getAsInt(comment, Comments.Columns.ID));
			insertComment(comment);
		}
		logd("[refreshComments] all comments was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Comments.CONTENT_URI, new String[] { Comments.Columns.ID, },
				null, null, null);
		logd("[refreshComments] db comments count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Comments.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int commentId = cur.getInt(idIndex);
				if (!serversIds.contains(commentId)) {
					// delete this row
					logd("[refreshComments] delete point: " + commentId);
					Uri uri = Comments.buildTableUri(commentId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshDescription(final int pointId) {

		String filter = Points.TABLE_NAME + "/" + Points.Columns.ID + " eq " + String.valueOf(pointId);
		Enumerable<OEntity> description = mConsumer.getEntities(Descriptions.TABLE_NAME).filter(filter)
				.expand(Points.TABLE_NAME + "," + Discussions.TABLE_NAME).execute();
		if (description.count() == 1) {
			insertDescription(description.first());
		} else {
			throw new IllegalStateException("Should be one description, was: " + description.count());
		}
	}

	public void refreshDescriptions() {

		logd("[refreshDescriptions]");
		Enumerable<OEntity> descriptions = getDescriptionsEntities();
		logd("[refreshDescriptions] descriptions entities count: " + descriptions.count());
		List<Integer> serversIds = new ArrayList<Integer>(descriptions.count());
		for (OEntity description : descriptions) {
			serversIds.add(getAsInt(description, Descriptions.Columns.ID));
			insertDescription(description);
		}
		logd("[refreshDescriptions] all descriptions was inserted");
		// check if server has a deleted descriptions
		Cursor cur = mContentResolver.query(Descriptions.CONTENT_URI,
				new String[] { Descriptions.Columns.ID }, null, null, null);
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Descriptions.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int descriptionId = cur.getInt(idIndex);
				if (!serversIds.contains(descriptionId)) {
					// delete this row
					logd("[refreshDescriptions] delete discussion: " + descriptionId);
					Uri uri = Descriptions.buildTableUri(descriptionId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshDiscussions() {

		logd("[refreshDiscussions]");
		Enumerable<OEntity> discussions = mConsumer.getEntities(Discussions.TABLE_NAME).execute();
		logd("[refreshDiscussions] discussions entities count: " + discussions.count());
		List<Integer> serversIds = new ArrayList<Integer>(discussions.count());
		for (OEntity discussion : discussions) {
			serversIds.add(getAsInt(discussion, Discussions.Columns.ID));
			ContentValues cv = OEntityToContentValue(discussion);
			mContentResolver.insert(Discussions.CONTENT_URI, cv);
		}
		logd("[refreshDiscussions] all discussions was inserted");
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
					logd("[refreshDiscussions] delete discussion: " + discussionId);
					Uri uri = Discussions.buildTableUri(discussionId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshPersons() {

		logd("[refreshPersons] ");
		Enumerable<OEntity> persons = mConsumer.getEntities(Persons.TABLE_NAME).execute();
		logd("[refreshPersons] topics entities count: " + persons.count());
		List<Integer> serversIds = new ArrayList<Integer>(persons.count());
		for (OEntity person : persons) {
			serversIds.add(getAsInt(person, Points.Columns.ID));
			ContentValues cv = OEntityToContentValue(person);
			mContentResolver.insert(Persons.CONTENT_URI, cv);
		}
		logd("[refreshPersons] all persons was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Persons.CONTENT_URI, new String[] { Persons.Columns.ID }, null,
				null, null);
		logd("[refreshPersons] db persons count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Points.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int personId = cur.getInt(idIndex);
				if (!serversIds.contains(personId)) {
					// delete this row
					logd("[refreshPersons] delete person: " + personId);
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

		logd("[refreshPoints] topic id: " + topicId);
		Enumerable<OEntity> points = getPointsEntities(topicId);
		logd("[refreshPoints] points entities count: " + points.count());
		List<Integer> serversIds = new ArrayList<Integer>(points.count());
		for (OEntity point : points) {
			serversIds.add(getAsInt(point, Points.Columns.ID));
			insertPoint(point);
		}
		logd("[refreshPoints] all points was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Points.CONTENT_URI, new String[] { Points.Columns.ID,
				BaseColumns._ID }, Points.Columns.TOPIC_ID + "=?", new String[] { String.valueOf(topicId) },
				null);
		logd("[refreshPoints] db points count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Points.Columns.ID);
			int localIdIndex = cur.getColumnIndexOrThrow(BaseColumns._ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int pointId = cur.getInt(idIndex);
				if (!serversIds.contains(pointId)) {
					// delete this row
					int rowId = cur.getInt(localIdIndex);
					logd("[refreshPoints] delete point: " + rowId);
					Uri uri = Points.buildTableUri(rowId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshSeats() {

		logd("[refreshSeats]");
		Enumerable<OEntity> seats = mConsumer.getEntities(Seats.TABLE_NAME).execute();
		logd("[refreshSeats] entities count: " + seats.count());
		List<Integer> serversIds = new ArrayList<Integer>(seats.count());
		for (OEntity seat : seats) {
			serversIds.add(getAsInt(seat, Seats.Columns.ID));
			ContentValues cv = OEntityToContentValue(seat);
			mContentResolver.insert(Seats.CONTENT_URI, cv);
		}
		logd("[refreshSeats] all seats was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Seats.CONTENT_URI, new String[] { Seats.Columns.ID }, null, null,
				null);
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Seats.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int seatId = cur.getInt(idIndex);
				if (!serversIds.contains(seatId)) {
					// delete this row
					logd("[refreshSeats] delete seat: " + seatId);
					Uri uri = Seats.buildTableUri(seatId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshSessions() {

		logd("[refreshSessions]");
		Enumerable<OEntity> sessions = mConsumer.getEntities(Sessions.TABLE_NAME).execute();
		logd("[refreshSessions] entities count: " + sessions.count());
		List<Integer> serversIds = new ArrayList<Integer>(sessions.count());
		for (OEntity session : sessions) {
			serversIds.add(getAsInt(session, Sessions.Columns.ID));
			ContentValues cv = OEntityToContentValue(session);
			mContentResolver.insert(Sessions.CONTENT_URI, cv);
		}
		logd("[refreshSessions] all sessions was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Sessions.CONTENT_URI, new String[] { Sessions.Columns.ID }, null,
				null, null);
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Sessions.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int sessionId = cur.getInt(idIndex);
				if (!serversIds.contains(sessionId)) {
					// delete this row
					logd("[refreshSessions] delete session: " + sessionId);
					Uri uri = Sessions.buildTableUri(sessionId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshSources() {

		logd("[refreshSources]");
		Enumerable<OEntity> sources = getSourcesEntities();
		logd("[refreshSources] entities count: " + sources.count());
		List<Integer> serversIds = new ArrayList<Integer>(sources.count());
		for (OEntity source : sources) {
			serversIds.add(getAsInt(source, Attachments.Columns.ID));
			insertSource(source);
		}
		logd("[refreshSources] all sources was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Sources.CONTENT_URI, new String[] { Sources.Columns.ID }, null,
				null, null);
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Sources.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int sourceId = cur.getInt(idIndex);
				if (!serversIds.contains(sourceId)) {
					// delete this row
					logd("[refreshSources] delete source: " + sourceId);
					Uri uri = Sources.buildTableUri(sourceId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshTopics() {

		logd("[refreshTopics] ");
		Enumerable<OEntity> topics = getTopicsEntities();
		// logd("[refreshTopics] topics entities count: " + topics.count());
		List<Integer> serversIds = new ArrayList<Integer>(/* topics.count() */);
		for (OEntity topic : topics) {
			serversIds.add(getAsInt(topic, Topics.Columns.ID));
			insertTopic(topic);
			insertPersonsTopics(topic);
		}
		logd("[refreshTopics] all topics was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Topics.CONTENT_URI, new String[] { Topics.Columns.ID }, null,
				null, null);
		logd("[refreshTopics] db topics count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Topics.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int topicId = cur.getInt(idIndex);
				if (!serversIds.contains(topicId)) {
					// delete this row
					logd("[refreshTopics] delete topic: " + topicId);
					Uri uri = Topics.buildTableUri(topicId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void updateComments(final int pointId) {

		logd("[refreshComments]");
		Enumerable<OEntity> comments = getCommentsEntities(pointId);
		logd("[refreshComments] comment entities count: " + comments.count());
		List<Integer> serversIds = new ArrayList<Integer>(comments.count());
		for (OEntity comment : comments) {
			serversIds.add(getAsInt(comment, Comments.Columns.ID));
			insertComment(comment);
		}
		logd("[refreshComments] all comments was inserted");
		// check if server has a deleted points
		String where = Comments.Columns.POINT_ID + "=?";
		String[] args = new String[] { String.valueOf(pointId) };
		Cursor cur = mContentResolver.query(Comments.CONTENT_URI, new String[] { Comments.Columns.ID, },
				where, args, null);
		logd("[refreshComments] db comments count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Comments.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int commentId = cur.getInt(idIndex);
				if (!serversIds.contains(commentId)) {
					// delete this row
					logd("[refreshComments] delete point: " + commentId);
					Uri uri = Comments.buildTableUri(commentId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void updatePoint(final int pointId) {

		OEntity entity = mConsumer.getEntity(Points.TABLE_NAME, pointId).expand(
				Topics.TABLE_NAME + "," + Persons.TABLE_NAME + "," + "Description").execute();
		updatePoint(entity);
		updateComments(pointId);
	}

	public void updatePointsFromTopic(final int topicId) {

		logd("[updatePointsFromTopic] topic id: " + topicId);
		Enumerable<OEntity> points = mConsumer.getEntities(Points.TABLE_NAME).expand(
				Topics.TABLE_NAME + "," + Persons.TABLE_NAME + "," + "Description").filter(
				"Topic/Id eq " + String.valueOf(topicId)).execute();
		logd("[updatePointsFromTopic] points entities count: " + points.count());
		List<Integer> serversIds = new ArrayList<Integer>(points.count());
		for (OEntity point : points) {
			serversIds.add(getAsInt(point, Points.Columns.ID));
			updatePoint(point);
			updateComments(getAsInt(point, Points.Columns.ID));
		}
		logd("[updatePointsFromTopic] all points was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Points.CONTENT_URI, new String[] { Points.Columns.ID,
				BaseColumns._ID }, Points.Columns.TOPIC_ID + "=?", new String[] { String.valueOf(topicId) },
				null);
		logd("[updatePointsFromTopic] db points count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Points.Columns.ID);
			int localIdIndex = cur.getColumnIndexOrThrow(BaseColumns._ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int pointId = cur.getInt(idIndex);
				if (!serversIds.contains(pointId)) {
					// delete this row
					int rowId = cur.getInt(localIdIndex);
					logd("[updatePointsFromTopic] delete point: " + rowId);
					Uri uri = Points.buildTableUri(rowId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	private Enumerable<OEntity> getAttachmentsEntities() {

		return mConsumer.getEntities(Attachments.TABLE_NAME).expand(Points.TABLE_NAME).execute();
	}

	private Enumerable<OEntity> getCommentsEntities() {

		return mConsumer.getEntities(Comments.TABLE_NAME)
				.expand(Points.TABLE_NAME + "," + Persons.TABLE_NAME).execute();
	}

	private Enumerable<OEntity> getCommentsEntities(final int pointId) {

		return mConsumer.getEntities(Comments.TABLE_NAME)
				.expand(Points.TABLE_NAME + "," + Persons.TABLE_NAME).filter(
						"ArgPoint/Id eq " + String.valueOf(pointId)).execute();
	}

	private Enumerable<OEntity> getDescriptionsEntities() {

		return mConsumer.getEntities(Descriptions.TABLE_NAME).expand(
				Points.TABLE_NAME + "," + Discussions.TABLE_NAME).execute();
	}

	private Enumerable<OEntity> getPointsEntities() {

		return mConsumer.getEntities(Points.TABLE_NAME).expand(Topics.TABLE_NAME + "," + Persons.TABLE_NAME)
				.execute();
	}

	private Enumerable<OEntity> getPointsEntities(final int topicId) {

		return mConsumer.getEntities(Points.TABLE_NAME).expand(Topics.TABLE_NAME + "," + Persons.TABLE_NAME)
				.filter("Topic/Id eq " + String.valueOf(topicId)).execute();
	}

	private Enumerable<OEntity> getSourcesEntities() {

		return mConsumer.getEntities(Sources.TABLE_NAME).expand(Descriptions.TABLE_NAME).execute();
	}

	private Enumerable<OEntity> getTopicsEntities() {

		ODataConsumer mConsumerXml = ODataJerseyConsumer.newBuilder(ODataConstants.SERVICE_URL).build();
		return mConsumerXml.getEntities(Topics.TABLE_NAME).expand(
				Discussions.TABLE_NAME + "," + Persons.TABLE_NAME).execute();
	}

	private Uri insertAttachment(final OEntity attachment) {

		ContentValues cv = OEntityToContentValue(attachment);
		OEntity point = attachment.getLink(Points.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if ((point == null)) {
			// TODO: thwo ex here
			// Log.e(TAG, "Related topic link was null for comment: " + getAsInt(comment,
			// Comments.Columns.ID));
			// if (ApplicationConstants.ODATA_SANITIZE) {
			// Log.w(TAG, "Try to delete comment: " + getAsInt(comment, Comments.Columns.ID));
			// mConsumer.deleteEntity(Comments.TABLE_NAME, getAsInt(comment, Comments.Columns.ID)).execute();
			// }
			return null;
		}
		cv.put(Attachments.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
		return mContentResolver.insert(Attachments.CONTENT_URI, cv);
	}

	private Uri insertComment(final OEntity comment) {

		// get properties
		ContentValues cv = OEntityToContentValue(comment);
		// get related point id
		OEntity point = comment.getLink(Points.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		if (point == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related topic link was null for comment: " + getAsInt(comment, Comments.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete comment: " + getAsInt(comment, Comments.Columns.ID));
				mConsumer.deleteEntity(Comments.TABLE_NAME, getAsInt(comment, Comments.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Comments.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
		// get related person id
		OEntity person = comment.getLink(Persons.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (person == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related person link was null for comment: " + getAsInt(comment, Comments.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete comment: " + getAsInt(comment, Comments.Columns.ID));
				mConsumer.deleteEntity(Comments.TABLE_NAME, getAsInt(comment, Comments.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Comments.Columns.PERSON_ID, getAsInt(person, Persons.Columns.ID));
		try {
			return mContentResolver.insert(Comments.CONTENT_URI, cv);
		} catch (DataIoException e) {
			// TODO: send an exception here. in case of db structure change it would not be able to understood
			// that applications is not working
			Log.e(TAG, "Unable insert comment " + getAsInt(comment, Comments.Columns.ID), e);
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete comment: " + getAsInt(comment, Comments.Columns.ID));
				mConsumer.deleteEntity(Comments.TABLE_NAME, getAsInt(comment, Comments.Columns.ID)).execute();
			}
			String where = Comments.Columns.ID + "=?";
			String[] args = new String[] { String.valueOf(getAsInt(comment, Comments.Columns.ID)) };
			mContentResolver.delete(Comments.CONTENT_URI, where, args);
			return null;
		}
	}

	private Uri insertDescription(final OEntity description) {

		// get properties
		ContentValues cv = OEntityToContentValue(description);
		// get related point id
		OEntity point = description.getLink(Points.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (point != null) {
			cv.put(Descriptions.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
		}
		// or related discussion id
		OEntity discussion = description.getLink(Discussions.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (discussion != null) {
			cv.put(Descriptions.Columns.DISCUSSION_ID, getAsInt(discussion, Discussions.Columns.ID));
		}
		// delete row if it is lost
		if ((discussion == null) && (point == null)) {
			// TODO: thwo ex here
			Log.e(TAG, "Both descriptions foreign key was null "
					+ getAsInt(description, Descriptions.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete point: " + getAsInt(description, Descriptions.Columns.ID));
				mConsumer.deleteEntity(Descriptions.TABLE_NAME,
						getAsInt(description, Descriptions.Columns.ID)).execute();
			}
			return null;
		}
		return insertDescriptionToDb(cv);
	}

	private Uri insertDescriptionToDb(final ContentValues descriptionValues) {

		try {
			return mContentResolver.insert(Descriptions.CONTENT_URI, descriptionValues);
		} catch (DataIoException e) {
			// TODO: send an exception here. in case of db structure change it would not be able to understood
			// that applications is not working
			int descriptionId = descriptionValues.getAsInteger(Descriptions.Columns.ID);
			Log.e(TAG, "Unable insert description " + descriptionId, e);
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete point: " + descriptionId);
				mConsumer.deleteEntity(Descriptions.TABLE_NAME, descriptionId).execute();
			}
			String where = Descriptions.Columns.ID + "=?";
			String[] args = new String[] { String.valueOf(descriptionId) };
			mContentResolver.delete(Descriptions.CONTENT_URI, where, args);
			return null;
		}
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

	private Uri insertSource(final OEntity source) {

		ContentValues cv = OEntityToContentValue(source);
		OEntity description = source.getLink(Descriptions.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if ((description == null)) {
			// TODO: thwo ex here
			return null;
		}
		cv.put(Sources.Columns.DESCRIPTION_ID, getAsInt(description, Sources.Columns.ID));
		return mContentResolver.insert(Sources.CONTENT_URI, cv);
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
				mConsumer.deleteEntity(Topics.TABLE_NAME, getAsInt(entity, Topics.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Topics.Columns.DISCUSSION_ID, getAsInt(discussion, Discussions.Columns.ID));
		return mContentResolver.insert(Topics.CONTENT_URI, cv);
	}

	private Uri updatePoint(final OEntity point) {

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
		Uri uri = mContentResolver.insert(Points.CONTENT_URI, cv);
		// description update
		OEntity description = point.getLink("Description", ORelatedEntityLinkInline.class).getRelatedEntity();
		if (description != null) {
			// get properties
			ContentValues cvDescription = OEntityToContentValue(description);
			// // get related point id
			cvDescription.put(Descriptions.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
			// mContentResolver.insert(Descriptions.CONTENT_URI, cvDescription);
			insertDescriptionToDb(cvDescription);
		}
		return uri;
	}
}
