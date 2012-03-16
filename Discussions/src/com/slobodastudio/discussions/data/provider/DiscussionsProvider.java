package com.slobodastudio.discussions.data.provider;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.PersonsTopics;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/** Provider that stores {@link DiscussionsContract} data. Data is usually inserted by SyncService , and
 * queried by various {@link Activity} instances. */
public class DiscussionsProvider extends ContentProvider {

	private static final int COMMENTS_DIR = 500;
	private static final int COMMENTS_ITEM = 501;
	private static final int DISCUSSIONS_DIR = 101;
	private static final int DISCUSSIONS_ITEM = 100;
	private static final int DISCUSSIONS_ITEM_TOPICS_DIR = 102;
	private static final boolean LOGV = true && ApplicationConstants.DEBUG_MODE;
	private static final int PERSONS_DIR = 201;
	private static final int PERSONS_ITEM = 200;
	private static final int PERSONS_ITEM_DISCUSSIONS_DIR = 204;
	private static final int PERSONS_ITEM_POINTS_DIR = 202;
	private static final int PERSONS_ITEM_TOPICS_DIR = 203;
	private static final int POINTS_DIR = 301;
	private static final int POINTS_ITEM = 300;
	private static final UriMatcher sUriMatcher = buildUriMatcher();
	private static final String TAG = DiscussionsProvider.class.getSimpleName();
	private static final int TOPICS_DIR = 401;
	private static final int TOPICS_ITEM = 400;
	private static final int TOPICS_ITEM_POINTS_DIR = 402;
	private DiscussionsDatabase mOpenHelper;

	/** Build a simple {@link SelectionBuilder} to match the requested {@link Uri}. This is usually enough to
	 * support {@link #insert}, {@link #update}, and {@link #delete} operations. */
	private static SelectionBuilder buildSimpleSelection(final Uri uri) {

		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case DISCUSSIONS_DIR:
				return builder.table(Discussions.TABLE_NAME);
			case DISCUSSIONS_ITEM: {
				final String valueId = Discussions.getValueId(uri);
				return builder.table(Discussions.TABLE_NAME).where(Discussions.Columns.ID + "=?", valueId);
			}
			case POINTS_DIR:
				return builder.table(Points.TABLE_NAME);
			case POINTS_ITEM: {
				final String valueId = Points.getValueId(uri);
				// NOTE: this select where is different column by _id, not a Id
				return builder.table(Points.TABLE_NAME).where(BaseColumns._ID + "=?", valueId);
			}
			case PERSONS_DIR:
				return builder.table(Persons.TABLE_NAME);
			case PERSONS_ITEM: {
				final String valueId = Persons.getValueId(uri);
				return builder.table(Persons.TABLE_NAME).where(Persons.Columns.ID + "=?", valueId);
			}
			case TOPICS_DIR:
				return builder.table(Topics.TABLE_NAME);
			case TOPICS_ITEM: {
				final String valueId = Topics.getValueId(uri);
				return builder.table(Topics.TABLE_NAME).where(Topics.Columns.ID + "=?", valueId);
			}
			case COMMENTS_DIR:
				return builder.table(Comments.TABLE_NAME);
			case COMMENTS_ITEM: {
				final String valueId = Comments.getValueId(uri);
				return builder.table(Comments.TABLE_NAME).where(Comments.Columns.ID + "=?", valueId);
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
		// discussion
		matcher.addURI(authority, Discussions.A_TABLE_PREFIX, DISCUSSIONS_DIR);
		matcher.addURI(authority, Discussions.A_TABLE_PREFIX + "/*", DISCUSSIONS_ITEM);
		matcher.addURI(authority, Discussions.A_TABLE_PREFIX + "/*/" + Topics.A_TABLE_PREFIX,
				DISCUSSIONS_ITEM_TOPICS_DIR);
		// point
		matcher.addURI(authority, Points.A_TABLE_PREFIX, POINTS_DIR);
		matcher.addURI(authority, Points.A_TABLE_PREFIX + "/*", POINTS_ITEM);
		// person
		matcher.addURI(authority, Persons.A_TABLE_PREFIX, PERSONS_DIR);
		matcher.addURI(authority, Persons.A_TABLE_PREFIX + "/*", PERSONS_ITEM);
		matcher.addURI(authority, Persons.A_TABLE_PREFIX + "/*/" + Points.A_TABLE_PREFIX,
				PERSONS_ITEM_POINTS_DIR);
		matcher.addURI(authority, Persons.A_TABLE_PREFIX + "/*/" + Topics.A_TABLE_PREFIX,
				PERSONS_ITEM_TOPICS_DIR);
		matcher.addURI(authority, Persons.A_TABLE_PREFIX + "/*/" + Discussions.A_TABLE_PREFIX,
				PERSONS_ITEM_DISCUSSIONS_DIR);
		// topic
		matcher.addURI(authority, Topics.A_TABLE_PREFIX, TOPICS_DIR);
		matcher.addURI(authority, Topics.A_TABLE_PREFIX + "/*", TOPICS_ITEM);
		matcher.addURI(authority, Topics.A_TABLE_PREFIX + "/*/" + Points.A_TABLE_PREFIX,
				TOPICS_ITEM_POINTS_DIR);
		// comment
		matcher.addURI(authority, Comments.A_TABLE_PREFIX, COMMENTS_DIR);
		matcher.addURI(authority, Comments.A_TABLE_PREFIX + "/*", COMMENTS_ITEM);
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
				return Discussions.CONTENT_DIR_TYPE;
			case DISCUSSIONS_ITEM:
				return Discussions.CONTENT_ITEM_TYPE;
			case DISCUSSIONS_ITEM_TOPICS_DIR:
				return Topics.CONTENT_DIR_TYPE;
			case POINTS_DIR:
				return Points.CONTENT_DIR_TYPE;
			case POINTS_ITEM:
				return Points.CONTENT_ITEM_TYPE;
			case PERSONS_DIR:
				return Persons.CONTENT_DIR_TYPE;
			case PERSONS_ITEM:
				return Persons.CONTENT_ITEM_TYPE;
			case PERSONS_ITEM_POINTS_DIR:
				return Points.CONTENT_DIR_TYPE;
			case PERSONS_ITEM_TOPICS_DIR:
				return Topics.CONTENT_DIR_TYPE;
			case PERSONS_ITEM_DISCUSSIONS_DIR:
				return Discussions.CONTENT_DIR_TYPE;
			case TOPICS_DIR:
				return Topics.CONTENT_DIR_TYPE;
			case TOPICS_ITEM:
				return Topics.CONTENT_ITEM_TYPE;
			case TOPICS_ITEM_POINTS_DIR:
				return Points.CONTENT_DIR_TYPE;
			case COMMENTS_DIR:
				return Comments.CONTENT_DIR_TYPE;
			case COMMENTS_ITEM:
				return Comments.CONTENT_ITEM_TYPE;
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
		try {
			switch (match) {
				case DISCUSSIONS_DIR:
					insertedId = db.insertOrThrow(Discussions.TABLE_NAME, null, values);
					insertedUri = Discussions.buildTableUri(insertedId);
					break;
				case POINTS_DIR:
					insertedId = db.insertOrThrow(Points.TABLE_NAME, null, values);
					insertedUri = Points.buildTableUri(insertedId);
					break;
				case PERSONS_DIR:
					insertedId = db.insertOrThrow(Persons.TABLE_NAME, null, values);
					insertedUri = Persons.buildTableUri(insertedId);
					break;
				case PERSONS_ITEM_TOPICS_DIR:
					insertedId = db.insertOrThrow(PersonsTopics.TABLE_NAME, null, values);
					insertedUri = Persons.buildTableUri(insertedId);
					break;
				case TOPICS_DIR:
					insertedId = db.insertOrThrow(Topics.TABLE_NAME, null, values);
					insertedUri = Topics.buildTableUri(insertedId);
					break;
				case COMMENTS_DIR:
					insertedId = db.insertOrThrow(Comments.TABLE_NAME, null, values);
					insertedUri = Comments.buildTableUri(insertedId);
					break;
				default:
					throw new IllegalArgumentException("Unknown uri: " + uri);
			}
		} catch (SQLiteException e) {
			throw new RuntimeException("Unable to insert uri: " + uri + ", value: " + values.toString(), e);
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
			Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ", selection="
					+ selection + ", selectArg=" + Arrays.toString(selectionArgs) + ", sortOrder="
					+ sortOrder + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		SelectionBuilder builder = new SelectionBuilder();
		Uri notificationUri;
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case DISCUSSIONS_ITEM_TOPICS_DIR: {
				final String valueId = Discussions.getValueId(uri);
				builder.table(Topics.TABLE_NAME).where(Topics.Columns.DISCUSSION_ID + "=?", valueId);
				notificationUri = Topics.CONTENT_URI;
				break;
			}
			case PERSONS_ITEM_POINTS_DIR: {
				final String valueId = Persons.getValueId(uri);
				builder.table(Points.TABLE_NAME).where(Points.Columns.PERSON_ID + "=?", valueId);
				notificationUri = Points.CONTENT_URI;
				break;
			}
			case PERSONS_ITEM_TOPICS_DIR: {
				final String valueId = Persons.getValueId(uri);
				builder.table(PersonsTopics.TABLE_NAME + "," + Topics.TABLE_NAME).mapToTable(BaseColumns._ID,
						Topics.TABLE_NAME).mapToTable(Topics.Columns.ID, Topics.TABLE_NAME).where(
						PersonsTopics.Columns.PERSON_ID + "=? AND " + PersonsTopics.Columns.TOPIC_ID + "="
								+ Topics.Columns.ID, valueId);
				notificationUri = Discussions.CONTENT_URI;
				Cursor c = builder.query(db, new String[] { BaseColumns._ID, Topics.Columns.ID,
						Topics.Columns.NAME, Topics.Columns.DISCUSSION_ID }, sortOrder);
				c.setNotificationUri(getContext().getContentResolver(), notificationUri);
				return c;
			}
			case PERSONS_ITEM_DISCUSSIONS_DIR: {
				final String valueId = Persons.getValueId(uri);
				builder.table(
						PersonsTopics.TABLE_NAME + "," + Topics.TABLE_NAME + "," + Discussions.TABLE_NAME)
						.mapToTable(BaseColumns._ID, Discussions.TABLE_NAME).mapToTable(
								Discussions.Columns.ID, Discussions.TABLE_NAME).where(
								PersonsTopics.Columns.PERSON_ID + "=? AND " + PersonsTopics.Columns.TOPIC_ID
										+ "=" + Topics.Qualified.TOPIC_ID + " AND "
										+ Topics.Columns.DISCUSSION_ID + "="
										+ Discussions.Qualified.DISCUSSION_ID, valueId);
				notificationUri = Discussions.CONTENT_URI;
				Cursor c = builder.query(db, new String[] { BaseColumns._ID, Discussions.Columns.ID,
						Discussions.Columns.SUBJECT }, Discussions.Qualified.DISCUSSION_ID, null, sortOrder,
						null);
				c.setNotificationUri(getContext().getContentResolver(), notificationUri);
				return c;
			}
			case TOPICS_ITEM_POINTS_DIR: {
				final String valueId = Topics.getValueId(uri);
				builder.table(Points.TABLE_NAME).where(Points.Columns.TOPIC_ID + "=?", valueId);
				notificationUri = Points.CONTENT_URI;
				break;
			}
			default:
				notificationUri = uri;
				builder = buildSimpleSelection(uri);
				builder.where(selection, selectionArgs);
		}
		Cursor c = builder.query(db, projection, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), notificationUri);
		return c;
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
}
