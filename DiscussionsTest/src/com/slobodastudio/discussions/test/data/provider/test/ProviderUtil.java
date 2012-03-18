package com.slobodastudio.discussions.test.data.provider.test;

import android.content.ContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ProviderUtil {

	public static void fetchTable(final Uri tableUri, final ContentProvider provider) {

		Cursor cursor = provider.query(tableUri, null, null, null, null);
		logCursor(cursor);
	}

	public static void fetchTable(final Uri tableUri, final Context context) {

		Cursor cursor = context.getContentResolver().query(tableUri, null, null, null, null);
		logCursor(cursor);
	}

	public static void logCursor(final Cursor cursor) {

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Log.v("Test", "======Cursor value " + cursor.getPosition() + "======");
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				try {
					if (TextUtils.isEmpty(cursor.getString(i))) {
						Log.v("Test", "  " + cursor.getColumnName(i) + ":null");
					} else {
						Log.v("Test", "  " + cursor.getColumnName(i) + ":" + cursor.getString(i));
					}
				} catch (SQLiteException e) {
					Log.v("Test", "  " + cursor.getColumnName(i) + ":" + cursor.getBlob(i));
				}
			}
		}
	}
}
