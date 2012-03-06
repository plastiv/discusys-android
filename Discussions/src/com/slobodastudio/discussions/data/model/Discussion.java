package com.slobodastudio.discussions.data.model;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;

import android.content.ContentValues;

public class Discussion implements Value {

	private final int id;
	private final String subject;

	public Discussion(final int id, final String subject) {

		this.id = id;
		this.subject = subject;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Discussions.Columns.DISCUSSION_ID, id);
		cv.put(Discussions.Columns.SUBJECT, subject);
		return cv;
	}
}
