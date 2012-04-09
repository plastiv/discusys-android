package com.slobodastudio.discussions.photon.constants;

public class StatsType {

	public static final byte BADGE_CREATED = 2;
	public static final byte BADGE_EDITED = 3;
	public static final byte CLUSTER_CREATED = 4;
	public static final byte CLUSTER_EDITED = 5;
	public static final byte CLUSTER_REMOVED = 6;
	public static final byte DISCUSSION_SESSION_STARTED = 0;
	public static final byte DISCUSSION_SESSION_STOPPED = 1;
	public static final byte FREE_DRAWING_CREATED = 9;
	public static final byte FREE_DRAWING_REMOVED = 10;
	public static final byte LINK_CREATED = 7;
	public static final byte LINK_REMOVED = 8;

	/** A private Constructor prevents class from instantiating. */
	private StatsType() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	public static String asString(final byte statsType) {

		switch (statsType) {
			case DISCUSSION_SESSION_STARTED:
				return "DISCUSSION_SESSION_STARTED";
			case DISCUSSION_SESSION_STOPPED:
				return "DISCUSSION_SESSION_STOPPED";
			case BADGE_CREATED:
				return "BADGE_CREATED";
			case BADGE_EDITED:
				return "BADGE_EDITED";
			case CLUSTER_CREATED:
				return "CLUSTER_CREATED";
			case CLUSTER_EDITED:
				return "CLUSTER_EDITED";
			case CLUSTER_REMOVED:
				return "CLUSTER_REMOVED";
			case LINK_CREATED:
				return "LINK_CREATED";
			case LINK_REMOVED:
				return "LINK_REMOVED";
			case FREE_DRAWING_CREATED:
				return "FREE_DRAWING_CREATED";
			case FREE_DRAWING_REMOVED:
				return "FREE_DRAWING_REMOVED";
			default:
				throw new IllegalArgumentException("Inknown stats type :" + statsType);
		}
	}
}
