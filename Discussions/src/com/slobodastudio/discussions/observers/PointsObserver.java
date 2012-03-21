package com.slobodastudio.discussions.observers;

import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.service.SyncService;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class PointsObserver extends ContentObserver {

	private static final String TAG = PointsObserver.class.getSimpleName();
	private final Context context;

	public PointsObserver(final Handler handler, final Context context) {

		super(handler);
		this.context = context;
	}

	@Override
	public boolean deliverSelfNotifications() {

		return false;
	}

	@Override
	public void onChange(final boolean selfChange) {

		super.onChange(selfChange);
		if (!selfChange) {
			Cursor cursor = context.getContentResolver().query(Points.CONTENT_URI,
					new String[] { Points.Columns.ID }, Points.Columns.SYNC + "=?",
					new String[] { String.valueOf(1) }, null);
			Log.d(TAG, "[onChange] cursor count: " + cursor.getCount());
			if (cursor.moveToFirst()) {
				for (; cursor.isAfterLast(); cursor.moveToNext()) {
					Uri uri = Points.buildTableUri(cursor.getLong(cursor
							.getColumnIndexOrThrow(Points.Columns.ID)));
					Cursor valueCur = context.getContentResolver().query(uri, null, null, null, null);
					Point point = new Point(valueCur);
					valueCur.close();
					Intent intent = new Intent(SyncService.ACTION_UPDATE);
					intent.putExtras(point.toBundle());
					context.startService(intent);
				}
			}
			cursor.close();
		}
	}
}
