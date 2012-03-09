package com.slobodastudio.discussions.ui.activities.base;

import com.slobodastudio.discussions.ui.activities.IntentExtrasKey;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/** Override onCreate view to add your own details view. */
public abstract class BaseDetailFragment extends Fragment {

	protected static final int NO_SELECTION_ID = -1;
	private final Uri baseUri;

	public BaseDetailFragment(final Uri baseUri) {

		super();
		this.baseUri = baseUri;
	}

	public Uri getDetailsUri() {

		if (getArguments().containsKey(IntentExtrasKey.ID)) {
			return ContentUris.withAppendedId(baseUri, getArguments().getInt(IntentExtrasKey.ID,
					NO_SELECTION_ID));
		}
		return getActivity().getIntent().getData();
	}

	public int getShownId() {

		return getArguments().getInt(IntentExtrasKey.ID);
	}

	@Override
	public abstract View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	public void setArgumentId(final int id) {

		Bundle args = new Bundle();
		args.putInt(IntentExtrasKey.ID, id);
		setArguments(args);
	}
}