package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.model.Attachment;
import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.model.Source;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sources;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.odata4j.core.OCreateRequest;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OModifyRequest;
import org.odata4j.core.OProperties;

public class OdataWriteClient extends BaseOdataClient {

	public OdataWriteClient(final Context context) {

		super(context);
	}

	public static void insertPersonTopicLink(final Context context, final int personId, final int topicId) {

		HttpUtil.insertPersonTopic(context, personId, topicId);
	}

	public void deleteAttachment(final int attachmentId) {

		mConsumer.deleteEntity(Attachments.TABLE_NAME, attachmentId).execute();
	}

	public void deleteComment(final int commentId) {

		mConsumer.deleteEntity(Comments.TABLE_NAME, commentId).execute();
	}

	public void deletePoint(final int pointId) {

		deleteCommentsByPointId(pointId);
		deleteDescriptionByPointId(pointId);
		deleteAttachmentsByPointId(pointId);
		mConsumer.deleteEntity(Points.TABLE_NAME, pointId).execute();
	}

	public OEntity insertAttachment(final Attachment attachment) {

		// @formatter:off
		return mConsumer.createEntity(Attachments.TABLE_NAME)
				.link(Attachments.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(attachment.getPersonId())))
				.properties(OProperties.string(Attachments.Columns.NAME, attachment.getName()))
				.link(Attachments.Columns.POINT_ID, OEntityKey.parse(String.valueOf(attachment.getPointId())))
				.properties(OProperties.int32(Attachments.Columns.FORMAT, attachment.getFormat()))
				.properties(OProperties.string(Attachments.Columns.TITLE, attachment.getTitle()))
				.properties(OProperties.string(Attachments.Columns.LINK, attachment.getLink()))
				.execute();
		// @formatter:on
	}

	public OEntity insertComment(final String text, final int personId, final int pointId) {

		// @formatter:off
		return mConsumer.createEntity(Comments.TABLE_NAME)
				.link(Comments.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(personId)))
				.properties(OProperties.string(Comments.Columns.TEXT, text))
				.link("ArgPoint", OEntityKey.parse(String.valueOf(pointId))).execute();
		// @formatter:on
	}

	public OEntity insertDescription(final Description description) {

		// @formatter:off
		OCreateRequest<OEntity> request = mConsumer.createEntity(Descriptions.TABLE_NAME)
				.properties(OProperties.string(Descriptions.Columns.TEXT, description.getText()));
		
		// @formatter:on
		if (description.getPointId() != null) {
			request.link(Descriptions.Columns.POINT_ID, OEntityKey.parse(String.valueOf(description
					.getPointId())));
		}
		if (description.getDiscussionId() != null) {
			request.link(Descriptions.Columns.DISCUSSION_ID, OEntityKey.parse(String.valueOf(description
					.getDiscussionId())));
		}
		return request.execute();
	}

	public OEntity insertDiscussion(final String subject) {

		// @formatter:off
		return mConsumer.createEntity(Discussions.TABLE_NAME)
				.properties(OProperties.string(Discussions.Columns.SUBJECT, subject))
				.execute();
		// @formatter:on
	}

	public OEntity insertPerson(final ContentValues person) {

		String name = person.getAsString(Persons.Columns.NAME);
		String email = person.getAsString(Persons.Columns.EMAIL);
		boolean online = person.getAsBoolean(Persons.Columns.ONLINE);
		int color = person.getAsInteger(Persons.Columns.COLOR);
		int seatId = person.getAsInteger(Persons.Columns.SEAT_ID);
		int sessionId = person.getAsInteger(Persons.Columns.SESSION_ID);
		int onlineDeviceType = person.getAsInteger(Persons.Columns.ONLINE_DEVICE_TYPE);
		// @formatter:off
		return mConsumer.createEntity(Persons.TABLE_NAME)
				.properties(OProperties.string(Persons.Columns.NAME, name))
				.properties(OProperties.string(Persons.Columns.EMAIL, email))
				.properties(OProperties.int32(Persons.Columns.COLOR, color))
				.properties(OProperties.boolean_(Persons.Columns.ONLINE, online))
				.properties(OProperties.int32(Persons.Columns.SEAT_ID, seatId))
				.properties(OProperties.int32(Persons.Columns.SESSION_ID, sessionId))
				.properties(OProperties.int32(Persons.Columns.ONLINE_DEVICE_TYPE, onlineDeviceType))
				.execute();
		// @formatter:on
	}

	public OEntity insertPerson(final String name, final String email, final Integer color,
			final boolean online) {

		// @formatter:off
		return mConsumer.createEntity(Persons.TABLE_NAME)
				.properties(OProperties.string(Persons.Columns.NAME, name))
				.properties(OProperties.string(Persons.Columns.EMAIL, email))
				.properties(OProperties.int32(Persons.Columns.COLOR, color))
				.properties(OProperties.boolean_(Persons.Columns.ONLINE, online))
				.execute();
		// @formatter:on
	}

	public void insertPersonTopicLinks(final int personId, final int[] topicIds) {

		OEntityId personEntityId = OEntityIds.create(Persons.TABLE_NAME, personId);
		for (int topicId : topicIds) {
			OEntityId topicEntityId = OEntityIds.create(Topics.TABLE_NAME, topicId);
			mConsumer.createLink(topicEntityId, Topics.Columns.PERSON_ID, personEntityId).execute();
		}
	}

	public OEntity insertPoint(final Point point) {

		// @formatter:off
		OCreateRequest<OEntity> request = mConsumer.createEntity(Points.TABLE_NAME)
				.properties(OProperties.int32(Points.Columns.AGREEMENT_CODE, point.getAgreementCode()))
				.properties(OProperties.boolean_(Points.Columns.EXPANDED, point.isExpanded()))
				.properties(OProperties.string(Points.Columns.NUMBERED_POINT, point.getNumberedPoint()))
				.link(Points.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(point.getPersonId())))
				.properties(OProperties.string(Points.Columns.NAME, point.getName()))
				.properties(OProperties.boolean_(Points.Columns.SHARED_TO_PUBLIC, Boolean.valueOf(point.isSharedToPublic())))
				.properties(OProperties.int32(Points.Columns.SIDE_CODE, Integer.valueOf(point.getSideCode())))	
				.properties(OProperties.string(Points.Columns.RECENTLY_ENTERED_MEDIA_URL, point.getRecentlyEnteredMediaUrl()))
				.properties(OProperties.string(Points.Columns.RECENTLY_ENTERED_SOURCE, point.getRecentlyEnteredSource()))
				.link(Points.Columns.TOPIC_ID, OEntityKey.parse(String.valueOf(point.getTopicId())));
		// @formatter:on
		if (point.getDrawing() != null) {
			request.properties(OProperties.binary(Points.Columns.DRAWING, point.getDrawing()));
		}
		if (point.getGroupId() != null) {
			request.link(Points.Columns.GROUP_ID_SERVER, OEntityKey.parse(String.valueOf(point.getGroupId())));
		}
		if (point.getNumberedPoint() != null) {
			request.properties(OProperties.string(Points.Columns.NUMBERED_POINT, point.getNumberedPoint()));
		}
		return request.execute();
	}

	public OEntity insertSource(final Source source) {

		// @formatter:off
		return mConsumer.createEntity(Sources.TABLE_NAME)
				.properties(OProperties.string(Sources.Columns.LINK, source.getLink()))
				.link(Sources.Columns.DESCRIPTION_ID, OEntityKey.parse(String.valueOf(source.getDescriptionId())))
				.execute();
		// @formatter:on
	}

	public OEntity insertTopic(final String topicName, final int discussionId, final int personId) {

		// @formatter:off
		return mConsumer.createEntity(Topics.TABLE_NAME)
				.link(Topics.Columns.DISCUSSION_ID, OEntityKey.parse(String.valueOf(discussionId)))
				.properties(OProperties.string(Topics.Columns.NAME, topicName))	
				.link(Topics.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(personId)))
				.execute();
		// @formatter:on
	}

	public boolean updateAttachment(final Attachment attachment, final int attachmentId) {

		// @formatter:off
		return mConsumer.mergeEntity(Attachments.TABLE_NAME, attachmentId)
				.link(Attachments.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(attachment.getPersonId())))
				.properties(OProperties.string(Attachments.Columns.NAME, attachment.getName()))
				.link(Attachments.Columns.POINT_ID, OEntityKey.parse(String.valueOf(attachment.getPointId())))
				.properties(OProperties.int32(Attachments.Columns.FORMAT, attachment.getFormat()))
				.properties(OProperties.string(Attachments.Columns.TITLE, attachment.getTitle()))
				.properties(OProperties.string(Attachments.Columns.LINK, attachment.getLink()))
				.execute();
		// @formatter:on
	}

	public boolean updateDescription(final Description description) {

		// @formatter:off
		OModifyRequest<OEntity> request =  mConsumer.mergeEntity(Descriptions.TABLE_NAME, description.getId())				
				.properties(OProperties.string(Descriptions.Columns.TEXT, description.getText()));
		// @formatter:on
		if (description.getPointId() != null) {
			request.link(Descriptions.Columns.POINT_ID, OEntityKey.parse(String.valueOf(description
					.getPointId())));
		}
		if (description.getDiscussionId() != null) {
			request.link(Descriptions.Columns.DISCUSSION_ID, OEntityKey.parse(String.valueOf(description
					.getDiscussionId())));
		}
		return request.execute();
	}

	public boolean updatePerson(final int personId, final String personName) {

		return mConsumer.mergeEntity(Persons.TABLE_NAME, personId).properties(
				OProperties.string(Persons.Columns.NAME, personName)).execute();
	}

	public boolean updatePoint(final Point point) {

		// @formatter:off
		return mConsumer.mergeEntity(Points.TABLE_NAME, point.getId())
				.properties(OProperties.int32(Points.Columns.AGREEMENT_CODE, point.getAgreementCode()))
				.properties(OProperties.binary(Points.Columns.DRAWING, point.getDrawing()))
				.properties(OProperties.boolean_(Points.Columns.EXPANDED, point.isExpanded()))
				//.link(Points.Columns.GROUP_ID_SERVER, OEntityKey.parse(String.valueOf(point.getGroupId())))
				.properties(OProperties.string(Points.Columns.NUMBERED_POINT, point.getNumberedPoint()))
				.link(Points.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(point.getPersonId())))
				.properties(OProperties.string(Points.Columns.NAME, point.getName()))
				.properties(OProperties.boolean_(Points.Columns.SHARED_TO_PUBLIC, Boolean.valueOf(point.isSharedToPublic())))
				.properties(OProperties.int32(Points.Columns.SIDE_CODE, Integer.valueOf(point.getSideCode())))		
				.link(Points.Columns.TOPIC_ID, OEntityKey.parse(String.valueOf(point.getTopicId())))
				.execute();
		// @formatter:on
	}

	private void deleteAttachmentsByPointId(final int pointId) {

		String[] projection = new String[] { Attachments.Columns.ID };
		String where = Attachments.Columns.POINT_ID + "=" + pointId;
		Cursor cursor = mContentResolver.query(Attachments.CONTENT_URI, projection, where, null, null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int attachmentId = cursor.getInt(cursor.getColumnIndexOrThrow(Attachments.Columns.ID));
			deleteAttachment(attachmentId);
		}
		cursor.close();
	}

	private void deleteCommentsByPointId(final int pointId) {

		String[] projection = new String[] { Comments.Columns.ID };
		String where = Comments.Columns.POINT_ID + "=" + pointId;
		Cursor commentsCursor = mContentResolver.query(Comments.CONTENT_URI, projection, where, null, null);
		for (commentsCursor.moveToFirst(); !commentsCursor.isAfterLast(); commentsCursor.moveToNext()) {
			int commentId = commentsCursor.getInt(commentsCursor.getColumnIndexOrThrow(Comments.Columns.ID));
			mConsumer.deleteEntity(Comments.TABLE_NAME, commentId).execute();
		}
		commentsCursor.close();
	}

	private void deleteDescriptionByPointId(final int pointId) {

		String[] projection = new String[] { Descriptions.Columns.ID };
		String where = Descriptions.Columns.POINT_ID + "=" + pointId;
		Cursor descriptionCur = mContentResolver.query(Descriptions.CONTENT_URI, projection, where, null,
				null);
		for (descriptionCur.moveToFirst(); !descriptionCur.isAfterLast(); descriptionCur.moveToNext()) {
			int descriptionId = descriptionCur.getInt(descriptionCur
					.getColumnIndexOrThrow(Descriptions.Columns.ID));
			deleteSourceByDescriptionId(descriptionId);
			mConsumer.deleteEntity(Descriptions.TABLE_NAME, descriptionId).execute();
		}
		descriptionCur.close();
	}

	private void deleteSourceByDescriptionId(final int descriptionId) {

		String[] projection = new String[] { Sources.Columns.ID };
		String where = Sources.Columns.DESCRIPTION_ID + "=" + descriptionId;
		Cursor cursor = mContentResolver.query(Sources.CONTENT_URI, projection, where, null, null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int sourceId = cursor.getInt(cursor.getColumnIndexOrThrow(Sources.Columns.ID));
			mConsumer.deleteEntity(Sources.TABLE_NAME, sourceId).execute();
		}
		cursor.close();
	}
}
