package com.pictureworks.android.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

public class BeautifyFilter extends Filter{

	
	 public Bitmap transform(Bitmap image) {
		 int width = image.getWidth();
			int height = image.getHeight();
			int dst[] = new int[width * height];
			image.getPixels(dst, 0, width, 0, 0, width, height);
			int R, G, B;
			int pos, pixColor;
			int newR, newG, newB;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					pos = y * width + x;
					pixColor = dst[pos];
					R = Color.red(pixColor);
					G = Color.green(pixColor);
					B = Color.blue(pixColor);
					newR = 255 - (255 - R) * (255 - R) / 255;
					newG = 255 - (255 - G) * (255 - G) / 255;
					newB = 255 - (255 - B) * (255 - B) / 255;
					dst[pos] = Color.rgb(clamp(newR), clamp(newG), clamp(newB));
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
