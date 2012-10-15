package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.utils.MyLog;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FileDownloader {

	private static final String DIRECTORY = Environment.DIRECTORY_DOWNLOADS;

	public static File createFile(final String fileName) {

		// Create a path where we will place our picture in the user's
		// public pictures directory. Note that you should be careful about
		// what you place here, since the user often manages these files. For
		// pictures and other media owned by the application, consider
		// Context.getExternalMediaDir().
		File path = Environment.getExternalStoragePublicDirectory(DIRECTORY);
		if (path.exists() == false) {
			path.mkdirs();
		}
		return new File(path, fileName);
	}

	private void deleteFile(final String fileName) {

		// Create a path where we will place our picture in the user's
		// public pictures directory and delete the file. If external
		// storage is not currently mounted this will fail.
		File path = Environment.getExternalStoragePublicDirectory(DIRECTORY);
		File file = new File(path, fileName);
		file.delete();
	}

	public static boolean hasFileDownloaded(final String fileName) {

		// Create a path where we will place our picture in the user's
		// public pictures directory and check if the file exists. If
		// external storage is not currently mounted this will think the
		// picture doesn't exist.
		File path = Environment.getExternalStoragePublicDirectory(DIRECTORY);
		File file = new File(path, fileName);
		return file.exists();
	}

	public static void downloadFromUrl(final String downloadUrl, final String fileName) {

		try {
			URL website = new URL(downloadUrl);
			Log.d("Downloader", "[DonwloadFile] path: " + website.getPath());
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			File file = createFile(fileName);
			FileOutputStream fos = new FileOutputStream(file);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		} catch (IOException e) {
			MyLog.e("FileDownloader", "Failed to download file: " + downloadUrl, e);
		}
	}
}
