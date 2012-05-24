package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Attachment;
import com.slobodastudio.discussions.data.model.SelectedPoint;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.ExtraKey;
import com.slobodastudio.discussions.ui.activities.BaseActivity;
import com.slobodastudio.discussions.ui.view.MediaList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class PointMediaTabFragment extends SherlockListFragment {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final int PICK_CAMERA_PHOTO = 0x03;
	private static final int PICK_IMAGE_REQUEST = 0x02;
	private static final String TAG = PointMediaTabFragment.class.getSimpleName();
	private boolean footerButtonsEnabled;
	private final AttachmentsCursorLoader mAttachmentsCursorLoader;
	private MediaList mediaList;
	private TextView mPointNameTextView;
	private SelectedPoint mSelectedPoint;
	private NewAttachment newAttachment;

	public PointMediaTabFragment() {

		mAttachmentsCursorLoader = new AttachmentsCursorLoader();
	}

	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
	public static Bundle intentToFragmentArguments(final Intent intent) {

		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}
		if (!intent.hasExtra(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalStateException("intent was without discussion id");
		}
		if (!intent.hasExtra(ExtraKey.POINT_ID)) {
			throw new IllegalStateException("intent was without point id");
		}
		if (!intent.hasExtra(ExtraKey.PERSON_ID)) {
			throw new IllegalStateException("intent was without person id");
		}
		if (!intent.hasExtra(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("intent was without topic id");
		}
		if (intent.getAction() == null) {
			throw new IllegalStateException("intent was without action string");
		}
		int discussionId = intent.getIntExtra(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
		int personId = intent.getIntExtra(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
		int topicId = intent.getIntExtra(ExtraKey.TOPIC_ID, Integer.MIN_VALUE);
		int pointId = intent.getIntExtra(ExtraKey.POINT_ID, Integer.MIN_VALUE);
		SelectedPoint point = new SelectedPoint();
		point.setDiscussionId(discussionId);
		point.setPersonId(personId);
		point.setTopicId(topicId);
		point.setPointId(pointId);
		arguments.putParcelable(ExtraKey.SELECTED_POINT, point);
		boolean viewEnabled = Intent.ACTION_EDIT.equals(intent.getAction());
		arguments.putBoolean(ExtraKey.VIEW_ENABLED, viewEnabled);
		return arguments;
	}

	public static void requestCameraPhoto(final Activity activity) {

		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		activity.startActivityForResult(cameraIntent, PICK_CAMERA_PHOTO);
	}

	public static void requestImageAttachment(final Activity activity) {

		Intent intent = new Intent();
		intent.setType("image/jpeg");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		activity.startActivityForResult(intent, PICK_IMAGE_REQUEST);
	}

	public static void requestPdfAttachment(final Activity activity) {

		// Intent intent = new Intent();
		// intent.setType("application/pdf");
		// intent.setAction(Intent.ACTION_GET_CONTENT);
		// startActivityForResult(intent, PICK_IMAGE_REQUEST);
		// FIXME: load pdf as a file here
		// http://stackoverflow.com/questions/8646246/uri-from-intent-action-get-content-into-file
	}

	private static AdapterContextMenuInfo castAdapterContextMenuInfo(final ContextMenuInfo contextMenuInfo) {

		try {
			// Casts the incoming data object into the type for AdapterView objects.
			return (AdapterContextMenuInfo) contextMenuInfo;
		} catch (ClassCastException e) {
			// If the menu object can't be cast, logs an error.
			throw new RuntimeException("bad menuInfo: " + contextMenuInfo, e);
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		setListShown(false);
		initFromArguments();
		addAttachmentsHeader();
		if (footerButtonsEnabled) {
			// addAttachmentsFooter();
		}
		mediaList = new MediaList(getActivity(), getListView());
		mediaList.setPositionOffset(1);
		initAttachmentsLoader();
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		Log.d(TAG, "[onActivityresult]");
		switch (requestCode) {
			case PICK_CAMERA_PHOTO:
				if (resultCode == Activity.RESULT_OK) {
					newAttachment = new NewAttachment(PICK_CAMERA_PHOTO, data.getData());
				} else {
					newAttachment = null;
				}
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				break;
			case PICK_IMAGE_REQUEST:
				if (resultCode == Activity.RESULT_OK) {
					newAttachment = new NewAttachment(PICK_IMAGE_REQUEST, data.getData());
				} else {
					newAttachment = null;
				}
				break;
			default:
				break;
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_delete:
				onActionDeleteAttachment(item);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = castAdapterContextMenuInfo(menuInfo);
		Cursor cursor = (Cursor) mediaList.getAdapter().getItem(info.position - 1);
		int textIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.TITLE);
		menu.setHeaderTitle(cursor.getString(textIndex));
		android.view.MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.context_attachments, menu);
	}

	public void onServiceConnected() {

		if (newAttachment != null) {
			switch (newAttachment.type) {
				case PICK_CAMERA_PHOTO:
					onAttachSourceAdded(newAttachment.uri, "Image, taken from android camera",
							Attachments.AttachmentType.JPG);
					break;
				case PICK_IMAGE_REQUEST:
					onAttachSourceAdded(newAttachment.uri, "Image, loaded from android sdcard",
							Attachments.AttachmentType.JPG);
					break;
				default:
					break;
			}
			newAttachment = null;
		}
	}

	private void addAttachmentsFooter() {

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View footerView = layoutInflater.inflate(R.layout.layout_media_footer, null, false);
		setAttachPhotoListener(footerView);
		setAttachImageListener(footerView);
		getListView().addFooterView(footerView);
	}

	private void addAttachmentsHeader() {

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View headerView = layoutInflater.inflate(R.layout.list_header_point_name, null, false);
		mPointNameTextView = (TextView) headerView.findViewById(R.id.list_header_point_name);
		getListView().addHeaderView(headerView);
	}

	private void initAttachmentsLoader() {

		Bundle args = new Bundle();
		args.putInt(ExtraKey.POINT_ID, mSelectedPoint.getPointId());
		getLoaderManager().initLoader(AttachmentsCursorLoader.ATTACHMENTS_ID, args, mAttachmentsCursorLoader);
		getLoaderManager().initLoader(AttachmentsCursorLoader.POINT_NAME_ID, args, mAttachmentsCursorLoader);
	}

	private void initFromArguments() {

		Bundle arguments = getArguments();
		if (arguments == null) {
			throw new NullPointerException("You are trying to instantiate fragment without arguments");
		}
		if (!arguments.containsKey(ExtraKey.SELECTED_POINT)) {
			throw new IllegalStateException("fragment was called without selected point extra");
		}
		if (!arguments.containsKey(ExtraKey.VIEW_ENABLED)) {
			throw new IllegalStateException("fragment was called without view enabled extra");
		}
		mSelectedPoint = arguments.getParcelable(ExtraKey.SELECTED_POINT);
		footerButtonsEnabled = arguments.getBoolean(ExtraKey.VIEW_ENABLED);
	}

	private void onActionDeleteAttachment(final MenuItem item) {

		AdapterContextMenuInfo info = castAdapterContextMenuInfo(item.getMenuInfo());
		Cursor cursor = (Cursor) mediaList.getAdapter().getItem(info.position - 1);
		int columnIndex = cursor.getColumnIndexOrThrow(Comments.Columns.ID);
		int attachmentId = cursor.getInt(columnIndex);
		((BaseActivity) getActivity()).getServiceHelper().deleteAttachment(attachmentId, mSelectedPoint);
	}

	private void onAttachSourceAdded(final Uri uri, final String attachmentDescription,
			final int attachmentType) {

		Attachment attachment = new Attachment();
		attachment.setName(attachmentDescription);
		attachment.setTitle(attachmentDescription);
		attachment.setPersonId(mSelectedPoint.getPersonId());
		attachment.setPointId(mSelectedPoint.getPointId());
		attachment.setFormat(attachmentType);
		((BaseActivity) getActivity()).getServiceHelper().insertAttachment(attachment, mSelectedPoint, uri);
	}

	private void setAttachImageListener(final View container) {

		Button attachImageButton = (Button) container.findViewById(R.id.btn_attach_image);
		attachImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {

				requestImageAttachment(getActivity());
			}
		});
	}

	private void setAttachPhotoListener(final View container) {

		Button attachPhotoButton = (Button) container.findViewById(R.id.btn_attach_photo);
		attachPhotoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {

				requestCameraPhoto(getActivity());
			}
		});
	}

	private class AttachmentsCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {

		private static final int ATTACHMENTS_ID = 0x00;
		private static final int POINT_NAME_ID = 0x01;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			if (!arguments.containsKey(ExtraKey.POINT_ID)) {
				throw new IllegalArgumentException("Loader was called without point id");
			}
			int myPointId = arguments.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
			if (DEBUG) {
				Log.d(TAG, "[onCreateLoader] point id: " + myPointId);
			}
			switch (loaderId) {
				case ATTACHMENTS_ID: {
					String where = Attachments.Columns.POINT_ID + "=?";
					String[] args = new String[] { String.valueOf(myPointId) };
					return new CursorLoader(getActivity(), Attachments.CONTENT_URI, null, where, args, null);
				}
				case POINT_NAME_ID: {
					String where = Points.Columns.ID + "=?";
					String[] args = new String[] { String.valueOf(myPointId) };
					String[] projection = new String[] { BaseColumns._ID, Points.Columns.NAME };
					return new CursorLoader(getActivity(), Points.CONTENT_URI, projection, where, args, null);
				}
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case ATTACHMENTS_ID:
					mediaList.getAdapter().swapCursor(null);
					break;
				case POINT_NAME_ID:
					mPointNameTextView.setText("");
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			if (DEBUG) {
				Log.d(TAG, "[onLoadFinished] cursor count: " + data.getCount() + ", id: " + loader.getId());
			}
			switch (loader.getId()) {
				case ATTACHMENTS_ID:
					mediaList.getAdapter().swapCursor(data);
					if (isResumed()) {
						setListShown(true);
					} else {
						setListShownNoAnimation(true);
					}
					break;
				case POINT_NAME_ID:
					if (data.moveToFirst()) {
						int nameColumnIndex = data.getColumnIndexOrThrow(Points.Columns.NAME);
						String name = data.getString(nameColumnIndex);
						mPointNameTextView.setText(name);
					}
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}
	}

	private class NewAttachment {

		int type;
		Uri uri;

		public NewAttachment(final int type, final Uri uri) {

			super();
			this.type = type;
			this.uri = uri;
		}
	}
}
