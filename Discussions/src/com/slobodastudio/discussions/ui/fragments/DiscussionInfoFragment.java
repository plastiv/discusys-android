package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.ui.ExtraKey;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class DiscussionInfoFragment extends SherlockFragment {

	private static final String TAG = DiscussionInfoFragment.class.getSimpleName();
	private TextView discussionText;

	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
	public static Bundle intentToFragmentArguments(final Intent intent) {

		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}
		final String action = intent.getAction();
		if (action != null) {
			arguments.putString(ExtraKey.ACTION, action);
		}
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			arguments.putAll(intent.getExtras());
		}
		return arguments;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		String action;
		if (getArguments() != null) {
			action = getArguments().getString(ExtraKey.ACTION);
		} else {
			action = getActivity().getIntent().getAction();
		}
		if (Intent.ACTION_VIEW.equals(action)) {
			startDiscussionInfoLoader();
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		// setup layout
		View layout = inflater.inflate(R.layout.fragment_discussion_description, container, false);
		discussionText = (TextView) layout.findViewById(R.id.tv_discussion_description);
		discussionText.setMovementMethod(new ScrollingMovementMethod());
		return layout;
	}

	private void startDiscussionInfoLoader() {

		DiscussionInfoCursorLoader loader = new DiscussionInfoCursorLoader();
		Bundle args = new Bundle();
		Uri uri;
		if (getArguments() != null) {
			uri = getArguments().getParcelable(ExtraKey.URI);
		} else {
			uri = getActivity().getIntent().getData();
		}
		args.putParcelable(ExtraKey.URI, uri);
		getSherlockActivity().getSupportLoaderManager().initLoader(DiscussionInfoCursorLoader.DESCRIPTION_ID,
				args, loader);
		getSherlockActivity().getSupportLoaderManager().initLoader(DiscussionInfoCursorLoader.DISCUSSION_ID,
				args, loader);
	}

	private class DiscussionInfoCursorLoader implements LoaderCallbacks<Cursor> {

		private static final int DESCRIPTION_ID = 0x00;
		private static final int DISCUSSION_ID = 0x01;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			switch (loaderId) {
				case DESCRIPTION_ID:
					return getDescriptionCursorLoader(arguments);
				case DISCUSSION_ID:
					return getDiscussionCursorLoader(arguments);
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
					getSherlockActivity().getSupportActionBar().setTitle("");
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
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		private CursorLoader getDescriptionCursorLoader(final Bundle arguments) {

			Uri discussionUri = getUriFromArguments(arguments);
			String discussionId = Discussions.getValueId(discussionUri);
			String where = Descriptions.Columns.DISCUSSION_ID + "=?";
			String[] args = new String[] { discussionId };
			return new CursorLoader(getActivity(), Descriptions.CONTENT_URI, null, where, args, null);
		}

		private CursorLoader getDiscussionCursorLoader(final Bundle arguments) {

			Uri discussionUri = getUriFromArguments(arguments);
			return new CursorLoader(getActivity(), discussionUri, null, null, null, null);
		}

		private Uri getUriFromArguments(final Bundle arguments) {

			if (!arguments.containsKey(ExtraKey.URI)) {
				throw new IllegalArgumentException("Loader was called without extra discussion uri");
			}
			return arguments.getParcelable(ExtraKey.URI);
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
				getSherlockActivity().getSupportActionBar().setTitle(subject);
			}
		}
	}
}
