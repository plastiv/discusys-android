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
public final class DiscussionsContract {

	/** A domain name for the {@link DiscussionsProvider} */
	public static final String CONTENT_AUTHORITY = "com.slobodastudio.discussions";
	static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	/** A private Constructor prevents class from instantiating. */
	private DiscussionsContract() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	/** Describes attachments's table. */
	public static final class Attachments {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "attachment";
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
		public static final String TABLE_NAME = "Attachment";

		/** A private Constructor prevents class from instantiating. */
		private Attachments() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** Build {@link Uri} that references any {@link Points} associated with the requested
		 * {@link Points.Columns#TOPIC_ID}.
		 * 
		 * @param valueId
		 *            foreign key value (from server, not primary key from database!) to fetch associated
		 *            table
		 * 
		 * @return a Uri for the given id */
		public static Uri buildPointUri(final int valueId) {

			return CONTENT_URI.buildUpon().appendPath(String.valueOf(valueId)).appendPath(
					Points.A_TABLE_PREFIX).build();
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

		public static final class AttachmentType {

			public static final int BMP = 3;
			public static final int GENERAL_WEB_LINK = 6;
			public static final int JPG = 1;
			public static final int NONE = 0;
			public static final int PDF = 4;
			public static final int PNG = 2;
			public static final int YOUTUBE = 5;
		}

		/** List of columns names. */
		public static final class Columns implements BaseColumns {

			/** Type byte[]. */
			public static final String DATA = "Data";
			/** Type Int32. Foreign key. Should be used only by servers data. */
			public static final String DISCUSSION_ID = "Discussion";
			/** Type Int32. Enum. */
			public static final String FORMAT = "Format";
			/** Type Int32. */
			public static final String ID = "Id";
			/** Type String. */
			public static final String LINK = "Link";
			/** Type String. */
			public static final String NAME = "Name";
			/** Type Int32. Foreign key. Should be used only by servers data. */
			public static final String PERSON_ID = "Person";
			/** Type Int32. Foreign key. Should be used only by servers data. */
			public static final String POINT_ID = "ArgPoint";
			/** Type byte[]. */
			public static final String THUMB = "Thumb";
			/** Type String. */
			public static final String TITLE = "Title";
			/** Type String. */
			public static final String VIDEO_EMBED_URL = "VideoEmbedURL";
			/** Type String. */
			public static final String VIDEO_LINK_URL = "VideoLinkURL";
			/** Type String. */
			public static final String VIDEO_THUMB_URL = "VideoThumbURL";
		}
	}

	/** Describes comment's table. Each comment is associated with a {@link Points} and {@link Persons}. */
	public static final class Comments {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "comment";
		/** The MIME type of {@link #CONTENT_URI} providing a directory of points */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The MIME type of {@link #CONTENT_URI} providing a single point */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(A_TABLE_PREFIX).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = Columns.TEXT + " ASC";
		/** Server's database table name */
		public static final String TABLE_NAME = "Comment";

		/** A private Constructor prevents class from instantiating. */
		private Comments() {

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
		public static final class Columns implements BaseColumns {

			/** Type Int32. */
			public static final String ID = "Id";
			/** Type Int32. Foreign key. */
			public static final String PERSON_ID = "Person";
			/** Type Int32. Foreign key. */
			public static final String POINT_ID = "Point";
			/** Type String. */
			public static final String TEXT = "Text";
		}

		/** {@link ScheduleContract} fields that are fully qualified with a specific parent table. Used when
		 * needed to work around SQL ambiguity. */
		static final class Qualified {

			static final String COMMENT_ID = TABLE_NAME + "." + Columns.ID;
		}
	}

	/** Describes description's table. Each description is associated with a {@link Points} or
	 * {@link Discussions}. */
	public static final class Descriptions {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "rich_text";
		/** The MIME type of {@link #CONTENT_URI} providing a directory of points */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The MIME type of {@link #CONTENT_URI} providing a single point */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(A_TABLE_PREFIX).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = Columns.TEXT + " ASC";
		/** Server's database table name */
		public static final String TABLE_NAME = "RichText";

		/** A private Constructor prevents class from instantiating. */
		private Descriptions() {

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
		public static final class Columns implements BaseColumns {

			/** Type Int32. Foreign key. */
			public static final String DISCUSSION_ID = "Discussion";
			/** Type Int32. */
			public static final String ID = "Id";
			/** Type Int32. Foreign key. */
			public static final String POINT_ID = "ArgPoint";
			/** Type String. */
			public static final String TEXT = "Text";
		}
	}

	/** Describes discussion's table. Discussion is an abstraction of room. */
	public static final class Discussions {

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
		private Discussions() {

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

		/** Build {@link Uri} that references any {@link Topics} associated with the requested
		 * {@link Columns#ID}.
		 * 
		 * @param discussionId
		 *            {@link Columns#ID} value (from server, not primary key from database!) to fetch
		 *            associated topics
		 * 
		 * @return a Uri for the given id */
		public static Uri buildTopicUri(final int discussionId) {

			return CONTENT_URI.buildUpon().appendPath(String.valueOf(discussionId)).appendPath(
					Topics.A_TABLE_PREFIX).build();
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
		public static final class Columns implements BaseColumns {

			/** Type Int32. */
			public static final String ID = "Id";
			/** Type String. */
			public static final String SUBJECT = "Subject";
		}

		/** {@link ScheduleContract} fields that are fully qualified with a specific parent table. Used when
		 * needed to work around SQL ambiguity. */
		static final class Qualified {

			static final String DISCUSSION_ID = TABLE_NAME + "." + Columns.ID;
		}
	}

	/** Describes group's table. */
	public static final class Group {

		/** Server's database table name */
		public static final String TABLE_NAME = "Group";

		/** A private Constructor prevents class from instantiating. */
		private Group() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** List of columns names. */
		public static final class Columns implements BaseColumns {

			/** Type Int32. */
			public static final String GROUP_ID = "Id";
			/** Type String. */
			public static final String NAME = "Name";
		}
	}

	/** Describes person's table. Basically users for discussions. */
	public static final class Persons {

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
		private Persons() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** Build {@link Uri} that references any {@link Discussions} associated with the requested
		 * {@link Persons}.
		 * 
		 * @param valueId
		 *            foreign key value (from server, not primary key from database!) to fetch associated
		 *            table
		 * 
		 * @return a Uri for the given id */
		public static Uri buildDiscussionUri(final long valueId) {

			return CONTENT_URI.buildUpon().appendPath(String.valueOf(valueId)).appendPath(
					Discussions.A_TABLE_PREFIX).build();
		}

		/** Build {@link Uri} that references any {@link Points} associated with the requested
		 * {@link Points.Columns#PERSON_ID}.
		 * 
		 * @param valueId
		 *            foreign key value (from server, not primary key from database!) to fetch associated
		 *            table
		 * 
		 * @return a Uri for the given id */
		public static Uri buildPointUri(final int valueId) {

			return CONTENT_URI.buildUpon().appendPath(String.valueOf(valueId)).appendPath(
					Points.A_TABLE_PREFIX).build();
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

		/** Build {@link Uri} that references any {@link Topics} associated with the requested
		 * {@link Points.Columns#PERSON_ID}.
		 * 
		 * @param valueId
		 *            foreign key value (from server, not primary key from database!) to fetch associated
		 *            table
		 * 
		 * @return a Uri for the given id */
		public static Uri buildTopicUri(final int valueId) {

			return CONTENT_URI.buildUpon().appendPath(String.valueOf(valueId)).appendPath(
					Topics.A_TABLE_PREFIX).build();
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
		public static final class Columns implements BaseColumns {

			/** Type Int32. */
			public static final String COLOR = "Color";
			/** Type String. */
			public static final String EMAIL = "Email";
			/** Type Int32. */
			public static final String ID = "Id";
			/** Type String. */
			public static final String NAME = "Name";
			/** Type Bit. */
			public static final String ONLINE = "Online";
			/** Type Int32. */
			public static final String ONLINE_DEVICE_TYPE = "OnlineDevType";
			/** Type Int32. */
			public static final String SEAT_ID = "SeatId";
			/** Type Int32. */
			public static final String SESSION_ID = "SessionId";
		}

		/** {@link ScheduleContract} fields that are fully qualified with a specific parent table. Used when
		 * needed to work around SQL ambiguity. */
		static final class Qualified {

			static final String PERSON_ID = TABLE_NAME + "." + Columns.ID;
		}
	}

	/** Internal Many-to-many relationship table between {@link Persons} and {@link Topics}. */
	public static final class PersonsTopics {

		public static final int DEF_PERSON_VALUE = Integer.MIN_VALUE;
		/** Server's database table name */
		static final String TABLE_NAME = Persons.TABLE_NAME + Topics.TABLE_NAME;

		/** A private Constructor prevents class from instantiating. */
		private PersonsTopics() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** List of columns names. */
		public static final class Columns implements BaseColumns {

			/** Type Int32. Foreign key. */
			public static final String PERSON_ID = Persons.A_TABLE_PREFIX + "_id";
			/** Type Int32. Foreign key. */
			public static final String TOPIC_ID = Topics.A_TABLE_PREFIX + "_id";
		}

		/** {@link ScheduleContract} fields that are fully qualified with a specific parent table. Used when
		 * needed to work around SQL ambiguity. */
		static final class Qualified {

			static final String PERSON_ID = TABLE_NAME + "." + Columns.PERSON_ID;
		}
	}

	/** Describes point's table. Each point is associated with a {@link Topics}, {@link Persons}, {@link Group}
	 * . */
	public static final class Points {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "point";
		public static final Uri CONTENT_AND_PERSON_URI = BASE_CONTENT_URI.buildUpon().appendPath(
				A_TABLE_PREFIX + "," + Persons.A_TABLE_PREFIX).build();
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
		public static final String TABLE_NAME = "ArgPoint";

		/** A private Constructor prevents class from instantiating. */
		private Points() {

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

		/** Agreements codes constants. */
		public static final class ArgreementCode {

			public static final int AGREED = 1;
			public static final int AGREED_GROUPED = 4;
			public static final int DISAGREED = 2;
			public static final int DISAGREED_GROUPED = 5;
			public static final int UNSOLVED = 0;
			public static final int UNSOLVED_GROUPED = 3;
		}

		/** List of columns names. */
		public static final class Columns implements BaseColumns, SyncColumns {

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
			/** Type Int32. */
			public static final String ID = "Id";
			/** Type String. */
			public static final String NAME = "Point";
			/** Type String. */
			public static final String NUMBERED_POINT = "NumberedPoint";
			/** Type Int32. */
			public static final String PERSON_ID = "Person";
			/** Type String. */
			public static final String RECENTLY_ENTERED_MEDIA_URL = "RecentlyEnteredMediaUrl";
			/** Type String. */
			public static final String RECENTLY_ENTERED_SOURCE = "RecentlyEnteredSource";
			/** Type Boolean. */
			public static final String SHARED_TO_PUBLIC = "SharedToPublic";
			/** Type Int32. */
			public static final String SIDE_CODE = "SideCode";
			/** Type Int32. */
			public static final String TOPIC_ID = "Topic";
		}

		/** Side codes constants. */
		public static final class SideCode {

			public static final int CONS = 2;
			public static final int NEUTRAL = 0;
			public static final int PROS = 1;
		}

		/** {@link ScheduleContract} fields that are fully qualified with a specific parent table. Used when
		 * needed to work around SQL ambiguity. */
		static final class Qualified {

			static final String POINT_ID = TABLE_NAME + "." + Columns.ID;
		}
	}

	/** Describes seat's table. */
	public static final class Seats {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "seat";
		/** The MIME type of {@link #CONTENT_URI} providing a directory of tuples */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The MIME type of {@link #CONTENT_URI} providing a single tuple */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(A_TABLE_PREFIX).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = Columns.NAME + " ASC";
		/** Server's database table name */
		public static final String TABLE_NAME = "Seat";

		/** A private Constructor prevents class from instantiating. */
		private Seats() {

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
		public static final class Columns implements BaseColumns {

			/** Type Int32. */
			public static final String COLOR = "Color";
			/** Type Int32. */
			public static final String ID = "Id";
			/** Type String. */
			public static final String NAME = "SeatName";
		}

		/** {@link ScheduleContract} fields that are fully qualified with a specific parent table. Used when
		 * needed to work around SQL ambiguity. */
		static final class Qualified {

			static final String SEAT_ID = TABLE_NAME + "." + Columns.ID;
		}
	}

	/** Describes session's table. */
	public static final class Sessions {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "session";
		/** The MIME type of {@link #CONTENT_URI} providing a directory of tuples */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The MIME type of {@link #CONTENT_URI} providing a single tuple */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(A_TABLE_PREFIX).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = Columns.NAME + " ASC";
		/** Server's database table name */
		public static final String TABLE_NAME = "Session";

		/** A private Constructor prevents class from instantiating. */
		private Sessions() {

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
		public static final class Columns implements BaseColumns {

			/** Type DateTime. Format YYYY-MM-DDTHH:MM:SS.SSS */
			public static final String ESTIMATED_DATA_TIME = "EstimatedDateTime";
			/** Type Int32. Fixed set of values: 0, 1, 2 */
			public static final String ESTIMATED_TIME_SLOT = "EstimatedTimeSlot";
			/** Type Int32. */
			public static final String ID = "Id";
			/** Type String. */
			public static final String NAME = "Name";
			/** Type Boolean. */
			public static final String RUNNING = "Running";
		}

		/** {@link ScheduleContract} fields that are fully qualified with a specific parent table. Used when
		 * needed to work around SQL ambiguity. */
		static final class Qualified {

			static final String SESSION_ID = TABLE_NAME + "." + Columns.ID;
		}
	}

	/** Describes sources's table. */
	public static final class Sources {

		/** Table name in lower case. */
		public static final String A_TABLE_PREFIX = "source";
		/** The MIME type of {@link #CONTENT_URI} providing a directory of tuples */
		public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The MIME type of {@link #CONTENT_URI} providing a single tuple */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.discussions."
				+ A_TABLE_PREFIX;
		/** The content:// style URL for this table */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(A_TABLE_PREFIX).build();
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = Columns.LINK + " ASC";
		/** Server's database table name */
		public static final String TABLE_NAME = "Source";

		/** A private Constructor prevents class from instantiating. */
		private Sources() {

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
		public static final class Columns implements BaseColumns {

			/** Type Int32. */
			public static final String DESCRIPTION_ID = "RichText";
			/** Type Int32. */
			public static final String ID = "Id";
			/** Type String. */
			public static final String LINK = "Text";
		}

		/** {@link ScheduleContract} fields that are fully qualified with a specific parent table. Used when
		 * needed to work around SQL ambiguity. */
		static final class Qualified {

			static final String SEAT_ID = TABLE_NAME + "." + Columns.ID;
		}
	}

	/** Describes topic's table. Each topic is associated with a {@link Discussions}. */
	public static final class Topics {

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
		private Topics() {

			throw new UnsupportedOperationException("Class is prevented from instantiation");
		}

		/** Build {@link Uri} that references any {@link Points} associated with the requested
		 * {@link Points.Columns#TOPIC_ID}.
		 * 
		 * @param valueId
		 *            foreign key value (from server, not primary key from database!) to fetch associated
		 *            table
		 * 
		 * @return a Uri for the given id */
		public static Uri buildPointUri(final int valueId) {

			return CONTENT_URI.buildUpon().appendPath(String.valueOf(valueId)).appendPath(
					Points.A_TABLE_PREFIX).build();
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
		public static final class Columns implements BaseColumns {

			/** Type Int32. Foreign key. */
			public static final String DISCUSSION_ID = "Discussion";
			/** Type Int32. */
			public static final String ID = "Id";
			/** Type String. */
			public static final String NAME = "Name";
			/** Type Int32. Should be used only by servers data. */
			public static final String PERSON_ID = "Person";
			/** Type Int32. Should be used only by servers data. */
			public static final String POINT_ID = "ArgPoint";
		}

		/** {@link ScheduleContract} fields that are fully qualified with a specific parent table. Used when
		 * needed to work around SQL ambiguity. */
		static final class Qualified {

			static final String TOPIC_ID = TABLE_NAME + "." + Columns.ID;
		}
	}

	interface SyncColumns {

		/** Type boolean. true - need to be upload to server. */
		public static final String SYNC = "sync_column";
	}
}
