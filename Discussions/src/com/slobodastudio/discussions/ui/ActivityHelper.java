package com.slobodastudio.discussions.ui;

import com.slobodastudio.discussions.ui.activities.WebViewActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class ActivityHelper {

	public static void startSearchWebActivityForResult(final Activity activity, final int requestCode) {

		startWebActivityForResult(activity, requestCode, "http://www.google.com");
	}

	public static void startSearchPictureActivityForResult(final Activity activity, final int requestCode) {

		startWebActivityForResult(activity, requestCode, "http://www.images.google.com");
	}

	public static void startSearchPdfActivityForResult(final Activity activity, final int requestCode) {

		startWebActivityForResult(activity, requestCode, "http://www.scholar.google.com");
	}

	public static void startWebActivityForResult(final Activity activity, final int requestCode,
			final String url) {

		Intent intent = new Intent(activity, WebViewActivity.class);
		Uri uri = Uri.parse(url);
		intent.setData(uri);
		activity.startActivityForResult(intent, requestCode);
	}
}
