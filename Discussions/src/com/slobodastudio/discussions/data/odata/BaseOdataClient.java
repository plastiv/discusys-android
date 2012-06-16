package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.data.PreferenceHelper;

import android.content.ContentResolver;
import android.content.Context;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class BaseOdataClient {

	protected final ODataConsumer mConsumer;
	protected final ContentResolver mContentResolver;
	protected final Context mContext;

	/** Sets service root uri by default to japan server */
	public BaseOdataClient(final Context context) {

		// FIXME: check if network is accessible
		// FIXME catch 404 errors from HTTP RESPONSE
		mContext = context;
		mConsumer = ODataJerseyConsumer.newBuilder(getOdataServerUrl()).setFormatType(FormatType.JSON)
				.build();
		if (ApplicationConstants.ODATA_DUMP_LOG) {
			ODataConsumer.dump.all(true);
		}
		mContentResolver = context.getContentResolver();
	}

	public BaseOdataClient(final String serviceRootUri, final Context context) {

		mConsumer = ODataJerseyConsumer.newBuilder(serviceRootUri).setFormatType(FormatType.JSON).build();
		mContext = context;
		mContentResolver = context.getContentResolver();
	}

	public void logServerMetaData() {

		ODataReportUtil.reportMetadata(mConsumer.getMetadata());
	}

	protected String getOdataServerUrl() {

		return PreferenceHelper.getOdataUrl(mContext);
	}
}
