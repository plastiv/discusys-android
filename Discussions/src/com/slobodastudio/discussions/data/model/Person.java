package com.slobodastudio.discussions.data.model;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;

import android.content.ContentValues;
import android.database.Cursor;

public class Person implements Value {

	private final byte[] avatar;
	private final int color;
	private final String email;
	private final int id;
	private final String name;
	private final boolean online;

	public Person(final byte[] avatar, final int color, final String email, final int id, final String name,
			final boolean online) {

		super();
		this.avatar = avatar;
		this.color = color;
		this.email = email;
		this.id = id;
		this.name = name;
		this.online = online;
	}

	public Person(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndexOrThrow(Persons.Columns.ID);
			int nameIndex = cursor.getColumnIndexOrThrow(Persons.Columns.NAME);
			int colorIndex = cursor.getColumnIndexOrThrow(Persons.Columns.COLOR);
			int emailIndex = cursor.getColumnIndexOrThrow(Persons.Columns.EMAIL);
			int onlineIndex = cursor.getColumnIndexOrThrow(Persons.Columns.ONLINE);
			int avatarIndex = cursor.getColumnIndexOrThrow(Persons.Columns.AVATAR);
			color = cursor.getInt(colorIndex);
			if (cursor.getInt(onlineIndex) == 0) {
				online = false;
			} else if (cursor.getInt(onlineIndex) == 1) {
				online = true;
			} else {
				throw new IllegalStateException("Value has unknown online: " + cursor.getInt(onlineIndex));
			}
			id = cursor.getInt(idIndex);
			name = cursor.getString(nameIndex);
			email = cursor.getString(emailIndex);
			avatar = cursor.getBlob(avatarIndex);
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}

	public byte[] getAvatar() {

		return avatar;
	}

	public int getColor() {

		return color;
	}

	public String getEmail() {

		return email;
	}

	public int getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public boolean isOnline() {

		return online;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Persons.Columns.AVATAR, avatar);
		cv.put(Persons.Columns.COLOR, color);
		cv.put(Persons.Columns.EMAIL, email);
		cv.put(Persons.Columns.ID, id);
		cv.put(Persons.Columns.NAME, name);
		cv.put(Persons.Columns.ONLINE, online);
		return cv;
	}

	@Override
	public String toMyString() {

		return toContentValues().toString();
	}
}
