package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.ui.activities.base.BaseActivity;
import com.slobodastudio.discussions.ui.activities.base.BaseDetailFragment;
import com.slobodastudio.discussions.ui.activities.base.BaseListFragment;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class TopicsActivity extends BaseActivity {

	private static final String TAG = TopicsActivity.class.getSimpleName();

	@Override
	protected Fragment onCreatePane() {

		return new TopicsListFragment();
	}

	public class TopicsListFragment extends BaseListFragment {

		public TopicsListFragment() {

			super(R.string.fragment_empty_topics, Topics.Columns.NAME, Topics.Columns.ID, Topics.CONTENT_URI);
		}

		@Override
		public boolean onContextItemSelected(final MenuItem item) {

			switch (item.getItemId()) {
				case R.id.menu_points: {
					// Otherwise we need to launch a new activity to display
					// the dialog fragment with selected text.
					Uri uri = Topics.buildPointUri(getItemId(item));
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
					return true;
				}
				default:
					return super.onContextItemSelected(item);
			}
		}

		@Override
		public void onListItemClick(final ListView l, final View v, final int position, final long id) {

			super.onListItemClick(l, v, position, id);
			Uri uri = Topics.buildPointUri(getItemId(position));
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}

		@Override
		protected BaseDetailFragment getDetailFragment() {

			return new TopicsDetailFragment();
		}
	}
}
