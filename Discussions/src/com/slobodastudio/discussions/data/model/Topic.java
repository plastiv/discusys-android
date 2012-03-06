package com.slobodastudio.discussions.data.model;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;

import android.content.ContentValues;

public class Topic implements Value {

	private final int discussionId;
	private final int id;
	private final String name;

	public Topic(final int discussionId, final int id, final String name) {

		super();
		this.discussionId = discussionId;
		this.id = id;
		this.name = name;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Topics.Columns.DISCUSSION_ID, discussionId);
		cv.put(Topics.Columns.ID, id);
		cv.put(Topics.Columns.NAME, name);
		return cv;
	}
}
