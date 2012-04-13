package com.slobodastudio.discussions.photon.constants;

import de.exitgames.client.photon.LiteOpCode;

public class DiscussionOperationCode {

	public static final byte GET_PROPERTIES = LiteOpCode.GetProperties;
	public static final byte JOIN = LiteOpCode.Join;
	public static final byte LEAVE = LiteOpCode.Leave;
	/** when user edits annotation and saves it, the client sends this, server broadcasts to other clients */
	public static final byte NOTIFY_ANNOTATION_UPDATED = 9;
	/** any attribute of given arg.point changed. replacement for NotifyStructureChanged when only single
	 * arg.point changed */
	public static final byte NOTIFY_ARGPOINT_CHANGED = 7;
	/** badge was expanded or collapsed */
	public static final byte NOTIFY_BADGE_EXPANSION_CHANGED = 4;
	/** client moved a badge and pushes changes to server; server broadcasts the changes to all clients in the
	 * room */
	public static final byte NOTIFY_BADGE_GEOMETRY_CHANGED = 2;
	/** server supports online status list across all discussions. when client disconnects, server detects
	 * disconnection and puts the user offline. it takes time. client can send this notification to inform
	 * server it's about to disconnect to speed up things. lite leave request doesn't work */
	public static final byte NOTIFY_LEAVE_USER = 8;
	/** when user changes his name after login (by clicking user name near avatar at top right of main form)
	 * other clients show previous name in online list. By sending this to server, client ensures that server
	 * broadcasts [acc plus minus] event to force all clients to update online list */
	public static final byte NOTIFY_NAME_CHANGED = 12;
	/** group/ungroup or drag/drop operation happened */
	public static final byte NOTIFY_STRUCTURE_CHANGED = 5;
	/** when moderator creates new user account or deletes existing one, the client sends this to server.
	 * server broadcasts to other clients, including the original client */
	public static final byte NOTIFY_USER_ACC_PLUS_MINUS = 10;
	/** client notifies its mouse cursor moved */
	public static final byte NOTIFY_USER_CURSOR_STATE = 6;
	/** client requests geometry of badges from server */
	public static final byte REQUEST_BADGE_GEOMETRY = 1;
	/** client asks server to refresh list of points (there are added/deleted points) */
	public static final byte REQUEST_SYNC_POINTS = 3;
	public static final byte STATS_EVENT = 11;
	public static final byte TEST = 0;

	/** A private Constructor prevents class from instantiating. */
	private DiscussionOperationCode() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	public static String asString(final byte operationCode) {

		switch (operationCode) {
			case GET_PROPERTIES:
				return "GET_PROPERTIES";
			case JOIN:
				return "JOIN";
			case LEAVE:
				return "LEAVE";
			case NOTIFY_ANNOTATION_UPDATED:
				return "NOTIFY_ANNOTATION_UPDATED";
			case NOTIFY_ARGPOINT_CHANGED:
				return "NOTIFY_ARGPOINT_CHANGED";
			case NOTIFY_BADGE_EXPANSION_CHANGED:
				return "NOTIFY_BADGE_EXPANSION_CHANGED";
			case NOTIFY_BADGE_GEOMETRY_CHANGED:
				return "NOTIFY_BADGE_GEOMETRY_CHANGED";
			case NOTIFY_LEAVE_USER:
				return "NOTIFY_LEAVE_USER";
			case NOTIFY_STRUCTURE_CHANGED:
				return "NOTIFY_STRUCTURE_CHANGED";
			case NOTIFY_USER_ACC_PLUS_MINUS:
				return "NOTIFY_USER_ACC_PLUS_MINUS";
			case NOTIFY_USER_CURSOR_STATE:
				return "NOTIFY_USER_CURSOR_STATE";
			case REQUEST_BADGE_GEOMETRY:
				return "REQUEST_BADGE_GEOMETRY";
			case REQUEST_SYNC_POINTS:
				return "REQUEST_SYNC_POINTS";
			case TEST:
				return "TEST";
			case STATS_EVENT:
				return "STATS_EVENT";
			default:
				throw new IllegalArgumentException("Inknown event code: " + operationCode);
		}
	}
}
