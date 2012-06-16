/*
 * Copyright 2012 sloboda-studio.com
 */
package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.PreferenceHelper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class HttpUtil {

	private static HttpClient customHttpClient;
	private static final String TAG = "HttpUtil";

	/** A private Constructor prevents class from instantiating. */
	private HttpUtil() throws UnsupportedOperationException {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	public static synchronized HttpClient getHttpClient() {

		if (customHttpClient == null) {
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams
					.setUserAgent(
							params,
							"Mozilla/5.0 (Linux; U; Android 2.1; en-us; ADR6200 Build/ERD79) AppleWebKit/530.17 (KHTML, like Gecko) Version/ 4.0 Mobile Safari/530.17");
			ConnManagerParams.setTimeout(params, 1000);
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 10000);
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
			customHttpClient = new DefaultHttpClient(conMgr, params);
		}
		return customHttpClient;
	}

	public static String getString(final String uri) {

		HttpGet httpGet = new HttpGet(uri);
		HttpResponse httpResponse = executeRequest(httpGet);
		return getHttpResonseAsString(httpResponse);
	}

	/** @return id of the inserted file in Attachment table */
	public static int insertAttachment(final Context context, final Uri attachmentUri) {

		FileEntity attachmentEntity = createAttachmentFileEntity(context, attachmentUri);
		HttpPost httpPost = createPostRequest(context, attachmentEntity);
		HttpResponse httpResponse = executeRequest(httpPost);
		String responseString = getHttpResonseAsString(httpResponse);
		return parseIdFromJson(responseString);
	}

	public static void insertPersonTopic(final Context context, final int personId, final int topicId) {

		try {
			String odataUrl = PreferenceHelper.getOdataUrl(context);
			HttpPost postRequest = new HttpPost(odataUrl + "Topic(" + topicId + ")/$links/Person");
			StringEntity entity = new StringEntity("{uri:\"" + odataUrl + "Person(" + personId + ")\"}",
					"utf-8");
			entity.setContentType("application/json");
			postRequest.setEntity(entity);
			HttpClient httpClient = getHttpClient();
			HttpResponse response = httpClient.execute(postRequest);
			if (response.getEntity() != null) {
				Log.w("HttpResponse", "HttpResponse: " + EntityUtils.toString(response.getEntity()));
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static FileEntity createAttachmentFileEntity(final Context context, final Uri imageUri) {

		String attachmentPath = getPathFromMediaStoreUri(context, imageUri);
		FileEntity entity = new FileEntity(new File(attachmentPath), "image/jpeg");
		entity.setContentType("image/jpeg");
		return entity;
	}

	private static Header[] createHeaders() {

		return new Header[] {
				new BasicHeader("Content-type", "image/jpeg"),
				new BasicHeader("Accept-Charset", "UTF-8"),
				new BasicHeader("DataServiceVersion", "1.0;NetFx"),
				new BasicHeader("MaxDataServiceVersion", "2.0;NetFx"),
				new BasicHeader("Accept", "application/json;odata=verbose"),
				new BasicHeader("Slug", "android.jpg"),
				new BasicHeader("Host", "localhost"),
				new BasicHeader("Expect", "100-continue"),
				new BasicHeader("User-Agent",
						"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2") };
	}

	private static HttpPost createPostRequest(final Context context, final HttpEntity httpEntity) {

		HttpPost postRequest = new HttpPost(PreferenceHelper.getOdataUrl(context) + "Attachment");
		postRequest.setHeaders(createHeaders());
		postRequest.setEntity(httpEntity);
		return postRequest;
	}

	private static HttpResponse executeRequest(final HttpUriRequest httpRequest) {

		if (httpRequest == null) {
			Log.e(TAG, "HttpUriRequest was null");
			return null;
		}
		HttpClient httpClient = getHttpClient();
		try {
			return httpClient.execute(httpRequest);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Failed to execute request: " + httpRequest.getURI(), e);
		} catch (IOException e) {
			Log.e(TAG, "Failed to execute request: " + httpRequest.getURI(), e);
		}
		return null;
	}

	private static String getHttpResonseAsString(final HttpResponse httpResponse) {

		if (httpResponse == null) {
			Log.e(TAG, "HttpResponse was null");
			return null;
		}
		if (httpResponse.getEntity() == null) {
			Log.e(TAG, "HttpResponse.getEntity was null");
			return null;
		}
		try {
			return EntityUtils.toString(httpResponse.getEntity());
		} catch (ParseException e) {
			Log.e(TAG, "Failed to convert http response to string", e);
		} catch (IOException e) {
			Log.e(TAG, "Failed to convert http response to string", e);
		}
		return null;
	}

	private static String getPathFromMediaStoreUri(final Context context, final Uri attachmentUri) {

		String[] filePathColumn = { MediaColumns.DATA };
		Cursor cursor = context.getContentResolver().query(attachmentUri, filePathColumn, null, null, null);
		String filePath;
		if (cursor.moveToFirst()) {
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			filePath = cursor.getString(columnIndex);
		} else {
			filePath = null;
		}
		cursor.close();
		return filePath;
	}

	private static int parseIdFromJson(final String jsonString) {

		try {
			JSONObject dValue = (JSONObject) new JSONObject(jsonString).get("d");
			return (Integer) dValue.get("Id");
		} catch (JSONException e) {
			Log.e(TAG, "Failed to parse Id from json: " + jsonString, e);
		}
		return -1;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {

		throw new CloneNotSupportedException();
	}
}
