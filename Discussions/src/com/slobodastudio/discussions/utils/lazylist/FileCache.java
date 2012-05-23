package com.slobodastudio.discussions.utils.lazylist;

import android.content.Context;

import java.io.File;

public class FileCache {

	private File cacheDir;

	public FileCache(final Context context) {

		// Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "LazyList");
		} else {
			cacheDir = context.getCacheDir();
		}
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
	}

	public void clear() {

		File[] files = cacheDir.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			f.delete();
		}
	}

	public File getFile(final String url) {

		// I identify images by hashcode. Not a perfect solution, good for the demo.
		String filename = String.valueOf(url.hashCode());
		// Another possible solution (thanks to grantland)
		// String filename = URLEncoder.encode(url);
		File f = new File(cacheDir, filename);
		return f;
	}
}