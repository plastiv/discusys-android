package com.slobodastudio.discussions.data.model;

import com.slobodastudio.discussions.data.DataIoException;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.RichText;

import android.content.ContentValues;
import android.database.Cursor;

public class Description implements Value {

	private final Integer discussionId;
	private final int id;
	private final Integer pointId;
	private final String text;

	public Description(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndexOrThrow(RichText.Columns.ID);
			int textIndex = cursor.getColumnIndexOrThrow(RichText.Columns.TEXT);
			int discussionIdIndex = cursor.getColumnIndexOrThrow(RichText.Columns.DISCUSSION_ID);
			int pointIdIndex = cursor.getColumnIndexOrThrow(RichText.Columns.POINT_ID);
			discussionId = cursor.getInt(discussionIdIndex);
			id = cursor.getInt(idIndex);
			text = cursor.getString(textIndex);
			pointId = cursor.getInt(pointIdIndex);
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}

	public Description(final int id, final String text, final Integer discussionId, final Integer pointId) {

		super();
		if ((discussionId == null) && (pointId == null)) {
			throw new DataIoException("Both foreign key cant be null");
		}
		this.id = id;
		this.text = text;
		this.discussionId = discussionId;
		this.pointId = pointId;
	}

	public String getText() {

		return text;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(RichText.Columns.ID, id);
		cv.put(RichText.Columns.TEXT, text);
		cv.put(RichText.Columns.DISCUSSION_ID, discussionId);
		cv.put(RichText.Columns.POINT_ID, pointId);
		return cv;
	}

	@Override
	public String toMyString() {

		return toContentValues().toString();
	}
}
