package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.ui.activities.base.BaseActivity;
import com.slobodastudio.discussions.ui.activities.base.BaseDetailFragment;
import com.slobodastudio.discussions.ui.activities.base.BaseListFragment;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;

public class PointsActivity extends BaseActivity {

	private static final String TAG = PointsActivity.class.getSimpleName();

	@Override
	protected Fragment onCreatePane() {

		return new PointsListFragment();
	}

	public class PointsListFragment extends BaseListFragment {

		public PointsListFragment() {

			super(R.string.fragment_empty_points, Points.Columns.NAME, Points.Columns.ID, Points.CONTENT_URI);
		}

		@Override
		public void onListItemClick(final ListView l, final View v, final int position, final long id) {

			super.onListItemClick(l, v, position, id);
			showDetails(position);
		}

		@Override
		protected BaseDetailFragment getDetailFragment() {

			return new PointsDetailFragment();
		}
	}
}
