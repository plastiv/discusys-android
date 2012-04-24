package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
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

	public static void insertPersonTopicLink(final int personId, final int topicId) {

		HttpUtil.insertPersonTopic(personId, topicId);
	}

	public void deleteComment(final int commentId) {

		mConsumer.deleteEntity(Comments.TABLE_NAME, commentId).execute();
	}

	public void deletePoint(final int pointId) {

		deleteCommentsByPointId(pointId);
		deleteDescriptionByPointId(pointId);
		mConsumer.deleteEntity(Points.TABLE_NAME, pointId).execute();
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

	public OEntity insertPoint(final int agreementCode, final Byte[] drawing, final boolean expanded,
			final Integer groupId, final String numberedPoint, final int personId, final String pointName,
			final boolean sharedToPublic, final int sideCode, final int topicId) {

		// @formatter:off
		OCreateRequest<OEntity> request = mConsumer.createEntity(Points.TABLE_NAME)
				.properties(OProperties.int32(Points.Columns.AGREEMENT_CODE, Integer.valueOf(agreementCode)))				
				.properties(OProperties.boolean_(Points.Columns.EXPANDED, Boolean.valueOf(expanded)))	
				.link(Points.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(personId)))
				.properties(OProperties.string(Points.Columns.NAME, pointName))
				.properties(OProperties.boolean_(Points.Columns.SHARED_TO_PUBLIC, Boolean.valueOf(sharedToPublic)))
				.properties(OProperties.int32(Points.Columns.SIDE_CODE, Integer.valueOf(sideCode)))		
				.link(Points.Columns.TOPIC_ID, OEntityKey.parse(String.valueOf(topicId)));
		
		if(drawing != null){
			//request.properties(OProperties.binary(Points.Columns.DRAWING, drawing));
		}
		if(groupId != null){
			
			request.link(Points.Columns.GROUP_ID_SERVER, OEntityKey.parse(String.valueOf(groupId)));
		}
		if(numberedPoint != null){
			request.properties(OProperties.string(Points.Columns.NUMBERED_POINT, numberedPoint));
		}
		return request.execute();
		// @formatter:on
	}

	public OEntity insertPoint(final Point point) {

		// @formatter:off
		return mConsumer.createEntity(Points.TABLE_NAME)
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

	public OEntity insertTopic(final String topicName, final int discussionId, final int personId) {

		// @formatter:off
		return mConsumer.createEntity(Topics.TABLE_NAME)
				.link(Topics.Columns.DISCUSSION_ID, OEntityKey.parse(String.valueOf(discussionId)))
				.properties(OProperties.string(Topics.Columns.NAME, topicName))	
				.link(Topics.Columns.PERSON_ID, OEntityKey.parse(String.valueOf(personId)))
				.execute();
		// @formatter:on
	}

	public boolean updateDescription(final Description description) {

		// @formatter:off
		OModifyRequest<OEntity> request=  mConsumer.mergeEntity(Descriptions.TABLE_NAME, description.getId())				
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
			mConsumer.deleteEntity(Descriptions.TABLE_NAME, descriptionId).execute();
		}
		descriptionCur.close();
	}
}
