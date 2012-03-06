package com.slobodastudio.discussions.data.provider;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.PersonsTopics;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;

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
	private static final int DATABASE_VERSION = 23;
	private static final String TAG = DiscussionsDatabase.class.getSimpleName();

	/** @param context
	 *            to use to open or create the database */
	public DiscussionsDatabase(final Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {

		// @formatter:off
		db.execSQL("CREATE TABLE " + Discussions.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Discussions.Columns.DISCUSSION_ID + " INTEGER NOT NULL,"
				+ Discussions.Columns.SUBJECT + " TEXT NOT NULL,"
				+ " UNIQUE (" + Discussions.Columns.DISCUSSION_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Persons.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Persons.Columns.ID + " INTEGER NOT NULL,"
				+ Persons.Columns.NAME + " TEXT NOT NULL,"
				+ Persons.Columns.EMAIL + " TEXT NOT NULL,"
				+ Persons.Columns.COLOR + " INTEGER NOT NULL,"
				+ Persons.Columns.ONLINE + " INTEGER NOT NULL,"
				+ " UNIQUE (" + Persons.Columns.ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Topics.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Topics.Columns.ID + " INTEGER NOT NULL,"
				+ Topics.Columns.NAME + " TEXT NOT NULL,"
				+ Topics.Columns.DISCUSSION_ID + " INTEGER NOT NULL " + References.DISCUSSION_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
				+ " UNIQUE (" + Topics.Columns.ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Points.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Points.Columns.ID + " INTEGER NOT NULL,"
				+ Points.Columns.PERSON_ID + " INTEGER NOT NULL " + References.PERSON_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
				+ Points.Columns.TOPIC_ID + " INTEGER NOT NULL " + References.TOPIC_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
			    + Points.Columns.GROUP_ID + " INTEGER NOT NULL,"
				+ Points.Columns.AGREEMENT_CODE + " INTEGER NOT NULL,"
				+ Points.Columns.DRAWING + " TEXT NOT NULL,"
				+ Points.Columns.EXPANDED + " INTEGER NOT NULL,"
				+ Points.Columns.NUMBERED_POINT + " TEXT NOT NULL,"
				+ Points.Columns.NAME + " TEXT NOT NULL,"
				+ Points.Columns.SHARED_TO_PUBLIC + " INTEGER NOT NULL,"
				+ Points.Columns.SIDE_CODE + " INTEGER NOT NULL,"
				+ " UNIQUE (" + Points.Columns.ID + ") ON CONFLICT REPLACE)");
		
		// many-to-many table
		db.execSQL("CREATE TABLE " + PersonsTopics.TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PersonsTopics.Columns.PERSON_ID + " INTEGER NOT NULL " + References.PERSON_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
                + PersonsTopics.Columns.TOPIC_ID + " INTEGER NOT NULL, " //+ References.TOPIC_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
                + "UNIQUE (" + PersonsTopics.Columns.PERSON_ID + "," + PersonsTopics.Columns.TOPIC_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TRIGGER " + Triggers.PERSONS_DELETE_TOPICS 
				+ " AFTER DELETE ON " + PersonsTopics.TABLE_NAME + " FOR EACH ROW "
				+ " BEGIN DELETE FROM " + Topics.TABLE_NAME  
				+ " WHERE " + Topics.Columns.ID + "=OLD." + PersonsTopics.Columns.TOPIC_ID + ";"
                + " END;");
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
			db.execSQL("DROP TABLE IF EXISTS " + Discussions.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Persons.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Points.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Topics.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + PersonsTopics.TABLE_NAME);
			db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.PERSONS_DELETE_TOPICS);
			onCreate(db);
		}
	}

	interface Triggers {

		static final String PERSONS_DELETE_TOPICS = "PersonsDeletesTopics";
	}

	/** {@code REFERENCES} clauses. */
	private interface References {

		static final String DISCUSSION_ID = "REFERENCES " + Discussions.TABLE_NAME + "("
				+ Discussions.Columns.DISCUSSION_ID + ")";
		static final String PERSON_ID = "REFERENCES " + Persons.TABLE_NAME + "(" + Persons.Columns.ID
				+ ")";
		static final String TOPIC_ID = "REFERENCES " + Topics.TABLE_NAME + "(" + Topics.Columns.ID
				+ ")";
	}
}
