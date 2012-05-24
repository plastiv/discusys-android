package com.slobodastudio.discussions.ui.view;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments.AttachmentType;
import com.slobodastudio.discussions.utils.lazylist.ImageLoaderSingleton;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MediaList {

	private static final String TAG = MediaList.class.getSimpleName();
	private final com.slobodastudio.discussions.utils.lazylist.ImageLoader imageLoader;
	private SimpleCursorAdapter mAttachmentsAdapter;
	private final ListView mAttachmentsList;
	private final Context mContext;
	private int postionOffset = 0;

	public MediaList(final Context context, final ListView listView) {

		mContext = context;
		imageLoader = ImageLoaderSingleton.getInstance(mContext);
		mAttachmentsList = listView;
		mAttachmentsList.setOnItemClickListener(new AttachmentsItemClickListener());
		setAttachmentsAdapter();
	}

	public SimpleCursorAdapter getAdapter() {

		return mAttachmentsAdapter;
	}

	public ListView getListView() {

		return mAttachmentsList;
	}

	public void setPositionOffset(final int positionOffset) {

		postionOffset = positionOffset;
	}

	private void setAttachmentsAdapter() {

		mAttachmentsAdapter = new SimpleCursorAdapter(mContext, R.layout.list_item_media, null, new String[] {
				Attachments.Columns.TITLE, Attachments.Columns.ID }, new int[] { R.id.text_attachment_name,
				R.id.image_attachment_preview }, 0);
		mAttachmentsAdapter.setViewBinder(new AttachmentsViewBinder());
		mAttachmentsList.setAdapter(mAttachmentsAdapter);
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

		@Override
		public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

			switch (view.getId()) {
				case R.id.image_attachment_preview:
					setPreviewImage((ImageView) view, cursor, columnIndex);
					return true;
				case R.id.text_attachment_name:
					((TextView) view).setText(cursor.getString(columnIndex));
					return true;
				default:
					Log.e(TAG, "[setViewValue] unknown view: " + view.getId());
					return false;
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
					String urlString = Attachments.getAttachmentDownloadLink(valueId);
					imageLoader.DisplayImage(urlString, imageView);
					break;
				case AttachmentType.YOUTUBE:
					int youtubeThumbColumn = cursor
							.getColumnIndexOrThrow(Attachments.Columns.VIDEO_THUMB_URL);
					final String youtubeThumbString = cursor.getString(youtubeThumbColumn);
					imageLoader.DisplayImage(youtubeThumbString, imageView);
					break;
				case AttachmentType.PDF:
					imageView.setImageResource(R.drawable.ic_attachment_pdf);
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
