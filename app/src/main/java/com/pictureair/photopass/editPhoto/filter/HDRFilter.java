package com.pictureair.photopass.editPhoto.filter;

import android.graphics.Bitmap;

public class HDRFilter extends Filter{

	 public Bitmap transform(Bitmap image) {
		 float contrast = 1.5f; // default value; 对比度
			float brightness = 1.0f; // default value; 亮度
			int width = image.getWidth();
			int height = image.getHeight();
			// src.getRGB( 0, 0, width, height, inPixels, 0, width );
			int dst[] = new int[width * height];
			image.getPixels(dst, 0, width, 0, 0, width, height);

			// calculate RED, GREEN, BLUE means of pixel
			int index = 0;
			int[] rgbmeans = new int[3];
			double redSum = 0, greenSum = 0, blueSum = 0;
			double total = height * width;
			for (int row = 0; row < height; row++) {
				int ta = 0, tr = 0, tg = 0, tb = 0;
				for (int col = 0; col < width; col++) {
					index = row * width + col;
					ta = (dst[index] >> 24) & 0xff;
					tr = (dst[index] >> 16) & 0xff;
					tg = (dst[index] >> 8) & 0xff;
					tb = dst[index] & 0xff;
					redSum += tr;
					greenSum += tg;
					blueSum += tb;
				}
			}

			rgbmeans[0] = (int) (redSum / total);
			rgbmeans[1] = (int) (greenSum / total);
			rgbmeans[2] = (int) (blueSum / total);

			// adjust contrast and brightness algorithm, here
			for (int row = 0; row < height; row++) {
				int ta = 0, tr = 0, tg = 0, tb = 0;
				for (int col = 0; col < width; col++) {
					index = row * width + col;
					ta = (dst[index] >> 24) & 0xff;
					tr = (dst[index] >> 16) & 0xff;
					tg = (dst[index] >> 8) & 0xff;
					tb = dst[index] & 0xff;

					// remove means
					tr -= rgbmeans[0];
					tg -= rgbmeans[1];
					tb -= rgbmeans[2];

					// adjust contrast now !!!
					tr = (int) (tr * contrast);
					tg = (int) (tg * contrast);
					tb = (int) (tb * contrast);

					// adjust brightness
					tr += (int) (rgbmeans[0] * brightness);
					tg += (int) (rgbmeans[1] * brightness);
					tb += (int) (rgbmeans[2] * brightness);
					dst[index] = (ta << 24) | (clamp(tr) << 16) | (clamp(tg) << 8)
							| clamp(tb);
				}
			}
			Bitmap acrossFlushBitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.RGB_565);
			acrossFlushBitmap.setPixels(dst, 0, width, 0, 0, width, height);
			return acrossFlushBitmap;
	 }
	 public static int clamp(int c) {
			return c > 255 ? 255 : ((c < 0) ? 0 : c);
		}
}
