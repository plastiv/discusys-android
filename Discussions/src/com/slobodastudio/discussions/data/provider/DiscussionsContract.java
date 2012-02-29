package com.slobodastudio.discussions.data.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/** Contract class for interacting with {@link DeliveryProvider}. Unless otherwise noted, all time-based fields
 * are milliseconds since epoch and can be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link Uri} are generated using stronger
 * {@link String} identifiers, instead of {@code int} {@link BaseColumns#_ID} values, which are prone to
 * shuffle during sync. */
public class DiscussionsContract {

	/** A domain name for the {@link DeliveryProvider} */
	public static final String CONTENT_AUTHORITY = "com.slobodastudio.delivery";
	/** Special value for {@link Tasks#TASK_STATE} indicating that a task finished with a fail. */
	public static final long STATE_FINISHED_FAIL = 3;
	/** Special value for {@link Tasks#TASK_STATE} indicating that a task finished correctly. */
	public static final long STATE_FINISHED_OK = 2;
	/** Special value for {@link Tasks#TASK_STATE} indicating that a task is started. */
	public static final long STATE_STARTED = 1;
	/** Special value for {@link Tasks#TASK_STATE} indicating that a task is not started. */
	public static final long STATE_WAIT = 0;
	private static final String PATH_LOCATIONS = "locations";
	private static final String PATH_POINTS = "points";
	private static final String PATH_TASKS = "tasks";
	static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	/** A private Constructor prevents class from instantiating. */
	private DiscussionsContract() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	/** Describes location's table. Each location is associated with a specific {@link Tasks}. */
	public static class Locations implements LocationsColumns, BaseColumns {

		/** The MIME type of {@link #CONTENT_URI} providing a directory of locations */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.delivery.location";
		/** The MIME type of {@link #CONTENT_URI} providing a single location */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.delivery.location";
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATIONS).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = LocationsColumns.LOCATION_TIMESTAMP + " DESC";

		/** A private Constructor prevents class from instantiating. */
		private Locations() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** Build {@link Uri} for requested location {@link #_ID}.
		 * 
		 * @param locationId
		 *            unique location identifier
		 * @return a Uri for the given id */
		public static Uri buildLocationsUri(long locationId) {

			return ContentUris.withAppendedId(CONTENT_URI, locationId);
		}

		/** Build {@link Uri} for requested location {@link #_ID}.
		 * 
		 * @param locationId
		 *            unique location identifier
		 * @return a Uri for the given id */
		public static Uri buildLocationsUri(String locationId) {

			return CONTENT_URI.buildUpon().appendPath(locationId).build();
		}

		/** Read {@link #_ID} from {@link Locations} {@link Uri}.
		 * 
		 * @param uri
		 *            a location uri that contains location id
		 * @return a unique identifier provided by location uri */
		public static String getLocationId(Uri uri) {

			return uri.getPathSegments().get(1);
		}
	}

	/** Describes point's table. Each point is associated with a specific {@link Tasks}. */
	public static class Points implements PointsColumns, BaseColumns {

		/** The MIME type of {@link #CONTENT_URI} providing a directory of points */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.delivery.point";
		/** The MIME type of {@link #CONTENT_URI} providing a single point */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.delivery.point";
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POINTS).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = PointsColumns.POINT_NAME + " ASC";

		/** A private Constructor prevents class from instantiating. */
		private Points() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** Build {@link Uri} for requested point {@link #_ID}.
		 * 
		 * @param pointsId
		 *            unique point identifier
		 * @return a Uri for the given id */
		public static Uri buildPointsUri(long pointsId) {

			return ContentUris.withAppendedId(CONTENT_URI, pointsId);
		}

		/** Build {@link Uri} for requested point {@link #_ID}.
		 * 
		 * @param pointsId
		 *            unique point identifier
		 * @return a Uri for the given id */
		public static Uri buildPointsUri(String pointsId) {

			return CONTENT_URI.buildUpon().appendPath(pointsId).build();
		}

		/** Read {@link #_ID} from {@link Points} {@link Uri}.
		 * 
		 * @param uri
		 *            a point uri that contains point id
		 * @return a unique identifier provided by point uri */
		public static String getPointId(Uri uri) {

			return uri.getPathSegments().get(1);
		}
	}

	/** Describes task's table. */
	public static class Tasks implements TasksColumns, BaseColumns {

		/** The MIME type of {@link #CONTENT_URI} providing a directory of tasks */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.delivery.task";
		/** The MIME type of {@link #CONTENT_URI} providing a single task */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.delivery.task";
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = TasksColumns.TASK_DATE + " DESC";

		/** A private Constructor prevents class from instantiating. */
		private Tasks() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** Build {@link Uri} for requested task {@link #_ID}.
		 * 
		 * @param taskId
		 *            unique task identifier
		 * @return a Uri for the given id */
		public static Uri buildTasksUri(long taskId) {

			return ContentUris.withAppendedId(CONTENT_URI, taskId);
		}

		/** Build {@link Uri} for requested task {@link #_ID}.
		 * 
		 * @param taskId
		 *            unique task identifier
		 * @return a Uri for the given id */
		public static Uri buildTasksUri(String taskId) {

			return CONTENT_URI.buildUpon().appendPath(taskId).build();
		}

		/** Read {@link #_ID} from {@link Tasks} {@link Uri}.
		 * 
		 * @param uri
		 *            a task uri that contains task's id
		 * @return a unique identifier provided by task uri */
		public static String getTaskId(Uri uri) {

			return uri.getPathSegments().get(1);
		}
	}

	interface LocationsColumns {

		/** Point latitude. in double */
		String LOCATION_LATITUDE = "location_latitude";
		/** Point longitude. in double */
		String LOCATION_LONGITUDE = "location_longitude";
		/** Time when location received. in milliseconds */
		String LOCATION_TIMESTAMP = "location_timestamp";
		/** {@link Tasks#_ID} that this location belongs to. */
		String TASK_ID = "task_id";
	}

	interface PointsColumns {

		/** Point address. in String */
		String POINT_ADDRESS = "point_address";
		/** Point confirmation. True if confirmation required, false otherwise. in boolean */
		String POINT_CONFIRMATION = "point_confirmation";
		/** Point description. in String */
		String POINT_DESCRIPTION = "point_description";
		/** Point latitude. in double */
		String POINT_LATITUDE = "point_latitude";
		/** Point longitude. in double */
		String POINT_LONGITUDE = "point_longitude";
		/** Point name. in String */
		String POINT_NAME = "point_name";
		/** {@link Tasks#_ID} that this point belongs to. */
		String TASK_ID = "task_id";
	}

	interface SyncColumns {

		/** Last time this entry was updated or synchronized. */
		String UPDATED = "updated";
	}

	interface TasksColumns {

		/** Date when task should be done. in milliseconds */
		String TASK_DATE = "task_date";
		/** Time when task was delivered. in milliseconds */
		String TASK_END = "task_end";
		/** Time when task was started. in milliseconds */
		String TASK_START = "task_start";
		/** Task state. in int */
		String TASK_STATE = "task_state";
		/** Title describes task. in String */
		String TASK_TITLE = "task_title";
	}
}
