package com.slobodastudio.discussions.photon;

import com.slobodastudio.discussions.data.model.ArgPointChanged;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.model.SelectedPoint;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.photon.PhotonController.SyncResultReceiver;
import com.slobodastudio.discussions.service.UploadService;

import android.os.Bundle;
import android.os.ResultReceiver;

public class PhotonHelper {

	/** A private Constructor prevents class from instantiating. */
	private PhotonHelper() throws UnsupportedOperationException {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	public static void sendArgPointChanged(final Bundle parameters, final int eventType) {

		if (!parameters.containsKey(UploadService.EXTRA_SELECTED_POINT)) {
			throw new IllegalArgumentException(
					"[sendArgPointChanged] called without required extra: selected point");
		}
		if (!parameters.containsKey(UploadService.EXTRA_PHOTON_RECEIVER)) {
			throw new IllegalArgumentException(
					"[sendArgPointChanged] called without required extra: photon receiver");
		}
		SelectedPoint selectedPoint = parameters.getParcelable(UploadService.EXTRA_SELECTED_POINT);
		ResultReceiver photonReceiver = (ResultReceiver) parameters
				.getParcelable(UploadService.EXTRA_PHOTON_RECEIVER);
		sendArgPointChanged(selectedPoint, eventType, photonReceiver);
	}

	public static void sendArgPointDeleted(final Point point, final ResultReceiver photonReceiver) {

		ArgPointChanged argPointChanged = new ArgPointChanged();
		argPointChanged.setEventType(Points.PointChangedType.DELETED);
		argPointChanged.setPointId(point.getId());
		argPointChanged.setTopicId(point.getTopicId());
		PhotonHelper.sendArgPointChanged(argPointChanged, photonReceiver);
	}

	public static void sendArgPointUpdated(final Point point, final ResultReceiver photonReceiver) {

		ArgPointChanged argPointChanged = new ArgPointChanged();
		argPointChanged.setEventType(Points.PointChangedType.MODIFIED);
		argPointChanged.setPointId(point.getId());
		argPointChanged.setTopicId(point.getTopicId());
		PhotonHelper.sendArgPointChanged(argPointChanged, photonReceiver);
	}

	public static void sendArgPointUpdated(final SelectedPoint selectedPoint,
			final ResultReceiver photonReceiver) {

		ArgPointChanged argPointChanged = new ArgPointChanged();
		argPointChanged.setEventType(Points.PointChangedType.MODIFIED);
		argPointChanged.setPointId(selectedPoint.getPointId());
		argPointChanged.setTopicId(selectedPoint.getTopicId());
		PhotonHelper.sendArgPointChanged(argPointChanged, photonReceiver);
	}

	private static void sendArgPointChanged(final ArgPointChanged argPointChanged,
			final ResultReceiver photonReceiver) {

		if (photonReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putParcelable(SyncResultReceiver.EXTRA_ARG_POINT_CHANGED, argPointChanged);
			photonReceiver.send(SyncResultReceiver.STATUS_ARG_POINT_CHANGED, bundle);
		}
	}

	private static void sendArgPointChanged(final SelectedPoint selectedPoint, final int eventType,
			final ResultReceiver photonReceiver) {

		ArgPointChanged argPointChanged = new ArgPointChanged();
		argPointChanged.setEventType(eventType);
		argPointChanged.setPointId(selectedPoint.getPointId());
		argPointChanged.setTopicId(selectedPoint.getTopicId());
		sendArgPointChanged(argPointChanged, photonReceiver);
	}
}
