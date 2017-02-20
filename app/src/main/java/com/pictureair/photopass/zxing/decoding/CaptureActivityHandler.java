/*
 * Copyright (C) 2008 ZXing authors
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

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.zxing.camera.CameraManager;
import com.pictureair.photopass.zxing.view.ViewfinderResultPointCallback;
import com.pictureair.photopass.zxing.view.ViewfinderView;

import java.util.Collection;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class CaptureActivityHandler extends Handler implements DecodeHandler.OnScanResultListener {

    private static final String TAG = CaptureActivityHandler.class.getSimpleName();

    private final DecodeThread decodeThread;
    private Context context;
    private OnDealCodeListener onDealCodeListener;
    private State state;
    private ViewfinderView viewfinderView;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    @Override
    public void getResultMessage(Message msg) {
        handleMessage(msg);
    }

    public CaptureActivityHandler(Context context,
                                  Collection<BarcodeFormat> decodeFormats,
                                  String characterSet,
                                  ViewfinderView viewfinderView,
                                  int scanType,
                                  boolean permission) {
        this.context = context;
        decodeThread = new DecodeThread(context, decodeFormats, characterSet, scanType, permission,
                new ViewfinderResultPointCallback(viewfinderView), this);
        decodeThread.start();
        state = State.SUCCESS;
        this.viewfinderView = viewfinderView;
        // Start ourselves capturing previews and decoding.
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    public void setScanType (int scanType) {
        decodeThread.setScanType(scanType);
        if (scanType == 1) {//之前有扫描ocr，因此整个流程不会断，现在没有了ocr，导致切换到手动输入页面之后，流程断了，因此切换回来需要重新开始流程
            PictureAirLog.d("state--> " + state);
            state = State.SUCCESS;
            restartPreviewAndDecode();
        }
    }

    public void setOnDealCodeListener(OnDealCodeListener onDealCodeListener) {
        this.onDealCodeListener = onDealCodeListener;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.restart_preview:
                PictureAirLog.d(TAG, "Got restart preview message");
                PictureAirLog.e("", "restart_preview");
                restartPreviewAndDecode();
                break;

            case R.id.decode_succeeded:
                PictureAirLog.d(TAG, "Got decode succeeded message");
                state = State.SUCCESS;
                onDealCodeListener.decodeSuccess((Result) message.obj);//交给回调进行处理
                break;

            case R.id.decode_failed:
                // We're decoding as fast as possible, so when one decode fails, start another.
                PictureAirLog.out("decode failed---->");
                state = State.PREVIEW;
                CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
                break;

        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_ocr_succeeded);
        removeMessages(R.id.decode_failed);
    }

    public void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
            viewfinderView.drawViewfinder();
        }
    }

}
