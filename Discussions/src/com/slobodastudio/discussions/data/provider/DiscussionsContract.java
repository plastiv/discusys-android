package com.slobodastudio.discussions.data.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/** Contract class for interacting with {@link DiscussionsProvider}. Unless otherwise noted, all time-based
 * fields are milliseconds since epoch and can be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link Uri} are generated using stronger
 * {@link String} identifiers, instead of {@code int} {@link BaseColumns#_ID} values, which are prone to
 * shuffle during sync. */
public class DiscussionsContract {

	/** A domain name for the {@link DiscussionsProvider} */
	public static final String CONTENT_AUTHORITY = "com.slobodastudio.discussions";
	static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	/** A private Constructor prevents class from instantiating. */
	private DiscussionsContract() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	/** Describes discussion's table. Discussion is an abstraction of room. */
	public static class Discussion {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "discussion";
		/** The MIME type of {@link #CONTENT_URI} providing a directory of points */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The MIME type of {@link #CONTENT_URI} providing a single point */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(A_TABLE_PREFIX).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = Columns.SUBJECT + " ASC";
		/** Server's database table name */
		public static final String TABLE_NAME = "Discussion";

		/** A private Constructor prevents class from instantiating. */
		private Discussion() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** Build {@link Uri} for requested {@link Columns#_ID}.
		 * 
		 * @param valueId
		 *            unique value identifier
		 * @return a Uri for the given id */
		public static Uri buildTableUri(final long valueId) {

			return ContentUris.withAppendedId(CONTENT_URI, valueId);
		}

		/** Build {@link Uri} for requested {@link Columns#_ID}.
		 * 
		 * @param valueId
		 *            unique row identifier
		 * @return a Uri for the given id */
		public static Uri buildTableUri(final String valueId) {

			return CONTENT_URI.buildUpon().appendPath(valueId).build();
		}

		/** Build {@link Uri} that references any {@link Topic} associated with the requested
		 * {@link Columns#DISCUSSION_ID}.
		 * 
		 * @param discussionId
		 *            {@link Columns#DISCUSSION_ID} value (from server, not primary key from database!) to
		 *            fetch associated topics
		 * 
		 * @return a Uri for the given id */
		public static Uri buildTopicUri(final String discussionId) {

			return CONTENT_URI.buildUpon().appendPath(discussionId).appendPath(Topic.A_TABLE_PREFIX).build();
		}

		/** Read {@link Columns#_ID} from this table {@link Uri}.
		 * 
		 * @param uri
		 *            a uri that contains value id
		 * @return a unique identifier provided by table uri */
		public static String getValueId(final Uri uri) {

			return uri.getPathSegments().get(1);
		}

		/** List of columns names. */
		public class Columns implements BaseColumns {

			/** Type Int32. */
			public static final String DISCUSSION_ID = "Id";
			/** Type String. */
			public static final String SUBJECT = "Subject";
		}
	}

	/** Describes group's table. */
	public static class Group {

		/** Server's database table name */
		public static final String TABLE_NAME = "Group";

		/** A private Constructor prevents class from instantiating. */
		private Group() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** List of columns names. */
		public class Columns implements BaseColumns {

			/** Type Int32. */
			public static final String GROUP_ID = "Id";
			/** Type String. */
			public static final String NAME = "Name";
		}
	}

	/** Describes person's table. Basically users for discussions. */
	public static class Person {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "person";
		/** The MIME type of {@link #CONTENT_URI} providing a directory of points */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The MIME type of {@link #CONTENT_URI} providing a single point */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(A_TABLE_PREFIX).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = Columns.NAME + " ASC";
		/** Server's database table name */
		public static final String TABLE_NAME = "Person";

		/** A private Constructor prevents class from instantiating. */
		private Person() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** Build {@link Uri} for requested {@link Columns#_ID}.
		 * 
		 * @param valueId
		 *            unique value identifier
		 * @return a Uri for the given id */
		public static Uri buildTableUri(final long valueId) {

			return ContentUris.withAppendedId(CONTENT_URI, valueId);
		}

		/** Build {@link Uri} for requested {@link Columns#_ID}.
		 * 
		 * @param valueId
		 *            unique row identifier
		 * @return a Uri for the given id */
		public static Uri buildTableUri(final String valueId) {

			return CONTENT_URI.buildUpon().appendPath(valueId).build();
		}

		/** Read {@link Columns#_ID} from this table {@link Uri}.
		 * 
		 * @param uri
		 *            a uri that contains value id
		 * @return a unique identifier provided by table uri */
		public static String getValueId(final Uri uri) {

			return uri.getPathSegments().get(1);
		}

		/** List of columns names. */
		public class Columns implements BaseColumns {

			/** Type Int32. */
			public static final String COLOR = "Color";
			/** Type String. */
			public static final String EMAIL = "Email";
			/** Type String. */
			public static final String NAME = "Name";
			/** Type Bit. */
			public static final String ONLINE = "Online";
			/** Type Int32. */
			public static final String PERSON_ID = "Id";
		}
	}

	/** Describes point's table. Each point is associated with a {@link Topic}, {@link Person}, {@link Group}. */
	public static class Point {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "point";
		/** The MIME type of {@link #CONTENT_URI} providing a directory of points */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The MIME type of {@link #CONTENT_URI} providing a single point */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(A_TABLE_PREFIX).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = Columns.POINT_NAME + " ASC";
		/** Server's database table name */
		public static final String TABLE_NAME = "ArgPoint";

		/** A private Constructor prevents class from instantiating. */
		private Point() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** Build {@link Uri} for requested {@link Columns#_ID}.
		 * 
		 * @param valueId
		 *            unique value identifier
		 * @return a Uri for the given id */
		public static Uri buildTableUri(final long valueId) {

			return ContentUris.withAppendedId(CONTENT_URI, valueId);
		}

		/** Build {@link Uri} for requested {@link Columns#_ID}.
		 * 
		 * @param valueId
		 *            unique row identifier
		 * @return a Uri for the given id */
		public static Uri buildTableUri(final String valueId) {

			return CONTENT_URI.buildUpon().appendPath(valueId).build();
		}

		/** Read {@link Columns#_ID} from this table {@link Uri}.
		 * 
		 * @param uri
		 *            a uri that contains value id
		 * @return a unique identifier provided by table uri */
		public static String getValueId(final Uri uri) {

			return uri.getPathSegments().get(1);
		}

		/** List of columns names. */
		public class Columns implements BaseColumns {

			/** Type Int32. 0-not agreed */
			public static final String AGREEMENT_CODE = "AgreementCode";
			/** Type Binary. */
			public static final String DRAWING = "Drawing";
			/** Type Boolean. */
			public static final String EXPANDED = "Expanded";
			/** Type Int32. Special value, because sqlite word Group is reserved. */
			public static final String GROUP_ID = "Group_id";
			/** Type Int32. */
			public static final String GROUP_ID_SERVER = "Group";
			/** Type String. */
			public static final String NUMBERED_POINT = "NumberedPoint";
			/** Type Int32. */
			public static final String PERSON_ID = "Person";
			/** Type Int32. */
			public static final String POINT_ID = "Id";
			/** Type String. */
			public static final String POINT_NAME = "Point";
			/** Type Boolean. */
			public static final String SHARED_TO_PUBLIC = "SharedToPublic";
			/** Type Int32. */
			public static final String SIDE_CODE = "SideCode";
			/** Type Int32. */
			public static final String TOPIC_ID = "Topic";
		}
	}

	/** Describes topic's table. Each topic is associated with a {@link Discussion}. */
	public static class Topic {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "topic";
		/** The MIME type of {@link #CONTENT_URI} providing a directory of points */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The MIME type of {@link #CONTENT_URI} providing a single point */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(A_TABLE_PREFIX).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = Columns.NAME + " ASC";
		/** Server's database table name */
		public static final String TABLE_NAME = "Topic";

		/** A private Constructor prevents class from instantiating. */
		private Topic() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** Build {@link Uri} for requested {@link Columns#_ID}.
		 * 
		 * @param valueId
		 *            unique value identifier
		 * @return a Uri for the given id */
		public static Uri buildTableUri(final long valueId) {

			return ContentUris.withAppendedId(CONTENT_URI, valueId);
		}

		/** Build {@link Uri} for requested {@link Columns#_ID}.
		 * 
		 * @param valueId
		 *            unique row identifier
		 * @return a Uri for the given id */
		public static Uri buildTableUri(final String valueId) {

			return CONTENT_URI.buildUpon().appendPath(valueId).build();
		}

		/** Read {@link Columns#_ID} from this table {@link Uri}.
		 * 
		 * @param uri
		 *            a uri that contains value id
		 * @return a unique identifier provided by table uri */
		public static String getValueId(final Uri uri) {

			return uri.getPathSegments().get(1);
		}

		/** List of columns names. */
		public class Columns implements BaseColumns {

			/** Type Int32. Foreign key. */
			public static final String DISCUSSION_ID = "Discussion";
			/** Type String. */
			public static final String NAME = "Name";
			/** Type Int32. */
			public static final String PERSON_ID = "Person";
			/** Type Int32. */
			public static final String TOPIC_ID = "Id";
		}
	}
}
