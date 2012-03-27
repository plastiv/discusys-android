package com.slobodastudio.discussions.data.odata;

import android.content.Context;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class BaseOdataClient {

	protected final ODataConsumer mConsumer;
	protected final Context mContext;

	/** Sets service root uri by default to japan server */
	public BaseOdataClient(final Context context) {

		// FIXME: check if network is accessible
		// TODO: set format type to json
		// FIXME catch 404 errors from HTTP RESPONSE
		mConsumer = ODataJerseyConsumer.newBuilder(ODataConstants.SERVICE_URL).setFormatType(FormatType.JSON)
				.build();
		this.mContext = context;
	}

	public BaseOdataClient(final String serviceRootUri, final Context context) {

		mConsumer = ODataJerseyConsumer.newBuilder(serviceRootUri).setFormatType(FormatType.JSON).build();
		this.mContext = context;
	}

	public void logServerMetaData() {

		ODataReportUtil.reportMetadata(mConsumer.getMetadata());
	}
}
