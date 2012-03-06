package com.slobodastudio.discussions.data.model;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;

import android.content.ContentValues;

public class Person implements Value {

	private final int color;
	private final String email;
	private final int id;
	private final String name;
	private final boolean online;

	public Person(final int color, final String email, final int id, final String name, final boolean online) {

		super();
		this.color = color;
		this.email = email;
		this.id = id;
		this.name = name;
		this.online = online;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Persons.Columns.COLOR, color);
		cv.put(Persons.Columns.EMAIL, email);
		cv.put(Persons.Columns.ID, id);
		cv.put(Persons.Columns.NAME, name);
		cv.put(Persons.Columns.ONLINE, online);
		return cv;
	}
}
