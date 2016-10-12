package com.pictureair.photopass.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BlurUtil {
	/**
	 * 制作高斯模糊
	 *
	 * 在创建缩略图的时候，因为float转int，会造成小数点丢失，因此会造成转换后的图片大小小于原图大小，因此在最后放大的时候，需要重新计算放大比例
	 * @param bkg 需要制作blur的图片
	 * @return
	 */
	public static Bitmap blur(Bitmap bkg) {
		// 缩放尺寸
		float scaleFactor = 6.0f;
		// 模糊度，数字越小，高斯效果越小，越清晰
		float radius = 4;
		Bitmap overlay = Bitmap.createBitmap((int) (bkg.getWidth() / scaleFactor), (int) (bkg.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(overlay);
		canvas.scale(1.0f / scaleFactor, 1.0f / scaleFactor);
		Paint paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);
		canvas.drawBitmap(bkg, 0, 0, paint);
		overlay = doBlur(overlay, (int) radius, true);
		Matrix matrix = new Matrix();
		matrix.postScale(bkg.getWidth() / (float) overlay.getWidth(), bkg.getHeight() / (float) overlay.getHeight());
		overlay = Bitmap.createBitmap(overlay, 0, 0, overlay.getWidth(), overlay.getHeight(), matrix, true);
		return overlay;
	}

	/** 圆形裁剪 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);
		// paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}

	/** 质量压缩图片 */
	public static Bitmap compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		PictureAirLog.out("3---------"+baos.toByteArray().length/1024);
		int options = 90;
		while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			PictureAirLog.out("4-------"+baos.toByteArray().length/1024);
			options -= 10;// 每次都减少10
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	/** 按比例大小压缩图片 */
	public static Bitmap comp(Bitmap image) {

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
		float hh = 800f;// 这里设置高度为800f
		float ww = 480f;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	/**
	 * 制作模糊图片
	 * @param sentBitmap
	 * @param radius
	 * @param canReuseInBitmap
	 * @return
	 */
	private static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {

		Bitmap bitmap;
		if (canReuseInBitmap) {
			bitmap = sentBitmap;
		} else {
			bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
		}

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		bitmap.setPixels(pix, 0, w, 0, 0, w, h);

		return (bitmap);
	}

	/**
	 * 制作周边渐变模糊
	 * 1.将正方形内切圆以外的部分进行全透明处理
	 * 2.将蒙版图片设置成全黑，边缘透明，中心不透明 的渐变效果
	 * 3.将截取的图片的透明值设为完全透明
	 * 4.将截图和蒙版进行合成，分两点，1）透明值合成，截图的全透明和蒙版的透明进行或运算，得到的是蒙版的透明值
	 * 2）颜色值的合成，截图的颜色值和蒙版的颜色值（蒙版只有黑色）进行或运算，得到合成后的颜色值
	 * 因为是以十六进制表示，所以高位表示透明值，低位表示颜色值
	 *
	 * @param b Bitmap对象
	 * @return resultBitmap 合成之后的bitmap对象
	 */
	public static Bitmap doMask(Bitmap b, Bitmap maskBmp) {
		PictureAirLog.out("b in mask width-->" + b.getWidth() + "---h--->" + b.getHeight());
		//创建一个新的bitmap，三个参数依次是宽，高，config
		Bitmap resultBitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Config.ARGB_8888);
		PictureAirLog.out("result w-->" + resultBitmap.getWidth() + "__h-->" + resultBitmap.getHeight());
		int w = maskBmp.getWidth();//获取mask蒙板的宽
		int h = maskBmp.getHeight();//获取高
		PictureAirLog.out("w-->" + w + " h--->" + h);
		float sw = (float) b.getWidth() / w;
		float sh = (float) b.getHeight() / h;
		//matrix为android自带的图片处理的一个类（矩阵）
		Matrix matrix = new Matrix();
		matrix.postScale(sw, sh);//设置缩放的比例
		//将mask蒙板缩放到和截图一样大小
		maskBmp = Bitmap.createBitmap(maskBmp, 0, 0, w, h, matrix, true);
		PictureAirLog.out("maskBmp w--?" + maskBmp.getWidth() + "__h-" + maskBmp.getHeight());

		//创建数组
		int[] pixels_b = new int[b.getWidth() * b.getHeight()];
		int[] pixels_bm = new int[maskBmp.getWidth() * maskBmp.getHeight()];
		//得到传入参数的像素值，并且放入pixels_b中
		b.getPixels(pixels_b, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
		//得到mask蒙板的像素值，并且放入pixels_bm中
		maskBmp.getPixels(pixels_bm, 0, maskBmp.getWidth(), 0, 0, maskBmp.getWidth(), maskBmp.getHeight());
		//遍历mask蒙板数组，图片全黑部分转化为全透明，其他地方和截取的图片进行合成
		for (int i = 0; i < pixels_bm.length; i++) {
			if (pixels_bm[i] == 0xff000000) {//ff000000为不透明的黑色
				//因为在截取图片的时候，只能截取方块，所以圆以外的部分做全透明处理
				pixels_b[i] = 0;//全透明的黑色
				//			} else if (pixels_b[i] == 0) {
			} else {
				pixels_bm[i] &= 0xff000000;//全部变成000000，但是透明度不变
				pixels_bm[i] = 0xff000000 - pixels_bm[i];//颜色不变，透明度翻转，这两步相当于把蒙版的透明度翻转，颜色值全部变为黑色
				pixels_b[i] &= 0x00ffffff;//颜色值不变，但是透明度全部变成完全透明，相当于将截取到的图片设为完全透明
				pixels_b[i] |= pixels_bm[i];//将蒙版和截图进行合成，分两块，一块是透明度的合成，一块是颜色值的合成
				//透明度的合成，截图完全透明，|操作，取的时蒙版的透明度
				//颜色值的合成，截图的颜色值和蒙版的颜色值进行|操作
			}
		}
		PictureAirLog.out("result w-->" + resultBitmap.getWidth() + "---h--->" + resultBitmap.getHeight());
		PictureAirLog.out("maskBmp w--?" + maskBmp.getWidth() + "__h-" + maskBmp.getHeight());
		resultBitmap.setPixels(pixels_b, 0, maskBmp.getWidth(), 0, 0, maskBmp.getWidth(), maskBmp.getHeight());
		return resultBitmap;
	}

	/**
	 *
	 * 计算当前圆半径
	 * @param curRadius 当前半径
	 * @param curShowBitW 当前显示的bit宽
	 * @param curShowBitH 当前显示的bit高
     * @return
     */
	public static int caculateRadius(int curRadius, int curShowBitW, int curShowBitH){
		return Math.min(curRadius, Math.min(curShowBitW, curShowBitH) / 2);
	}

	/**
	 * 计算当前截取的X坐标
	 * @param currentCropX 当前x或者y
	 * @param curShowBmpWidth 当前显示的bit宽或者高
	 * @param zoomW 缩放区域的宽或者高
	 * @param oriBitW 原始图宽或者高
	 * @param isMove 是否移动中
     * @return
     */
	public static int caculateStartCropXOrY(int currentCropX, int curShowBmpWidth, int zoomW, int oriBitW, boolean isMove){
		if (isMove && currentCropX > curShowBmpWidth - Math.min(curShowBmpWidth, zoomW)) {
			currentCropX = curShowBmpWidth - Math.min(curShowBmpWidth, zoomW);
		}

		if (currentCropX > oriBitW - Math.min(zoomW, oriBitW)) {
			currentCropX = oriBitW - Math.min(zoomW, oriBitW);
		}

		if (currentCropX < 0) {
			currentCropX = 0;
		}
		return currentCropX;
	}

	/**
	 * 将资源图片缩放到和imageview一样大小的图片
	 * @param srcBmp
	 * @param scale
	 * @param targetW
	 * @param targetH
     * @return
     */
	public static Bitmap zoomClearBmp(Bitmap srcBmp, float scale, int targetW, int targetH){
		if (srcBmp == null || srcBmp.isRecycled()) {
			return srcBmp;
		}
		Bitmap zoomClearBmp = null;
		Matrix matrix = new Matrix();
		switch (AppUtil.getOrientationMarginByAspectRatio(srcBmp.getWidth(), srcBmp.getHeight(), targetW, targetH)){
			case AppUtil.HORIZONTAL_MARGIN://左右留边
				matrix.postScale(targetH * scale / srcBmp.getHeight(), targetH * scale / srcBmp.getHeight());
				zoomClearBmp = Bitmap.createBitmap(srcBmp, 0, 0, srcBmp.getWidth(), srcBmp.getHeight(), matrix, false);
				break;

			case AppUtil.VERTICAL_MARGIN://上下留白
				matrix.postScale(targetW * scale / srcBmp.getWidth(), targetW * scale / srcBmp.getWidth());
				zoomClearBmp = Bitmap.createBitmap(srcBmp, 0, 0, srcBmp.getWidth(), srcBmp.getHeight(), matrix, false);
				break;
		}
		return zoomClearBmp;
	}

	/**
	 * 获取开始截取的坐标点
	 * @param positionX
	 * @param positionY
	 * @param bmpW
	 * @param bmpH
	 * @param r
	 * @param matrixX 有正负值
     * @param matrixY 有正负值
     * @return
     */
	public static Point getStartCropPoint(float positionX, float positionY, int bmpW, int bmpH, int r, int matrixX, int matrixY) {
		int x = (int) (positionX - r);
		int y = (int) (positionY - 2 * r);//设定起始坐标，要显示在手指上方，所以减直径

		if (x > bmpW + matrixX - 2 * r) {//右边距
			x = bmpW + matrixX - 2 * r;
		}

		if (x < matrixX) {//左边距
			x = matrixX;
		}

		if (y > bmpH + matrixY - 2 * r) {//下边距
			y = bmpH + matrixY - 2 * r;
		}

		if (y < matrixY) {//上边距
			y = matrixY;
		}
		return new Point(x, y);
	}
}
