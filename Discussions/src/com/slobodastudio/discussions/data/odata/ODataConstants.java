package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.ApplicationConstants;

/** Helper class that consist of constants for open data protocol client SDK. */
public class ODataConstants {

	/** Server url with open data protocol service running. */
	public static final String SERVICE_URL = ApplicationConstants.ODATA_LOCAL ? "http://192.168.1.122/DiscSvc/discsvc.svc/"
			: "http://123.108.5.30/DiscSvc/discsvc.svc/";

	/** A private Constructor prevents class from instantiating. */
	private ODataConstants() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}
}
