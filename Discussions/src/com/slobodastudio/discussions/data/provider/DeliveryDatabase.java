package com.slobodastudio.discussions.data.provider;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.LocationsColumns;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.PointsColumns;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.TasksColumns;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/** Helper for managing {@link SQLiteDatabase} that stores data for {@link DeliveryProvider}. */
public class DeliveryDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "delivery.db";
	// NOTE: carefully update onUpgrade() when bumping database versions to make
	// sure user data is saved.
	private static final int DATABASE_VERSION = 4;
	private static final String TAG = "DeliveryDatabase";

	/** @param context
	 *            to use to open or create the database */
	public DeliveryDatabase(Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + Tables.TASKS + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + TasksColumns.TASK_TITLE + " TEXT NOT NULL,"
				+ TasksColumns.TASK_DATE + " INTEGER NOT NULL," + TasksColumns.TASK_START + " INTEGER,"
				+ TasksColumns.TASK_END + " INTEGER," + TasksColumns.TASK_STATE
				+ " INTEGER NOT NULL DEFAULT 0," + " UNIQUE (" + TasksColumns.TASK_TITLE
				+ ") ON CONFLICT REPLACE)");
		db.execSQL("CREATE TABLE " + Tables.LOCATIONS + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + LocationsColumns.LOCATION_LATITUDE
				+ " REAL NOT NULL," + LocationsColumns.LOCATION_LONGITUDE + " REAL NOT NULL,"
				+ LocationsColumns.LOCATION_TIMESTAMP + " INTEGER NOT NULL," + LocationsColumns.TASK_ID
				+ " INTEGER NOT NULL " + References.TASK_ID + "," + " UNIQUE ("
				+ LocationsColumns.LOCATION_TIMESTAMP + ") ON CONFLICT REPLACE)");
		db.execSQL("CREATE TABLE " + Tables.POINTS + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + PointsColumns.POINT_LATITUDE + " REAL NOT NULL,"
				+ PointsColumns.POINT_LONGITUDE + " REAL NOT NULL," + PointsColumns.POINT_CONFIRMATION
				+ " INTEGER NOT NULL DEFAULT 0," + PointsColumns.POINT_ADDRESS + " TEXT NOT NULL,"
				+ PointsColumns.POINT_DESCRIPTION + " TEXT," + PointsColumns.POINT_NAME + " TEXT NOT NULL,"
				+ PointsColumns.TASK_ID + " INTEGER NOT NULL," + " FOREIGN KEY(" + PointsColumns.TASK_ID
				+ ") " + References.TASK_ID + "," + " UNIQUE (" + PointsColumns.POINT_NAME
				+ ") ON CONFLICT REPLACE)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
		if (oldVersion != DATABASE_VERSION) {
			Log.w(TAG, "Destroying old data during upgrade");
			db.execSQL("DROP TABLE IF EXISTS " + Tables.LOCATIONS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.POINTS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.TASKS);
			onCreate(db);
		}
	}

	private interface References {

		String TASK_ID = " REFERENCES " + Tables.TASKS + "(" + BaseColumns._ID
				+ ") ON DELETE CASCADE ON UPDATE CASCADE";
	}

	interface Tables {

		String LOCATIONS = "locations";
		String POINTS = "points";
		String TASKS = "tasks";
	}
}
