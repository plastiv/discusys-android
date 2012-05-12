package com.slobodastudio.discussions.data.model;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

public class Point implements Value {

	private int agreementCode;
	private byte[] drawing;
	private boolean expanded;
	private Integer groupId;
	private int id;
	private String name;
	private String numberedPoint;
	private int personId;
	private String recentlyEnteredMediaUrl;
	private String recentlyEnteredSource;
	private boolean sharedToPublic;
	private int sideCode;
	private int topicId;

	public Point() {

		super();
	}

	public Point(final Bundle bundle) {

		super();
		agreementCode = bundle.getInt(Points.Columns.AGREEMENT_CODE);
		drawing = bundle.getByteArray(Points.Columns.DRAWING);
		expanded = bundle.getBoolean(Points.Columns.EXPANDED);
		if (bundle.getInt(Points.Columns.GROUP_ID) == Integer.MIN_VALUE) {
			groupId = null;
		} else {
			groupId = bundle.getInt(Points.Columns.GROUP_ID);
		}
		id = bundle.getInt(Points.Columns.ID);
		name = bundle.getString(Points.Columns.NAME);
		numberedPoint = bundle.getString(Points.Columns.NUMBERED_POINT);
		personId = bundle.getInt(Points.Columns.PERSON_ID);
		sharedToPublic = bundle.getBoolean(Points.Columns.SHARED_TO_PUBLIC);
		sideCode = bundle.getInt(Points.Columns.SIDE_CODE);
		topicId = bundle.getInt(Points.Columns.TOPIC_ID);
		recentlyEnteredMediaUrl = bundle.getString(Points.Columns.RECENTLY_ENTERED_MEDIA_URL);
		recentlyEnteredSource = bundle.getString(Points.Columns.RECENTLY_ENTERED_SOURCE);
	}

	public Point(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			int agreementCodeIndex = cursor.getColumnIndexOrThrow(Points.Columns.AGREEMENT_CODE);
			int drawingIndex = cursor.getColumnIndexOrThrow(Points.Columns.DRAWING);
			int expandedIndex = cursor.getColumnIndexOrThrow(Points.Columns.EXPANDED);
			int groupIdIndex = cursor.getColumnIndexOrThrow(Points.Columns.GROUP_ID);
			int idIndex = cursor.getColumnIndexOrThrow(Points.Columns.ID);
			int nameIndex = cursor.getColumnIndexOrThrow(Points.Columns.NAME);
			int numberedPointIndex = cursor.getColumnIndexOrThrow(Points.Columns.NUMBERED_POINT);
			int personIdIndex = cursor.getColumnIndexOrThrow(Points.Columns.PERSON_ID);
			int sharedToPublicIndex = cursor.getColumnIndexOrThrow(Points.Columns.SHARED_TO_PUBLIC);
			int sideCodeIndex = cursor.getColumnIndexOrThrow(Points.Columns.SIDE_CODE);
			int topicIdIndex = cursor.getColumnIndexOrThrow(Points.Columns.TOPIC_ID);
			agreementCode = cursor.getInt(agreementCodeIndex);
			drawing = cursor.getBlob(drawingIndex);
			if (cursor.getInt(expandedIndex) == 0) {
				expanded = false;
			} else if (cursor.getInt(expandedIndex) == 1) {
				expanded = true;
			} else {
				throw new IllegalStateException("Point has unknown expanded: " + cursor.getInt(expandedIndex));
			}
			groupId = cursor.getInt(groupIdIndex);
			id = cursor.getInt(idIndex);
			name = cursor.getString(nameIndex);
			numberedPoint = cursor.getString(numberedPointIndex);
			personId = cursor.getInt(personIdIndex);
			if (cursor.getInt(sharedToPublicIndex) == 0) {
				sharedToPublic = false;
			} else if (cursor.getInt(sharedToPublicIndex) == 1) {
				sharedToPublic = true;
			} else {
				throw new IllegalStateException("Point has unknown shared to public: "
						+ cursor.getInt(sharedToPublicIndex));
			}
			sideCode = cursor.getInt(sideCodeIndex);
			topicId = cursor.getInt(topicIdIndex);
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}

	public int getAgreementCode() {

		return agreementCode;
	}

	public byte[] getDrawing() {

		return drawing;
	}

	public Integer getGroupId() {

		return groupId;
	}

	public int getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public String getNumberedPoint() {

		return numberedPoint;
	}

	public int getPersonId() {

		return personId;
	}

	public String getRecentlyEnteredMediaUrl() {

		return recentlyEnteredMediaUrl;
	}

	public String getRecentlyEnteredSource() {

		return recentlyEnteredSource;
	}

	public int getSideCode() {

		return sideCode;
	}

	public int getTopicId() {

		return topicId;
	}

	public boolean isExpanded() {

		return expanded;
	}

	public boolean isSharedToPublic() {

		return sharedToPublic;
	}

	public void setAgreementCode(final int agreementCode) {

		this.agreementCode = agreementCode;
	}

	// public Point(final int agreementCode, final byte[] drawing, final boolean expanded, final int groupId,
	// final int id, final String name, final String numberedPoint, final int personId,
	// final boolean sharedToPublic, final int sideCode, final int topicId) {
	//
	// super();
	// this.agreementCode = agreementCode;
	// this.drawing = drawing;
	// this.expanded = expanded;
	// this.groupId = groupId;
	// this.id = id;
	// this.name = name;
	// this.numberedPoint = numberedPoint;
	// this.personId = personId;
	// this.sharedToPublic = sharedToPublic;
	// this.sideCode = sideCode;
	// this.topicId = topicId;
	// }
	public void setDrawing(final byte[] drawing) {

		this.drawing = drawing;
	}

	public void setExpanded(final boolean expanded) {

		this.expanded = expanded;
	}

	public void setGroupId(final Integer groupId) {

		this.groupId = groupId;
	}

	public void setId(final int id) {

		this.id = id;
	}

	public void setName(final String name) {

		this.name = name;
	}

	public void setNumberedPoint(final String numberedPoint) {

		this.numberedPoint = numberedPoint;
	}

	public void setPersonId(final int personId) {

		this.personId = personId;
	}

	public void setRecentlyEnteredMediaUrl(final String recentlyEnteredMediaUrl) {

		this.recentlyEnteredMediaUrl = recentlyEnteredMediaUrl;
	}

	public void setRecentlyEnteredSource(final String recentlyEnteredSource) {

		this.recentlyEnteredSource = recentlyEnteredSource;
	}

	public void setSharedToPublic(final boolean sharedToPublic) {

		this.sharedToPublic = sharedToPublic;
	}

	public void setSideCode(final int sideCode) {

		this.sideCode = sideCode;
	}

	public void setTopicId(final int topicId) {

		this.topicId = topicId;
	}

	public Bundle toBundle() {

		Bundle bundle = new Bundle();
		bundle.putInt(Points.Columns.AGREEMENT_CODE, agreementCode);
		bundle.putByteArray(Points.Columns.DRAWING, drawing);
		bundle.putBoolean(Points.Columns.EXPANDED, expanded);
		if (groupId == null) {
			bundle.putInt(Points.Columns.GROUP_ID, Integer.MIN_VALUE);
		} else {
			bundle.putInt(Points.Columns.GROUP_ID, groupId);
		}
		bundle.putInt(Points.Columns.ID, id);
		bundle.putString(Points.Columns.NAME, name);
		bundle.putString(Points.Columns.NUMBERED_POINT, numberedPoint);
		bundle.putInt(Points.Columns.PERSON_ID, personId);
		bundle.putBoolean(Points.Columns.SHARED_TO_PUBLIC, sharedToPublic);
		bundle.putInt(Points.Columns.SIDE_CODE, sideCode);
		bundle.putInt(Points.Columns.TOPIC_ID, topicId);
		bundle.putString(Points.Columns.RECENTLY_ENTERED_MEDIA_URL, recentlyEnteredMediaUrl);
		bundle.putString(Points.Columns.RECENTLY_ENTERED_SOURCE, recentlyEnteredSource);
		return bundle;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Points.Columns.AGREEMENT_CODE, agreementCode);
		cv.put(Points.Columns.DRAWING, drawing);
		cv.put(Points.Columns.EXPANDED, expanded);
		cv.put(Points.Columns.GROUP_ID, groupId);
		cv.put(Points.Columns.ID, id);
		cv.put(Points.Columns.NAME, name);
		cv.put(Points.Columns.NUMBERED_POINT, numberedPoint);
		cv.put(Points.Columns.PERSON_ID, personId);
		cv.put(Points.Columns.RECENTLY_ENTERED_MEDIA_URL, recentlyEnteredMediaUrl);
		cv.put(Points.Columns.RECENTLY_ENTERED_SOURCE, recentlyEnteredSource);
		cv.put(Points.Columns.SHARED_TO_PUBLIC, sharedToPublic);
		cv.put(Points.Columns.SIDE_CODE, sideCode);
		cv.put(Points.Columns.TOPIC_ID, topicId);
		return cv;
	}

	@Override
	public String toMyString() {

		return toBundle().toString();
	}
}
