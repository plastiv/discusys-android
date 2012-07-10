package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.DataIoException;
import com.slobodastudio.discussions.data.model.ArgPointChanged;
import com.slobodastudio.discussions.data.model.Attachment;
import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.model.SelectedPoint;
import com.slobodastudio.discussions.data.model.Source;
import com.slobodastudio.discussions.data.odata.HttpUtil;
import com.slobodastudio.discussions.data.odata.OdataWriteClient;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sources;
import com.slobodastudio.discussions.photon.PhotonController.SyncResultReceiver;
import com.slobodastudio.discussions.photon.constants.StatsType;
import com.slobodastudio.discussions.service.ServiceHelper.OdataSyncResultReceiver;
import com.slobodastudio.discussions.ui.IntentAction;
import com.slobodastudio.discussions.utils.ConnectivityUtil;
import com.slobodastudio.discussions.utils.MediaStoreHelper;
import com.slobodastudio.discussions.utils.MyLog;
import com.slobodastudio.discussions.utils.YoutubeHelper;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.odata4j.core.OEntity;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class UploadService extends IntentService {

	public static final String EXTRA_DISCUSSION_ID = "intent.extra.key.EXTRA_DISCUSSION_ID";
	public static final String EXTRA_PHOTON_RECEIVER = "intent.extra.key.PHOTON_RECEIVER";
	public static final String EXTRA_SELECTED_POINT = "intent.extra.key.EXTRA_SELECTED_POINT";
	public static final String EXTRA_TOPIC_ID = "intent.extra.key.EXTRA_TOPIC_ID";
	public static final String EXTRA_TYPE_ID = "intent.extra.key.EXTRA_TYPE_ID";
	public static final String EXTRA_URI = "intent.extra.key.EXTRA_URI";
	public static final String EXTRA_VALUE = "intent.extra.key.EXTRA_VALUE";
	public static final int TYPE_INSERT_ATTACHMENT = 0x6;
	public static final int TYPE_INSERT_COMMENT = 0x5;
	public static final int TYPE_INSERT_DESCRIPTION = 0x3;
	public static final int TYPE_INSERT_POINT_AND_DESCRIPTION = 0x4;
	public static final int TYPE_INSERT_SOURCE = 0x7;
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

	private static void notifyPhotonArgPointChanged(final ResultReceiver photonReceiver,
			final ArgPointChanged argPointChanged) {

		logd("[notifyPhoton] changed arg point id: " + argPointChanged.getPointId() + ", photonReceiver: "
				+ photonReceiver);
		if (photonReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putParcelable(SyncResultReceiver.EXTRA_ARG_POINT_CHANGED, argPointChanged);
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

	private static void sendArgPointChanged(final Intent intent, final byte eventType) {

		if (!intent.hasExtra(EXTRA_SELECTED_POINT)) {
			throw new IllegalArgumentException(
					"[sendArgPointChanged] called without required extra: selected point");
		}
		if (!intent.hasExtra(EXTRA_PHOTON_RECEIVER)) {
			throw new IllegalArgumentException(
					"[sendArgPointChanged] called without required extra: photon receiver");
		}
		SelectedPoint selectedPoint = intent.getParcelableExtra(EXTRA_SELECTED_POINT);
		ArgPointChanged argPointChanged = new ArgPointChanged();
		argPointChanged.setEventType(eventType);
		argPointChanged.setPointId(selectedPoint.getPointId());
		argPointChanged.setTopicId(selectedPoint.getTopicId());
		notifyPhotonArgPointChanged((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
				argPointChanged);
	}

	private static void sendPhotonStatsEvent(final Intent intent, final byte statsType) {

		if (!intent.hasExtra(EXTRA_SELECTED_POINT)) {
			throw new IllegalArgumentException(
					"[sendPhotonStatsEvent] called without required extra: selected point");
		}
		if (!intent.hasExtra(EXTRA_PHOTON_RECEIVER)) {
			throw new IllegalArgumentException(
					"[sendPhotonStatsEvent] called without required extra: photon receiver");
		}
		SelectedPoint selectedPoint = intent.getParcelableExtra(EXTRA_SELECTED_POINT);
		notifyPhotonStatsEvent((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
				selectedPoint.getDiscussionId(), selectedPoint.getPersonId(), selectedPoint.getTopicId(),
				statsType);
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		if (!IntentAction.UPLOAD.equals(intent.getAction())) {
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
				case TYPE_INSERT_COMMENT:
					insertComment(intent);
					break;
				case TYPE_INSERT_ATTACHMENT:
					insertAttachment(intent);
					break;
				case TYPE_INSERT_SOURCE:
					insertSource(intent);
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

	private void insertAttachment(final Intent intent) {

		if (!intent.hasExtra(EXTRA_URI)) {
			throw new IllegalArgumentException(
					"[insertAttachment] was called without required attachment uri");
		}
		Attachment attachment = intent.getParcelableExtra(EXTRA_VALUE);
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		logd("[insertAttachment] " + attachment.getTitle());
		Uri attachmentUri = intent.getParcelableExtra(EXTRA_URI);
		int attachmentId;
		switch (attachment.getFormat()) {
			case Attachments.AttachmentType.JPG:
				attachment.setTitle(MediaStoreHelper.getTitleFromUri(this, attachmentUri));
				attachmentId = HttpUtil.insertImageAttachment(this, attachmentUri);
				break;
			case Attachments.AttachmentType.PDF:
				attachment.setTitle(attachmentUri.getLastPathSegment());
				attachmentId = HttpUtil.insertPdfAttachment(this, attachmentUri);
				break;
			case Attachments.AttachmentType.YOUTUBE:
				OEntity entity = odataWrite.insertAttachment(attachment);
				attachmentId = (Integer) entity.getProperty(Attachments.Columns.ID).getValue();
				attachment.setTitle(YoutubeHelper.getVideoTitle(attachmentUri.toString()));
				attachment.setLink(attachmentUri.toString());
				attachment.setVideoLinkURL(attachmentUri.toString());
				String vid = attachmentUri.getQueryParameter("v");
				attachment.setVideoEmbedURL("http://www.youtube.com/embed/" + vid);
				attachment.setVideoThumbURL(YoutubeHelper.getThumbImageUrl(attachmentUri.toString()));
				break;
			default:
				throw new UnsupportedOperationException(
						"[insertAttachment] was called with unknown attachment type: "
								+ attachment.getFormat());
		}
		attachment.setName(attachment.getTitle());
		boolean updated = odataWrite.updateAttachment(attachment, attachmentId);
		if (updated) {
			attachment.setAttachmentId(attachmentId);
			ContentValues cv = attachment.toContentValues();
			getContentResolver().insert(Attachments.CONTENT_URI, cv);
			sendArgPointChanged(intent, Points.PointChangedType.MODIFIED);
			sendPhotonStatsEvent(intent, StatsType.BADGE_EDITED);
		} else {
			throw new DataIoException("Failed to update newly inserted attachment with id: " + attachmentId);
		}
	}

	private void insertComment(final Intent intent) {

		Bundle commentBundle = intent.getBundleExtra(EXTRA_VALUE);
		logd("[insertComment] " + commentBundle.getString(Comments.Columns.TEXT));
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		String text = commentBundle.getString(Comments.Columns.TEXT);
		int personId = commentBundle.getInt(Comments.Columns.PERSON_ID, Integer.MIN_VALUE);
		int pointId = commentBundle.getInt(Comments.Columns.POINT_ID, Integer.MIN_VALUE);
		OEntity entity = odataWrite.insertComment(text, personId, pointId);
		int commentId = (Integer) entity.getProperty(Comments.Columns.ID).getValue();
		logd("[insertComment] new comment id: " + commentId);
		ContentValues cv = new ContentValues();
		cv.put(Comments.Columns.ID, commentId);
		cv.put(Comments.Columns.TEXT, text);
		cv.put(Comments.Columns.PERSON_ID, personId);
		cv.put(Comments.Columns.POINT_ID, pointId);
		getContentResolver().insert(Comments.CONTENT_URI, cv);
		// TODO: rewrite this part
		if (!intent.hasExtra(EXTRA_DISCUSSION_ID)) {
			throw new IllegalArgumentException("[insertComment] called without required discussion id");
		}
		int discussionId = intent.getIntExtra(EXTRA_DISCUSSION_ID, Integer.MIN_VALUE);
		if (!intent.hasExtra(EXTRA_TOPIC_ID)) {
			throw new IllegalArgumentException("[insertComment] called without required topic id");
		}
		int topicId = intent.getIntExtra(EXTRA_TOPIC_ID, Integer.MIN_VALUE);
		SelectedPoint selectedPoint = new SelectedPoint();
		selectedPoint.setDiscussionId(discussionId);
		selectedPoint.setPersonId(personId);
		selectedPoint.setPointId(pointId);
		selectedPoint.setTopicId(topicId);
		intent.putExtra(EXTRA_SELECTED_POINT, selectedPoint);
		sendArgPointChanged(intent, Points.PointChangedType.MODIFIED);
		sendPhotonStatsEvent(intent, StatsType.BADGE_EDITED);
	}

	private void insertDescription(final Intent intent) {

		Bundle descriptionBundle = intent.getBundleExtra(EXTRA_VALUE);
		Description description = new Description(descriptionBundle);
		logd("[insertDescription] " + description.toMyString());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		OEntity entity = odataWrite.insertDescription(description);
		int newId = (Integer) entity.getProperty(Descriptions.Columns.ID).getValue();
		logd("[insertDescription] new description id: " + newId);
		description.setId(newId);
		getContentResolver().insert(Descriptions.CONTENT_URI, description.toContentValues());
	}

	private void insertPointAndDescription(final Intent intent) {

		Bundle pointBundle = intent.getBundleExtra(EXTRA_VALUE);
		Point point = new Point(pointBundle);
		logd("[insertPoint] " + point.toMyString()); // insert new description
		Bundle descriptionBundle = intent.getBundleExtra(EXTRA_VALUE);
		Description description = new Description(descriptionBundle);
		logd("[insertDescription] " + description.toMyString());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		OEntity entity = odataWrite.insertPoint(point);
		int newPointId = (Integer) entity.getProperty(Points.Columns.ID).getValue();
		logd("[insertPoint] new point id: " + newPointId);
		point.setId(newPointId);
		description.setPointId(newPointId);
		OEntity entityDesription = odataWrite.insertDescription(description);
		int newId = (Integer) entityDesription.getProperty(Descriptions.Columns.ID).getValue();
		logd("[insertDescription] new description id: " + newId);
		description.setId(newId);
		// notifyPhotonArgPointChanged((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
		// point
		// .getId());
		// if (!intent.hasExtra(EXTRA_DISCUSSION_ID)) {
		// throw new IllegalArgumentException("[updatePoint] called without required discussion id");
		// }
		// int discussionId = intent.getIntExtra(EXTRA_DISCUSSION_ID, Integer.MIN_VALUE);
		// notifyPhotonStatsEvent((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
		// discussionId, point.getPersonId(), point.getTopicId(), StatsType.BADGE_CREATED);
		getContentResolver().insert(Points.CONTENT_URI, point.toContentValues());
		getContentResolver().insert(Descriptions.CONTENT_URI, description.toContentValues());
		// TODO: rewrite this part
		if (!intent.hasExtra(EXTRA_DISCUSSION_ID)) {
			throw new IllegalArgumentException("[insertComment] called without required discussion id");
		}
		int discussionId = intent.getIntExtra(EXTRA_DISCUSSION_ID, Integer.MIN_VALUE);
		SelectedPoint selectedPoint = new SelectedPoint();
		selectedPoint.setDiscussionId(discussionId);
		selectedPoint.setPersonId(point.getPersonId());
		selectedPoint.setPointId(point.getId());
		selectedPoint.setTopicId(point.getTopicId());
		intent.putExtra(EXTRA_SELECTED_POINT, selectedPoint);
		sendArgPointChanged(intent, Points.PointChangedType.CREATED);
		sendPhotonStatsEvent(intent, StatsType.BADGE_CREATED);
	}

	private void insertSource(final Intent intent) {

		Source source = intent.getParcelableExtra(EXTRA_VALUE);
		logd("[insertSource] " + source.getLink());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		OEntity entity = odataWrite.insertSource(source);
		int sourceId = (Integer) entity.getProperty(Sources.Columns.ID).getValue();
		logd("[insertSource] new attachment id: " + sourceId);
		source.setSourceId(sourceId);
		ContentValues cv = source.toContentValues();
		getContentResolver().insert(Sources.CONTENT_URI, cv);
		sendArgPointChanged(intent, Points.PointChangedType.MODIFIED);
		sendPhotonStatsEvent(intent, StatsType.BADGE_EDITED);
	}

	private void updateDescription(final Intent intent) {

		Bundle descriptionBundle = intent.getBundleExtra(EXTRA_VALUE);
		Description description = new Description(descriptionBundle);
		logd("[updateDescription] " + description.toMyString());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.updateDescription(description);
		String where = Descriptions.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(description.getId()) };
		getContentResolver().update(Descriptions.CONTENT_URI, description.toContentValues(), where, args);
	}

	private void updatePoint(final Intent intent) {

		Bundle pointBundle = intent.getBundleExtra(EXTRA_VALUE);
		Point point = new Point(pointBundle);
		logd("[updatePoint] " + point.toMyString());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.updatePoint(point);
		// notifyPhotonArgPointChanged((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
		// point
		// .getId());
		// if (!intent.hasExtra(EXTRA_DISCUSSION_ID)) {
		// throw new IllegalArgumentException("[updatePoint] called without required discussion id");
		// }
		// int discussionId = intent.getIntExtra(EXTRA_DISCUSSION_ID, Integer.MIN_VALUE);
		// notifyPhotonStatsEvent((ResultReceiver) intent.getParcelableExtra(EXTRA_PHOTON_RECEIVER),
		// discussionId, point.getPersonId(), point.getTopicId(), StatsType.BADGE_EDITED);
		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(point.getId()) };
		getContentResolver().update(Points.CONTENT_URI, point.toContentValues(), where, args);
		// TODO: rewrite this part
		if (!intent.hasExtra(EXTRA_DISCUSSION_ID)) {
			throw new IllegalArgumentException("[insertComment] called without required discussion id");
		}
		int discussionId = intent.getIntExtra(EXTRA_DISCUSSION_ID, Integer.MIN_VALUE);
		SelectedPoint selectedPoint = new SelectedPoint();
		selectedPoint.setDiscussionId(discussionId);
		selectedPoint.setPersonId(point.getPersonId());
		selectedPoint.setPointId(point.getId());
		selectedPoint.setTopicId(point.getTopicId());
		intent.putExtra(EXTRA_SELECTED_POINT, selectedPoint);
		sendArgPointChanged(intent, Points.PointChangedType.MODIFIED);
		sendPhotonStatsEvent(intent, StatsType.BADGE_EDITED);
	}
}
