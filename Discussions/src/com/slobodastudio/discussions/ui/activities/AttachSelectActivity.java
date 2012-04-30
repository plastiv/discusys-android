package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Attachments;
import com.slobodastudio.discussions.ui.ExtraKey;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;

public class AttachSelectActivity extends BaseActivity {

	private static final int PICK_IMAGE_REQUEST = 0x02;
	private static final int PICK_URL_REQUEST = 0x01;
	private static final String TAG = AttachSelectActivity.class.getSimpleName();

	private static byte[] getBitmapAsByteArray(final Bitmap bitmap) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// Middle value is quality, but PNG is lossless, so it's ignored.
		bitmap.compress(CompressFormat.PNG, 0, outputStream);
		return outputStream.toByteArray();
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		Log.d(TAG, "[onActivityResult] ");
		switch (requestCode) {
			case PICK_URL_REQUEST:
				if (resultCode == Activity.RESULT_OK) {
					byte[] bitmapArray = data.getByteArrayExtra(ExtraKey.BINARY_DATA);
					String description = data.getStringExtra(ExtraKey.BINARY_DATA_DESCRIPTION);
					Bundle attachment = new Bundle();
					attachment.putInt(Attachments.Columns.ID, 1);
					attachment.putString(Attachments.Columns.NAME, description);
					attachment.putByteArray(Attachments.Columns.DATA, bitmapArray);
					attachment.putInt(Attachments.Columns.POINT_ID, 1);
					attachment.putInt(Attachments.Columns.PERSON_ID, 1);
					attachment.putInt(Attachments.Columns.FORMAT, Attachments.AttachmentType.PNG);
					getServiceHelper().insertAttachment(attachment, 1, 1);
				}
				break;
			case PICK_IMAGE_REQUEST:
				if (resultCode == Activity.RESULT_OK) {
					Uri selectedImageUri = data.getData();
					String[] projection = { MediaColumns.DATA };
					Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
					int column_index_data = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
					cursor.moveToFirst();
					String selectedImagePath = cursor.getString(column_index_data);
					cursor.close();
					Bitmap galleryImage = BitmapFactory.decodeFile(selectedImagePath);
					byte[] bitmapArray = getBitmapAsByteArray(galleryImage);
					Bundle attachment = new Bundle();
					attachment.putInt(Attachments.Columns.ID, 1);
					attachment.putString(Attachments.Columns.NAME, "Attached image");
					attachment.putByteArray(Attachments.Columns.DATA, bitmapArray);
					attachment.putInt(Attachments.Columns.POINT_ID, 1);
					attachment.putInt(Attachments.Columns.PERSON_ID, 1);
					attachment.putInt(Attachments.Columns.FORMAT, Attachments.AttachmentType.BMP);
					getServiceHelper().insertAttachment(attachment, 1, 1);
				}
				break;
			default:
				break;
		}
	}

	public void onAttachImage(final View v) {

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, PICK_IMAGE_REQUEST);
	}

	public void onAttachUrl(final View v) {

		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("text/url");
		startActivityForResult(intent, PICK_URL_REQUEST);
	}

	public void onAttachVideo(final View v) {

		// Intent intent = new Intent();
		// intent.setType("application/pdf");
		// intent.setAction(Intent.ACTION_GET_CONTENT);
		// startActivityForResult(intent, PICK_IMAGE_REQUEST);
		// FIXME: load pdf as a file here
		// http://stackoverflow.com/questions/8646246/uri-from-intent-action-get-content-into-file
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_attach);
	}

	@Override
	protected void onControlServiceConnected() {

		// TODO: this activity doesn't react with service
	}
}
