package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.DataIoException;
import com.slobodastudio.discussions.data.PreferenceHelper;
import com.slobodastudio.discussions.data.SharedPreferenceHelper;
import com.slobodastudio.discussions.data.odata.HttpUtil;
import com.slobodastudio.discussions.data.odata.OdataReadClientWithBatchTransactions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Seats;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sessions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sources;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.ui.IntentAction;
import com.slobodastudio.discussions.utils.ConnectivityUtil;
import com.slobodastudio.discussions.utils.MyLog;
import com.slobodastudio.discussions.utils.lazylist.ImageLoader;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.sun.jersey.api.client.ClientHandlerException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class DownloadService extends IntentService {

	public static final int TYPE_ALL = 0x0;
	public static final int TYPE_POINT_FROM_TOPIC = 0x2;
	public static final int TYPE_UPDATE_POINT = 0x7;
	public static final int TYPE_PDF_FILE = 0x8;
	private static final boolean DEBUG = true && ApplicationConstants.LOGD_SERVICE;
	private static final String TAG = DownloadService.class.getSimpleName();
	private ResultReceiver activityReceiver;

	public DownloadService() {

		super(TAG);
	}

	private static ResultReceiver getActivityReceiverFromExtra(final Intent intent) {

		return intent.getParcelableExtra(ServiceExtraKeys.ACTIVITY_RECEIVER);
	}

	private static final String getTypeAsString(final int typeId) {

		switch (typeId) {
			case TYPE_ALL:
				return "ALL";
			case TYPE_UPDATE_POINT:
				return "updated point";
			case TYPE_POINT_FROM_TOPIC:
				return "points for topic";
			case TYPE_PDF_FILE:
				return "pdf files";
			default:
				throw new IllegalArgumentException("Illegal type id: " + typeId);
		}
	}

	private static int getTypeFromExtra(final Intent intent) {

		return intent.getIntExtra(ServiceExtraKeys.TYPE_ID, Integer.MIN_VALUE);
	}

	private static int getValueIdFromExtra(final Intent intent) {

		return intent.getIntExtra(ServiceExtraKeys.VALUE_ID, Integer.MIN_VALUE);
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	private static void validateIntent(final Intent intent) {

		if (!IntentAction.DOWNLOAD.equals(intent.getAction())) {
			throw new IllegalArgumentException("Service was started with unknown intent: "
					+ intent.getAction());
		}
		if (intent.getExtras() == null) {
			throw new IllegalArgumentException("Service was started without extras");
		}
		if (!intent.hasExtra(ServiceExtraKeys.ACTIVITY_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.ACTIVITY_RECEIVER);
		}
		if (!intent.hasExtra(ServiceExtraKeys.TYPE_ID)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.TYPE_ID);
		}
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		logd("[onHandleIntent] intent: " + intent.toString());
		validateIntent(intent);
		activityReceiver = getActivityReceiverFromExtra(intent);
		if (isConnected()) {
			ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_connecting), 0);
		} else {
			String errorString = getString(R.string.text_error_network_off);
			ActivityResultHelper.sendStatusError(activityReceiver, errorString);
			return;
		}
		try {
			switch (getTypeFromExtra(intent)) {
				case TYPE_ALL:
					downloadAll();
					break;
				case TYPE_UPDATE_POINT:
					updatePoint(intent);
					break;
				case TYPE_POINT_FROM_TOPIC:
					downloadPointsFromTopic(intent);
					break;
				case TYPE_PDF_FILE:
					downloadPdfFile(intent);
					break;
				default:
					throw new IllegalArgumentException("Illegal type id: " + getTypeFromExtra(intent));
			}
		} catch (ClientHandlerException e) {
			MyLog.e(TAG, "[onHandleIntent] ClientHandlerException. Intent action: " + intent.getAction(), e);
			String errorString = getString(R.string.text_error_client_handler);
			ActivityResultHelper.sendStatusError(activityReceiver, errorString);
			return;
		} catch (DataIoException e) {
			MyLog.e(TAG, "[onHandleIntent] DataIoException. Intent action: " + intent.getAction(), e);
			int downloadType = getTypeFromExtra(intent);
			String errorMsg = getString(R.string.text_error_database_io, getTypeAsString(downloadType));
			ActivityResultHelper.sendStatusError(activityReceiver, errorMsg);
			return;
		} catch (Exception e) {
			MyLog.e(TAG, "[onHandleIntent] sync error. Intent action: " + intent.getAction(), e);
			ActivityResultHelper.sendStatusError(activityReceiver, e.getMessage());
			return;
		}
		ActivityResultHelper.sendStatusFinished(activityReceiver);
		logd("[onHandleIntent] sync finished");
	}

	private void downloadAll() {

		logd("[downloadAll]");
		long startTime = System.currentTimeMillis();
		if (!testConnection()) {
			return;
		}
		// TODO: delete all rows in all tables, clean cache here
		// TODO: only download new (without delete in odata service)
		// TODO: progress download sessions
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_calculate_count), 0);
		new ImageLoader(getApplicationContext()).clearCache();
		int downloadedCount = 0;
		//
		int sessionCount = getTableCount(Sessions.TABLE_NAME);
		int seatsCount = getTableCount(Seats.TABLE_NAME);
		int personsCount = getTableCount(Persons.TABLE_NAME);
		int discussionsCount = getTableCount(Discussions.TABLE_NAME);
		int pointsCount = getTableCount(Points.TABLE_NAME);
		int topicsCount = getTableCount(Topics.TABLE_NAME);
		int attachmentsCount = getTableCount(Attachments.TABLE_NAME);
		int sourcesCount = getTableCount(Sources.TABLE_NAME);
		int descriptionCount = getTableCount(Descriptions.TABLE_NAME);
		int commentsCount = getTableCount(Comments.TABLE_NAME);
		int totalCount = sessionCount + seatsCount + personsCount + discussionsCount + pointsCount
				+ topicsCount + attachmentsCount + sourcesCount + descriptionCount + commentsCount;
		ActivityResultHelper.sendStatusStartWithCount(activityReceiver, totalCount);
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_sessions), downloadedCount);
		OdataReadClientWithBatchTransactions odataClient = new OdataReadClientWithBatchTransactions(this);
		//
		odataClient.refreshSessions();
		logd("[downloadAll] sessions completed");
		downloadedCount += sessionCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_seats),
				downloadedCount);
		//
		odataClient.refreshSeats();
		logd("[downloadAll] seats completed");
		downloadedCount += seatsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_persons),
				downloadedCount);
		//
		odataClient.refreshPersons();
		logd("[downloadAll] persons completed");
		downloadedCount += personsCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_discussions), downloadedCount);
		//
		odataClient.refreshDiscussions();
		logd("[downloadAll] discussions completed");
		downloadedCount += discussionsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_topics),
				downloadedCount);
		odataClient.refreshTopics();
		logd("[downloadAll] topics completed");
		downloadedCount += topicsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_points),
				downloadedCount);
		odataClient.refreshPoints();
		logd("[downloadAll] points completed");
		downloadedCount += pointsCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_descriptions), downloadedCount);
		odataClient.refreshDescriptions();
		logd("[downloadAll] descriptions completed");
		downloadedCount += descriptionCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_comments), downloadedCount);
		odataClient.refreshComments();
		logd("[downloadAll] comments completed");
		downloadedCount += commentsCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_attachments), downloadedCount);
		odataClient.refreshAttachments();
		logd("[downloadAll] attachments completed");
		downloadedCount += attachmentsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_sources),
				downloadedCount);
		odataClient.refreshSources();
		logd("[downloadAll] sources completed");
		odataClient.applyBatchOperations();
		downloadedCount += sourcesCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_finished), downloadedCount);
		logd("[downloadAll] load time: " + (System.currentTimeMillis() - startTime));
		SharedPreferenceHelper.setUpdatedTime(this, System.currentTimeMillis());
	}

	private void downloadPointsFromTopic(final Intent intent) {

		int topicId = getValueIdFromExtra(intent);
		if (topicId < 0) {
			throw new IllegalArgumentException("Illegal topic id for download points: " + topicId);
		}
		logd("[downloadPointsFromTopic] topic id: " + topicId);
		OdataReadClientWithBatchTransactions odata = new OdataReadClientWithBatchTransactions(this);
		odata.updateTopicPoints(topicId);
		odata.applyBatchOperations();
	}

	private Integer getTableCount(final String tableName) {

		String odataUrl = PreferenceHelper.getOdataUrl(this);
		String count = HttpUtil.getString(odataUrl + tableName + "/$count");
		if (TextUtils.isEmpty(count)) {
			return 0;
		}
		try {
			return Integer.valueOf(count);
		} catch (NumberFormatException e) {
			Log.e(TAG, "Failed to parse string as integer: " + count, e);
		}
		return 0;
	}

	private boolean isConnected() {

		boolean connected;
		if (ApplicationConstants.DEV_MODE) {
			connected = true;
		} else {
			connected = ConnectivityUtil.isNetworkConnected(this);
		}
		return connected;
	}

	private boolean testConnection() {

		String odataUrl = PreferenceHelper.getOdataUrl(this);
		HttpGet httpGet = new HttpGet(odataUrl);
		try {
			HttpUtil.getHttpClient().execute(httpGet);
			return true;
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Couldnt make a connection to: " + odataUrl, e);
			String errorString = getString(R.string.text_error_client_handler);
			ActivityResultHelper.sendStatusError(activityReceiver, errorString);
		} catch (IOException e) {
			Log.e(TAG, "Couldnt make a connection to: " + odataUrl, e);
			String errorString = getString(R.string.text_error_client_handler);
			ActivityResultHelper.sendStatusError(activityReceiver, errorString);
		}
		return false;
	}

	private void updatePoint(final Intent intent) {

		int pointId = getValueIdFromExtra(intent);
		if (pointId < 0) {
			throw new IllegalArgumentException("Illegal point id for download: " + pointId);
		}
		logd("[updatePoint] point id: " + pointId);
		OdataReadClientWithBatchTransactions odata = new OdataReadClientWithBatchTransactions(this);
		odata.updatePoint(pointId);
		odata.applyBatchOperations();
	}

	private void downloadPerSession(final Intent intent) {

		int sessionId = getValueIdFromExtra(intent);
		if (sessionId < 0) {
			throw new IllegalArgumentException("Illegal point id for download: " + sessionId);
		}
		logd("[updatePoint] point id: " + sessionId);
		OdataReadClientWithBatchTransactions odata = new OdataReadClientWithBatchTransactions(this);
		odata.updatePoint(sessionId);
		odata.applyBatchOperations();
	}

	private void downloadPdfFile(final Intent intent) {

		Uri uri = intent.getData();
		String pdfUrl = uri.getEncodedPath();
		logd("[downloadPdfFile] url: " + pdfUrl);
		String fileName = uri.getLastPathSegment();
		logd("[dowloadPdfFile] fileName: " + fileName);
		FileDownloader.downloadFromUrl(pdfUrl, fileName);
	}
}
