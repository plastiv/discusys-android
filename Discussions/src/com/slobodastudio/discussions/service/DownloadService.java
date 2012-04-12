package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.data.DataIoException;
import com.slobodastudio.discussions.data.ProviderTestData;
import com.slobodastudio.discussions.data.odata.OdataReadClient;
import com.slobodastudio.discussions.service.ServiceHelper.OdataSyncResultReceiver;
import com.slobodastudio.discussions.utils.MyLog;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.sun.jersey.api.client.ClientHandlerException;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class DownloadService extends IntentService {

	public static final String ACTION_DOWNLOAD = "com.slobodastudio.action.download";
	public static final String EXTRA_TYPE_ID = "intent.extra.key.EXTRA_TYPE_ID";
	public static final String EXTRA_VALUE_ID = "intent.extra.key.EXTRA_VALUE_ID";
	public static final int TYPE_ALL = 0x0;
	public static final int TYPE_DESCRIPTION_ITEM = 0x6;
	public static final int TYPE_DESCRIPTIONS = 0x5;
	public static final int TYPE_DISCUSSIONS = 0x3;
	public static final int TYPE_POINT = 0x1;
	public static final int TYPE_POINT_FROM_TOPIC = 0x2;
	public static final int TYPE_TOPICS = 0x4;
	public static final int TYPE_UPDATE_POINT = 0x7;
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = DownloadService.class.getSimpleName();

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
			case TYPE_DISCUSSIONS:
				return "discussions";
			case TYPE_TOPICS:
				return "topics";
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

		if (!intent.getAction().equals(ACTION_DOWNLOAD)) {
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
		final ResultReceiver receiver = intent
				.getParcelableExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER);
		if (receiver != null) {
			receiver.send(OdataSyncResultReceiver.STATUS_RUNNING, Bundle.EMPTY);
		}
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
				case TYPE_DISCUSSIONS:
					downloadDiscussions();
					break;
				case TYPE_TOPICS:
					downloadTopics();
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
			if (receiver != null) {
				final Bundle bundle = new Bundle();
				bundle.putString(Intent.EXTRA_TEXT,
						"Network error. Check if wifi is on and server is working.");
				receiver.send(OdataSyncResultReceiver.STATUS_ERROR, bundle);
			}
			stopSelf();
			return;
		} catch (DataIoException e) {
			MyLog.e(TAG, "[onHandleIntent] DataIoException. Intent action: " + intent.getAction(), e);
			if (receiver != null) {
				final Bundle bundle = new Bundle();
				bundle.putString(Intent.EXTRA_TEXT, "Data structure error while downloading "
						+ getTypeAsString(intent.getIntExtra(EXTRA_TYPE_ID, Integer.MIN_VALUE)));
				receiver.send(OdataSyncResultReceiver.STATUS_ERROR, bundle);
			}
			stopSelf();
			return;
		} catch (Exception e) {
			MyLog.e(TAG, "[onHandleIntent] sync error. Intent action: " + intent.getAction(), e);
			if (receiver != null) {
				// Pass back error to surface listener
				final Bundle bundle = new Bundle();
				bundle.putString(Intent.EXTRA_TEXT, e.toString());
				receiver.send(OdataSyncResultReceiver.STATUS_ERROR, bundle);
			}
			stopSelf();
			return;
		}
		logd("[onHandleIntent] sync finished");
		// Announce success to any surface listener
		if (receiver != null) {
			receiver.send(OdataSyncResultReceiver.STATUS_FINISHED, Bundle.EMPTY);
		}
	}

	private void downloadAll() {

		logd("[downloadAll] local: " + ApplicationConstants.PROVIDER_LOCAL);
		if (ApplicationConstants.PROVIDER_LOCAL) {
			ProviderTestData.deleteData(this);
			ProviderTestData.generateData(this);
		} else {
			OdataReadClient odataClient = new OdataReadClient(this);
			odataClient.refreshPersons();
			logd("[downloadAll] persons completed");
			odataClient.refreshDiscussions();
			logd("[downloadAll] discussions completed");
			odataClient.refreshTopics();
			logd("[downloadAll] topics completed");
			odataClient.refreshPoints();
			logd("[downloadAll] points completed");
			odataClient.refreshDescriptions();
			logd("[downloadAll] descriptions completed");
			odataClient.refreshComments();
			logd("[downloadAll] comments completed");
		}
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

	@Deprecated
	private void downloadDiscussions() {

		logd("[downloadDiscussions]");
		OdataReadClient odataClient = new OdataReadClient(this);
		// topic will download related discussions
		odataClient.refreshTopics();
		logd("[downloadTopics] topics and discussions completed");
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

	@Deprecated
	private void downloadTopics() {

		logd("[downloadTopics]");
		OdataReadClient odataClient = new OdataReadClient(this);
		odataClient.refreshTopics();
		logd("[downloadTopics] topics completed");
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
