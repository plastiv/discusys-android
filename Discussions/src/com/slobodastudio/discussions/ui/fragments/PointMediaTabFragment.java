package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Attachment;
import com.slobodastudio.discussions.data.model.SelectedPoint;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments.AttachmentType;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.ExtraKey;
import com.slobodastudio.discussions.ui.activities.BaseActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class PointMediaTabFragment extends SherlockFragment {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final int PICK_CAMERA_PHOTO = 0x03;
	private static final int PICK_IMAGE_REQUEST = 0x02;
	private static final String TAG = PointMediaTabFragment.class.getSimpleName();
	private boolean footerButtonsEnabled;
	private SimpleCursorAdapter mAttachmentsAdapter;
	private final AttachmentsCursorLoader mAttachmentsCursorLoader;
	private ListView mAttachmentsList;
	private TextView mPointNameTextView;
	private SelectedPoint mSelectedPoint;

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

		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		activity.startActivityForResult(cameraIntent, PICK_CAMERA_PHOTO);
	}

	public static void requestImageAttachment(final Activity activity) {

		Intent intent = new Intent();
		intent.setType("image/*");
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

	private static byte[] getBitmapAsByteArray(final Bitmap bitmap) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// Middle value is quality, but PNG is lossless, so it's ignored.
		bitmap.compress(CompressFormat.PNG, 0, outputStream);
		return outputStream.toByteArray();
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		Log.d(TAG, "[onActivityresult]");
		switch (requestCode) {
			case PICK_CAMERA_PHOTO: {
				Bitmap bitmap = (Bitmap) data.getExtras().get("data");
				byte[] bitmapArray = getBitmapAsByteArray(bitmap);
				onAttachSourceAdded(bitmapArray, "Image, taken from android camera",
						Attachments.AttachmentType.JPG);
				getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				break;
			}
			case PICK_IMAGE_REQUEST:
				if (resultCode == Activity.RESULT_OK) {
					onAttachSourceAdded(data.getData(), "Image, loaded from android sdcard",
							Attachments.AttachmentType.PNG);
				}
				break;
			default:
				break;
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		initFromArguments();
		View attachmentsView = inflater.inflate(R.layout.tab_fragment_point_attachments, container, false);
		mAttachmentsList = (ListView) attachmentsView.findViewById(R.id.listview_attachments);
		addAttachmentsHeader();
		if (footerButtonsEnabled) {
			addAttachmentsFooter();
		}
		setAttachmentsAdapter();
		initAttachmentsLoader();
		return attachmentsView;
	}

	private void addAttachmentsFooter() {

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View footerView = layoutInflater.inflate(R.layout.layout_media_footer, null, false);
		setAttachPhotoListener(footerView);
		setAttachImageListener(footerView);
		mAttachmentsList.addFooterView(footerView);
	}

	private void addAttachmentsHeader() {

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View headerView = layoutInflater.inflate(R.layout.list_header_point_name, null, false);
		mPointNameTextView = (TextView) headerView.findViewById(R.id.list_header_point_name);
		mAttachmentsList.addHeaderView(headerView);
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

	private void loadImage(final Intent intent) {

		Uri selectedImageUri = intent.getData();
		Bitmap galleryImage;
		try {
			galleryImage = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(
					selectedImageUri));
			if (galleryImage != null) {
				// Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
				// bitmap.recycle();
				// if (newBitmap != null) {
				// publishProgress(new LoadedImage(newBitmap));
				// }
				byte[] bitmapArray = getBitmapAsByteArray(galleryImage);
				galleryImage.recycle();
				onAttachSourceAdded(bitmapArray, "Image, loaded from android sdcard",
						Attachments.AttachmentType.PNG);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void onAttachSourceAdded(final byte[] attachmentData, final String attachmentDescription,
			final int attachmentType) {

		Attachment attachment = new Attachment();
		attachment.setData(attachmentData);
		attachment.setName(attachmentDescription);
		attachment.setTitle(attachmentDescription);
		attachment.setPersonId(mSelectedPoint.getPersonId());
		attachment.setPointId(mSelectedPoint.getPointId());
		attachment.setFormat(attachmentType);
		((BaseActivity) getActivity()).getServiceHelper().insertAttachment(attachment, mSelectedPoint);
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

	private void setAttachmentsAdapter() {

		mAttachmentsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_media, null,
				new String[] { Attachments.Columns.NAME, Attachments.Columns.DATA }, new int[] {
						R.id.text_attachment_name, R.id.image_attachment_preview }, 0);
		mAttachmentsAdapter.setViewBinder(new AttachmentsViewBinder());
		mAttachmentsList.setAdapter(mAttachmentsAdapter);
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
					mAttachmentsAdapter.swapCursor(null);
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
					mAttachmentsAdapter.swapCursor(data);
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

	private class AttachmentsViewBinder implements ViewBinder {

		@Override
		public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

			switch (view.getId()) {
				case R.id.image_attachment_preview:
					setPreviewImage((ImageView) view, cursor);
					return true;
				case R.id.text_attachment_name:
					((TextView) view).setText(cursor.getString(columnIndex));
					return true;
				default:
					// TODO: throw exception
					return false;
			}
		}

		private float convertPixelsFromDensityPixel(final int valueInDp) {

			return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, getResources()
					.getDisplayMetrics());
		}

		private Bitmap scaleDown(final Bitmap realImage) {

			return scaleDown(realImage, convertPixelsFromDensityPixel(100), true);
		}

		private Bitmap scaleDown(final Bitmap realImage, final float maxImageSize, final boolean filter) {

			float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
			int width = Math.round(ratio * realImage.getWidth());
			int height = Math.round(ratio * realImage.getHeight());
			Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
			return newBitmap;
		}

		private void setPreviewImage(final ImageView imageView, final Cursor cursor) {

			int formatColumnIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.FORMAT);
			int attachmentFormat = cursor.getInt(formatColumnIndex);
			switch (attachmentFormat) {
				case AttachmentType.JPG:
				case AttachmentType.PNG:
				case AttachmentType.BMP:
					int dataColumnIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.DATA);
					byte[] pictureData = cursor.getBlob(dataColumnIndex);
					BitmapFactory.Options bounds = new BitmapFactory.Options();
					bounds.inSampleSize = 4;
					Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length, bounds);
					imageView.setImageBitmap(scaleDown(bitmap));
					break;
				case AttachmentType.GENERAL_WEB_LINK:
				case AttachmentType.NONE:
				case AttachmentType.PDF:
				case AttachmentType.YOUTUBE:
					imageView.setImageResource(R.drawable.image_not_found);
					break;
				default:
					// TODO: throw ex
					break;
			}
		}
	}
}
