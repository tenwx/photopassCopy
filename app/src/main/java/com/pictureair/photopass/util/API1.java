package com.pictureair.photopass.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.DealingInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.SendAddress;
import com.pictureair.photopass.http.BasicResultCallTask;
import com.pictureair.photopass.http.CallTaskManager;
import com.pictureair.photopass.widget.PWProgressBarDialog;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;


/**
 * 所有与后台的交互都封装到此类
 */
public class API1 {

    private static final String TAG = "API";

    public static final int GET_DEFAULT_PHOTOS = 1;//获取默认图片
    public static final int GET_NEW_PHOTOS = 2;//获取最新图片
    public static final int GET_OLD_PHOTOS = 3;//获取旧图片


    //选择已有PP＋
    public static final int GET_PPPS_BY_SHOOTDATE_SUCCESS = 2051;
    public static final int GET_PPPS_BY_SHOOTDATE_FAILED = 2050;

    public static final int GET_NEW_PHOTOS_INFO_FAILED = 2060;
    public static final int GET_NEW_PHOTOS_INFO_SUCCESS = 2061;

    /**
     * 获取视频信息
     */
    public static final int UPLOAD_PHOTO_MAKE_VIDEO_FAILED = 2080;
    public static final int UPLOAD_PHOTO_MAKE_VIDEO_SUCCESS = 2081;

    public static final int GET_AD_LOCATIONS_FAILED = 2090;
    public static final int GET_AD_LOCATIONS_SUCCESS = 2091;


    //Shop模块 start
    public static final int GET_GOODS_FAILED = 4010;
    public static final int GET_GOODS_SUCCESS = 4011;

    public static final int GET_CART_FAILED = 4020;
    public static final int GET_CART_SUCCESS = 4021;

    public static final int ADD_TO_CART_FAILED = 4030;
    public static final int ADD_TO_CART_SUCCESS = 4031;

    public static final int MODIFY_CART_FAILED = 4040;
    public static final int MODIFY_CART_SUCCESS = 4041;

    public static final int DELETE_CART_FAILED = 4050;
    public static final int DELETE_CART_SUCCESS = 4051;

    public static final int BUY_PHOTO_FAILED = 4080;
    public static final int BUY_PHOTO_SUCCESS = 4081;

    public static final int GET_OUTLET_ID_FAILED = 4090;
    public static final int GET_OUTLET_ID_SUCCESS = 4091;

    public static final int BATCH_ADD_TO_CARTS_SUCCESS = 4121;
    public static final int BATCH_ADD_TO_CARTS_FAILED = 4120;

    public static final int ADD_ADDRESS_LIST_FAILED = 4130;
    public static final int ADD_ADDRESS_LIST_SUCCESS = 4131;

    public static final int MODIFY_ADDRESS_LIST_FAILED = 4140;
    public static final int MODIFY_ADDRESS_LIST_SUCCESS = 4141;

    public static final int DELETE_ADDRESS_LIST_FAILED = 4150;
    public static final int DELETE_ADDRESS_LIST_SUCCESS = 4151;

    public static final int ADDRESS_LIST_FAILED = 4170;
    public static final int ADDRESS_LIST_SUCCESS = 4171;

    public static final int GET_DEALING_GOODS_SUCCESS = 4181;
    public static final int GET_DEALING_GOODS_FAILED = 4180;
    //Shop模块 end

    //我的模块 start
    public static final int UPLOAD_PHOTO_SUCCESS = 5061;
    public static final int UPLOAD_PHOTO_FAILED = 5060;
    public static final int UPLOAD_PHOTO_PROGRESS = 5062;

    //PP & PP＋模块
    public static final int ADD_PHOTO_TO_PPP_FAILED = 5120;
    public static final int ADD_PHOTO_TO_PPP_SUCCESS = 5121;

    //从订单中获取所有优惠卷
    public static final int GET_COUPON_SUCCESS = 5141;
    public static final int GET_COUPON_FAILED = 5140;

    //添加一张优惠卷
    public static final int INSERT_COUPON_SUCCESS = 5151;
    public static final int INSERT_COUPON_FAILED = 5150;


    /**
     * 获取照片的最新数据
     *
     * @param tokenId
     * @param handler
     */
    public static BasicResultCallTask getNewPhotosInfo(String tokenId, String photoId, final int id, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        JSONArray ids = new JSONArray();
        ids.add(photoId);
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (ids!= null) {
            params.put(Common.EPPP_IDS, ids.toJSONString());
        }

        BasicResultCallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_PHOTOS_BY_CONDITIONS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("jsonobject---->" + jsonObject.toString());
                JSONArray photos = jsonObject.getJSONArray("photos");
                if (photos.size() > 0) {
                    PhotoInfo photoInfo = JsonUtil.getPhoto(photos.getJSONObject(0));
                    PictureAirLog.out("jsonobject---->" + photoInfo.getPhotoThumbnail_1024());
                    handler.obtainMessage(GET_NEW_PHOTOS_INFO_SUCCESS, id, 0, photoInfo).sendToTarget();

                } else {
                    handler.obtainMessage(GET_NEW_PHOTOS_INFO_FAILED, 401, 0).sendToTarget();
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_NEW_PHOTOS_INFO_FAILED, status, 0).sendToTarget();
            }
        });
        return task;
    }

    /**
     * 获取有广告的地点
     *
     * @param handler
     */
    public static BasicResultCallTask getADLocations(final int oldPosition, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());

        BasicResultCallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_AD_LOCATIONS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_AD_LOCATIONS_SUCCESS, oldPosition, 0, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_AD_LOCATIONS_FAILED, status, 0).sendToTarget();
            }
        });
        return task;
    }

    /***************************************我的模块 start**************************************/

    /**
     * 上传个人图片信息，头像或背景图
     *
     * @param params
     * @param handler
     * @param position 修改图片的时候需要这个参数来定位
     * @throws FileNotFoundException
     */
    public static BasicResultCallTask SetPhoto(Map<String,RequestBody> params, final Handler handler, final int position) throws FileNotFoundException {
        // 需要更新服务器中用户背景图片信息
        BasicResultCallTask task = HttpUtil1.asyncUpload(Common.BASE_URL_TEST + Common.UPLOAD_PHOTOS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(UPLOAD_PHOTO_SUCCESS, position, 0, jsonObject).sendToTarget();

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(UPLOAD_PHOTO_FAILED, status, 0).sendToTarget();


            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                Message msg = handler.obtainMessage(UPLOAD_PHOTO_PROGRESS);
                Bundle bundle = new Bundle();
                bundle.putLong("bytesWritten",bytesWritten);
                bundle.putLong("totalSize",totalSize);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
        return task;
    }

    /**
     * 使用体验卡绑定未购买的图片
     *
     * @param pppCode  体验卡卡号
     * @param photoIds 绑定的图片
     * @param handler
     */
    public static BasicResultCallTask useExperiencePPP(String pppCode, JSONArray photoIds, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (pppCode != null) {
            params.put(Common.EPPP,pppCode);
        }
        if (photoIds != null) {
            params.put(Common.EPPP_IDS, photoIds.toJSONString());
        }
        PictureAirLog.out("photo ids --->" + photoIds);
        PictureAirLog.out("params--->" + params.toString());
        BasicResultCallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.USE_EXPERIENCE_PPP, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(ADD_PHOTO_TO_PPP_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(ADD_PHOTO_TO_PPP_FAILED, status, 0).sendToTarget();
            }
        });
        return task;
    }

    /***************************************我的模块 end**************************************/


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
     * 获取用户购物车信息
     *
     * @param cartIdsArray
     * @param handler      handler
     */
    public static BasicResultCallTask getCarts(JSONArray cartIdsArray, final Handler handler) {
        PictureAirLog.out("getCarts---》" + MyApplication.getTokenId());
        final int flag;//表示请求类型： 初始化/选中取消选中
        Map<String,Object> params = new HashMap<>();
        if (cartIdsArray == null) {
            flag = -1;
        } else {
            if (cartIdsArray.size() > 0) {
                params.put("cartItemIds", cartIdsArray.toString());
            }
            flag = GET_CART_SUCCESS;
        }
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
        BasicResultCallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_CART, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_CART_SUCCESS, flag, flag, jsonObject).sendToTarget();

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_CART_FAILED, status, 0).sendToTarget();

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


    /**
     * 移除用户购物车信息
     *
     * @param cartIdsArray 购物车项id参数(可选,不填时为移除全部)
     * @param handler      handler
     */
    public static BasicResultCallTask removeCartItems(JSONArray cartIdsArray, final Handler handler) {
        String url = Common.BASE_URL_TEST + Common.DELETE_TO_CART;
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (cartIdsArray != null && cartIdsArray.size() > 0) {
            params.put("cartIdsArray", cartIdsArray.toString());
        }
        PictureAirLog.v(TAG, "params" + params.toString());
        BasicResultCallTask task = HttpUtil1.asyncDelete(url, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(DELETE_CART_SUCCESS, jsonObject).sendToTarget();

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(DELETE_CART_FAILED, status, 0).sendToTarget();

            }
        });
        return task;
    }

    /**
     * 获得发票的所有地址列表
     *
     * @param handler
     */
    public static BasicResultCallTask getInvoiceAddressList(final Handler handler ){
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        BasicResultCallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(ADDRESS_LIST_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(ADDRESS_LIST_FAILED, status, 0).sendToTarget();

            }
        });
        return task;
    }

    /**
     * 添加发票的地址
     *
     * @param handler
     */
    public static BasicResultCallTask addInvoiceAddress(final Handler handler , SendAddress address){
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (address.getName() != null) {
            params.put("consignee", address.getName());
        }
        if (address.getDetailAddress() != null) {
            params.put("detailedAddress", address.getDetailAddress());
        }
        if (address.getArea() != null) {
            params.put("area",address.getArea());
        }
        if (address.getProvince() != null) {
            params.put("provinces", address.getProvince());
        }
        if (address.getCity() != null) {
            params.put("city", address.getCity());
        }
        if (address.getCountry() != null) {
            params.put("county", address.getCountry());
        }
        if (address.getZip() != null) {
            params.put("zip", address.getZip());
        }
        if (address.getMobilePhone() != null) {
            params.put("mobileNum", address.getMobilePhone());
        }
        if (address.getTelePhone() != null) {
            params.put("telephone", address.getTelePhone());
        }
        params.put("defaultChose", address.isSelected());
        BasicResultCallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, new HttpCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("success---->" + jsonObject.toString());
                handler.obtainMessage(ADD_ADDRESS_LIST_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(ADD_ADDRESS_LIST_FAILED, status, 0).sendToTarget();

            }
        });
        return task;
    }

    /**
     * 修改发票的地址
     *
     * @param handler
     */
    public static BasicResultCallTask modifyInvoiceAddress(final Handler handler , SendAddress address){
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (address.getAddressId() != null) {
            params.put("addressId", address.getAddressId());
        }
        if (address.getName() != null) {
            params.put("consignee", address.getName());
        }
        if (address.getDetailAddress() != null) {
            params.put("detailedAddress", address.getDetailAddress());
        }
        if (address.getArea() != null) {
            params.put("area", address.getArea());
        }
        if (address.getProvince() != null) {
            params.put("provinces", address.getProvince());
        }
        if (address.getCity() != null) {
            params.put("city", address.getCity());
        }
        if (address.getCountry() != null) {
            params.put("county",address.getCountry());
        }
        if (address.getZip() != null) {
            params.put("zip", address.getZip());
        }
        if (address.getMobilePhone() != null) {
            params.put("mobileNum",address.getMobilePhone());
        }
        if (address.getTelePhone() != null) {
            params.put("telephone",address.getTelePhone());
        }
        params.put("defaultChose", address.isSelected());
        PictureAirLog.out("modify address ------>"+params.toString());
        BasicResultCallTask task = HttpUtil1.asyncPut(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, new HttpCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("modify success---->" + jsonObject.toString());
                handler.obtainMessage(MODIFY_ADDRESS_LIST_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.out("modify failure---->"+status);
                handler.obtainMessage(MODIFY_ADDRESS_LIST_FAILED, status, 0).sendToTarget();

            }
        });
        return task;
    }

    /**
     * 删除发票的地址
     *
     * @param handler
     */
    public static BasicResultCallTask deleteInvoiceAddress(final Handler handler , String[] ids){
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put("addressesIds", ids);
        BasicResultCallTask task = HttpUtil1.asyncDelete(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(DELETE_ADDRESS_LIST_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(DELETE_ADDRESS_LIST_FAILED, status, 0).sendToTarget();

            }
        });
        return task;
    }

    /**
     * 购买单张照片
     * 一键放入数码商品至购物车信息
     *
     * @param photoId photoId
     * @param handler handler
     */
    public static BasicResultCallTask buyPhoto(String photoId, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (photoId != null) {
            params.put(Common.PHOTO_ID, photoId);
        }
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
        BasicResultCallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.BUY_PHOTO, params,
                new HttpCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        super.onSuccess(jsonObject);
                        handler.obtainMessage(BUY_PHOTO_SUCCESS, jsonObject)
                                .sendToTarget();
                    }

                    @Override
                    public void onFailure(int status) {
                        super.onFailure(status);
                        handler.obtainMessage(BUY_PHOTO_FAILED, status, 0)
                                .sendToTarget();

                    }
                });
        return task;
    }

    /**
     * 获取门店地址信息
     *
     * @param handler handler
     */
    public static BasicResultCallTask getOutlets(final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
        BasicResultCallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.GET_OUTLET_ID, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_OUTLET_ID_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_OUTLET_ID_FAILED, status, 0).sendToTarget();
            }
        });
        return task;
    }

    /**
     * 获取抢单活动信息
     * @param tokenId
     * @param language
     * @param handler
     * @return
     */
    public static BasicResultCallTask getDealingGoods(String tokenId, String language, final Handler handler) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LANGUAGE, language);
        BasicResultCallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_ALL_DEALINGS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                long localTime = System.currentTimeMillis();
                PictureAirLog.d("getDealingGoods localTime",new Date(localTime).toString());
                PictureAirLog.json(jsonObject.toString());
                DealingInfo dealingInfo = JsonUtil.getDealingInfo(jsonObject);
                if (dealingInfo != null) {
                    try {
                        PictureAirLog.d("getDealingGoods getCurrTime",dealingInfo.getCurrTime());
                        Date currentSystemServerDate = AppUtil.getDateLocalFromStr(dealingInfo.getCurrTime());//服务器时间转换成手机本地时间,目的是不同时区可以准确计时
                        PictureAirLog.d("getDealingGoods format",currentSystemServerDate.toString());
                        dealingInfo.setTimeOffset(localTime - currentSystemServerDate.getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    handler.obtainMessage(GET_DEALING_GOODS_SUCCESS, dealingInfo).sendToTarget();

                } else {
                    handler.obtainMessage(GET_DEALING_GOODS_FAILED, 401, 0).sendToTarget();
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_DEALING_GOODS_FAILED, status, 0).sendToTarget();
            }
        });
        return task;
    }

    /***************************************Shop模块 end**************************************/


    public final static String checkUpdateTestingString = "{'version': {'_id': '560245482cd4db6c0a3a21e3','appName': 'pictureAir',"
            + "'version': '2.1.4', 'createdOn': '2015-09-23T06:06:17.371Z', "
            + " 'mandatory': 'true',  '__v': 0, "
            + " 'versionOS': ['android'], "
            + " 'content': '1、新增修改密码功能；\n2、优化注册功能；\n3、调整部分界面UI；\n1、新增修改密码功能；\n2、优化注册功能；\n3、调整部分界面UI；',"
            + " 'content_EN': '1、Add password modification ;\n2、Improve register function ;\n3、Beautify UI design ;' ,'content_EN':'1、Addpasswordmodification;\n2、Improveregisterfunction;\n3、BeautifyUIdesign;',"
            + "'downloadChannel':[ {'channel':'website',"
            + "'downloadUrl':'http://www.disneyphotopass.com.cn/downloads/android/photopass/PhotoPassV1.1.0-website.apk'},"
            + " { 'channel':'tencent',"
            + "'downloadUrl':'http://dd.myapp.com/16891/2FA495F1283F48658CEACFF53DB6F856.apk?fsname=com.pictureair.photopass_1.1.1_4.apk'}]}}";


    /***************************************推送 Start**************************************/
    /**
     * socket链接后处理方法
     */
    public static BasicResultCallTask noticeSocketConnect() {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        BasicResultCallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.APNS_CONNECT, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.v(TAG, "noticeSocketConnect 链接成功");
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.v(TAG, "noticeSocketConnect 链接失败,状态码：" + status);
            }
        });
        return task;
    }

    /**
     * 手机端接收到推送后，调用清空推送数据
     *
     * @param clearType
     */
    public static BasicResultCallTask clearSocketCachePhotoCount(String clearType) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (clearType != null) {
            params.put(Common.CLEAR_TYPE, clearType);
        }
        BasicResultCallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.CLEAR_PHOTO_COUNT, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.v(TAG, "clearSocketCachePhotoCount 收到推送 清空服务器消息成功");
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.v(TAG, "clearSocketCachePhotoCount 收到推送 清空服务器消息失败,状态码：" + status);
            }
        });
        return task;
    }

    /***************************************
     * 推送 End
     **************************************/

    /**
     * 根据商品查询所有可以使用的优惠卷
     * 1. tokenId
     * 2. cartItemIds:array<string>,用户选中的购物项(可选)
     */
    public static BasicResultCallTask getCartItemCoupons(final Handler handler, JSONArray cartItemIds) {
        Map<String,Object> params = new HashMap<>();
        if (null != cartItemIds) {//订单页面发来的请求
            params.put(Common.CART_ITEM_IDS, cartItemIds);
        }
        if (null != MyApplication.getTokenId()){
            params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        }
        BasicResultCallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_COUPONS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_COUPON_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_COUPON_FAILED, status, 0).sendToTarget();
            }
        });
        return task;
    }


    /**
     * 添加优惠卷
     * * 两个业务处理AB
     * A在me中进入的添加优惠卷
     * 1. tokenId
     * 2. 优惠code
     * B在订单页面进入的添加优惠卷
     * 1. tokenId
     * 2. 优惠code
     * 3. cartItemIds:array<string>,用户选中的购物项(可选)
     */
    public static BasicResultCallTask addCoupons(final Handler handler, String couponsCode, JSONArray cartItemIds) {
        Map<String,Object> params = new HashMap<>();
        if (null != cartItemIds) {//订单页面发来的请求
            params.put(Common.CART_ITEM_IDS, cartItemIds);
        }
        if (couponsCode != null) {
            params.put(Common.couponCode, couponsCode);
        }
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        PictureAirLog.e(TAG, MyApplication.getTokenId());

        BasicResultCallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.ADD_COUPONS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(INSERT_COUPON_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(INSERT_COUPON_FAILED, status, 0).sendToTarget();
            }
        });
        return task;
    }

    /**
     * 从me中进入查询抵用劵
     *
     * @param handler
     */
    public static BasicResultCallTask getCoupons(final Handler handler) {
        Map<String,Object> params = new HashMap<>();

        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        PictureAirLog.e(TAG, "===========" + MyApplication.getTokenId());

        BasicResultCallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_ME_COUPONS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.e(TAG, "============" + jsonObject);
                handler.obtainMessage(GET_COUPON_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.e(TAG, "============" + status);
                handler.obtainMessage(GET_COUPON_FAILED, status, 0).sendToTarget();
            }
        });
        return task;
    }

    public static void cancelAllRequest() {
        CallTaskManager.getInstance().cancleAllTask();
        CallTaskManager.getInstance().clearAllTask();
    }

}