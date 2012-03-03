package com.slobodastudio.discussions.data.provider;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussion;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Person;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topic;
import com.slobodastudio.discussions.data.provider.DiscussionsDatabase.Tables;

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
public class DiscussionsProvider extends ContentProvider {

	private static final int DISCUSSIONS_DIR = 101;
	private static final int DISCUSSIONS_ITEM = 100;
	private static final int DISCUSSIONS_ITEM_TOPICS = 102;
	private static final boolean LOGV = true;
	private static final int PERSONS_DIR = 201;
	private static final int PERSONS_ITEM = 200;
	private static final int POINTS_DIR = 301;
	private static final int POINTS_ITEM = 300;
	private static final UriMatcher sUriMatcher = buildUriMatcher();
	private static final String TAG = DiscussionsProvider.class.getSimpleName();
	private static final int TOPICS_DIR = 401;
	private static final int TOPICS_ITEM = 400;
	private DiscussionsDatabase mOpenHelper;

	/** Build a simple {@link SelectionBuilder} to match the requested {@link Uri}. This is usually enough to
	 * support {@link #insert}, {@link #update}, and {@link #delete} operations. */
	private static SelectionBuilder buildSimpleSelection(final Uri uri) {

		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case DISCUSSIONS_DIR:
				return builder.table(Discussion.TABLE_NAME);
			case DISCUSSIONS_ITEM: {
				final String valueId = Discussion.getValueId(uri);
				return builder.table(Discussion.TABLE_NAME).where(BaseColumns._ID + "=?", valueId);
			}
			case POINTS_DIR:
				return builder.table(Point.TABLE_NAME);
			case POINTS_ITEM: {
				final String valueId = Point.getValueId(uri);
				return builder.table(Point.TABLE_NAME).where(BaseColumns._ID + "=?", valueId);
			}
			case PERSONS_DIR:
				return builder.table(Person.TABLE_NAME);
			case PERSONS_ITEM: {
				final String valueId = Person.getValueId(uri);
				return builder.table(Person.TABLE_NAME).where(BaseColumns._ID + "=?", valueId);
			}
			case TOPICS_DIR:
				return builder.table(Topic.TABLE_NAME);
			case TOPICS_ITEM: {
				final String valueId = Topic.getValueId(uri);
				return builder.table(Topic.TABLE_NAME).where(BaseColumns._ID + "=?", valueId);
			}
			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
	}

	/** Build and return a {@link UriMatcher} that catches all {@link Uri} variations supported by this
	 * {@link ContentProvider}. */
	private static UriMatcher buildUriMatcher() {

		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = DiscussionsContract.CONTENT_AUTHORITY;
		matcher.addURI(authority, Discussion.A_TABLE_PREFIX, DISCUSSIONS_DIR);
		matcher.addURI(authority, Discussion.A_TABLE_PREFIX + "/*", DISCUSSIONS_ITEM);
		matcher.addURI(authority, Discussion.A_TABLE_PREFIX + "/*/" + Topic.A_TABLE_PREFIX,
				DISCUSSIONS_ITEM_TOPICS);
		matcher.addURI(authority, Point.A_TABLE_PREFIX, POINTS_DIR);
		matcher.addURI(authority, Point.A_TABLE_PREFIX + "/*", POINTS_ITEM);
		matcher.addURI(authority, Person.A_TABLE_PREFIX, PERSONS_DIR);
		matcher.addURI(authority, Person.A_TABLE_PREFIX + "/*", PERSONS_ITEM);
		matcher.addURI(authority, Topic.A_TABLE_PREFIX, TOPICS_DIR);
		matcher.addURI(authority, Topic.A_TABLE_PREFIX + "/*", TOPICS_ITEM);
		return matcher;
	}

	/** Apply the given set of {@link ContentProviderOperation}, executing inside a {@link SQLiteDatabase}
	 * transaction. All changes will be rolled back if any single one fails. */
	@Override
	public ContentProviderResult[] applyBatch(final ArrayList<ContentProviderOperation> operations)
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
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {

		if (LOGV) {
			Log.v(TAG, "delete(uri=" + uri + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int rowsCount = builder.where(selection, selectionArgs).delete(db);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsCount;
	}

	@Override
	public String getType(final Uri uri) {

		final int match = sUriMatcher.match(uri);
		switch (match) {
			case DISCUSSIONS_DIR:
				return Discussion.CONTENT_DIR_TYPE;
			case DISCUSSIONS_ITEM:
				return Discussion.CONTENT_ITEM_TYPE;
			case DISCUSSIONS_ITEM_TOPICS:
				return Topic.CONTENT_DIR_TYPE;
			case POINTS_DIR:
				return Point.CONTENT_DIR_TYPE;
			case POINTS_ITEM:
				return Point.CONTENT_ITEM_TYPE;
			case PERSONS_DIR:
				return Person.CONTENT_DIR_TYPE;
			case PERSONS_ITEM:
				return Person.CONTENT_ITEM_TYPE;
			case TOPICS_DIR:
				return Topic.CONTENT_DIR_TYPE;
			case TOPICS_ITEM:
				return Topic.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {

		if (LOGV) {
			Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		final long insertedId;
		final Uri insertedUri;
		switch (match) {
			case DISCUSSIONS_DIR:
				insertedId = db.insertOrThrow(Discussion.TABLE_NAME, null, values);
				insertedUri = Discussion.buildTableUri(insertedId);
				break;
			case POINTS_DIR:
				insertedId = db.insertOrThrow(Point.TABLE_NAME, null, values);
				insertedUri = Point.buildTableUri(insertedId);
				break;
			case PERSONS_DIR:
				insertedId = db.insertOrThrow(Person.TABLE_NAME, null, values);
				insertedUri = Person.buildTableUri(insertedId);
				break;
			case TOPICS_DIR:
				insertedId = db.insertOrThrow(Topic.TABLE_NAME, null, values);
				insertedUri = Topic.buildTableUri(insertedId);
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
		mOpenHelper = new DiscussionsDatabase(context);
		return true;
	}

	@Override
	public ParcelFileDescriptor openFile(final Uri uri, final String mode) {

		throw new UnsupportedOperationException("With uri: " + uri + ", mode: " + mode);
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection,
			final String[] selectionArgs, final String sortOrder) {

		if (LOGV) {
			Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case DISCUSSIONS_DIR:
			case DISCUSSIONS_ITEM:
				builder.table(Discussion.TABLE_NAME);
				break;
//			// @formatter:off
//			case DISCUSSIONS_ITEM_TOPICS: {
//				final String valueId = Discussion.getValueId(uri);
//				builder.table(Tables.DISCUSSIONS_JOIN_TOPICS)
//						.mapToTable(BaseColumns._ID, Topic.TABLE_NAME)
//						.mapToTable(Topic.Columns.DISCUSSION_ID, Topic.TABLE_NAME)
//						.mapToTable(Topic.Columns.TOPIC_ID, Topic.TABLE_NAME)
//						.where(Qualified.TOPIC_DISCUSSION_ID + "=?", valueId);
//				return builder.query(db, projection, sortOrder);
//			}// @formatter:on
			case DISCUSSIONS_ITEM_TOPICS: {
				final String valueId = Discussion.getValueId(uri);
				builder.table(Topic.TABLE_NAME).where(Topic.Columns.DISCUSSION_ID + "=?", valueId);
				return builder.query(db, projection, sortOrder);
			}
			case POINTS_DIR:
			case POINTS_ITEM:
				builder.table(Point.TABLE_NAME);
				break;
			case PERSONS_DIR:
			case PERSONS_ITEM:
				builder.table(Person.TABLE_NAME);
				break;
			case TOPICS_DIR:
			case TOPICS_ITEM:
				builder.table(Topic.TABLE_NAME);
				break;
			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		builder.where(selection, selectionArgs);
		return builder.query(db, projection, sortOrder);
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection,
			final String[] selectionArgs) {

		if (LOGV) {
			Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int rowCount = builder.where(selection, selectionArgs).update(db, values);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowCount;
	}

	/** Build an advanced {@link SelectionBuilder} to match the requested {@link Uri}. This is usually only
	 * used by {@link #query}, since it performs table joins useful for {@link Cursor} data. */
	private SelectionBuilder buildExpandedSelection(final Uri uri, final int match) {

		final SelectionBuilder builder = new SelectionBuilder();
		switch (match) {
			case DISCUSSIONS_ITEM_TOPICS: {
				final String valueId = Discussion.getValueId(uri);
				return builder.table(Tables.DISCUSSIONS_JOIN_TOPICS).mapToTable(BaseColumns._ID,
						Discussion.TABLE_NAME).mapToTable(Discussion.Columns.DISCUSSION_ID,
						Discussion.TABLE_NAME).where(Qualified.TOPIC_DISCUSSION_ID + "=?", valueId);
			}
			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
	}

	/** {@link ScheduleContract} fields that are fully qualified with a specific parent {@link Tables}. Used
	 * when needed to work around SQL ambiguity. */
	private interface Qualified {

		String TOPIC_DISCUSSION_ID = Topic.TABLE_NAME + "." + Topic.Columns.DISCUSSION_ID;
	}
}
