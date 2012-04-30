package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.photon.PhotonController;
import com.slobodastudio.discussions.ui.IntentAction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/** Should attach mOdataListener when activity is active. */
public class ServiceHelper {

	static final String TAG = ServiceHelper.class.getSimpleName();
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	OdataSyncResultListener mOdataListener;
	boolean mOdataSyncing;
	private final Context mContext;
	private final ResultReceiver mOdataResultReceiver;
	private final PhotonController mPhotonController;

	public ServiceHelper(final Context context, final PhotonController photonController) {

		super();
		mContext = context;
		mPhotonController = photonController;
		// default values
		mOdataSyncing = false;
		mOdataResultReceiver = new OdataSyncResultReceiver(new Handler());
	}

	public void deleteComment(final int commentId, final int pointId, final int discussionId,
			final int topicId, final int personId) {

		Intent intent = new Intent(IntentAction.DELETE);
		intent.putExtra(DeleteService.EXTRA_TYPE_ID, DeleteService.TYPE_DELETE_COMMENT);
		intent.putExtra(DeleteService.EXTRA_VALUE_ID, commentId);
		intent.putExtra(DeleteService.EXTRA_POINT_ID, pointId);
		intent.putExtra(DeleteService.EXTRA_DISCUSSION_ID, discussionId);
		intent.putExtra(DeleteService.EXTRA_TOPIC_ID, topicId);
		intent.putExtra(DeleteService.EXTRA_PERSON_ID, personId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void deletePoint(final int pointId) {

		Intent intent = new Intent(IntentAction.DELETE);
		intent.putExtra(DeleteService.EXTRA_TYPE_ID, DeleteService.TYPE_DELETE_POINT);
		intent.putExtra(DeleteService.EXTRA_VALUE_ID, pointId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void downloadAll() {

		final Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_ALL);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	public void downloadDescription(final int pointId) {

		// TODO: make a queue of download requests
		Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_DESCRIPTION_ITEM);
		intent.putExtra(DownloadService.EXTRA_VALUE_ID, pointId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	public void downloadDescriptions() {

		Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_DESCRIPTIONS);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	/** Start intent service to download point into local database.
	 * 
	 * @param pointId */
	public void downloadPoint(final int pointId) {

		// TODO: make a queue of download requests
		Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_POINT);
		intent.putExtra(DownloadService.EXTRA_VALUE_ID, pointId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	public void downloadPointsFromTopic(final int topicId) {

		Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_POINT_FROM_TOPIC);
		intent.putExtra(DownloadService.EXTRA_VALUE_ID, topicId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
	}

	public void insertAttachment(final Bundle attachmentValues, final int discussionId, final int topicId) {

		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(UploadService.EXTRA_TYPE_ID, UploadService.TYPE_INSERT_ATTACHMENT);
		intent.putExtra(UploadService.EXTRA_VALUE, attachmentValues);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		intent.putExtra(UploadService.EXTRA_DISCUSSION_ID, discussionId);
		intent.putExtra(UploadService.EXTRA_TOPIC_ID, topicId);
		mContext.startService(intent);
	}

	public void insertComment(final Bundle commentValues, final int discussionId, final int topicId) {

		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(UploadService.EXTRA_TYPE_ID, UploadService.TYPE_INSERT_COMMENT);
		intent.putExtra(UploadService.EXTRA_VALUE, commentValues);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		intent.putExtra(UploadService.EXTRA_DISCUSSION_ID, discussionId);
		intent.putExtra(UploadService.EXTRA_TOPIC_ID, topicId);
		mContext.startService(intent);
	}

	public void insertDescription(final Bundle descriptionValues) {

		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(UploadService.EXTRA_TYPE_ID, UploadService.TYPE_INSERT_DESCRIPTION);
		intent.putExtra(UploadService.EXTRA_VALUE, descriptionValues);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void insertPointAndDescription(final Bundle values, final int discussionId) {

		Intent intent = new Intent(IntentAction.UPLOAD);
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

		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(UploadService.EXTRA_TYPE_ID, UploadService.TYPE_UPDATE_DESCRIPTION);
		intent.putExtra(UploadService.EXTRA_VALUE, descriptionValues);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void updatePoint(final Bundle pointValue, final int discussionId) {

		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(UploadService.EXTRA_TYPE_ID, UploadService.TYPE_UPDATE_POINT);
		intent.putExtra(UploadService.EXTRA_VALUE, pointValue);
		intent.putExtra(UploadService.EXTRA_DISCUSSION_ID, discussionId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		intent.putExtra(UploadService.EXTRA_PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void updatePoint(final int pointId) {

		Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(DownloadService.EXTRA_TYPE_ID, DownloadService.TYPE_UPDATE_POINT);
		intent.putExtra(DownloadService.EXTRA_VALUE_ID, pointId);
		intent.putExtra(OdataSyncResultReceiver.EXTRA_STATUS_RECEIVER, mOdataResultReceiver);
		mContext.startService(intent);
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
					if (mOdataListener != null) {
						mOdataListener.handleError(resultData.getString(Intent.EXTRA_TEXT));
					}
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
