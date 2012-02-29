package com.slobodastudio.discussions.photon.constants;

public class DiscussionOperationCode {

	public static final byte JoinFromJava = 8;
	// badge was expanded or collapsed
	public static final byte NotifyBadgeExpansionChanged = 4;
	// client moved a badge and pushes changes to server;
	// server broadcasts the changes to all clients in the room
	public static final byte NotifyBadgeGeometryChanged = 2;
	// group/ungroup or drag/drop operation happened
	public static final byte NotifyStructureChanged = 5;
	// client notifies its mouse cursor moved
	public static final byte NotifyUserCursorState = 6;
	// client requests geometry of badges from server
	public static final byte RequestBadgeGeometry = 1;
	// client asks server to refresh list of points (there are added/deleted points)
	public static final byte RequestSyncPoints = 3;
	public static final byte Test = 0;
}
