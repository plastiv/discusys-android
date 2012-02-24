package com.slobodastudio.discussions.photon;

public class DiscussionEventCode {

	/** Someone expanded/collapsed badge. */
	public static final byte BADGE_EXPANSION_CHANGED = 1;
	/** Server notifies client about changed geometry. */
	public static final byte BADGE_GEOMETRY_CHANGED = 0;
	/** Client notified server structure was changed, broadcast. */
	public static final byte STRUCTURE_CHANGED = 2;
	public static final byte USER_CURSOR_CHANGED = 3;
}
