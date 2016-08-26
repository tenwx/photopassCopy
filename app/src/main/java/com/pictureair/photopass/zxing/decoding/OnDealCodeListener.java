package com.pictureair.photopass.zxing.decoding;

import android.os.Bundle;

import com.google.zxing.Result;

/**
 * Created by bauer_bao on 16/3/21.
 */
public interface OnDealCodeListener {
    void decodeSuccess(Result result);
    void decodeOCRSuccess(Bundle bundle);
}
