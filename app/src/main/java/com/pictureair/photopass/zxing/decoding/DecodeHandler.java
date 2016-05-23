/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pictureair.photopass.zxing.decoding;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.MipCaptureActivity;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.OCRUtils;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.zxing.camera.CameraManager;
import com.pictureair.photopass.zxing.camera.PlanarYUVLuminanceSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Hashtable;

final class DecodeHandler extends Handler {
    YuvImage image;
    int a_x;
    int a_y;
    int recHeight;
    int recWidth;
    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final MipCaptureActivity activity;
    private final MultiFormatReader multiFormatReader;

    DecodeHandler(MipCaptureActivity activity, Hashtable<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.decode:
                //Log.d(TAG, "Got decode message");
                if (MipCaptureActivity.scanType == 1) { // 第一种模式
                    decode((byte[]) message.obj, message.arg1, message.arg2);
                } else {// OCR模式
                    decodeOCR((byte[]) message.obj, message.arg1, message.arg2);
                }

                break;
            case R.id.quit:
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        Result rawResult = null;

        //modify here
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap);
        } catch (ReaderException re) {
            // continue
        } finally {
            multiFormatReader.reset();
        }

        if (rawResult != null) {
            long end = System.currentTimeMillis();
            PictureAirLog.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
            Message message = Message.obtain(activity.getHandler(), R.id.decode_succeeded, rawResult);
            Bundle bundle = new Bundle();
            bundle.putParcelable(DecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
            message.setData(bundle);
            //Log.d(TAG, "Sending decode succeeded message...");
            message.sendToTarget();
        } else {
            Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
    }

    /**
     * 处理OCR 的具体方法。 旧版PP+
     */
//  private void decodeOCR(byte[] data, int width, int height){
//    image = new YuvImage(data, ImageFormat.NV21, width, height, null);
//    if(image!=null){
////      PictureAirLog.e("","width:"+width+"_height:"+height);
//      //计算出矩形区域的长和宽。  长边为宽，短边为高
//      recHeight = height/3*2;
//      recWidth = recHeight*85/54;
//      //计算出状态栏高度，计算出标题栏高度。
//      Rect frame = new Rect();
//      activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//      int topHeight = frame.top;
//      int topBarHeight = ScreenUtil.dip2px(activity,52);
//      //计算出 横向PP+卡左上角的坐标。
//      a_x = (width - topHeight - topBarHeight - recWidth)/2 + topHeight + topHeight;  //横向  坐标。
//      a_y = (height - recHeight)/2;
//      PictureAirLog.e("","a_x:"+a_x+"a_y:"+a_y);
//
//
//      //横向情况，计算出 卡号 区域左上角的坐标。
//      int b_x = a_x + recWidth*20/85; // 横向情况，计算出 卡号 区域左上角的坐标。
//      int b_y = a_y + recHeight*39/54;
//
//      // 计算出PP＋号码区域 矩形的长和宽。长边为宽，短边为高
//      int targetWidth = recWidth*(85-20*2)/85;
//      int targetHeight = recHeight * 8/54;
//
//      ByteArrayOutputStream stream = new ByteArrayOutputStream();
//      image.compressToJpeg(new Rect(b_x, b_y, b_x + targetWidth, b_y + targetHeight), 90, stream);
//      Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
//      try {
//        stream.close();
//      } catch (Exception ex) {
//      }
//      regonize(bmp,data);
//    }
//  }


    /**
     * 处理OCR 的具体方法。新版PP+
     */
    private void decodeOCR(byte[] data, int width, int height) {
        image = new YuvImage(data, ImageFormat.NV21, width, height, null);
        if (image != null) {
            //计算出矩形区域的长和宽。  长边为宽，短边为高
            recHeight = height / 3 * 2;
            recWidth = recHeight * 85 / 54;
            //计算出状态栏高度，计算出标题栏高度。
            Rect frame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int topHeight = frame.top;
            int topBarHeight = ScreenUtil.dip2px(activity, 52);
            //计算出 横向PP+卡左上角的坐标。
            a_x = (width - topHeight - topBarHeight - recWidth) / 2 + topHeight + topHeight;  //横向  坐标。
            a_y = (height - recHeight) / 2;
            PictureAirLog.e("", "a_x:" + a_x + "a_y:" + a_y);


            //横向情况，计算出 卡号 区域左上角的坐标。
            int b_x = a_x + recWidth * 6 / 85; // 横向情况，计算出 卡号 区域左上角的坐标。
            int b_y = a_y + recHeight * 16 / 54;

            // 计算出PP＋号码区域 矩形的长和宽。长边为宽，短边为高
            int targetWidth = recWidth * 55 / 85;
            int targetHeight = recHeight * 8 / 54;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(b_x, b_y, b_x + targetWidth, b_y + targetHeight), 90, stream);
            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            try {
                stream.close();
            } catch (Exception ex) {
            }
            regonize(bmp, data);
        }
    }


    /**
     * 1,裁减图片
     * 2,识别图片中的文字
     * 3,判断识别出来的字符是否正确，如果正确，显示结果，结束相机，如果错误，不做操作继续扫描识别。
     */
    private void regonize(Bitmap bitmap, byte[] data) {
//    bitmap = OCRUtils.transform(bitmap);
        String text = doOcr(bitmap, "eng");
        PictureAirLog.e("", "测试结果:" + text);
        text = OCRUtils.dealCode(text);
        boolean flag = OCRUtils.checkCode(text);
        if (flag) { //扫描成功。截取矩形Bitmap
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(a_x, a_y, a_x + recWidth, a_y + recHeight), 50, stream);
            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

            MipCaptureActivity.tempBitmap = bmp;
            Message message = activity.getHandler().obtainMessage();
            message.what = R.id.decode_ocr_succeeded;
            Bundle b = new Bundle();
            b.putString("text", text);
            message.setData(b);
            message.sendToTarget();
        } else {
            Message message = Message.obtain(activity.getHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
    }

    /**
     * 具体的识别方法
     *
     * @param bitmap
     * @param language
     * @return
     */
    private String doOcr(Bitmap bitmap, String language) {
        TessBaseAPI baseApi = new TessBaseAPI();

        File tessdata = new File(Common.PHOTO_SAVE_PATH + "tessdata");
        if (tessdata.exists() && tessdata.isDirectory()) {
        } else {
            return "";
        }

        baseApi.init(Common.PHOTO_SAVE_PATH, language);
        // 必须加此行，tess-two要求BMP必须为此配置
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        baseApi.setImage(bitmap);
        String text = baseApi.getUTF8Text();
        baseApi.clear();
        baseApi.end();
        return text;
    }

}
