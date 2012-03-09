package com.slobodastudio.discussions.data.model;

import android.content.ContentValues;

public interface Value {

	public ContentValues toContentValues();

	public String toMyString();
}
