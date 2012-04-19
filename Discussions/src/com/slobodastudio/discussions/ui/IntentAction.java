package com.slobodastudio.discussions.ui;

public class IntentAction {

	public static final String DELETE = "com.slobodastudio.intent.action.DELETE";
	public static final String DOWNLOAD = "com.slobodastudio.intent.action.DOWNLOAD";
	public static final String NEW = "com.slobodastudio.intent.action.NEW";
	public static final String UPLOAD = "com.slobodastudio.intent.action.UPLOAD";

	/** A private Constructor prevents class from instantiating. */
	private IntentAction() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}
}
