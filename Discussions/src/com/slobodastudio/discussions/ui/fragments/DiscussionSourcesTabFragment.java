package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sources;
import com.slobodastudio.discussions.ui.ExtraKey;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class DiscussionSourcesTabFragment extends SherlockFragment {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = DiscussionSourcesTabFragment.class.getSimpleName();
	private SimpleCursorAdapter mSourcesAdapter;
	private final SourcesCursorLoader mSourcesCursorLoader;
	private ListView mSourcesList;

	public DiscussionSourcesTabFragment() {

		mSourcesCursorLoader = new SourcesCursorLoader();
	}

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
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		View attachmentsView = inflater.inflate(R.layout.tab_fragment_point_sources, container, false);
		mSourcesList = (ListView) attachmentsView.findViewById(R.id.listview_sources);
		setSourcesAdapter();
		initSourcesLoader();
		return attachmentsView;
	}

	private void initSourcesLoader() {

		Bundle args = new Bundle();
		Uri uri;
		if (getArguments() != null) {
			uri = getArguments().getParcelable(ExtraKey.URI);
		} else {
			uri = getActivity().getIntent().getData();
		}
		args.putParcelable(ExtraKey.URI, uri);
		getLoaderManager().initLoader(SourcesCursorLoader.DESCRIPTION_ID, args, mSourcesCursorLoader);
	}

	private void setSourcesAdapter() {

		mSourcesAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_source, null,
				new String[] { Sources.Columns.LINK }, new int[] { R.id.text_source_link }, 0);
		mSourcesList.setAdapter(mSourcesAdapter);
		mSourcesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position,
					final long id) {

				TextView linkTextView = (TextView) view.findViewById(R.id.text_source_link);
				if (!TextUtils.isEmpty(linkTextView.getText())) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkTextView.getText()
							.toString()));
					startActivity(intent);
				}
			}
		});
	}

	private class SourcesCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {

		private static final int DESCRIPTION_ID = 0x02;
		private static final int SOURCE_ID = 0x00;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			switch (loaderId) {
				case SOURCE_ID: {
					if (!arguments.containsKey(ExtraKey.DESCRIPTION_ID)) {
						throw new IllegalArgumentException("Loader was called without description id");
					}
					int descriptionId = arguments.getInt(ExtraKey.DESCRIPTION_ID, Integer.MIN_VALUE);
					String where = Sources.Columns.DESCRIPTION_ID + "=?";
					String[] args = new String[] { String.valueOf(descriptionId) };
					return new CursorLoader(getActivity(), Sources.CONTENT_URI, null, where, args, null);
				}
				case DESCRIPTION_ID: {
					Uri uri = arguments.getParcelable(ExtraKey.URI);
					int discussionId = Integer.valueOf(Discussions.getValueId(uri));
					String where = Descriptions.Columns.DISCUSSION_ID + "=?";
					String[] args = new String[] { String.valueOf(discussionId) };
					return new CursorLoader(getActivity(), Descriptions.CONTENT_URI, null, where, args, null);
				}
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case SOURCE_ID:
					mSourcesAdapter.swapCursor(null);
					break;
				case DESCRIPTION_ID:
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
				case SOURCE_ID:
					mSourcesAdapter.swapCursor(data);
					break;
				case DESCRIPTION_ID:
					if (data.getCount() == 1) {
						Description description = new Description(data);
						Bundle args = new Bundle();
						args.putInt(ExtraKey.DESCRIPTION_ID, description.getId());
						getLoaderManager().initLoader(SourcesCursorLoader.SOURCE_ID, args,
								mSourcesCursorLoader);
					} else {
						Log.w(TAG, "[onLoadFinished] LOADER_DESCRIPTION_ID count was: " + data.getCount());
					}
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}
	}
}
