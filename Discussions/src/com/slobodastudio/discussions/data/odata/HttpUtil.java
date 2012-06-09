/*
 * Copyright 2012 sloboda-studio.com
 */
package com.slobodastudio.discussions.data.odata;

import com.slobodastudio.discussions.data.PreferenceHelper;

import android.content.Context;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class HttpUtil {

	private static HttpClient customHttpClient;

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
		HttpClient httpClient = getHttpClient();
		try {
			HttpResponse response = httpClient.execute(httpGet);
			return EntityUtils.toString(response.getEntity());
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void insertAttachment(final Context context) {

		try {
			HttpPost postRequest = new HttpPost(PreferenceHelper.getOdataUrl(context) + "Attachment");
			// StringEntity entity = new StringEntity("{uri:\"" + ODataConstants.SERVICE_URL + "Person(" + 1
			// + ")\"}", "utf-8");
			// FileEntity entity = new FileEntity(new File("/data/data/com.slobodastudio.discussions/files",
			// "odata.jpg"), "image/jpeg");
			InputStreamEntity entity = new InputStreamEntity(context.getAssets().open("odata.jpg"), context
					.getAssets().openFd("odata.jpg").getLength());
			Header[] headers = {
					new BasicHeader("Content-type", "image/jpeg"),
					new BasicHeader("Accept-Charset", "UTF-8"),
					new BasicHeader("DataServiceVersion", "1.0;NetFx"),
					new BasicHeader("MaxDataServiceVersion", "2.0;NetFx"),
					new BasicHeader("Accept", "application/json;odata=verbose"),
					new BasicHeader("Slug", "odata.jpg"),
					new BasicHeader("Host", "localhost"),
					new BasicHeader("Expect", "100-continue"),
					new BasicHeader("User-Agent",
							"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2") };
			entity.setContentType("image/jpeg");
			postRequest.setHeaders(headers);
			postRequest.setEntity(entity);
			HttpClient httpClient = getHttpClient();
			for (Header header : postRequest.getAllHeaders()) {
				Log.d("HttpRequest", header.getName() + ": " + header.getValue());
			}
			HttpResponse response = httpClient.execute(postRequest);
			for (Header header : response.getAllHeaders()) {
				Log.d("HttpResponse", header.getName() + ": " + header.getValue());
			}
			Log.d("HttpResponse", "Entity not null: " + (response.getEntity() != null));
			String responseString = EntityUtils.toString(response.getEntity());
			if (responseString != null) {
				try {
					JSONObject dValue = (JSONObject) new JSONObject(responseString).get("d");
					Integer attachmentId = (Integer) dValue.get("Id");
					Log.d("HttpResponse", "AttachmentId: " + attachmentId);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.d("HttpResponse", "HttpResponse: " + responseString);
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

	@Override
	public Object clone() throws CloneNotSupportedException {

		throw new CloneNotSupportedException();
	}
}
