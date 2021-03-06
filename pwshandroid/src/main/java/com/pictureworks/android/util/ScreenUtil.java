package com.pictureworks.android.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**屏幕计算*/
public class ScreenUtil {

	/**
	 * 得到设备屏幕的宽度
	 */
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 得到设备屏幕的高度
	 */
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	/**
	 * 得到设备的密度
	 */
	public static float getScreenDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}

	/**
	 * 把密度转换为像素
	 */
	public static int dip2px(Context context, float px) {
		final float scale = getScreenDensity(context);
		return (int) (px * scale + 0.5f);
	}
	/**
	 * 把像素转换为密度
	 */
	public static int px2dip(Context context, float pxValue) {
	    final float scale = getScreenDensity(context);
	    return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 得到url中正确的文件名,根据 isVideo 获取是视频还是图片
	 * @param url 原始url
	 * @param isVideo 判断是照片还是视频，0 代表是照片。
	 * @return 文件名
	 */
	public static String getReallyFileName(String url,int isVideo) {
		String filename = url;
		if (isVideo == 0){
			if (url.endsWith(".jpg") || url.endsWith(".JPG")) {
				filename = filename.substring(filename.lastIndexOf("/") + 1);
			} else {
				filename = filename.substring(filename.lastIndexOf("/") + 1)+".jpg";
			}
        } else {
            if (url.endsWith(".mp4") || url.endsWith(".MP4")) {
				filename = filename.substring(filename.lastIndexOf("/") + 1);
			} else {
				filename = filename.substring(filename.lastIndexOf("/") + 1)+".mp4";
			}
		}
		return filename;
	}


	/**
	 * 设置全屏显示
	 * @param context
	 */
	public static void setFullScreen(Activity context){
		int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		Window myWindow = context.getWindow();
		myWindow.setFlags(flag, flag);// 设置为全屏
	}

	/**
	 * 计算状态栏的高度
	 * @param context
	 * @return
	 */
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;

		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = context.getResources().getDimensionPixelSize(x);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return sbar;
	}

	/**
	 * 获取控件到父控件的上边距
	 * @param myView
	 * @return
	 */
	public static int getRelativeTop(View myView) {
		return myView.getId() == android.R.id.content ? myView.getTop() : myView.getTop() + getRelativeTop((View) myView.getParent());
	}

	/**
	 * 获取控件到父控件的左边距
	 * @param myView
	 * @return
	 */
	public static int getRelativeLeft(View myView) {
		return myView.getId() == android.R.id.content ? myView.getLeft() : myView.getLeft() + getRelativeLeft((View) myView.getParent());
	}

}
