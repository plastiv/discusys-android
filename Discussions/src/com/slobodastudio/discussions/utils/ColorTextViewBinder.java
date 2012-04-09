package com.slobodastudio.discussions.utils;

import com.slobodastudio.discussions.R;

import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ColorTextViewBinder implements SimpleCursorAdapter.ViewBinder {

	private final int mColorId;
	private final String mtextColumnName;

	public ColorTextViewBinder(final String mtextColumnName, final int mColorId) {

		super();
		this.mtextColumnName = mtextColumnName;
		this.mColorId = mColorId;
	}

	@Override
	public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

		int viewId = view.getId();
		switch (viewId) {
			case R.id.image_person_color:
				ImageView colorView = (ImageView) view;
				// colorView.setBackgroundColor(mColorId);
				colorView.setBackgroundColor(cursor.getInt(columnIndex));
				return true;
			case R.id.list_item_text:
				TextView itemText = (TextView) view;
				itemText.setText(cursor.getString(columnIndex));
				return true;
			default:
				return false;
		}
	}
}
