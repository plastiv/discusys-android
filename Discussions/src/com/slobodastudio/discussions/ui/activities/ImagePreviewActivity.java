package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.ImageView;

import com.actionbarsherlock.view.MenuItem;

public class ImagePreviewActivity extends BaseActivity {

	private static final String EXTRA_URI = "EXTRA_URI";
	private ImageView imageView;

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onControlServiceConnected() {

		// No operation with service in this activity
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_preview);
		imageView = (ImageView) findViewById(R.id.iv_full_image);
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			startAttachmentImageLoader();
		}
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		Log.d("ImageView", getIntent().getDataString());
	}

	private void startAttachmentImageLoader() {

		AttachmentImageCursorLoader loader = new AttachmentImageCursorLoader();
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_URI, getIntent().getData());
		getSupportLoaderManager().initLoader(AttachmentImageCursorLoader.ATTACHMENT_ID, args, loader);
	}

	private class AttachmentImageCursorLoader implements LoaderCallbacks<Cursor> {

		private static final int ATTACHMENT_ID = 0x00;
		private final byte[] buffer = new byte[16 * 1024];

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			switch (loaderId) {
				case ATTACHMENT_ID:
					return getAttachmentCursorLoader(arguments);
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case ATTACHMENT_ID:
					imageView.setImageBitmap(null);
					getSupportActionBar().setTitle("");
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			switch (loader.getId()) {
				case ATTACHMENT_ID:
					swapAttachmentImage(data);
					swapAttachmentTitle(data);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		private CursorLoader getAttachmentCursorLoader(final Bundle arguments) {

			Uri attachmentUri = getUriFromArguments(arguments);
			return new CursorLoader(ImagePreviewActivity.this, attachmentUri, null, null, null, null);
		}

		private Uri getUriFromArguments(final Bundle arguments) {

			if (!arguments.containsKey(EXTRA_URI)) {
				throw new IllegalArgumentException("Loader was called without extra discussion uri");
			}
			return arguments.getParcelable(EXTRA_URI);
		}

		private void swapAttachmentImage(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int dataColumnIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.DATA);
				byte[] pictureData = cursor.getBlob(dataColumnIndex);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inTempStorage = buffer;
				options.inDither = false; // Disable Dithering mode
				options.inPurgeable = true; // Tell to gc that whether it needs free memory, the Bitmap can
											// be cleared
				options.inInputShareable = true; // Which kind of reference will be used to recover the
													// Bitmap data after being clear, when it will be used in
													// the future
				// options.inSampleSize = 8;
				Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length, options);
				imageView.setImageBitmap(bitmap);
			}
		}

		private void swapAttachmentTitle(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int titleColumn = cursor.getColumnIndexOrThrow(Attachments.Columns.NAME);
				String title = cursor.getString(titleColumn);
				getSupportActionBar().setTitle(title);
			}
		}
	}
}