package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.ExtraKey;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class WebViewActivity extends BaseActivity {

	WebView mWebView;

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl("http://www.google.com");
		mWebView.getSettings().setBuiltInZoomControls(true);
		final EditText urlEditText = (EditText) findViewById(R.id.edittext_url);
		urlEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_GO) {
					// search pressed and perform your functionality.
					String enteredText = urlEditText.getText().toString();
					if (!enteredText.startsWith("http")) {
						enteredText = "http://www." + enteredText;
						urlEditText.setText(enteredText);
					}
					mWebView.loadUrl(enteredText);
					return true;
				}
				return false;
			}
		});
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {

				urlEditText.setText(url);
				mWebView.loadUrl(url);
				return true;
			}
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
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onControlServiceConnected() {

		// TODO: this activity doesn't react with service
	}

	private void onActionSave() {

		Intent intent = getIntent();
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
