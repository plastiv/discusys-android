package com.slobodastudio.discussions.utils;

import android.text.Editable;
import android.widget.EditText;

public class EditTextUtils {

	public static String toString(final EditText editText) {

		return toString(editText, "");
	}

	public static String toString(final EditText editText, final String defaultValue) {

		if (editText == null) {
			return defaultValue;
		}
		Editable editable = editText.getText();
		if (editable == null) {
			return defaultValue;
		}
		return editable.toString();
	}
}
