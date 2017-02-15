package com.pictureair.photopass.util;

import android.os.Handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.http.BasicResultCallTask;
import com.pictureair.photopass.widget.PWProgressBarDialog;

import java.util.HashMap;
import java.util.Map;


/**
 * 所有与后台的交互都封装到此类
 */
public class API1 {
    private static final String TAG = "API";

    //Shop模块 start
    public static final int GET_GOODS_FAILED = 4010;
    public static final int GET_GOODS_SUCCESS = 4011;

    public static final int ADD_TO_CART_FAILED = 4030;
    public static final int ADD_TO_CART_SUCCESS = 4031;

    public static final int MODIFY_CART_FAILED = 4040;
    public static final int MODIFY_CART_SUCCESS = 4041;

    public static final int BATCH_ADD_TO_CARTS_SUCCESS = 4121;
    public static final int BATCH_ADD_TO_CARTS_FAILED = 4120;
    //Shop模块 end

    //我的模块 start
    public static final int UPLOAD_PHOTO_SUCCESS = 5061;
    public static final int UPLOAD_PHOTO_FAILED = 5060;
    public static final int UPLOAD_PHOTO_PROGRESS = 5062;

    /***************************************Shop模块 start**************************************/

    /**
     * 获取全部商品
     *
     * @param handler handler
     */
    public static BasicResultCallTask getGoods(final Handler handler) {
        PictureAirLog.v(TAG, "getGoods");
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
        BasicResultCallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_GOODS, params, new HttpCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, jsonObject.toString(), ACache.TIME_DAY);
                handler.obtainMessage(GET_GOODS_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_GOODS_FAILED, status, 0).sendToTarget();

            }
        });
        return task;
    }

    /**
     * 添加购物车
     *
     * @param goodsKey    商品项key（必须）
     * @param qty         商品数量(可选)
     * @param isJustBuy   是否立即购买(可选)
     * @param embedPhotos 商品项对应配备的照片id与ppcode映射数组数据(可选)
     * @param handler     handler
     */
    public static BasicResultCallTask addToCart(String goodsKey, int qty, Boolean isJustBuy, JSONArray embedPhotos, final Handler handler) {
        PictureAirLog.v(TAG, "addToCart");
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (goodsKey != null) {
            params.put(Common.GOODS_KEY,goodsKey);
        }
        params.put(Common.IS_JUST_BUY, isJustBuy);
        params.put(Common.QTY, qty);
        if (embedPhotos != null) {
            params.put(Common.EMBEDPHOTOS, embedPhotos.toString());
        }
        BasicResultCallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.ADD_TO_CART, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(ADD_TO_CART_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(ADD_TO_CART_FAILED, status, 0).sendToTarget();

            }
        });
        return task;
    }

    /**
     * 批量加入购物车
     * @param tokenId
     * @param goods
     * @param handler
     */
    public static BasicResultCallTask batchAddToCarts(String tokenId, String goods, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (goods != null) {
            params.put(Common.GOODS, goods);
        }
        BasicResultCallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.BATCH_ADD_TO_CART, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(BATCH_ADD_TO_CARTS_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(BATCH_ADD_TO_CARTS_FAILED, status, 0).sendToTarget();

            }
        });
        return task;
    }

    /**
     * 修改购物车
     *
     * @param cartId      购物车项id参数(可选,不填时为移除全部)
     * @param goodsKey    商品项key（可选）
     * @param qty         商品数量(可选)
     * @param embedPhotos 商品项对应配备的照片id与ppcode映射数组数据(可选)
     * @param handler     handler
     */
    public static BasicResultCallTask modifyCart(String cartId, String goodsKey, int qty, JSONArray embedPhotos, final Handler handler, final PWProgressBarDialog diaBarPop) {
        PictureAirLog.v(TAG, "modifyCart");
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (goodsKey != null) {
            params.put(Common.GOODS_KEY, goodsKey);
        }
        if (embedPhotos != null) {
            params.put(Common.EMBEDPHOTOS, embedPhotos.toString());
        }
        params.put(Common.QTY, qty);
        String url = Common.BASE_URL_TEST + Common.MODIFY_TO_CART + "/" + cartId;
        BasicResultCallTask task = HttpUtil1.asyncPut(url, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(MODIFY_CART_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(MODIFY_CART_FAILED, status, 0).sendToTarget();

            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                diaBarPop.setProgress(bytesWritten, totalSize);
            }
        });
        return task;
    }

    /***************************************Shop模块 end**************************************/

}