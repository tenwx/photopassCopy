package com.pictureair.photopass.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraUtil {
	public static final int DELAY_TIME_0S = 0;
	public static final int DELAY_TIME_3S = 1;
	public static final int DELAY_TIME_10S = 2;
	public static final long MAX_SZIE = 1024 * 1024;// 1M 大于 1M 就 压缩。
	public static Bitmap getImageCompress(Bitmap bitmap, int screenW,
			int screenH) {
		if (getBitmapByte(bitmap) > MAX_SZIE) { // 大于标准，就压缩。
			bitmap = compress(bitmap, screenW, screenH);
		}
		return bitmap;
	}
	/**
	 *  通过分辨率压缩 Bitmap。（按比例缩放）
	 * @param image
	 * @param screenW
	 * @param screenH
	 * @return
	 */
	public static Bitmap compress(Bitmap image, int screenW, int screenH) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		if (baos.toByteArray().length / 1024 > 1024) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		// float hh = 800f;// 这里设置高度为800f
		// float ww = 480f;// 这里设置宽度为480f
		int hh = screenW;// 这里设置高度为800f // 根据分辨率压缩 图片。
		int ww = screenH;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		PictureAirLog.e("CameraUtil", "压缩的比例值 ："+be);
//		newOpts.inSampleSize = be;// 设置缩放比例
		newOpts.inSampleSize = 2; // 设置的压缩比例，暂时是压缩一半。
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		// return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
		return bitmap; // test 比例压缩。质量不压缩。
	}

	/**
	 * 图片质量压缩  camera 暂时没有质量压缩。
	 * @param image
	 * @return
	 */
	private static Bitmap compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;

		while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;// 每次都减少10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	/**
	 * 获取 Bitmap 的大小。
	 * 
	 * @param bitmap
	 * @return
	 */
	public static long getBitmapByte(Bitmap bitmap) {
		return bitmap.getByteCount();
	}	
	/**
	 * 设置焦点和测光区域
	 * 
	 * @param event
	 */
	public static void focusOnTouch(MotionEvent event,View relativeLayout,View view_focus,Camera mycamera) {
		//		if (!taking_flag) {
		int[] location = new int[2];
		relativeLayout.getLocationOnScreen(location);

		Rect focusRect = calculateTapArea(view_focus.getWidth(),
				view_focus.getHeight(), 1f, event.getRawX(),
				event.getRawY(), location[0],
				location[0] + relativeLayout.getWidth(), location[1],
				location[1] + relativeLayout.getHeight());
		Rect meteringRect = calculateTapArea(view_focus.getWidth(),
				view_focus.getHeight(), 1.5f, event.getRawX(),
				event.getRawY(), location[0],
				location[0] + relativeLayout.getWidth(), location[1],
				location[1] + relativeLayout.getHeight());

		Camera.Parameters parameters = mycamera.getParameters();
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		if (parameters.getMaxNumFocusAreas() > 0) {
			List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
			focusAreas.add(new Camera.Area(focusRect, 1000));
			parameters.setFocusAreas(focusAreas);
		}

		if (parameters.getMaxNumMeteringAreas() > 0) {
			List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
			meteringAreas.add(new Camera.Area(meteringRect, 1000));

			parameters.setMeteringAreas(meteringAreas);
		}

		try {
			mycamera.setParameters(parameters);
		} catch (Exception e) {

		}
	}
	   /**
	    * 聚焦 的具体方法
	    */
		public static Rect calculateTapArea(int focusWidth, int focusHeight,
				float areaMultiple, float x, float y, int previewleft,
				int previewRight, int previewTop, int previewBottom) {
			int areaWidth = (int) (focusWidth * areaMultiple);
			int areaHeight = (int) (focusHeight * areaMultiple);
			int centerX = (previewleft + previewRight) / 2;
			int centerY = (previewTop + previewBottom) / 2;
			double unitx = ((double) previewRight - (double) previewleft) / 2000;
			double unity = ((double) previewBottom - (double) previewTop) / 2000;
			int left = clamp((int) (((x - areaWidth / 2) - centerX) / unitx), -1000, 1000);
			int top = clamp((int) (((y - areaHeight / 2) - centerY) / unity), -1000, 1000);
			int right = clamp((int) (left + areaWidth / unitx), -1000, 1000);
			int bottom = clamp((int) (top + areaHeight / unity), -1000, 1000);
			return new Rect(left, top, right, bottom);
		}
		
		public static int clamp(int x, int min, int max) {
			if (x > max)
				return max;
			if (x < min)
				return min;
			return x;
		}
		/**
		 * //主动让媒体库去更新最新文件
		 * 
		 * @param file 需要扫描的文件
		 */
		public static void scan(String file,Context context) {
			new PWMediaScanner(context, file, "image/*", new PWMediaScanner.ScannerListener() {
				@Override
				public void OnScannerFinish() {

				}
			});
		}
		
		/**
		 * 保存 二进制文件到本地。
		 * @param photoFile
		 * @param picData
		 */
		public static void outputPhotoForStream(File photoFile,byte[] picData){
			FileOutputStream outputStream;
			try {
				outputStream = new FileOutputStream(photoFile);
				outputStream.write(picData); // 写入sd卡中
				outputStream.close(); // 关闭输出流
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		
		/**
		 * 保存 Bitmap 到本地。
		 * @param photoFile
		 */
		public static void outputPhotoForBimap(Bitmap bitmap,File photoFile){
			if (null != bitmap) {
//				System.out.println(photoFile.toString());
				FileOutputStream fileOutputStream = null;
				try {
					fileOutputStream = new FileOutputStream(
							photoFile);
					BufferedOutputStream bos = new BufferedOutputStream(
							fileOutputStream);
					bitmap.compress(Bitmap.CompressFormat.JPEG,
							95, bos);
					bos.flush();
					bos.close();
					fileOutputStream.close();
//					stickerView.setVisibility(View.GONE);
//					stickerView.clear();
				} catch (IOException e) {
					// TODO: handle exception
				}
				bitmap.recycle();
			}
		}
		
	
//		 public static void setCameraDisplayOrientation(Activity activity,
//		         int cameraId, android.hardware.Camera camera) {
//		     android.hardware.Camera.CameraInfo info =
//		             new android.hardware.Camera.CameraInfo();
//		     android.hardware.Camera.getCameraInfo(cameraId, info);
//		     int rotation = activity.getWindowManager().getDefaultDisplay()
//		             .getRotation();
//		     
//		     Log.e("=======", "info.orientation:"+info.orientation);
//		     Log.e("rotation", "rotation:"+rotation);
//		     
//		     int degrees = 0;
//		     switch (rotation) {
//		         case Surface.ROTATION_0: degrees = 0; break;
//		         case Surface.ROTATION_90: degrees = 90; break;
//		         case Surface.ROTATION_180: degrees = 180; break;
//		         case Surface.ROTATION_270: degrees = 270; break;
//		     }
//
//		     int result;
//		     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//		         result = (info.orientation + degrees) % 360;
//		         result = (360 - result) % 360;  // compensate the mirror
//		     } else {  // back-facing
//		         result = (info.orientation - degrees + 360) % 360;
//		     }
//		     Log.e("result ::::", "result :"+result);
//		     camera.setDisplayOrientation(result);
//		 }
		
}
