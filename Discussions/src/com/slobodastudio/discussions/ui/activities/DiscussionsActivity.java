package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.ui.activities.base.BaseActivity;
import com.slobodastudio.discussions.ui.activities.base.BaseDetailFragment;
import com.slobodastudio.discussions.ui.activities.base.BaseListFragment;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

public class DiscussionsActivity extends BaseActivity {

	private static final String TAG = DiscussionsActivity.class.getSimpleName();

	@Override
	protected Fragment onCreatePane() {

		return new DiscussionsListFragment();
	}

	public class DiscussionsListFragment extends BaseListFragment {

		public DiscussionsListFragment() {

			super(R.string.fragment_empty_discussions, Discussions.Columns.SUBJECT, Discussions.Columns.ID,
					Discussions.CONTENT_URI);
		}

		@Override
		public boolean onContextItemSelected(final MenuItem item) {

			switch (item.getItemId()) {
				case R.id.menu_topics: {
					// Otherwise we need to launch a new activity to display
					// the dialog fragment with selected text.
					Uri uri = Discussions.buildTopicUri(getItemId(item));
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
					return true;
				}
				default:
					return super.onOptionsItemSelected(item);
			}
		}

		@Override
		protected BaseDetailFragment getDetailFragment() {

			return new DiscussionsDetailFragment();
		}
	}
}
