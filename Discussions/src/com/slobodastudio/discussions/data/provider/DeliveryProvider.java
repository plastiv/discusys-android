package com.slobodastudio.discussions.data.provider;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Locations;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Tasks;
import com.slobodastudio.discussions.data.provider.DeliveryDatabase.Tables;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/** Provider that stores {@link DiscussionsContract} data. Data is usually inserted by {@link SyncService}, and
 * queried by various {@link Activity} instances. */
public class DeliveryProvider extends ContentProvider {

	private static final int LOCATIONS_DIR = 201;
	private static final int LOCATIONS_ITEM = 200;
	private static final boolean LOGV = false;
	private static final int POINTS_DIR = 301;
	private static final int POINTS_ITEM = 300;
	private static final UriMatcher sUriMatcher = buildUriMatcher();
	private static final String TAG = "ScheduleProvider";
	private static final int TASKS_DIR = 101;
	private static final int TASKS_ITEM = 100;
	private DeliveryDatabase mOpenHelper;

	/** Build a simple {@link SelectionBuilder} to match the requested {@link Uri}. This is usually enough to
	 * support {@link #insert}, {@link #update}, and {@link #delete} operations. */
	private static SelectionBuilder buildSimpleSelection(Uri uri) {

		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case TASKS_DIR:
				return builder.table(Tables.TASKS);
			case TASKS_ITEM:
				final String taskId = Tasks.getTaskId(uri);
				return builder.table(Tables.TASKS).where(BaseColumns._ID + "=?", taskId);
			case POINTS_DIR:
				return builder.table(Tables.POINTS);
			case POINTS_ITEM:
				final String pointId = Points.getPointId(uri);
				return builder.table(Tables.POINTS).where(BaseColumns._ID + "=?", pointId);
			case LOCATIONS_DIR:
				return builder.table(Tables.LOCATIONS);
			case LOCATIONS_ITEM:
				final String locationId = Locations.getLocationId(uri);
				return builder.table(Tables.LOCATIONS).where(BaseColumns._ID + "=?", locationId);
			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
	}

	/** Build and return a {@link UriMatcher} that catches all {@link Uri} variations supported by this
	 * {@link ContentProvider}. */
	private static UriMatcher buildUriMatcher() {

		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = DiscussionsContract.CONTENT_AUTHORITY;
		matcher.addURI(authority, "tasks", TASKS_DIR);
		matcher.addURI(authority, "tasks/*", TASKS_ITEM);
		matcher.addURI(authority, "points", POINTS_DIR);
		matcher.addURI(authority, "points/*", POINTS_ITEM);
		matcher.addURI(authority, "locations", LOCATIONS_DIR);
		matcher.addURI(authority, "locations/*", LOCATIONS_ITEM);
		return matcher;
	}

	/** Apply the given set of {@link ContentProviderOperation}, executing inside a {@link SQLiteDatabase}
	 * transaction. All changes will be rolled back if any single one fails. */
	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {

		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			final int numOperations = operations.size();
			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
			for (int i = 0; i < numOperations; i++) {
				results[i] = operations.get(i).apply(this, results, i);
			}
			db.setTransactionSuccessful();
			return results;
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		if (LOGV) {
			Log.v(TAG, "delete(uri=" + uri + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int retVal = builder.where(selection, selectionArgs).delete(db);
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}

	@Override
	public String getType(Uri uri) {

		final int match = sUriMatcher.match(uri);
		switch (match) {
			case TASKS_DIR:
				return Tasks.CONTENT_DIR_TYPE;
			case TASKS_ITEM:
				return Tasks.CONTENT_ITEM_TYPE;
			case POINTS_DIR:
				return Points.CONTENT_DIR_TYPE;
			case POINTS_ITEM:
				return Points.CONTENT_ITEM_TYPE;
			case LOCATIONS_DIR:
				return Locations.CONTENT_DIR_TYPE;
			case LOCATIONS_ITEM:
				return Locations.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		if (LOGV) {
			Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		final long insertedId;
		final Uri insertedUri;
		switch (match) {
			case TASKS_DIR:
				insertedId = db.insertOrThrow(Tables.TASKS, null, values);
				insertedUri = Tasks.buildTasksUri(insertedId);
				break;
			case POINTS_DIR:
				insertedId = db.insertOrThrow(Tables.POINTS, null, values);
				insertedUri = Points.buildPointsUri(insertedId);
				break;
			case LOCATIONS_DIR:
				insertedId = db.insertOrThrow(Tables.LOCATIONS, null, values);
				insertedUri = Locations.buildLocationsUri(insertedId);
				break;
			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return insertedUri;
	}

	@Override
	public boolean onCreate() {

		final Context context = getContext();
		mOpenHelper = new DeliveryDatabase(context);
		return true;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) {

		throw new UnsupportedOperationException("With uri: " + uri + ", mode: " + mode);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {

		if (LOGV) {
			Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case TASKS_DIR:
			case TASKS_ITEM:
				builder.table(Tables.TASKS);
				break;
			case POINTS_DIR:
			case POINTS_ITEM:
				builder.table(Tables.POINTS);
				break;
			case LOCATIONS_DIR:
			case LOCATIONS_ITEM:
				builder.table(Tables.LOCATIONS);
				break;
			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		builder.where(selection, selectionArgs);
		return builder.query(db, projection, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		if (LOGV) {
			Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int retVal = builder.where(selection, selectionArgs).update(db, values);
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}
}
