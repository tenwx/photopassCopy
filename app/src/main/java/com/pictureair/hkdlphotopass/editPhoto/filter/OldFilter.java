package com.pictureair.hkdlphotopass.editPhoto.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

public class OldFilter extends Filter{

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
					newR = (int) (((double) R * 0.393) + ((double) G * 0.769) + ((double) B * 0.189));
					newG = (int) (((double) R * 0.349) + ((double) G * 0.686) + ((double) B * 0.168));
					newB = (int) (((double) R * 0.272) + ((double) G * 0.534) + ((double) B * 0.131));
					// newR = (int)colorBlend(noise(), (R * 0.393) + (G * 0.769) +
					// (B * 0.189), R);
					// newG = (int)colorBlend(noise(), (R * 0.349) + (G * 0.686) +
					// (B * 0.168), G);
					// newB = (int)colorBlend(noise(), (R * 0.272) + (G * 0.534) +
					// (B * 0.131), B);
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
