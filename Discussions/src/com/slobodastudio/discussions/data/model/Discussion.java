package com.slobodastudio.discussions.data.model;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;

import android.content.ContentValues;
import android.database.Cursor;

public class Discussion implements Value {

	private final int id;
	private final boolean running;
	private final String subject;

	public Discussion(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndexOrThrow(Discussions.Columns.ID);
			int subjectIndex = cursor.getColumnIndexOrThrow(Discussions.Columns.SUBJECT);
			int runningIndex = cursor.getColumnIndexOrThrow(Discussions.Columns.RUNNING);
			id = cursor.getInt(idIndex);
			subject = cursor.getString(subjectIndex);
			switch (cursor.getInt(runningIndex)) {
				case 0:
					running = false;
					break;
				case 1:
					running = true;
					break;
				default:
					throw new IllegalArgumentException("Illegal running: " + cursor.getInt(runningIndex));
			}
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}

	public Discussion(final int id, final boolean running, final String subject) {

		super();
		this.id = id;
		this.running = running;
		this.subject = subject;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Discussions.Columns.ID, id);
		cv.put(Discussions.Columns.SUBJECT, subject);
		cv.put(Discussions.Columns.RUNNING, running);
		return cv;
	}

	@Override
	public String toMyString() {

		return toContentValues().toString();
	}
}
