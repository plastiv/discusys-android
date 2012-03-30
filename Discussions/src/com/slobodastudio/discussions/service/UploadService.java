package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.photon.PhotonService;
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
	public static final String EXTRA_PHOTON_RECEIVER = "intent.extra.key.PHOTON_RECEIVER";
	public static final String EXTRA_TYPE_ID = "intent.extra.key.EXTRA_TYPE_ID";
	public static final String EXTRA_VALUE = "intent.extra.key.EXTRA_VALUE";
	public static final int TYPE_INSERT_POINT = 0x0;
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
			bundle.putInt(PhotonService.EXTRA_POINT_ID, pointId);
			photonReceiver.send(PhotonService.STATUS_ARG_POINT_CHANGED, bundle);
		}
	}

	private static void notifyPhotonStructureChanged(final ResultReceiver photonReceiver, final int topicId) {

		logd("[notifyPhoton] changed topic id: " + topicId + ", photonReceiver: " + photonReceiver);
		if (photonReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putInt(PhotonService.EXTRA_TOPIC_ID, topicId);
			photonReceiver.send(PhotonService.STATUS_STRUCTURE_CHANGED, bundle);
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
				case TYPE_UPDATE_POINT:
					updatePoint(intent);
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
		}
		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(point.getId()) };
		getContentResolver().update(Points.CONTENT_URI, point.toContentValues(), where, args);
	}
}
