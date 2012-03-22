package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.data.ProviderTestData;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.odata.ODataConstants;
import com.slobodastudio.discussions.data.odata.OdataSyncService;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.utils.MyLog;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.odata4j.core.OEntity;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class SyncService extends IntentService {

	public static final String ACTION_DOWNLOAD = "com.slobodastudio.action.download";
	public static final String ACTION_INSERT = "com.slobodastudio.action.insert";
	public static final String ACTION_SYNC = "com.slobodastudio.action.sync";
	public static final String ACTION_UPDATE = "com.slobodastudio.action.update";
	public static final String EXTRA_PHOTON_RECEIVER = "intent.extra.key.PHOTON_RECEIVER";
	public static final String EXTRA_POINT_ID = "intent.extra.key.EXTRA_POINT_ID";
	public static final String EXTRA_STATUS_RECEIVER = "intent.extra.key.STATUS_RECEIVER";
	public static final String EXTRA_TOPIC_ID = "intent.extra.key.EXTRA_TOPIC_ID";
	public static final String EXTRA_URI = "intent.extra.key.EXTRA_URI";
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;
	public static final int STATUS_NOTIFICATION = 0x4;
	public static final int STATUS_RUNNING = 0x1;
	private static final boolean DEBUG = true && ApplicationConstants.DEBUG_MODE;
	private static final boolean LOCAL = false && ApplicationConstants.DEBUG_MODE;
	private static final String TAG = SyncService.class.getSimpleName();

	public SyncService() {

		super(TAG);
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	private static void notifyPhoton(final Intent intent) {

		final ResultReceiver photonReceiver = intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER);
		if (photonReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putInt(EXTRA_TOPIC_ID, intent.getIntExtra(Points.Columns.TOPIC_ID, Integer.MIN_VALUE));
			photonReceiver.send(STATUS_FINISHED, bundle);
		}
	}

	private static void sendNotification(final ResultReceiver receiver, final String message) {

		if (receiver != null) {
			// Pass back error to surface listener
			final Bundle bundle = new Bundle();
			bundle.putString(Intent.EXTRA_TEXT, message);
			receiver.send(STATUS_NOTIFICATION, bundle);
		}
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		if (receiver != null) {
			receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		}
		logd("[onHandleIntent] intent: " + intent.toString() + ", receiver: " + receiver);
		try {
			if (intent.getAction().equals(ACTION_SYNC)) {
				if (LOCAL) {
					ProviderTestData.deleteData(this);
					ProviderTestData.generateData(this);
				} else {
					ProviderTestData.deleteData(this);
					OdataSyncService service = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN, this);
					service.downloadAllValues();
				}
			} else if (intent.getAction().equals(ACTION_INSERT)) {
				if (intent.getExtras() == null) {
					throw new IllegalStateException("Intent does not have point value to insert");
				}
				if (LOCAL) {
					Point point = new Point(intent.getExtras());
					getContentResolver().insert(Points.CONTENT_URI, point.toContentValues());
				} else {
					OdataWriteClient odataWrite = new OdataWriteClient(ODataConstants.SERVICE_URL_JAPAN);
					OEntity insertedItem = odataWrite.insertPoint(new Point(intent.getExtras()));
					OdataSyncService odataSync = new OdataSyncService(ODataConstants.SERVICE_URL_JAPAN,
							getBaseContext());
					odataSync.insertPoint(insertedItem);
					notifyPhoton(intent);
				}
			} else if (intent.getAction().equals(ACTION_UPDATE)) {
				if (intent.getExtras() == null) {
					throw new IllegalStateException("Intent does not have point value to update");
				}
				Point point = new Point(intent.getExtras());
				if (!LOCAL) {
					OdataWriteClient odataWrite = new OdataWriteClient(ODataConstants.SERVICE_URL_JAPAN);
					odataWrite.updatePoint(point);
					notifyPhoton(intent);
				}
				String where = Points.Columns.ID + "=?";
				String[] args = new String[] { String.valueOf(point.getId()) };
				getContentResolver().update(Points.CONTENT_URI, point.toContentValues(), where, args);
			} else if (intent.getAction().equals(ACTION_DOWNLOAD)) {
				if (intent.getExtras() == null) {
					throw new IllegalStateException("Intent does not have point value to update");
				}
				if (intent.hasExtra(EXTRA_POINT_ID)) {
					int pointId = intent.getIntExtra(EXTRA_POINT_ID, -1);
					if (pointId != -1) {
						OdataSyncService odata = new OdataSyncService(this);
						odata.downloadPoint(pointId);
					}
				} else if (intent.hasExtra(EXTRA_TOPIC_ID)) {
					int topicId = intent.getIntExtra(EXTRA_TOPIC_ID, -1);
					if (topicId != -1) {
						OdataSyncService odata = new OdataSyncService(this);
						odata.downloadPoints(topicId);
					}
				} else {
					throw new IllegalArgumentException("Intent doesnt have needed extras: "
							+ intent.getExtras());
				}
			} else {
				throw new IllegalArgumentException("Unkknown action: " + intent.getAction());
			}
		} catch (Exception e) {
			MyLog.e(TAG, "[onHandleIntent] sync error. Intent action: " + intent.getAction(), e);
			if (receiver != null) {
				// Pass back error to surface listener
				final Bundle bundle = new Bundle();
				bundle.putString(Intent.EXTRA_TEXT, e.toString());
				receiver.send(STATUS_ERROR, bundle);
			}
			return;
		}
		logd("[onHandleIntent] sync finished");
		// Announce success to any surface listener
		if (receiver != null) {
			receiver.send(STATUS_FINISHED, Bundle.EMPTY);
		}
	}

	private void onDownloadAction(final Intent intent) {

		if (!intent.hasExtra(EXTRA_URI)) {
			throw new IllegalArgumentException("Intent doesnt have download uri in extras: "
					+ intent.getExtras());
		}
		Uri uri = intent.getParcelableExtra(EXTRA_URI);
		if (intent.hasExtra(EXTRA_URI)) {
			int pointId = intent.getIntExtra(EXTRA_POINT_ID, -1);
			if (pointId != -1) {
				OdataSyncService odata = new OdataSyncService(this);
				odata.downloadPoint(pointId);
			}
		} else if (intent.hasExtra(EXTRA_TOPIC_ID)) {
			int topicId = intent.getIntExtra(EXTRA_TOPIC_ID, -1);
			if (topicId != -1) {
				OdataSyncService odata = new OdataSyncService(this);
				odata.downloadPoints(topicId);
			}
		} else {
			throw new IllegalArgumentException("Intent doesnt have needed extras: " + intent.getExtras());
		}
	}
}
