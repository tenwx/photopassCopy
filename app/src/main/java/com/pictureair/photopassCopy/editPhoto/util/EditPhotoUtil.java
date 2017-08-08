
package com.pictureair.photopassCopy.editPhoto.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;

import com.pictureair.photopassCopy.util.PictureAirLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditPhotoUtil {

    /**
     * 移动文件夹
     *
     * @param oldPath String 原文件路径
     * @param newPath String 复制后路径
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    PictureAirLog.out(bytesum + "");
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            PictureAirLog.out("复制单个文件操作出错");
            e.printStackTrace();

        }
    }


    /**
     * 将获取的bitmap进行压缩
     *
     * @param bitW     bitmap的宽
     * @param bitH     bitmap的高
     * @param requestW 需要预览的宽
     * @param requestH 需要预览的高
     * @param bitmap   传入的bitmap
     * @return
     */
    public static Bitmap zoomBitmap(int bitW, int bitH, int requestW, int requestH,
                                    Bitmap bitmap) {
        Matrix m = new Matrix();
        if (bitW > requestW || bitH > requestH) {
            final double widthRatio = (float) bitW / (float) requestW;
            final double heightRatio = (float) bitH / (float) requestH;

            if (widthRatio > heightRatio) {// 图片是横的
                m.postScale((float) requestW / bitW, (float) requestW / bitW);
            } else {// 图片是竖着的
                m.postScale((float) requestH / bitH, (float) requestH / bitH);
            }
            PictureAirLog.out("size=" + "_" + widthRatio + "_" + heightRatio);
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), m, true);
    }


    /**
     * 合并两张bitmap为一张
     *
     * @param orginBitmap
     * @param accessoryBitmap
     * @param x
     * @param y
     * @return Bitmap
     */
    public static Bitmap combineBitmap(Bitmap orginBitmap, Bitmap accessoryBitmap, float x, float y) {
        if (orginBitmap == null) {
            return null;
        }
        int bgWidth = orginBitmap.getWidth();
        int bgHeight = orginBitmap.getHeight();
        int fgWidth = accessoryBitmap.getWidth();
        int fgHeight = accessoryBitmap.getHeight();
        Bitmap newmap = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(newmap);
        canvas.drawBitmap(orginBitmap, 0, 0, null);
        canvas.drawBitmap(accessoryBitmap, x,
                y, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return newmap;
    }


    /**
     * 从Assert文件夹中读取位图数据
     *  @param Context
     * @param fileName
     *
     * @return
     */

//	public static Bitmap getImageFromAssetsFile(Context context,String fileName) {
//		Bitmap image = null;
//		AssetManager am = context.getResources().getAssets();
//		try {
//			InputStream is = am.open(fileName);
//			image = BitmapFactory.decodeStream(is);
//			is.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return image;
//	}

    /**
     * 保存Bitmap图片到指定文件
     *
     * @param bm
     * @param filePath
     */
    public static void saveBitmap(Bitmap bm, String filePath) {
        if (bm == null) {
            return;
        }
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // PictureAirLog.out("保存文件--->" + f.getAbsolutePath());
    }

    //清空 temp 文件夹中的所有内容。

    public static void deleteTempPic(String path) {
        File file = new File(path);
        DeleteFile(file);
    }

    private static void DeleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
                return;
            }
            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return;
                }
                for (File f : childFile) {
                    DeleteFile(f);
                }
                // file.delete();
            }
        }
    }

    //将文字转化为bitmap
    public static Bitmap textAsBitmap(String text, Typeface typeface, int color, float textSize, int width, Context context) {
//		textSize = ScreenUtil.getScreenWidth(context)/(text.length());
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG); //抗锯齿
        textPaint.setColor(ContextCompat.getColor(context, color));
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(typeface);
        textPaint.setAntiAlias(true);  // 抗锯齿
        StaticLayout layout = new StaticLayout(text, textPaint, width,
                Alignment.ALIGN_CENTER, 1f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG)); //抗锯齿
        canvas.drawColor(Color.TRANSPARENT);
//		canvas.drawColor(Color.BLUE);

        layout.draw(canvas);
        PictureAirLog.d("textAsBitmap",
                String.format("1:%d %d", layout.getWidth(), layout.getHeight()));
        return bitmap;
    }

    //动态设置margin
    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }


    //创造一个透明的bitmap,然后把文字加在图片上。
    public static Bitmap createEmptyBitmap(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width,
                height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
//		canvas.drawColor(Color.TRANSPARENT);
        canvas.drawColor(Color.CYAN);
        return bitmap;
    }

    /**
     * 图片上写文字
     *
     * @param src      源图片
     * @param msg      文字
     * @param angle
     * @param x
     * @param y
     * @param colors
     * @param textSize
     * @return
     */
    public static Bitmap drawText(Bitmap src, String msg, float angle, float x,
                                  float y, int colors, float textSize) {
        Bitmap msrc = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Config.ARGB_8888); // 建立一个空的BItMap
        // 另外创建一张图片
        Canvas canvas = new Canvas(msrc);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG
                | Paint.DEV_KERN_TEXT_FLAG);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        paint.setColor(colors);
        paint.setTextSize(textSize);// 字体大小
        paint.setTypeface(Typeface.DEFAULT_BOLD);// 采用默认的宽度
        paint.setAntiAlias(true);  //抗锯齿

        canvas.drawBitmap(src, 0, 0, paint);
        canvas.drawText(msg, x + 200, y + 200, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return msrc;
    }

    /**
     * 图片旋转
     *
     * @param bit     旋转原图像
     * @param degrees 旋转度数
     * @return 旋转之后的图像
     */
    public static Bitmap rotateImage(Bitmap bit, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bit, 0, 0, bit.getWidth(),
                bit.getHeight(), matrix, true);
    }


    /**
     * 按照一定的宽高比例裁剪图片
     *
     * @param bitmap
     * @param num1   长边的比例
     * @param num2   短边的比例
     * @return
     */
    public static Bitmap cropBitmap(Bitmap bitmap, int num1, int num2) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int retX, retY;
        int nw, nh;
        if (w > h) {
            if (h > w * num2 / num1) {
                nw = w;
                nh = w * num2 / num1;
                retX = 0;
                retY = (h - nh) / 2;
            } else {
                nw = h * num1 / num2;
                nh = h;
                retX = (w - nw) / 2;
                retY = 0;
            }
        } else {
            if (w > h * num2 / num1) {
                nh = h;
                nw = h * num2 / num1;
                retY = 0;
                retX = (w - nw) / 2;
            } else {
                nh = w * num1 / num2;
                nw = w;
                retY = (h - nh) / 2;
                retX = 0;
            }
        }
        return Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null, false);
    }


}
