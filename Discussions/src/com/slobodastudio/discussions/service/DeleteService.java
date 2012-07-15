package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.model.SelectedPoint;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.photon.PhotonController.SyncResultReceiver;
import com.slobodastudio.discussions.photon.PhotonHelper;
import com.slobodastudio.discussions.ui.IntentAction;
import com.slobodastudio.discussions.utils.ConnectivityUtil;
import com.slobodastudio.discussions.utils.MyLog;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class DeleteService extends IntentService {

	public static final int TYPE_DELETE_ATTACHMENT = 0x2;
	public static final int TYPE_DELETE_COMMENT = 0x1;
	public static final int TYPE_DELETE_POINT = 0x0;
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = DeleteService.class.getSimpleName();

	public DeleteService() {

		super(TAG);
	}

	private static ResultReceiver getActivityReceiverFromExtra(final Intent intent) {

		return intent.getParcelableExtra(ServiceExtraKeys.ACTIVITY_RECEIVER);
	}

	private static ResultReceiver getPhotonReceiverFromExtra(final Intent intent) {

		return intent.getParcelableExtra(ServiceExtraKeys.PHOTON_RECEIVER);
	}

	private static SelectedPoint getSelectedPointFromExtra(final Intent intent) {

		if (!intent.hasExtra(ServiceExtraKeys.SELECTED_POINT)) {
			throw new IllegalArgumentException("[getSelectedPointFromExtra] called without required extra: "
					+ ServiceExtraKeys.SELECTED_POINT);
		}
		return intent.getParcelableExtra(ServiceExtraKeys.SELECTED_POINT);
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

	private static void validateIntent(final Intent intent) {

		if (!IntentAction.DELETE.equals(intent.getAction())) {
			throw new IllegalArgumentException("Service was started with unknown intent: "
					+ intent.getAction());
		}
		if (intent.getExtras() == null) {
			throw new IllegalArgumentException("Service was started without extras");
		}
		if (!intent.hasExtra(ServiceExtraKeys.PHOTON_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.PHOTON_RECEIVER);
		}
		if (!intent.hasExtra(ServiceExtraKeys.TYPE_ID)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.TYPE_ID);
		}
		if (!intent.hasExtra(ServiceExtraKeys.VALUE_ID)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.VALUE_ID);
		}
		if (!intent.hasExtra(ServiceExtraKeys.ACTIVITY_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.ACTIVITY_RECEIVER);
		}
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		logd("[onHandleIntent] intent: " + intent.toString());
		validateIntent(intent);
		final ResultReceiver activityReceiver = getActivityReceiverFromExtra(intent);
		if (isConnected()) {
			ActivityResultHelper.sendStatusStart(activityReceiver);
		} else {
			String errorString = getString(R.string.text_error_network_off);
			ActivityResultHelper.sendStatusError(activityReceiver, errorString);
			stopSelf();
			return;
		}
		try {
			switch (getTypeFromExtra(intent)) {
				case TYPE_DELETE_POINT:
					deletePoint(intent);
					break;
				case TYPE_DELETE_COMMENT:
					deleteComment(intent);
					break;
				case TYPE_DELETE_ATTACHMENT:
					deleteAttachment(intent);
					break;
				default:
					throw new IllegalArgumentException("Illegal type id: " + getTypeFromExtra(intent));
			}
		} catch (Exception e) {
			MyLog.e(TAG, "[onHandleIntent] sync error. Intent action: " + intent.getAction(), e);
			ActivityResultHelper.sendStatusError(activityReceiver, e.toString());
			stopSelf();
			return;
		}
		ActivityResultHelper.sendStatusFinished(activityReceiver);
		logd("[onHandleIntent] delete service finished");
	}

	private void deleteAttachment(final Intent intent) {

		int attachmentId = getValueIdFromExtra(intent);
		logd("[deleteAttachment] id: " + attachmentId);
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.deleteAttachment(attachmentId);
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		PhotonHelper.sendArgPointUpdated(selectedPoint, photonReceiver);
		// TODO: send stats event here
		// notifyPhotonStatsEvent(getPhotonReceiverFromExtra(intent), selectedPoint.getDiscussionId(),
		// selectedPoint.getPersonId(), selectedPoint.getTopicId(), StatsType.BADGE_EDITED);
		String where = Attachments.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(attachmentId) };
		getContentResolver().delete(Attachments.CONTENT_URI, where, args);
	}

	private void deleteComment(final Intent intent) {

		int commentId = getValueIdFromExtra(intent);
		logd("[deleteComment] comment id: " + commentId);
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.deleteComment(commentId);
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		PhotonHelper.sendArgPointUpdated(selectedPoint, getPhotonReceiverFromExtra(intent));
		// TODO send stats here
		// notifyPhotonStatsEvent((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
		// discussionId, personId, topicId, StatsType.BADGE_EDITED);
		String where = Comments.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(commentId) };
		getContentResolver().delete(Comments.CONTENT_URI, where, args);
	}

	private void deletePoint(final Intent intent) {

		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		//
		int pointId = selectedPoint.getPointId();
		logd("[deletePoint] point id: " + pointId);
		//
		Point deletePoint = getPointFromLocal(pointId);
		deletePointOnServer(deletePoint);
		deletePointOnLocal(deletePoint);
		updatePointOrderNumbers(deletePoint, photonReceiver);
		PhotonHelper.sendArgPointDeleted(deletePoint, photonReceiver);
		// TODO: send stats here
	}

	private int deletePointOnLocal(final Point point) {

		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(point.getId()) };
		return getContentResolver().delete(Points.CONTENT_URI, where, args);
	}

	private void deletePointOnServer(final Point point) {

		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.deletePoint(point.getId());
	}

	private Point getPointFromLocal(final int pointId) {

		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(pointId) };
		Cursor cursor = getContentResolver().query(Points.CONTENT_URI, null, where, args, null);
		Point point;
		if (cursor.moveToFirst()) {
			point = new Point(cursor);
		} else {
			// dump, empty point
			point = new Point();
		}
		cursor.close();
		return point;
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

	private void updatePoint(final Point point, final ResultReceiver photonReceiver) {

		updatePointOnServer(point);
		updatePointOnLocal(point);
		PhotonHelper.sendArgPointUpdated(point, photonReceiver);
		// TODO: trigger photon stats
		// sendPhotonStatsEvent(intent, StatsType.BADGE_EDITED);
	}

	private int updatePointOnLocal(final Point point) {

		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(point.getId()) };
		return getContentResolver().update(Points.CONTENT_URI, point.toContentValues(), where, args);
	}

	private boolean updatePointOnServer(final Point point) {

		OdataWriteClient odataWrite = new OdataWriteClient(this);
		return odataWrite.updatePoint(point);
	}

	private void updatePointOrderNumbers(final Point deletedPoint, final ResultReceiver photonReceiver) {

		int orderNum = deletedPoint.getOrderNumber();
		String whereUpdated = Points.Columns.ORDER_NUMBER + ">" + orderNum + " AND "
				+ Points.Columns.PERSON_ID + "= " + deletedPoint.getPersonId() + " AND "
				+ Points.Columns.TOPIC_ID + "=" + deletedPoint.getTopicId();
		Cursor pointGreaterDeletedCursor = getContentResolver().query(Points.CONTENT_URI, null, whereUpdated,
				null, Points.Columns.ORDER_NUMBER);
		// decrease order num foreach point after deleted by one
		for (pointGreaterDeletedCursor.moveToFirst(); !pointGreaterDeletedCursor.isAfterLast(); pointGreaterDeletedCursor
				.moveToNext()) {
			Point point = new Point(pointGreaterDeletedCursor);
			point.setOrderNumber(point.getOrderNumber() - 1);
			updatePoint(point, photonReceiver);
		}
		pointGreaterDeletedCursor.close();
	}
}
