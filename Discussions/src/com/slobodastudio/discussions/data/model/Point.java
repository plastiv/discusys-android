package com.slobodastudio.discussions.data.model;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;

import android.content.ContentValues;

public class Point implements Value {

	private final int agreementCode;
	private final byte[] drawing;
	private final boolean expanded;
	private final int groupId;
	private final int id;
	private final String name;
	private final String numberedPoint;
	private final int personId;
	private final boolean sharedToPublic;
	private final int sideCode;
	private final int topicId;

	public Point(final int agreementCode, final byte[] drawing, final boolean expanded, final int groupId,
			final int id, final String name, final String numberedPoint, final int personId,
			final boolean sharedToPublic, final int sideCode, final int topicId) {

		super();
		this.agreementCode = agreementCode;
		this.drawing = drawing;
		this.expanded = expanded;
		this.groupId = groupId;
		this.id = id;
		this.name = name;
		this.numberedPoint = numberedPoint;
		this.personId = personId;
		this.sharedToPublic = sharedToPublic;
		this.sideCode = sideCode;
		this.topicId = topicId;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Points.Columns.AGREEMENT_CODE, agreementCode);
		// FIXME
		cv.put(Points.Columns.DRAWING, new byte[] { 1 });
		cv.put(Points.Columns.EXPANDED, expanded);
		// FIXME
		cv.put(Points.Columns.GROUP_ID, 1);
		cv.put(Points.Columns.ID, id);
		cv.put(Points.Columns.NAME, name);
		// FIXME
		cv.put(Points.Columns.NUMBERED_POINT, "test");
		cv.put(Points.Columns.PERSON_ID, personId);
		cv.put(Points.Columns.SHARED_TO_PUBLIC, sharedToPublic);
		cv.put(Points.Columns.SIDE_CODE, sideCode);
		cv.put(Points.Columns.TOPIC_ID, topicId);
		return cv;
	}
}
