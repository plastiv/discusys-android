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
	private static final int DATABASE_VERSION = 5;
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
				+ Topic.Columns.DISCUSSION_ID + " INTEGER NOT NULL,"
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
		// @formatter:on
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
			onCreate(db);
		}
	}
}
