package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.ExtraKey;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.io.ByteArrayOutputStream;

public class WebViewActivity extends BaseActivity {

	WebView mWebView;

	private static byte[] getBitmapAsByteArray(final Bitmap bitmap) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// Middle value is quality, but PNG is lossless, so it's ignored.
		bitmap.compress(CompressFormat.PNG, 0, outputStream);
		return outputStream.toByteArray();
	}

	private static int getSmallestSide(final Bitmap bitmap) {

		if (bitmap.getWidth() > bitmap.getHeight()) {
			return bitmap.getHeight();
		}
		return bitmap.getWidth();
	}

	private static Bitmap pictureDrawable2Bitmap(final Picture picture) {

		PictureDrawable pictureDrawable = new PictureDrawable(picture);
		Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable
				.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawPicture(pictureDrawable.getPicture());
		return bitmap;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl("http://www.google.com");
		mWebView.getSettings().setBuiltInZoomControls(true);
		final EditText urlEditText = (EditText) findViewById(R.id.edittext_url);
		urlEditText.setEnabled(false);
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {

				urlEditText.setText(url);
				mWebView.loadUrl(url);
				return true;
			}
			// @Override
			// public void onLoadResource(WebView view, String url){
			// if( url.equals("http://cnn.com") ){
			// // do whatever you want
			// //download the image from url and save it whereever you want
			// }
			// }
		});
		mWebView.requestFocus();
	}

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_webview, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_cancel:
				setMyResult(RESULT_CANCELED, null);
				finish();
				break;
			case R.id.menu_new_attachment:
				onActionSave();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onControlServiceConnected() {

		// TODO: this activity doesn't react with service
	}

	private void onActionSave() {

		// FIXME add try catch out of memory block
		Bitmap nonCroppedBitmap = pictureDrawable2Bitmap(mWebView.capturePicture());
		int croppedWidht = getSmallestSide(nonCroppedBitmap);
		int croppedHeight = getSmallestSide(nonCroppedBitmap);
		Bitmap croppedBitmap = Bitmap.createBitmap(nonCroppedBitmap, 0, 0, croppedWidht, croppedHeight);
		byte[] bitmapArray = getBitmapAsByteArray(croppedBitmap);
		Intent intent = getIntent();
		intent.putExtra(ExtraKey.BINARY_DATA, bitmapArray);
		intent.putExtra(ExtraKey.BINARY_DATA_DESCRIPTION, mWebView.getUrl());
		setMyResult(RESULT_OK, intent);
		finish();
	}

	private void setMyResult(final int code, final Intent intent) {

		if (getParent() == null) {
			setResult(code, intent);
		} else {
			getParent().setResult(code, intent);
		}
	}
}
