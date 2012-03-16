package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class PointsDetailFragment extends BaseDetailFragment {

	public PointsDetailFragment() {

		super(Points.CONTENT_URI);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		if (container == null) {
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.
			return null;
		}
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.point_desctiption_item, null);
		if (getArguments() != null) {
			if (getShownId() == BaseDetailFragment.NO_SELECTION_ID) {
				// TODO: move string to resources
				TextView text = (TextView) inflater.inflate(R.layout.details_item, null);
				text.setText("Select item to show details");
				return text;
			}
			Cursor cursor = getActivity().getContentResolver().query(getDetailsUri(), null, null, null, null);
			if (cursor.getCount() > 0) {
				Point value = new Point(cursor);
				// name
				EditText editText = (EditText) layout.findViewById(R.id.et_point_name);
				editText.setText(value.getName());
				// agreement code
				Spinner s = (Spinner) layout.findViewById(R.id.spinner_point_agreement_code);
				s.setSelection(value.getAgreementCode());
				// shared to public
				CheckBox checkBox = (CheckBox) layout.findViewById(R.id.chb_share_to_public);
				checkBox.setChecked(value.isSharedToPublic());
			}
		}
		return layout;
	}
}
