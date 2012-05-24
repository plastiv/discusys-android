package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.ui.view.MediaList;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.method.ScrollingMovementMethod;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;

public class DiscussionInfoActivity extends BaseActivity {

	private static final String EXTRA_URI = "EXTRA_URI";
	private TextView discussionText;
	private MediaList mediaList;

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
		setContentView(R.layout.activity_discussion_description);
		discussionText = (TextView) findViewById(R.id.tv_discussion_description);
		discussionText.setMovementMethod(new ScrollingMovementMethod());
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			startDiscussionInfoLoader();
		}
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		mediaList = new MediaList(this, (ListView) findViewById(R.id.listview_attachments));
	}

	private void startDiscussionInfoLoader() {

		DiscussionInfoCursorLoader loader = new DiscussionInfoCursorLoader();
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_URI, getIntent().getData());
		getSupportLoaderManager().initLoader(DiscussionInfoCursorLoader.DESCRIPTION_ID, args, loader);
		getSupportLoaderManager().initLoader(DiscussionInfoCursorLoader.DISCUSSION_ID, args, loader);
		getSupportLoaderManager().initLoader(DiscussionInfoCursorLoader.ATTACHMENT_ID, args, loader);
	}

	private class DiscussionInfoCursorLoader implements LoaderCallbacks<Cursor> {

		private static final int ATTACHMENT_ID = 0x02;
		private static final int DESCRIPTION_ID = 0x00;
		private static final int DISCUSSION_ID = 0x01;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			switch (loaderId) {
				case DESCRIPTION_ID:
					return getDescriptionCursorLoader(arguments);
				case DISCUSSION_ID:
					return getDiscussionCursorLoader(arguments);
				case ATTACHMENT_ID:
					return getAttachmentCursorLoader(arguments);
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case DESCRIPTION_ID:
					discussionText.setText("");
					break;
				case DISCUSSION_ID:
					getSupportActionBar().setTitle("");
					break;
				case ATTACHMENT_ID:
					mediaList.getAdapter().swapCursor(null);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			switch (loader.getId()) {
				case DESCRIPTION_ID:
					swapDiscussionInfoText(data);
					break;
				case DISCUSSION_ID:
					swapDiscussionTitle(data);
					break;
				case ATTACHMENT_ID:
					mediaList.getAdapter().swapCursor(data);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		private CursorLoader getAttachmentCursorLoader(final Bundle arguments) {

			Uri discussionUri = getUriFromArguments(arguments);
			String discussionId = Discussions.getValueId(discussionUri);
			String where = Attachments.Columns.DISCUSSION_ID + "=?";
			String[] args = new String[] { discussionId };
			return new CursorLoader(DiscussionInfoActivity.this, Attachments.CONTENT_URI, null, where, args,
					null);
		}

		private CursorLoader getDescriptionCursorLoader(final Bundle arguments) {

			Uri discussionUri = getUriFromArguments(arguments);
			String discussionId = Discussions.getValueId(discussionUri);
			String where = Descriptions.Columns.DISCUSSION_ID + "=?";
			String[] args = new String[] { discussionId };
			return new CursorLoader(DiscussionInfoActivity.this, Descriptions.CONTENT_URI, null, where, args,
					null);
		}

		private CursorLoader getDiscussionCursorLoader(final Bundle arguments) {

			Uri discussionUri = getUriFromArguments(arguments);
			return new CursorLoader(DiscussionInfoActivity.this, discussionUri, null, null, null, null);
		}

		private Uri getUriFromArguments(final Bundle arguments) {

			if (!arguments.containsKey(EXTRA_URI)) {
				throw new IllegalArgumentException("Loader was called without extra discussion uri");
			}
			return arguments.getParcelable(EXTRA_URI);
		}

		private void swapDiscussionInfoText(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int textColumn = cursor.getColumnIndexOrThrow(Descriptions.Columns.TEXT);
				String text = cursor.getString(textColumn);
				discussionText.setText(text);
			}
		}

		private void swapDiscussionTitle(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int subjectColumn = cursor.getColumnIndexOrThrow(Discussions.Columns.SUBJECT);
				String subject = cursor.getString(subjectColumn);
				getSupportActionBar().setTitle(subject);
			}
		}
	}
}