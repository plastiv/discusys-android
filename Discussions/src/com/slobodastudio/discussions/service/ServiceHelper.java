package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.photon.PhotonController;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

/** Should attach mOdataListener when activity is active. */
public class ServiceHelper {

	static final String TAG = ServiceHelper.class.getSimpleName();
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	OdataSyncResultListener mOdataListener;
	boolean mOdataSyncing = false;
	private final Context mContext;
	private final ResultReceiver mOdataResultReceiver = new OdataSyncResultReceiver(new Handler());
	private final PhotonController mPhotonController;

	public ServiceHelper(final Context context, final PhotonController photonController) {

		super();
		mContext = context;
		mPhotonController = photonController;
	}

	public void deletePoint(final int pointId) {

		Intent intent = new Intent(DeleteService.ACTION_DELETE);
		intent.putExtra(DeleteService.EXTRA_TYPE_ID, DeleteService.TYPE_DELETE_POINT);
		intent.putExtra(DeleteService.EXTRA_VALUE_ID, pointId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void downloadAll() {

		assertReady();
		final Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_ALL);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	public void downloadDescription(final int pointId) {

		assertReady();
		// TODO: make a queue of download requests
		Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_DESCRIPTION_ITEM);
		intent.putExtra(DownloadService.EXTRA_VALUE_ID, pointId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	public void downloadDescriptions() {

		assertReady();
		Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_DESCRIPTIONS);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	public void downloadDiscussions() {

		assertReady();
		Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_DISCUSSIONS);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	/** Start intent service to download point into local database.
	 * 
	 * @param pointId */
	public void downloadPoint(final int pointId) {

		assertReady();
		// TODO: make a queue of download requests
		Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_POINT);
		intent.putExtra(DownloadService.EXTRA_VALUE_ID, pointId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	public void downloadPointsFromTopic(final int topicId) {

		assertReady();
		Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_POINT_FROM_TOPIC);
		intent.putExtra(DownloadService.EXTRA_VALUE_ID, topicId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	@Deprecated
	public void downloadTopics() {

		assertReady();
		Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_TOPICS);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	public void insertDescription(final Bundle descriptionValues) {

		assertReady();
		Intent intent = new Intent(UploadService.ACTION_UPLOAD);
		intent.putExtra(UploadService.EXTRA_TYPE_ID, UploadService.TYPE_INSERT_DESCRIPTION);
		intent.putExtra(UploadService.EXTRA_VALUE, descriptionValues);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void insertPoint(final Bundle pointValue) {

		assertReady();
		Intent intent = new Intent(UploadService.ACTION_UPLOAD);
		intent.putExtra(UploadService.EXTRA_TYPE_ID, UploadService.TYPE_INSERT_POINT);
		intent.putExtra(UploadService.EXTRA_VALUE, pointValue);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void insertPointAndDescription(final Bundle values, final int discussionId) {

		assertReady();
		Intent intent = new Intent(UploadService.ACTION_UPLOAD);
		intent.putExtra(UploadService.EXTRA_TYPE_ID, UploadService.TYPE_INSERT_POINT_AND_DESCRIPTION);
		intent.putExtra(UploadService.EXTRA_VALUE, values);
		intent.putExtra(UploadService.EXTRA_DISCUSSION_ID, discussionId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public boolean isSyncing() {

		return mOdataSyncing;
	}

	public void setOdataListener(final OdataSyncResultListener odataListener) {

		mOdataListener = odataListener;
	}

	public void updateDescription(final Bundle descriptionValues) {

		assertReady();
		Intent intent = new Intent(UploadService.ACTION_UPLOAD);
		intent.putExtra(UploadService.EXTRA_TYPE_ID, UploadService.TYPE_UPDATE_DESCRIPTION);
		intent.putExtra(UploadService.EXTRA_VALUE, descriptionValues);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void updatePoint(final Bundle pointValue, final int discussionId) {

		assertReady();
		Intent intent = new Intent(UploadService.ACTION_UPLOAD);
		intent.putExtra(UploadService.EXTRA_TYPE_ID, UploadService.TYPE_UPDATE_POINT);
		intent.putExtra(UploadService.EXTRA_VALUE, pointValue);
		intent.putExtra(UploadService.EXTRA_DISCUSSION_ID, discussionId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void updatePoint(final int pointId) {

		assertReady();
		Intent intent = new Intent(DownloadService.ACTION_DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_UPDATE_POINT);
		intent.putExtra(DownloadService.EXTRA_VALUE_ID, pointId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	private void assertReady() {

		if (mOdataSyncing) {
			Toast.makeText(mContext, "Need to finish previous task", Toast.LENGTH_SHORT).show();
		}
	}

	public class OdataSyncResultReceiver extends ResultReceiver {

		public static final String EXTRA_STATUS_RECEIVER = "intent.extra.key.STATUS_RECEIVER";
		public static final int STATUS_ERROR = 0x2;
		public static final int STATUS_FINISHED = 0x3;
		public static final int STATUS_RUNNING = 0x1;

		public OdataSyncResultReceiver(final Handler handler) {

			super(handler);
		}

		@Override
		public void onReceiveResult(final int resultCode, final Bundle resultData) {

			if (DEBUG) {
				Log.d(TAG, "[onReceiveResult] resultCode: " + resultCode + ", resultData: " + resultData);
			}
			switch (resultCode) {
				case STATUS_RUNNING:
					mOdataSyncing = true;
					break;
				case STATUS_FINISHED:
					mOdataSyncing = false;
					break;
				case STATUS_ERROR:
					// Error happened down in SyncService, show as toast.
					mOdataSyncing = false;
					mOdataListener.handleError(resultData.getString(Intent.EXTRA_TEXT));
					break;
				default:
					throw new IllegalArgumentException("Unknown result code: " + resultCode);
			}
			if (mOdataListener != null) {
				mOdataListener.updateSyncStatus(mOdataSyncing);
			}
		}
	}
}
