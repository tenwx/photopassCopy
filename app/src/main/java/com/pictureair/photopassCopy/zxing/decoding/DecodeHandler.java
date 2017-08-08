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

package com.pictureair.photopassCopy.zxing.decoding;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.pictureair.photopassCopy.zxing.camera.CameraManager;

import java.util.Map;

final class DecodeHandler extends Handler {
    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final MultiFormatReader multiFormatReader;
    private int scanType;
    private OnScanResultListener onScanResultListener;

    private boolean running = true;

    public interface OnScanResultListener {
        void getResultMessage(Message msg);
    }

    DecodeHandler(Context context, int scanType, boolean permission, Map<DecodeHintType, Object> hints, OnScanResultListener onScanResultListener) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.scanType = scanType;
        this.onScanResultListener = onScanResultListener;
    }

    public void setScanType(int scanType) {
        this.scanType = scanType;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case R.id.decode:
                if (scanType == 1) { // 第一种模式
                    decode((byte[]) message.obj, message.arg1, message.arg2);
                } else {
                    //这样写，会一直走失败的流程，会导致效率低
//                    Message message1 = new Message();
//                    message1.what = R.id.decode_failed;
//                    onScanResultListener.getResultMessage(message1);
                }
                break;

            case R.id.quit:
                running = false;
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

        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (Exception re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        Message message = new Message();
        if (rawResult != null) {
            long end = System.currentTimeMillis();
            PictureAirLog.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
            message.what = R.id.decode_succeeded;
            message.obj = rawResult;
        } else {
            message.what = R.id.decode_failed;
        }
        onScanResultListener.getResultMessage(message);
    }
}
