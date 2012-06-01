package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.ui.view.TouchImageView;
import com.slobodastudio.discussions.utils.lazylist.ImageLoader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ImagePreviewActivity extends BaseActivity {

	private static final String EXTRA_URI = "EXTRA_URI";
	private ImageLoader imageLoader;
	private TouchImageView imageView;

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_image_preview, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menu_delete:
				Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
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
		imageView = (TouchImageView) findViewById(R.id.iv_full_image);
		imageLoader = new ImageLoader(getApplicationContext());
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			String attachmentId = Attachments.getValueId(getIntent().getData());
			String uriString = Attachments.getAttachmentDownloadLink(this, attachmentId);
			imageLoader.setScaled(false);
			imageLoader.DisplayImage(uriString, imageView);
			startAttachmentImageLoader();
		}
		getSupportActionBar().setDisplayShowHomeEnabled(false);
	}

	private void startAttachmentImageLoader() {

		AttachmentImageCursorLoader loader = new AttachmentImageCursorLoader();
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_URI, getIntent().getData());
		getSupportLoaderManager().initLoader(AttachmentImageCursorLoader.ATTACHMENT_ID, args, loader);
	}

	private class AttachmentImageCursorLoader implements LoaderCallbacks<Cursor> {

		private static final int ATTACHMENT_ID = 0x00;

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

		private void swapAttachmentTitle(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int titleColumn = cursor.getColumnIndexOrThrow(Attachments.Columns.TITLE);
				String title = cursor.getString(titleColumn);
				getSupportActionBar().setTitle(title);
			}
		}
	}
}