package com.pictureworks.android.zxing.decoding;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.zxing.Result;

/**
 * Created by bauer_bao on 16/3/21.
 */
public interface OnDealCodeListener {
    void decodeSuccess(Result result, Bitmap bitmap);
    void decodeOCRSuccess(Bundle bundle);
}
