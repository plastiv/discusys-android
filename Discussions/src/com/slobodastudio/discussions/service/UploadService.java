package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.photon.PhotonController.SyncResultReceiver;
import com.slobodastudio.discussions.photon.constants.StatsType;
import com.slobodastudio.discussions.service.ServiceHelper.OdataSyncResultReceiver;
import com.slobodastudio.discussions.utils.MyLog;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.odata4j.core.OEntity;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class UploadService extends IntentService {

	public static final String ACTION_UPLOAD = "com.slobodastudio.action.upload";
	public static final String EXTRA_DISCUSSION_ID = "intent.extra.key.EXTRA_DISCUSSION_ID";
	public static final String EXTRA_PHOTON_RECEIVER = "intent.extra.key.PHOTON_RECEIVER";
	public static final String EXTRA_TYPE_ID = "intent.extra.key.EXTRA_TYPE_ID";
	public static final String EXTRA_VALUE = "intent.extra.key.EXTRA_VALUE";
	public static final int TYPE_INSERT_DESCRIPTION = 0x3;
	public static final int TYPE_INSERT_POINT = 0x0;
	public static final int TYPE_INSERT_POINT_AND_DESCRIPTION = 0x4;
	public static final int TYPE_UPDATE_DESCRIPTION = 0x2;
	public static final int TYPE_UPDATE_POINT = 0x1;
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = UploadService.class.getSimpleName();

	public UploadService() {

		super(TAG);
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	private static void notifyPhotonArgPointChanged(final ResultReceiver photonReceiver, final int pointId) {

		logd("[notifyPhoton] changed arg point id: " + pointId + ", photonReceiver: " + photonReceiver);
		if (photonReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putInt(SyncResultReceiver.EXTRA_POINT_ID, pointId);
			photonReceiver.send(SyncResultReceiver.STATUS_ARG_POINT_CHANGED, bundle);
		}
	}

	private static void notifyPhotonStatsEvent(final ResultReceiver photonReceiver, final int discussionId,
			final int userId, final int changedTopicId, final byte statsEventId) {

		logd("[notifyPhoton] discussion id: " + discussionId + ", user id: " + userId + ", topic id: "
				+ changedTopicId + ", event id: " + statsEventId + ", photonReceiver: " + photonReceiver);
		if (photonReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putInt(SyncResultReceiver.EXTRA_DISCUSSION_ID, discussionId);
			bundle.putInt(SyncResultReceiver.EXTRA_USER_ID, userId);
			bundle.putInt(SyncResultReceiver.EXTRA_TOPIC_ID, changedTopicId);
			bundle.putInt(SyncResultReceiver.EXTRA_EVENT_TYPE, statsEventId);
			photonReceiver.send(SyncResultReceiver.STATUS_EVENT_CHANGED, bundle);
		}
	}

	private static void notifyPhotonStructureChanged(final ResultReceiver photonReceiver, final int topicId) {

		logd("[notifyPhoton] changed topic id: " + topicId + ", photonReceiver: " + photonReceiver);
		if (photonReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putInt(SyncResultReceiver.EXTRA_TOPIC_ID, topicId);
			photonReceiver.send(SyncResultReceiver.STATUS_STRUCTURE_CHANGED, bundle);
		}
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		if (!intent.getAction().equals(ACTION_UPLOAD)) {
			throw new IllegalArgumentException("Service was started with unknown intent: "
					+ intent.getAction());
		}
		if (intent.getExtras() == null) {
			throw new IllegalArgumentException("Service was started without extras");
		}
		if (!intent.hasExtra(EXTRA_PHOTON_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: photon receiver");
		}
		if (!intent.hasExtra(EXTRA_TYPE_ID)) {
			throw new IllegalArgumentException("Service was started without extras: type id");
		}
		if (!intent.hasExtra(EXTRA_VALUE)) {
			throw new IllegalArgumentException("Service was started without extras: value");
		}
		if (!intent.hasExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: status receiver");
		}
		final ResultReceiver receiver = intent
				.getParcelableExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER);
		if (receiver != null) {
			receiver.send(OdataSyncResultReceiver.STATUS_RUNNING, Bundle.EMPTY);
		}
		logd("[onHandleIntent] intent: " + intent.toString());
		try {
			switch (intent.getIntExtra(EXTRA_TYPE_ID, Integer.MIN_VALUE)) {
				case TYPE_INSERT_POINT:
					insertPoint(intent);
					break;
				case TYPE_INSERT_POINT_AND_DESCRIPTION:
					insertPointAndDescription(intent);
					break;
				case TYPE_INSERT_DESCRIPTION:
					insertDescription(intent);
					break;
				case TYPE_UPDATE_POINT:
					updatePoint(intent);
					break;
				case TYPE_UPDATE_DESCRIPTION:
					updateDescription(intent);
					break;
				default:
					throw new IllegalArgumentException("Illegal type id: "
							+ intent.getIntExtra(EXTRA_TYPE_ID, Integer.MIN_VALUE));
			}
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

	private void insertDescription(final Intent intent) {

		Bundle descriptionBundle = intent.getBundleExtra(EXTRA_VALUE);
		Description description = new Description(descriptionBundle);
		logd("[insertDescription] " + description.toMyString());
		if (!ApplicationConstants.PROVIDER_LOCAL) {
			OdataWriteClient odataWrite = new OdataWriteClient();
			OEntity entity = odataWrite.insertDescription(description);
			int newId = (Integer) entity.getProperty(Descriptions.Columns.ID).getValue();
			logd("[insertDescription] new description id: " + newId);
			description.setId(newId);
		}
		getContentResolver().insert(Descriptions.CONTENT_URI, description.toContentValues());
	}

	@Deprecated
	private void insertPoint(final Intent intent) {

		Bundle pointBundle = intent.getBundleExtra(EXTRA_VALUE);
		Point point = new Point(pointBundle);
		logd("[insertPoint] " + point.toMyString());
		if (!ApplicationConstants.PROVIDER_LOCAL) {
			OdataWriteClient odataWrite = new OdataWriteClient(ODataConstants.SERVICE_URL);
			OEntity entity = odataWrite.insertPoint(point);
			int newPointId = (Integer) entity.getProperty(Points.Columns.ID).getValue();
			logd("[insertPoint] new point id: " + newPointId);
			point.setId(newPointId);
			notifyPhotonArgPointChanged((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
					point.getId());
		}
		getContentResolver().insert(Points.CONTENT_URI, point.toContentValues());
	}

	private void insertPointAndDescription(final Intent intent) {

		Bundle pointBundle = intent.getBundleExtra(EXTRA_VALUE);
		Point point = new Point(pointBundle);
		logd("[insertPoint] " + point.toMyString());// insert new description
		Bundle descriptionBundle = intent.getBundleExtra(EXTRA_VALUE);
		Description description = new Description(descriptionBundle);
		logd("[insertDescription] " + description.toMyString());
		if (!ApplicationConstants.PROVIDER_LOCAL) {
			OdataWriteClient odataWrite = new OdataWriteClient(ODataConstants.SERVICE_URL);
			OEntity entity = odataWrite.insertPoint(point);
			int newPointId = (Integer) entity.getProperty(Points.Columns.ID).getValue();
			logd("[insertPoint] new point id: " + newPointId);
			point.setId(newPointId);
			description.setPointId(newPointId);
			OEntity entityDesription = odataWrite.insertDescription(description);
			int newId = (Integer) entityDesription.getProperty(Descriptions.Columns.ID).getValue();
			logd("[insertDescription] new description id: " + newId);
			description.setId(newId);
			notifyPhotonArgPointChanged((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
					point.getId());
			if (!intent.hasExtra(EXTRA_DISCUSSION_ID)) {
				throw new IllegalArgumentException("[updatePoint] called without required discussion id");
			}
			int discussionId = intent.getIntExtra(EXTRA_DISCUSSION_ID, Integer.MIN_VALUE);
			notifyPhotonStatsEvent((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
					discussionId, point.getPersonId(), point.getTopicId(), StatsType.BADGE_CREATED);
		}
		getContentResolver().insert(Points.CONTENT_URI, point.toContentValues());
		getContentResolver().insert(Descriptions.CONTENT_URI, description.toContentValues());
	}

	private void updateDescription(final Intent intent) {

		Bundle descriptionBundle = intent.getBundleExtra(EXTRA_VALUE);
		Description description = new Description(descriptionBundle);
		logd("[updateDescription] " + description.toMyString());
		if (!ApplicationConstants.PROVIDER_LOCAL) {
			OdataWriteClient odataWrite = new OdataWriteClient();
			odataWrite.updateDescription(description);
		}
		String where = Descriptions.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(description.getId()) };
		getContentResolver().update(Descriptions.CONTENT_URI, description.toContentValues(), where, args);
	}

	private void updatePoint(final Intent intent) {

		Bundle pointBundle = intent.getBundleExtra(EXTRA_VALUE);
		Point point = new Point(pointBundle);
		logd("[updatePoint] " + point.toMyString());
		if (!ApplicationConstants.PROVIDER_LOCAL) {
			OdataWriteClient odataWrite = new OdataWriteClient(ODataConstants.SERVICE_URL);
			odataWrite.updatePoint(point);
			// TODO: send another event to photon
			notifyPhotonArgPointChanged((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
					point.getId());
			if (!intent.hasExtra(EXTRA_DISCUSSION_ID)) {
				throw new IllegalArgumentException("[updatePoint] called without required discussion id");
			}
			int discussionId = intent.getIntExtra(EXTRA_DISCUSSION_ID, Integer.MIN_VALUE);
			notifyPhotonStatsEvent((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
					discussionId, point.getPersonId(), point.getTopicId(), StatsType.BADGE_EDITED);
		}
		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(point.getId()) };
		getContentResolver().update(Points.CONTENT_URI, point.toContentValues(), where, args);
	}
}
