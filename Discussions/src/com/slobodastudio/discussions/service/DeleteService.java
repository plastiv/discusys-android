package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.photon.PhotonController.SyncResultReceiver;
import com.slobodastudio.discussions.service.ServiceHelper.OdataSyncResultReceiver;
import com.slobodastudio.discussions.ui.IntentAction;
import com.slobodastudio.discussions.utils.ConnectivityUtil;
import com.slobodastudio.discussions.utils.MyLog;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class DeleteService extends IntentService {

	public static final String EXTRA_PHOTON_RECEIVER = "intent.extra.key.PHOTON_RECEIVER";
	public static final String EXTRA_TYPE_ID = "intent.extra.key.EXTRA_TYPE_ID";
	public static final String EXTRA_VALUE_ID = "intent.extra.key.EXTRA_VALUE_ID";
	public static final int TYPE_DELETE_POINT = 0x0;
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = DeleteService.class.getSimpleName();

	public DeleteService() {

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

	@Override
	protected void onHandleIntent(final Intent intent) {

		if (!IntentAction.DELETE.equals(intent.getAction())) {
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
		if (!intent.hasExtra(EXTRA_VALUE_ID)) {
			throw new IllegalArgumentException("Service was started without extras: value id");
		}
		if (!intent.hasExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: status receiver");
		}
		final ResultReceiver receiver = intent
				.getParcelableExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER);
		boolean connected;
		if (ApplicationConstants.DEV_MODE) {
			connected = true;
		} else {
			connected = ConnectivityUtil.isNetworkConnected(this);
		}
		if (connected) {
			if (receiver != null) {
				receiver.send(OdataSyncResultReceiver.STATUS_RUNNING, Bundle.EMPTY);
			}
		} else {
			if (receiver != null) {
				final Bundle bundle = new Bundle();
				bundle.putString(Intent.EXTRA_TEXT, getString(R.string.text_error_network_off));
				receiver.send(OdataSyncResultReceiver.STATUS_ERROR, bundle);
			}
			stopSelf();
			return;
		}
		logd("[onHandleIntent] intent: " + intent.toString());
		try {
			switch (intent.getIntExtra(EXTRA_TYPE_ID, Integer.MIN_VALUE)) {
				case TYPE_DELETE_POINT:
					deletePoint(intent);
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

	private void deletePoint(final Intent intent) {

		int pointId = intent.getIntExtra(EXTRA_VALUE_ID, Integer.MIN_VALUE);
		logd("[deletePoint] point id: " + pointId);
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.deletePoint(pointId);
		// -1 special value to refresh current topics list
		notifyPhotonArgPointChanged((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER), -1);
		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(pointId) };
		getContentResolver().delete(Points.CONTENT_URI, where, args);
	}
}
