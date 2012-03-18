package com.slobodastudio.discussions.data.model;

import com.slobodastudio.discussions.data.DataIoException;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;

import android.content.ContentValues;
import android.database.Cursor;

public class Comment implements Value {

	private final int id;
	private final String name;
	private final Integer personId;
	private final Integer pointId;

	public Comment(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndexOrThrow(Comments.Columns.ID);
			int nameIndex = cursor.getColumnIndexOrThrow(Comments.Columns.NAME);
			int personIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.PERSON_ID);
			int pointIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.POINT_ID);
			personId = cursor.getInt(personIdIndex);
			id = cursor.getInt(idIndex);
			name = cursor.getString(nameIndex);
			pointId = cursor.getInt(pointIdIndex);
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}

	public Comment(final int id, final String name, final Integer personId, final Integer pointId) {

		super();
		if ((personId == null) && (pointId == null)) {
			throw new DataIoException("Both foreign key cant be null");
		}
		this.id = id;
		this.name = name;
		this.personId = personId;
		this.pointId = pointId;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Comments.Columns.ID, id);
		cv.put(Comments.Columns.NAME, name);
		cv.put(Comments.Columns.PERSON_ID, personId);
		cv.put(Comments.Columns.POINT_ID, pointId);
		return cv;
	}

	@Override
	public String toMyString() {

		return toContentValues().toString();
	}
}
