package com.pictureair.hkdlphotopass.zxing.decoding;

import com.google.zxing.Result;

/**
 * Created by bauer_bao on 16/3/21.
 */
public interface OnDealCodeListener {
    void decodeSuccess(Result result);
}
