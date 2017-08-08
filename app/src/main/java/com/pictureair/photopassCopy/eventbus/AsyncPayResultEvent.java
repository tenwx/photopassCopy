package com.pictureair.photopassCopy.eventbus;

import org.json.JSONObject;

/**
 * 支付异步回调通知类型
 * Created by bauer_bao on 16/3/25.
 */
public class AsyncPayResultEvent implements BaseBusEvent {
    private JSONObject asyncPayResult;

    public AsyncPayResultEvent(JSONObject asyncPayResult) {
        this.asyncPayResult = asyncPayResult;
    }

    public JSONObject getAsyncPayResult() {
        return asyncPayResult;
    }
}
