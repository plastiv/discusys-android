package com.slobodastudio.discussions.utils.lazylist;

import com.slobodastudio.discussions.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

	private final ExecutorService executorService;
	private final FileCache fileCache;
	private final MemoryCache memoryCache = new MemoryCache();
	private final int stub_id = R.drawable.stub;
	private final Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	private boolean scaled = true;

	public ImageLoader(final Context context) {

		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	public void clearCache() {

		memoryCache.clear();
		fileCache.clear();
	}

	public void DisplayImage(final String url, final ImageView imageView) {

		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageView);
			imageView.setImageResource(stub_id);
		}
	}

	public void setScaled(final boolean scaled) {

		this.scaled = scaled;
	}

	boolean imageViewReused(final PhotoToLoad photoToLoad) {

		String tag = imageViews.get(photoToLoad.imageView);
		if ((tag == null) || !tag.equals(photoToLoad.url)) {
			return true;
		}
		return false;
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(final File f) {

		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream inputStream = new FileInputStream(f);
			BitmapFactory.decodeStream(inputStream, null, o);
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			if (scaled) {
				final int REQUIRED_SIZE = 70;
				int width_tmp = o.outWidth, height_tmp = o.outHeight;
				while (true) {
					if (((width_tmp / 2) < REQUIRED_SIZE) || ((height_tmp / 2) < REQUIRED_SIZE)) {
						break;
					}
					width_tmp /= 2;
					height_tmp /= 2;
					scale *= 2;
				}
			}
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			FileInputStream inputStream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream2, null, o2);
			try {
				inputStream2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return bitmap;
		} catch (FileNotFoundException e) {}
		return null;
	}

	private Bitmap getBitmap(final String url) {

		File f = fileCache.getFile(url);
		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null) {
			return b;
		}
		// from web
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			Log.d("ImageLoader", "filename" + f.toString());
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Exception ex) {
			Log.d("ImageLoader", "fromWeb error " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}

	private void queuePhoto(final String url, final ImageView imageView) {

		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {

		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(final Bitmap b, final PhotoToLoad p) {

			bitmap = b;
			photoToLoad = p;
		}

		@Override
		public void run() {

			if (imageViewReused(photoToLoad)) {
				return;
			}
			if (bitmap != null) {
				photoToLoad.imageView.setImageBitmap(bitmap);
			} else {
				photoToLoad.imageView.setImageResource(stub_id);
			}
		}
	}

	class PhotosLoader implements Runnable {

		PhotoToLoad photoToLoad;

		PhotosLoader(final PhotoToLoad photoToLoad) {

			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {

			if (imageViewReused(photoToLoad)) {
				return;
			}
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad)) {
				return;
			}
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	// Task for the queue
	private class PhotoToLoad {

		public ImageView imageView;
		public String url;

		public PhotoToLoad(final String u, final ImageView i) {

			url = u;
			imageView = i;
		}
	}
}
