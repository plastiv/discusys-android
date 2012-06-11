package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.DataIoException;
import com.slobodastudio.discussions.data.PreferenceHelper;
import com.slobodastudio.discussions.data.odata.HttpUtil;
import com.slobodastudio.discussions.data.odata.OdataReadClient;
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
import com.slobodastudio.discussions.service.ServiceHelper.OdataSyncResultReceiver;
import com.slobodastudio.discussions.ui.IntentAction;
import com.slobodastudio.discussions.utils.ConnectivityUtil;
import com.slobodastudio.discussions.utils.MyLog;
import com.slobodastudio.discussions.utils.fragmentasynctask.ResultCodes;
import com.slobodastudio.discussions.utils.lazylist.ImageLoader;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.sun.jersey.api.client.ClientHandlerException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class DownloadService extends IntentService {

	public static final String EXTRA_TYPE_ID = "intent.extra.key.EXTRA_TYPE_ID";
	public static final String EXTRA_VALUE_ID = "intent.extra.key.EXTRA_VALUE_ID";
	public static final int TYPE_ALL = 0x0;
	public static final int TYPE_DESCRIPTION_ITEM = 0x6;
	public static final int TYPE_DESCRIPTIONS = 0x5;
	public static final int TYPE_POINT = 0x1;
	public static final int TYPE_POINT_FROM_TOPIC = 0x2;
	public static final int TYPE_UPDATE_POINT = 0x7;
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = DownloadService.class.getSimpleName();
	ResultReceiver receiver;

	public DownloadService() {

		super(TAG);
	}

	private static final String getTypeAsString(final int typeId) {

		switch (typeId) {
			case TYPE_ALL:
				return "ALL";
			case TYPE_POINT:
				return "point";
			case TYPE_UPDATE_POINT:
				return "updated point";
			case TYPE_POINT_FROM_TOPIC:
				return "points for topic";
			case TYPE_DESCRIPTIONS:
				return "descriptions";
			case TYPE_DESCRIPTION_ITEM:
				return "single description";
			default:
				throw new IllegalArgumentException("Illegal type id: " + typeId);
		}
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	@Override
	public void onDestroy() {

		logd("[onDestroy]");
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		if (!IntentAction.DOWNLOAD.equals(intent.getAction())) {
			throw new IllegalArgumentException("Service was started with unknown intent: "
					+ intent.getAction());
		}
		if (intent.getExtras() == null) {
			throw new IllegalArgumentException("Service was started without extras");
		}
		if (!intent.hasExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: status receiver");
		}
		if (!intent.hasExtra(EXTRA_TYPE_ID)) {
			throw new IllegalArgumentException("Service was started without extras: type id");
		}
		receiver = intent.getParcelableExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER);
		if (!ConnectivityUtil.isNetworkConnected(this)) {
			publishError(getString(R.string.text_error_network_off));
			return;
		}
		publishProgress(getString(R.string.progress_connecting), 0);
		logd("[onHandleIntent] intent: " + intent.toString() + ", receiver: " + receiver);
		try {
			switch (intent.getIntExtra(EXTRA_TYPE_ID, Integer.MIN_VALUE)) {
				case TYPE_ALL:
					downloadAll();
					break;
				case TYPE_POINT:
					downloadPoint(intent);
					break;
				case TYPE_UPDATE_POINT:
					updatePoint(intent);
					break;
				case TYPE_POINT_FROM_TOPIC:
					downloadPointsFromTopic(intent);
					break;
				case TYPE_DESCRIPTIONS:
					downloadDescriptions();
					break;
				case TYPE_DESCRIPTION_ITEM:
					downloadDescription(intent);
					break;
				default:
					throw new IllegalArgumentException("Illegal type id: "
							+ intent.getIntExtra(EXTRA_TYPE_ID, Integer.MIN_VALUE));
			}
		} catch (ClientHandlerException e) {
			MyLog.e(TAG, "[onHandleIntent] ClientHandlerException. Intent action: " + intent.getAction(), e);
			publishError(getString(R.string.text_error_client_handler));
			return;
		} catch (DataIoException e) {
			MyLog.e(TAG, "[onHandleIntent] DataIoException. Intent action: " + intent.getAction(), e);
			int downloadType = intent.getIntExtra(EXTRA_TYPE_ID, Integer.MIN_VALUE);
			String errorMsg = getString(R.string.text_error_database_io, getTypeAsString(downloadType));
			publishError(errorMsg);
			return;
		} catch (Exception e) {
			MyLog.e(TAG, "[onHandleIntent] sync error. Intent action: " + intent.getAction(), e);
			publishError(e.getMessage());
			return;
		}
		logd("[onHandleIntent] sync finished");
		// Announce success to any surface listener
		if (receiver != null) {
			receiver.send(OdataSyncResultReceiver.STATUS_FINISHED, Bundle.EMPTY);
		}
	}

	private void downloadAll() {

		logd("[downloadAll]");
		if (!testConnection()) {
			return;
		}
		// TODO: delete all rows in all tables, clean cache here
		// TODO: only download new (without delete in odata service)
		// TODO: progress download sessions
		publishProgress(getString(R.string.progress_calculate_count), 0);
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
		publishMaxCount(totalCount);
		OdataReadClient odataClient = new OdataReadClient(this);
		//
		odataClient.refreshSessions();
		logd("[downloadAll] sessions completed");
		downloadedCount += sessionCount;
		publishProgress(getString(R.string.progress_downloading_seats), downloadedCount);
		//
		odataClient.refreshSeats();
		logd("[downloadAll] seats completed");
		downloadedCount += seatsCount;
		publishProgress(getString(R.string.progress_downloading_persons), downloadedCount);
		//
		odataClient.refreshPersons();
		logd("[downloadAll] persons completed");
		downloadedCount += personsCount;
		publishProgress(getString(R.string.progress_downloading_discussions), downloadedCount);
		//
		odataClient.refreshDiscussions();
		logd("[downloadAll] discussions completed");
		downloadedCount += discussionsCount;
		publishProgress(getString(R.string.progress_downloading_topics), downloadedCount);
		odataClient.refreshTopics();
		logd("[downloadAll] topics completed");
		downloadedCount += topicsCount;
		publishProgress(getString(R.string.progress_downloading_points), downloadedCount);
		odataClient.refreshPoints();
		logd("[downloadAll] points completed");
		downloadedCount += pointsCount;
		publishProgress(getString(R.string.progress_downloading_descriptions), downloadedCount);
		odataClient.refreshDescriptions();
		logd("[downloadAll] descriptions completed");
		downloadedCount += descriptionCount;
		publishProgress(getString(R.string.progress_downloading_comments), downloadedCount);
		odataClient.refreshComments();
		logd("[downloadAll] comments completed");
		downloadedCount += commentsCount;
		publishProgress(getString(R.string.progress_downloading_attachments), downloadedCount);
		odataClient.refreshAttachments();
		logd("[downloadAll] attachments completed");
		downloadedCount += attachmentsCount;
		publishProgress(getString(R.string.progress_downloading_sources), downloadedCount);
		odataClient.refreshSources();
		logd("[downloadAll] sources completed");
		downloadedCount += sourcesCount;
		publishProgress(getString(R.string.progress_downloading_finished), downloadedCount);
	}

	private void downloadDescription(final Intent intent) {

		int pointId = intent.getIntExtra(EXTRA_VALUE_ID, Integer.MIN_VALUE);
		if (pointId < 0) {
			throw new IllegalArgumentException("Illegal point id for download: " + pointId);
		}
		logd("[downloadDescription] point id: " + pointId);
		OdataReadClient odata = new OdataReadClient(this);
		odata.refreshDescription(pointId);
	}

	private void downloadDescriptions() {

		logd("[downloadDescriptions]");
		OdataReadClient odataClient = new OdataReadClient(this);
		odataClient.refreshDescriptions();
		logd("[downloadDescriptions] descriptions completed");
	}

	private void downloadPoint(final Intent intent) {

		int pointId = intent.getIntExtra(EXTRA_VALUE_ID, Integer.MIN_VALUE);
		if (pointId < 0) {
			throw new IllegalArgumentException("Illegal point id for download: " + pointId);
		}
		logd("[downloadPoint] point id: " + pointId);
		OdataReadClient odata = new OdataReadClient(this);
		odata.refreshPoint(pointId);
	}

	private void downloadPointsFromTopic(final Intent intent) {

		int topicId = intent.getIntExtra(EXTRA_VALUE_ID, Integer.MIN_VALUE);
		if (topicId < 0) {
			throw new IllegalArgumentException("Illegal topic id for download points: " + topicId);
		}
		logd("[downloadPointsFromTopic] topic id: " + topicId);
		OdataReadClient odata = new OdataReadClient(this);
		odata.updatePointsFromTopic(topicId);
	}

	private Integer getTableCount(final String tableName) {

		String odataUrl = PreferenceHelper.getOdataUrl(this);
		String count = HttpUtil.getString(odataUrl + tableName + "/$count");
		if (count == null) {
			return 0;
		}
		return Integer.valueOf(count);
	}

	private void publishError(final String errorMessage) {

		if (receiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putString(Intent.EXTRA_TEXT, errorMessage);
			receiver.send(OdataSyncResultReceiver.STATUS_ERROR, bundle);
		}
	}

	private void publishMaxCount(final int count) {

		if (receiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putInt("EXTRA_MAX_PROGRESS", count);
			receiver.send(ResultCodes.STATUS_STARTED, bundle);
		}
	}

	private void publishProgress(final String message, final int progress) {

		if (receiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putString(Intent.EXTRA_TEXT, message);
			bundle.putInt("EXTRA_RESULT_PROGRESS", progress);
			receiver.send(OdataSyncResultReceiver.STATUS_RUNNING, bundle);
		}
	}

	private boolean testConnection() {

		String odataUrl = PreferenceHelper.getOdataUrl(this);
		HttpGet httpGet = new HttpGet(odataUrl);
		try {
			HttpUtil.getHttpClient().execute(httpGet);
			return true;
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Couldnt make a connection to: " + odataUrl, e);
			publishError(getString(R.string.text_error_client_handler));
		} catch (IOException e) {
			Log.e(TAG, "Couldnt make a connection to: " + odataUrl, e);
			publishError(getString(R.string.text_error_client_handler));
		}
		return false;
	}

	private void updatePoint(final Intent intent) {

		int pointId = intent.getIntExtra(EXTRA_VALUE_ID, Integer.MIN_VALUE);
		if (pointId < 0) {
			throw new IllegalArgumentException("Illegal point id for download: " + pointId);
		}
		logd("[updatePoint] point id: " + pointId);
		OdataReadClient odata = new OdataReadClient(this);
		odata.updatePoint(pointId);
	}
}
