package com.slobodastudio.discussions.data.provider.test;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

public class ProviderUtil {

	public static void logCursor(final Cursor cursor) {

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Log.v("Test", "======Cursor value======");
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				if (TextUtils.isEmpty(cursor.getString(i))) {
					Log.v("Test", "  " + cursor.getColumnName(i) + ":null");
				} else {
					Log.v("Test", "  " + cursor.getColumnName(i) + ":" + cursor.getString(i));
				}
			}
		}
	}
}
