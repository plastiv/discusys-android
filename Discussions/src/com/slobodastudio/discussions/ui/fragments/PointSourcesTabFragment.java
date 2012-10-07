package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.model.SelectedPoint;
import com.slobodastudio.discussions.data.model.Source;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sources;
import com.slobodastudio.discussions.ui.ExtraKey;
import com.slobodastudio.discussions.ui.activities.BaseActivity;
import com.slobodastudio.discussions.ui.activities.WebViewActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PointSourcesTabFragment extends SherlockFragment {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final int PICK_URL_REQUEST = 0x01;
	private static final String TAG = PointSourcesTabFragment.class.getSimpleName();
	private boolean footerButtonsEnabled;
	private int mDescriptionId;
	private TextView mPointNameTextView;
	private SelectedPoint mSelectedPoint;
	private SimpleCursorAdapter mSourcesAdapter;
	private final SourcesCursorLoader mSourcesCursorLoader;
	private ListView mSourcesList;

	public PointSourcesTabFragment() {

		mSourcesCursorLoader = new SourcesCursorLoader();
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

	public static void requestUrlAttachment(final Activity activity) {

		Intent intent = new Intent(activity, WebViewActivity.class);
		activity.startActivityForResult(intent, PICK_URL_REQUEST);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		Log.d(TAG, "[onActivityresult]");
		switch (requestCode) {
			case PICK_URL_REQUEST:
				if (resultCode == Activity.RESULT_OK) {
					String description = data.getStringExtra(ExtraKey.BINARY_DATA_DESCRIPTION);
					onAttachSourceAdded(description);
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
		View attachmentsView = inflater.inflate(R.layout.tab_fragment_point_sources, container, false);
		mSourcesList = (ListView) attachmentsView.findViewById(R.id.listview_sources);
		addSourcesHeader();
		if (footerButtonsEnabled) {
			addSourcesFooter();
		}
		setSourcesAdapter();
		initSourcesLoader();
		return attachmentsView;
	}

	private void addSourcesFooter() {

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View footerView = layoutInflater.inflate(R.layout.layout_source_footer, null, false);
		setAttachUrlListener(footerView);
		mSourcesList.addFooterView(footerView);
	}

	private void addSourcesHeader() {

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View headerView = layoutInflater.inflate(R.layout.list_header_point_name, null, false);
		mPointNameTextView = (TextView) headerView.findViewById(R.id.list_header_point_name);
		mSourcesList.addHeaderView(headerView, null, false);
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

	private void initSourcesLoader() {

		Bundle args = new Bundle();
		args.putInt(ExtraKey.POINT_ID, mSelectedPoint.getPointId());
		getLoaderManager().initLoader(SourcesCursorLoader.POINT_NAME_ID, args, mSourcesCursorLoader);
		getLoaderManager().initLoader(SourcesCursorLoader.DESCRIPTION_ID, args, mSourcesCursorLoader);
	}

	private void onAttachSourceAdded(final String sourceLink) {

		Source source = new Source();
		source.setLink(sourceLink);
		source.setDescriptionId(mDescriptionId);
		((BaseActivity) getActivity()).getServiceHelper().insertSource(source, mSelectedPoint);
	}

	private void setAttachUrlListener(final View container) {

		Button attachUrlButton = (Button) container.findViewById(R.id.btn_attach_url);
		attachUrlButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {

				requestUrlAttachment(getActivity());
			}
		});
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
		private static final int POINT_NAME_ID = 0x01;
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
				case POINT_NAME_ID: {
					if (!arguments.containsKey(ExtraKey.POINT_ID)) {
						throw new IllegalArgumentException("Loader was called without point id");
					}
					int myPointId = arguments.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
					String where = Points.Columns.ID + "=?";
					String[] args = new String[] { String.valueOf(myPointId) };
					String[] projection = new String[] { BaseColumns._ID, Points.Columns.NAME };
					return new CursorLoader(getActivity(), Points.CONTENT_URI, projection, where, args, null);
				}
				case DESCRIPTION_ID: {
					if (!arguments.containsKey(ExtraKey.POINT_ID)) {
						throw new IllegalArgumentException("Loader was called without point id");
					}
					int myPointId = arguments.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
					String where = Descriptions.Columns.POINT_ID + "=?";
					String[] args = new String[] { String.valueOf(myPointId) };
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
				case POINT_NAME_ID:
					mPointNameTextView.setText("");
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
				case POINT_NAME_ID:
					if (data.moveToFirst()) {
						int nameColumnIndex = data.getColumnIndexOrThrow(Points.Columns.NAME);
						String name = data.getString(nameColumnIndex);
						mPointNameTextView.setText(name);
					}
					break;
				case DESCRIPTION_ID:
					if (data.getCount() == 1) {
						Description description = new Description(data);
						mDescriptionId = description.getId();
						Bundle args = new Bundle();
						args.putInt(ExtraKey.DESCRIPTION_ID, mDescriptionId);
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
