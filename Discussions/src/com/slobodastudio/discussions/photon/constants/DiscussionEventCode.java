package com.slobodastudio.discussions.photon.constants;

import de.exitgames.client.photon.LiteEventCode;

public class DiscussionEventCode {

	/** some annotation was updated */
	public static final byte ANNOTATION_CHANGED = 6;
	/** arg.point changed */
	public static final byte ARG_POINT_CHANGED = 4;
	/** Someone expanded/collapsed badge. */
	public static final byte BADGE_EXPANSION_CHANGED = 1;
	/** Server notifies client about changed geometry. */
	public static final byte BADGE_GEOMETRY_CHANGED = 0;
	/** used for instant updates of global online list 1. in response to NotifyLeaveNotify server notifies
	 * clients for fast onlien list update 2. client joins lobby, not discussion room. */
	public static final byte INSTANT_USER_PLUS_MINUS = 5;
	public static final byte JOIN = LiteEventCode.Join;
	public static final byte LEAVE = LiteEventCode.Leave;
	/** Client notified server structure was changed, broadcast. */
	public static final byte STRUCTURE_CHANGED = 2;
	/** new user account created/deleted */
	public static final byte USER_ACC_PLUS_MINUS = 7;
	public static final byte USER_CURSOR_CHANGED = 3;

	/** A private Constructor prevents class from instantiating. */
	private DiscussionEventCode() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	public static String asString(final byte eventCode) {

		switch (eventCode) {
			case ANNOTATION_CHANGED:
				return "ANNOTATION_CHANGED";
			case ARG_POINT_CHANGED:
				return "ARG_POINT_CHANGED";
			case BADGE_EXPANSION_CHANGED:
				return "BADGE_EXPANSION_CHANGED";
			case BADGE_GEOMETRY_CHANGED:
				return "BADGE_GEOMETRY_CHANGED";
			case INSTANT_USER_PLUS_MINUS:
				return "INSTANT_USER_PLUS_MINUS";
			case JOIN:
				return "JOIN";
			case LEAVE:
				return "LEAVE";
			case STRUCTURE_CHANGED:
				return "STRUCTURE_CHANGED";
			case USER_ACC_PLUS_MINUS:
				return "USER_ACC_PLUS_MINUS";
			case USER_CURSOR_CHANGED:
				return "USER_CURSOR_CHANGED";
			default:
				throw new IllegalArgumentException("Inknown event code: " + eventCode);
		}
	}
}
