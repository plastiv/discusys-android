package com.slobodastudio.discussions.data.provider;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussion;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Person;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/** Helper for managing {@link SQLiteDatabase} that stores data for {@link DiscussionsProvider}. */
public class DiscussionsDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "discussions.db";
	// NOTE: carefully update onUpgrade() when bumping database versions to make
	// sure user data is saved.
	private static final int DATABASE_VERSION = 8;
	private static final String TAG = DiscussionsDatabase.class.getSimpleName();

	/** @param context
	 *            to use to open or create the database */
	public DiscussionsDatabase(final Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {

		// @formatter:off
		db.execSQL("CREATE TABLE " + Discussion.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Discussion.Columns.DISCUSSION_ID + " INTEGER NOT NULL,"
				+ Discussion.Columns.SUBJECT + " TEXT NOT NULL,"
				+ " UNIQUE (" + Discussion.Columns.DISCUSSION_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Person.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Person.Columns.PERSON_ID + " INTEGER NOT NULL,"
				+ Person.Columns.NAME + " TEXT NOT NULL,"
				+ Person.Columns.EMAIL + " TEXT NOT NULL,"
				+ Person.Columns.COLOR + " INTEGER NOT NULL,"
				+ Person.Columns.ONLINE + " INTEGER NOT NULL,"
				+ " UNIQUE (" + Person.Columns.PERSON_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Topic.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Topic.Columns.TOPIC_ID + " INTEGER NOT NULL,"
				+ Topic.Columns.NAME + " TEXT NOT NULL,"
				+ Topic.Columns.DISCUSSION_ID + " INTEGER NOT NULL " + References.DISCUSSION_ID +" ON UPDATE CASCADE ON DELETE CASCADE,"
				+ " UNIQUE (" + Topic.Columns.TOPIC_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Point.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Point.Columns.POINT_ID + " INTEGER NOT NULL,"
				+ Point.Columns.PERSON_ID + " INTEGER NOT NULL,"
				+ Point.Columns.TOPIC_ID + " INTEGER NOT NULL,"
			    + Point.Columns.GROUP_ID + " INTEGER NOT NULL,"
				+ Point.Columns.AGREEMENT_CODE + " INTEGER NOT NULL,"
				+ Point.Columns.DRAWING + " TEXT NOT NULL,"
				+ Point.Columns.EXPANDED + " INTEGER NOT NULL,"
				+ Point.Columns.NUMBERED_POINT + " TEXT NOT NULL,"
				+ Point.Columns.POINT_NAME + " TEXT NOT NULL,"
				+ Point.Columns.SHARED_TO_PUBLIC + " INTEGER NOT NULL,"
				+ Point.Columns.SIDE_CODE + " INTEGER NOT NULL,"
				+ " UNIQUE (" + Point.Columns.POINT_ID + ") ON CONFLICT REPLACE)");
		
		// many-to-many table
		db.execSQL("CREATE TABLE " + Tables.TOPIC_PERSON + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TopicsPersons.PERSON_ID + " TEXT NOT NULL " + References.PERSON_ID + ","
                + TopicsPersons.TOPIC_ID + " TEXT NOT NULL " + References.TOPIC_ID + ","
                + "UNIQUE (" + TopicsPersons.PERSON_ID + "," + TopicsPersons.TOPIC_ID + ") ON CONFLICT REPLACE)");
		// @formatter:on
	}

	@Override
	public void onOpen(final SQLiteDatabase db) {

		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

		Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
		if (oldVersion != DATABASE_VERSION) {
			Log.w(TAG, "Destroying old data during upgrade");
			db.execSQL("DROP TABLE IF EXISTS " + Discussion.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Person.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Point.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Topic.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.DISCUSSIONS_JOIN_TOPICS);
			onCreate(db);
		}
	}

	private interface Columns {

		final String DISCUSSION_DISCUSSION_ID = Discussion.TABLE_NAME + "."
				+ Discussion.Columns.DISCUSSION_ID;
		final String TOPIC_DISCUSSION_ID = Topic.TABLE_NAME + "." + Topic.Columns.DISCUSSION_ID;
	}

	/** {@code REFERENCES} clauses. */
	private interface References {

		final String DISCUSSION_ID = "REFERENCES " + Discussion.TABLE_NAME + "("
				+ Discussion.Columns.DISCUSSION_ID + ")";
		final String PERSON_ID = "REFERENCES " + Person.TABLE_NAME + "(" + Person.Columns.PERSON_ID + ")";
		final String TOPIC_ID = "REFERENCES " + Topic.TABLE_NAME + "(" + Topic.Columns.TOPIC_ID + ")";
	}

	private interface TopicsPersons {

		final String PERSON_ID = Person.A_TABLE_PREFIX + "_id";
		final String TOPIC_ID = Topic.A_TABLE_PREFIX + "_id";
	}

	interface Tables {

		final String DISCUSSIONS_JOIN_TOPICS = Discussion.TABLE_NAME + " LEFT OUTER JOIN " + Topic.TABLE_NAME
				+ " ON " + Columns.DISCUSSION_DISCUSSION_ID + "=" + Columns.TOPIC_DISCUSSION_ID;
		final String TOPIC_PERSON = Topic.TABLE_NAME + "_" + Person.TABLE_NAME;
	}
}
