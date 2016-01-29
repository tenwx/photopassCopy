package com.pictureair.photopass.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.pictureair.photopass.blur.BlurUtil;

public class UniversalImageLoadTool {
	private static final String TAG = "UniversalImageLoadTool";
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
			System.out.println("no tag"+"___"+uri);
			if (imageView.getTag() != null) {
				System.out.println("old tag is "+imageView.getTag().toString());
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
			System.out.println("tag--"+imageView.getTag().toString()+"___"+uri);
		}
	}


	/**
	 * 使用imageloader加载图片，可以解决图片闪烁问题
	 * @param uri
	 * @param imageView
	 */
	public static void loadDiscoverImage(final String uri, final ImageView imageView, DisplayImageOptions option){
		if (imageView.getTag() == null || !imageView.getTag().toString().equals(uri)) {
			System.out.println("no tag" + imageView.getTag());
			imageLoader.displayImage(uri, imageView, option, new ImageLoadingListener() {

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
			System.out.println("tag--"+imageView.getTag().toString()+"___"+uri);
		}
	}

	/**
	 * 直接通过url生成高斯模糊图
	 * @param uri
	 * @param imageView
	 */
	public static void loadBlurImage(final String uri, final ImageView imageView, DisplayImageOptions options){
		Log.d(TAG, "url-->"+uri);
		if (imageView.getTag() == null || !imageView.getTag().toString().equals(uri)) {
			System.out.println("blur no tag");
			imageLoader.displayImage(uri, imageView, options, new ImageLoadingListener() {

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
					imageView.setImageBitmap(BlurUtil.blur(loadedImage));
					imageView.setTag(uri);
				}

				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					// TODO Auto-generated method stub

				}
			});
		}else {
			System.out.println("blur tag--"+imageView.getTag().toString()+"___"+uri);
		}
	}

	public static void clear(){
		imageLoader.clearMemoryCache();		
		imageLoader.clearDiskCache();
	}

	public static void resume(){
		imageLoader.resume();
	}
	/**
	 * 暂停加载
	 */
	public static void pause(){
		imageLoader.pause();
	}
	/**
	 * 停止加载
	 */
	public static void stop(){
		imageLoader.stop();
	}
	/**
	 * 销毁加载
	 */
	public static void destroy() {
		imageLoader.destroy();
	}
}
