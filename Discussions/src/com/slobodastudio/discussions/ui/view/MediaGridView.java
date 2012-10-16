package com.slobodastudio.discussions.ui.view;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments.AttachmentType;
import com.slobodastudio.discussions.service.DownloadService;
import com.slobodastudio.discussions.service.ServiceExtraKeys;
import com.slobodastudio.discussions.ui.IntentAction;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class MediaGridView extends GridView {

	private static final String TAG = MediaGridView.class.getSimpleName();
	private final ImageLoader imageLoader;
	private SimpleCursorAdapter mAttachmentsAdapter;
	private final Context mContext;
	private int postionOffset = 0;

	public MediaGridView(final Context context) {

		this(context, null);
	}

	public MediaGridView(final Context context, final AttributeSet attrs) {

		this(context, attrs, android.R.attr.gridViewStyle);
	}

	public MediaGridView(final Context context, final AttributeSet attrs, final int defStyle) {

		super(context, attrs, defStyle);
		mContext = context;
		imageLoader = ImageLoader.getInstance();
		setOnItemClickListener(new AttachmentsItemClickListener());
	}

	@Override
	public SimpleCursorAdapter getAdapter() {

		return mAttachmentsAdapter;
	}

	public void setAttachmentsAdapter() {

		mAttachmentsAdapter = new SimpleCursorAdapter(mContext, R.layout.grid_item_media, null, new String[] {
				Attachments.Columns.TITLE, Attachments.Columns.ID, Attachments.Columns.FORMAT }, new int[] {
				R.id.text_attachment_name, R.id.image_attachment_preview, R.id.image_attachment_filetype }, 0);
		mAttachmentsAdapter.setViewBinder(new AttachmentsViewBinder());
		setAdapter(mAttachmentsAdapter);
	}

	public void setPositionOffset(final int positionOffset) {

		postionOffset = positionOffset;
	}

	private class AttachmentsItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(final AdapterView<?> arg0, final View v, final int position, final long id) {

			Cursor cursor = mAttachmentsAdapter.getCursor();
			if ((cursor != null) && cursor.moveToPosition(position - postionOffset)) {
				int formatColumnIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.FORMAT);
				int attachmentFormat = cursor.getInt(formatColumnIndex);
				switch (attachmentFormat) {
					case AttachmentType.JPG:
					case AttachmentType.PNG:
					case AttachmentType.BMP:
					case AttachmentType.PNG_SCREENSHOT:
						fireFullImageIntent(cursor);
						break;
					case AttachmentType.YOUTUBE:
						fireYoutubeIntent(cursor);
						break;
					case AttachmentType.PDF:
						// firePdfDownloadIntent(cursor);
						// break;
					case AttachmentType.GENERAL_WEB_LINK:
					case AttachmentType.NONE:
						Log.d(TAG, "[onItemClick] clicked on format: " + attachmentFormat);
						break;
					default:
						Log.e(TAG, "[onItemClick] unknown attachment format: " + attachmentFormat);
						break;
				}
			}
		}

		private void firePdfDownloadIntent(final Cursor cursor) {

			int idColumn = cursor.getColumnIndexOrThrow(Attachments.Columns.ID);
			final int valueId = cursor.getInt(idColumn);
			String pdfUrl = Attachments.getAttachmentDownloadLink(mContext, valueId);
			Intent intent = new Intent(IntentAction.DOWNLOAD);
			intent.putExtra(ServiceExtraKeys.TYPE_ID, DownloadService.TYPE_PDF_FILE);
			intent.setData(Uri.parse(pdfUrl));
			mContext.startService(intent);
		}

		private void fireFullImageIntent(final Cursor cursor) {

			int idColumn = cursor.getColumnIndexOrThrow(Attachments.Columns.ID);
			final int valueId = cursor.getInt(idColumn);
			Intent intent = new Intent(Intent.ACTION_VIEW, Attachments.buildTableUri(valueId));
			mContext.startActivity(intent);
		}

		private void fireYoutubeIntent(final Cursor cursor) {

			int youtubeVideoColumn = cursor.getColumnIndexOrThrow(Attachments.Columns.VIDEO_LINK_URL);
			final String youtubeLink = cursor.getString(youtubeVideoColumn);
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink));
			mContext.startActivity(intent);
		}
	}

	private class AttachmentsViewBinder implements ViewBinder {

		private final byte[] buffer = new byte[16 * 1024];

		@Override
		public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

			switch (view.getId()) {
				case R.id.image_attachment_preview:
					setPreviewImage((ImageView) view, cursor, columnIndex);
					return true;
				case R.id.text_attachment_name:
					((TextView) view).setText(cursor.getString(columnIndex));
					return true;
				case R.id.image_attachment_filetype:
					setFiletypeImage((ImageView) view, cursor, columnIndex);
					return true;
				default:
					Log.e(TAG, "[setViewValue] unknown view: " + view.getId());
					return false;
			}
		}

		private void setFiletypeImage(final ImageView imageView, final Cursor cursor, final int columnIndex) {

			int attachmentFormat = cursor.getInt(columnIndex);
			switch (attachmentFormat) {
				case AttachmentType.JPG:
				case AttachmentType.PNG:
				case AttachmentType.BMP:
					imageView.setImageResource(R.drawable.ic_filetype_image_white);
					break;
				case AttachmentType.PNG_SCREENSHOT:
					imageView.setImageResource(R.drawable.ic_filetype_screenshot_white);
					break;
				case AttachmentType.YOUTUBE:
					imageView.setImageResource(R.drawable.ic_filetype_youtube_white);
					break;
				case AttachmentType.PDF:
					imageView.setImageResource(R.drawable.ic_filetype_pdf_white);
					break;
				case AttachmentType.GENERAL_WEB_LINK:
				case AttachmentType.NONE:
					imageView.setImageResource(R.drawable.stub);
					break;
				default:
					Log.e(TAG, "[setPreviewImage] unknown attachment format: " + attachmentFormat);
					imageView.setImageResource(R.drawable.stub);
					break;
			}
		}

		private void setPreviewImage(final ImageView imageView, final Cursor cursor, final int columnIndex) {

			int formatColumnIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.FORMAT);
			int attachmentFormat = cursor.getInt(formatColumnIndex);
			switch (attachmentFormat) {
				case AttachmentType.JPG:
				case AttachmentType.PNG:
				case AttachmentType.BMP:
				case AttachmentType.PNG_SCREENSHOT:
					final int valueId = cursor.getInt(columnIndex);
					String urlString = Attachments.getAttachmentDownloadLink(mContext, valueId);
					imageLoader.displayImage(urlString, imageView);
					break;
				case AttachmentType.YOUTUBE:
					int youtubeThumbColumn = cursor
							.getColumnIndexOrThrow(Attachments.Columns.VIDEO_THUMB_URL);
					final String youtubeThumbString = cursor.getString(youtubeThumbColumn);
					imageLoader.displayImage(youtubeThumbString, imageView);
					break;
				case AttachmentType.PDF:
					int dataColumnIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.THUMB);
					byte[] thumbData = cursor.getBlob(dataColumnIndex);
					if (thumbData == null) {
						imageView.setImageResource(R.drawable.ic_attachment_pdf);
						break;
					}
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inTempStorage = buffer;
					options.inDither = false;
					options.inPurgeable = true;
					options.inInputShareable = true;
					options.inSampleSize = 2;
					Bitmap bitmap = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, options);
					imageView.setImageBitmap(bitmap);
					break;
				case AttachmentType.GENERAL_WEB_LINK:
				case AttachmentType.NONE:
					imageView.setImageResource(R.drawable.stub);
					break;
				default:
					Log.e(TAG, "[setPreviewImage] unknown attachment format: " + attachmentFormat);
					imageView.setImageResource(R.drawable.stub);
					break;
			}
		}
	}
}
