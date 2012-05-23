package com.slobodastudio.discussions.utils.lazylist;

import android.content.Context;

public class ImageLoaderSingleton {

	private static volatile ImageLoader instance;

	private ImageLoaderSingleton() {

	}

	public static ImageLoader getInstance(final Context applicationContext) {

		if (instance == null) {
			synchronized (ImageLoader.class) {
				if (instance == null) {
					instance = new ImageLoader(applicationContext);
				}
			}
		}
		return instance;
	}
}
