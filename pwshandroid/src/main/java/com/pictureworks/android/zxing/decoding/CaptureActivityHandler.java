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

package com.pictureworks.android.zxing.decoding;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.pictureworks.android.R;
import com.pictureworks.android.util.PictureAirLog;
import com.pictureworks.android.zxing.camera.CameraManager;
import com.pictureworks.android.zxing.view.ViewfinderResultPointCallback;
import com.pictureworks.android.zxing.view.ViewfinderView;

import java.util.Vector;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class CaptureActivityHandler extends Handler implements DecodeHandler.OnScanResultListener{

  private static final String TAG = CaptureActivityHandler.class.getSimpleName();

  private final DecodeThread decodeThread;
  private Context context;
  private OnDealCodeListener onDealCodeListener;
  private State state;
  private ViewfinderView viewfinderView;

  @Override
  public void getResultMessage(Message msg) {
    handleMessage(msg);
  }

  private enum State {
    PREVIEW,
    SUCCESS,
    DONE
  }

  public CaptureActivityHandler(Context context, Vector<BarcodeFormat> decodeFormats,
                                String characterSet, ViewfinderView viewfinderView, int scanType) {
    this.context = context;
    decodeThread = new DecodeThread(context, decodeFormats, characterSet, scanType,
            new ViewfinderResultPointCallback(viewfinderView), this);
    decodeThread.start();
    state = State.SUCCESS;
    this.viewfinderView = viewfinderView;
    // Start ourselves capturing previews and decoding.
    CameraManager.get().startPreview();
    restartPreviewAndDecode();
  }

  public void setOnDealCodeListener(OnDealCodeListener onDealCodeListener) {
    this.onDealCodeListener = onDealCodeListener;
  }

  @Override
  public void handleMessage(Message message) {
    if (message.what == R.id.auto_focus) {
      // When one auto focus pass finishes, start another. This is the closest thing to
      // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
      if (state == State.PREVIEW) {
        CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
      }
    } else if (message.what == R.id.restart_preview) {
      PictureAirLog.d(TAG, "Got restart preview message");
      PictureAirLog.e("", "restart_preview");
      restartPreviewAndDecode();
    } else if (message.what == R.id.decode_succeeded) {
      PictureAirLog.d(TAG, "Got decode succeeded message");
      state = State.SUCCESS;
      Bundle bundle = message.getData();

      Bitmap barcode = bundle == null ? null :
              (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);

      onDealCodeListener.decodeSuccess((Result) message.obj, barcode);//交给回调进行处理
    } else if (message.what == R.id.decode_failed) {
      // We're decoding as fast as possible, so when one decode fails, start another.
      state = State.PREVIEW;
      CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
    } else if (message.what == R.id.decode_ocr_succeeded) {
      //跳转页面。
      onDealCodeListener.decodeOCRSuccess(message.getData());
    }
  }

  public void quitSynchronously() {
    state = State.DONE;
    CameraManager.get().stopPreview();
    Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
    quit.sendToTarget();
    try {
      decodeThread.join();
    } catch (InterruptedException e) {
      // continue
    }

    // Be absolutely sure we don't send any queued up messages
    removeMessages(R.id.decode_succeeded);
    removeMessages(R.id.decode_failed);
  }

  public void restartPreviewAndDecode() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
      CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
      viewfinderView.drawViewfinder();
    }
  }

}
