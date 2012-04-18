/*
 * Copyright 2012 sloboda-studio.com
 */
package com.slobodastudio.discussions.data.odata;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	public static void insertPersonTopic(final int personId, final int topicId) {

		try {
			HttpPost postRequest = new HttpPost(ODataConstants.SERVICE_URL + "Topic(" + topicId
					+ ")/$links/Person");
			StringEntity entity = new StringEntity("{uri:\"" + ODataConstants.SERVICE_URL + "Person("
					+ personId + ")\"}", "utf-8");
			entity.setContentType("application/json");
			postRequest.setEntity(entity);
			HttpClient httpClient = getHttpClient();
			HttpResponse response = httpClient.execute(postRequest);
			if (response.getEntity() != null) {
				Log.w("HttpResponse", "HttpResponse: " + getString(response));
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

	private static String getString(final HttpResponse httpResponse) {

		if (httpResponse == null) {
			throw new IllegalArgumentException("HttpResponse was passed as null");
		}
		InputStream inputStream;
		try {
			inputStream = httpResponse.getEntity().getContent();
		} catch (IllegalStateException e) {
			throw new RuntimeException("Failed to read InputStream from HttpResponse", e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read InputStream from HttpResponse", e);
		}
		// 8192 = 8k size buffer
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream), 8192);
		StringBuilder total = new StringBuilder();
		try {
			String line;
			while ((line = br.readLine()) != null) {
				total.append(line);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to read string from buffer reader", e);
		}
		return total.toString();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {

		throw new CloneNotSupportedException();
	}
}
