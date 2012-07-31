package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;

public class CommentDetailsActivity extends BaseActivity {

	private static final String EXTRA_URI = "extras_uri";
	private TextView commentTextView;

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
		setContentView(R.layout.activity_comment);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		commentTextView = (TextView) findViewById(R.id.textViewComment);
		startCommentsLoader();
	}

	private void startCommentsLoader() {

		CommentLoader loader = new CommentLoader();
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_URI, getIntent().getData());
		getSupportLoaderManager().initLoader(CommentLoader.COMMENT_ID, args, loader);
	}

	private class CommentLoader implements LoaderCallbacks<Cursor> {

		private static final int COMMENT_ID = 0x00;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			switch (loaderId) {
				case COMMENT_ID:
					return getCommentLoader(arguments);
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case COMMENT_ID:
					commentTextView.setText("");
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			switch (loader.getId()) {
				case COMMENT_ID:
					swapComment(data);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		private CursorLoader getCommentLoader(final Bundle arguments) {

			Uri commentsUri = getUriFromArguments(arguments);
			return new CursorLoader(CommentDetailsActivity.this, commentsUri, null, null, null, null);
		}

		private Uri getUriFromArguments(final Bundle arguments) {

			if (!arguments.containsKey(EXTRA_URI)) {
				throw new IllegalArgumentException("Loader was called without extra discussion uri");
			}
			return arguments.getParcelable(EXTRA_URI);
		}

		private void swapComment(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int commentColumn = cursor.getColumnIndexOrThrow(Comments.Columns.TEXT);
				String comment = cursor.getString(commentColumn);
				commentTextView.setText(comment);
			}
		}
	}
}