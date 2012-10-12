package com.slobodastudio.discussions.ui;

import com.slobodastudio.discussions.ui.activities.WebViewActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class ActivityHelper {

	public static void startSearchWebActivityForResult(final Activity activity, final int requestCode) {

		Intent intent = new Intent(activity, WebViewActivity.class);
		Uri uri = Uri.parse("http://www.google.com");
		intent.setData(uri);
		activity.startActivityForResult(intent, requestCode);
	}

	public static void startSearchPictureActivityForResult(final Activity activity, final int requestCode) {

		Intent intent = new Intent(activity, WebViewActivity.class);
		Uri uri = Uri.parse("http://www.images.google.com");
		intent.setData(uri);
		activity.startActivityForResult(intent, requestCode);
	}
}
