package com.slobodastudio.discussions.photon.constants;

public class DiscussionParameterKey {

	public static final byte AnnotationId = 19;
	// TODO: refact style to java of constants
	public static final byte ARG_POINT_ID = 11;
	public static final byte ArrayOfIds = 4;
	public static final byte ArrayOfOrientations = 3;
	public static final byte ArrayOfViewTypes = 12;
	public static final byte ArrayOfX = 1;
	public static final byte ArrayOfY = 2;
	public static final byte BadgeExpansionFlag = 10;
	public static final byte BoxHeight = 9;
	public static final byte BoxWidth = 8;
	public static final byte CHANGED_TOPIC_ID = 13;
	/** structure change notification not fired on initiating client. but when moderator dashboad is open, and
	 * moderator adds/removes topic, and private board is open, we want the list of topics in private board to
	 * be updated. */
	public static final byte ForceSelfNotification = 20;
	public static final byte GeometryChangedWithStruct = 14;
	public static final byte Message = 0;
	public static final byte NumArrayEntries = 5;
	public static final byte STRUCT_CHANGE_ACTOR_NR = 15;
	public static final byte UserCursorName = 6;
	public static final byte UserCursorState = 7;
	public static final byte UserCursorUsrId = 18;
	public static final byte UserCursorX = 16;
	public static final byte UserCursorY = 17;
}
