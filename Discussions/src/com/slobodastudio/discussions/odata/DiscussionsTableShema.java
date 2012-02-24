package com.slobodastudio.discussions.odata;

public class DiscussionsTableShema {

	public class Discussion {

		/** Type String. */
		public static final String SUBJECT = "Subject";
	}

	public class Person {

		/** Type Int32. */
		public static final String COLOR = "Color";
		/** Type String. */
		public static final String EMAIL = "Email";
		/** Type String. */
		public static final String NAME = "Name";
	}

	public class Point {

		/** Type Int32. */
		public static final String AGREEMENT_CODE = "AgreementCode";
		/** Type Binary. */
		public static final String DRAWING = "Drawing";
		/** Type Boolean. */
		public static final String EXPANDED = "Expanded";
		/** Type Int32. */
		public static final String GROUP_ID = "Group";
		/** Type String. */
		public static final String NUMBERED_POINT = "NumberedPoint";
		/** Type Int32. */
		public static final String PERSON_ID = "Person";
		/** Type String. */
		public static final String POINT = "Point";
		/** Type Boolean. */
		public static final String SHARED_TO_PUBLIC = "SharedToPublic";
		/** Type Int32. */
		public static final String SIDE_CODE = "SideCode";
		/** Type Int32. */
		public static final String TOPIC_ID = "Topic";
	}

	public class Tables {

		public static final String DISCUSSION = "Discussion";
		public static final String PERSON = "Person";
		public static final String POINT = "ArgPoint";
		public static final String TOPIC = "Topic";
	}

	public class Topic {

		/** Type Int32. */
		public static final String DISCUSSION_ID = "Discussion";
		/** Type String. */
		public static final String NAME = "Name";
		/** Type Int32. */
		public static final String PERSON_ID = "Person";
	}
}
