package com.pictureworks.android.util;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class UniversalImageLoadTool {
	private static ImageLoader imageLoader = ImageLoader.getInstance();

	public static ImageLoader getImageLoader(){
		return imageLoader;
	}

	/**
	 * 使用imageloader加载图片，可以解决图片闪烁问题
	 * @param uri
	 * @param imageView
	 */
	public static void loadImage(final String uri, final ImageView imageView){
		if (imageView.getTag() == null || !imageView.getTag().toString().equals(uri)) {
			PictureAirLog.out("no tag"+"___"+uri);
			if (imageView.getTag() != null) {
				PictureAirLog.out("old tag is "+imageView.getTag().toString());
			}
			imageLoader.displayImage(uri, imageView, new ImageLoadingListener() {

				@Override
				public void onLoadingStarted(String imageUri, View view) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onLoadingFailed(String imageUri, View view,
						FailReason failReason) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					// TODO Auto-generated method stub
					imageView.setTag(uri);
				}

				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					// TODO Auto-generated method stub

				}

			});
		}else {
			PictureAirLog.out("tag--"+imageView.getTag().toString()+"___"+uri);
		}
	}

	public static void clear(){
		imageLoader.clearMemoryCache();		
		imageLoader.clearDiskCache();
	}

}
