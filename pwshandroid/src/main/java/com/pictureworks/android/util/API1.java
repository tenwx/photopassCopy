package com.pictureworks.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.RequestParams;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureworks.android.entity.CartItemInfo;
import com.pictureworks.android.entity.OrderInfo;
import com.pictureworks.android.entity.PPPinfo;
import com.pictureworks.android.entity.PPinfo;
import com.pictureworks.android.widget.CheckUpdateManager;
import com.pictureworks.android.widget.CustomProgressBarPop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * 所有与后台的交互都封装到此类
 */
public class API1 {

    private static final String TAG = "API";

    /**
     * 启动
     */
    public static final int GET_TOKEN_ID_SUCCESS = 1001;
    public static final int GET_TOKEN_ID_FAILED = 1000;

    public static final int LOGIN_SUCCESS = 1011;
    public static final int LOGIN_FAILED = 1010;

    public static final int SIGN_SUCCESS = 1021;
    public static final int SIGN_FAILED = 1020;

    public static final int LOGOUT_SUCCESS = 1031;
    public static final int LOGOUT_FAILED = 1030;

    public static final int GET_PPS_SUCCESS = 1041;
    public static final int GET_PPS_FAILED = 1040;

    //忘记密码
    public static final int FIND_PWD_SUCCESS = 1051;
    public static final int FIND_PWD_FAILED = 1050;

    //发送验证码
    public static final int SEND_SMS_VALIDATE_CODE_SUCCESS = 1061;
    public static final int SEND_SMS_VALIDATE_CODE_FAILED = 1060;

    //验证码判断
    public static final int VALIDATECODE_SUCCESS = 1071;
    public static final int VALIDATECODE_FAILED = 1070;


    /**
     * Story
     */
    public static final int GET_ALL_LOCATION_FAILED = 2000;
    public static final int GET_ALL_LOCATION_SUCCESS = 2001;

    public static final int GET_ALL_PHOTOS_BY_CONDITIONS_FAILED = 2010;
    public static final int GET_ALL_PHOTOS_BY_CONDITIONS_SUCCESS = 2011;

    public static final int GET_REFRESH_PHOTOS_BY_CONDITIONS_FAILED = 2020;
    public static final int GET_REFRESH_PHOTOS_BY_CONDITIONS_SUCCESS = 2021;

    /**
     * 扫描
     */
    public static final int CHECK_CODE_FAILED = 2030;
    public static final int CHECK_CODE_SUCCESS = 2031;

    public static final int ADD_SCANE_CODE_FAIED = 2040;
    public static final int ADD_PP_CODE_TO_USER_SUCCESS = 2041;
    public static final int ADD_PPP_CODE_TO_USER_SUCCESS = 2042;

    /**
     * 获取视频信息
     */
    public static final int GET_ALL_VIDEO_LIST_FAILED = 2050;
    public static final int GET_ALL_VIDEO_LIST_SUCCESS = 2051;

    public static final int GET_REFRESH_VIDEO_LIST_FAILED = 2060;
    public static final int GET_REFRESH_VIDEO_LIST_SUCCESS = 2061;

    public static final int GET_LAST_CONTENT_SUCCESS = 2071;
    public static final int GET_LAST_CONTENT_FAILED = 2070;


    public static final int UPLOAD_PHOTO_MAKE_VIDEO_FAILED = 2080;
    public static final int UPLOAD_PHOTO_MAKE_VIDEO_SUCCESS = 2081;

    public static final int GET_AD_LOCATIONS_FAILED = 2090;
    public static final int GET_AD_LOCATIONS_SUCCESS = 2091;

    public static final int DELETE_PHOTOS_SUCCESS = 2101;
    public static final int DELETE_PHOTOS_FAILED = 2100;

    /**
     * 发现
     */
    public static final int GET_FAVORITE_LOCATION_FAILED = 3000;
    public static final int GET_FAVORITE_LOCATION_SUCCESS = 3001;

    public static final int EDIT_FAVORITE_LOCATION_SUCCESS = 3020;
    public static final int EDIT_FAVORITE_LOCATION_FAILED = 3021;


    //Shop模块 start
    public static final int GET_STOREID_FAILED = 4000;
    public static final int GET_STOREID_SUCCESS = 4001;

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

    public static final int ADD_ORDER_FAILED = 4060;
    public static final int ADD_ORDER_SUCCESS = 4061;

    public static final int GET_ORDER_SUCCESS = 4070;
    public static final int GET_ORDER_FAILED = 4071;

    public static final int BUY_PHOTO_FAILED = 4080;
    public static final int BUY_PHOTO_SUCCESS = 4081;

    public static final int GET_OUTLET_ID_FAILED = 4090;
    public static final int GET_OUTLET_ID_SUCCESS = 4091;

    public static final int DELETE_ORDER_FAILED = 4100;
    public static final int DELETE_ORDER_SUCCESS = 4101;

    public static final int UNIONPAY_GET_TN_SUCCESS = 4111;
    public static final int UNIONPAY_GET_TN_FAILED = 4110;


    //Shop模块 end

    //我的模块 start
    public static final int BIND_PP_FAILURE = 5000;
    public static final int BIND_PP_SUCCESS = 5001;

    public static final int UPDATE_PROFILE_SUCCESS = 5011;
    public static final int UPDATE_PROFILE_FAILED = 5010;
    public static final int UPDATE_PROFILE_NAME = 5012;
    public static final int UPDATE_PROFILE_GENDER = 5013;
    public static final int UPDATE_PROFILE_BIRTHDAY = 5014;
    public static final int UPDATE_PROFILE_COUNTRY = 5015;
    public static final int UPDATE_PROFILE_ALL = 5016;

    public static final int GET_HELP_SUCCESS = 5021;
    public static final int GET_HELP_FAILED = 5020;

    public static final int UPDATE_USER_IMAGE_FAILED = 5030;
    public static final int UPDATE_USER_IMAGE_SUCCESS = 5031;

    public static final int SCAN_PPP_FAILED = 5040;
    public static final int SCAN_PPP_SUCCESS = 5041;

    public static final int HIDE_PP_SUCCESS = 5051;
    public static final int HIDE_PP_FAILED = 5050;

    public static final int UPLOAD_PHOTO_SUCCESS = 5061;
    public static final int UPLOAD_PHOTO_FAILED = 5060;

    public static final int GET_PPP_SUCCESS = 5071;
    public static final int GET_PPP_FAILED = 5070;

    //PP & PP＋模块
    public static final int GET_PPS_BY_PPP_AND_DATE_FAILED = 5080;
    public static final int GET_PPS_BY_PPP_AND_DATE_SUCCESS = 5081;

    public static final int BIND_PPS_DATE_TO_PP_FAILED = 5090;
    public static final int BIND_PPS_DATE_TO_PP_SUCESS = 5091;

    public static final int MODIFY_PWD_SUCCESS = 5101;
    public static final int MODIFY_PWD_FAILED = 5100;

    public static final int ADD_CODE_TO_USER_FAILED = 5110;
    public static final int ADD_CODE_TO_USER_SUCCESS = 5111;

    public static final int ADD_PHOTO_TO_PPP_FAILED = 5120;
    public static final int ADD_PHOTO_TO_PPP_SUCCESS = 5121;

    public static final int REMOVE_PP_SUCCESS = 5131;
    public static final int REMOVE_PP_FAILED = 5130;

    //从订单中获取所有优惠卷
    public static final int GET_COUPON_SUCCESS = 5141;
    public static final int GET_COUPON_FAILED = 5140;

    //添加一张优惠卷
    public static final int INSERT_COUPON_SUCCESS = 5151;
    public static final int INSERT_COUPON_FAILED = 5150;

    //使用优惠券
    public static final int PREVIEW_COUPON_SUCCESS = 5161;
    public static final int PREVIEW_COUPON_FAILED = 5160;

    //我的模块 end


    public static final int APK_NEED_UPDATE = 6001;
    public static final int APK_NEED_NOT_UPDATE = 6000;

    public static final int DOWNLOAD_APK_SUCCESS = 6011;
    public static final int DOWNLOAD_APK_FAILED = 6010;


    // 推送
    public static final int SOCKET_DISCONNECT_FAILED = 6020;
    public static final int SOCKET_DISCONNECT_SUCCESS = 6021;

    //手动拉取推送
    public static final int GET_SOCKET_DATA_FAILED = 6120;
    public static final int GET_SOCKET_DATA_SUCCESS = 6121;

    //分享
    public static final int GET_SHARE_URL_SUCCESS = 6031;
    public static final int GET_SHARE_URL_FAILED = 6030;

    //下载
    public static final int DOWNLOAD_PHOTO_SUCCESS = 6041;
    public static final int DOWNLOAD_PHOTO_FAILED = 6040;


    //选择已有PP＋
    public static final int GET_PPPS_BY_SHOOTDATE_SUCCESS = 6091;
    public static final int GET_PPPS_BY_SHOOTDATE_FAILED = 6090;

    /**
     * 发送设备ID获取tokenId
     *
     * @param context
     * @param handler
     */
    public static void getTokenId(final Context context, final String appType, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.TERMINAL, "android");
        params.put(Common.UUID, Installation.id(context));
        params.put(Common.APP_ID, AppUtil.md5(PWJniUtil.getAPPKey(appType) + PWJniUtil.getAppSecret(appType)));
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_TOKENID, params, new HttpCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                super.onSuccess(jsonObject);
                try {
                    SharedPreferences sp = context.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
                    Editor e = sp.edit();
                    e.putString(Common.USERINFO_TOKENID, AESKeyHelper.encryptString(jsonObject.getString(Common.USERINFO_TOKENID), PWJniUtil.getAESKey(appType)));
                    e.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (handler != null) {
                    handler.sendEmptyMessage(GET_TOKEN_ID_SUCCESS);
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                if (handler != null) {
                    handler.obtainMessage(GET_TOKEN_ID_FAILED, status, 0).sendToTarget();
                }
            }
        });
    }


    /**
     * 登录
     *
     * @param context
     * @param userName
     * @param password
     * @param handler
     */
    public static void Login(final Context context, String tokenId, final String userName, String password, final Handler handler) {
        RequestParams params = new RequestParams();
        PictureAirLog.v("MyApplication.getTokenId()", tokenId);
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.USERINFO_USERNAME, userName);
        params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.LOGIN, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                try {
                    JsonUtil.getUserInfo(context, jsonObject, userName, handler);
                    handler.sendEmptyMessage(LOGIN_SUCCESS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(LOGIN_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 登出账号
     *
     * @param handler handler
     */
    public static void Logout(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        PictureAirLog.v("MyApplication.getTokenId()", tokenId);
        params.put(Common.USERINFO_TOKENID, tokenId);
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.LOGOUT, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.e(TAG, "Logout onSuccess:" + jsonObject);
                handler.sendEmptyMessage(LOGOUT_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.e(TAG, "Logout onFailure: status" + status);
                handler.sendEmptyMessage(LOGOUT_FAILED);
            }
        });


    }

    /**
     * 下载头像或者背景文件
     *
     * @param downloadUrl
     * @param folderPath
     * @param fileName
     */
    public static void downloadHeadFile(String downloadUrl, final String folderPath, final String fileName) {
        HttpUtil1.asyncDownloadBinaryData(downloadUrl, new HttpCallback() {
            @Override
            public void onSuccess(byte[] binaryData) {
                super.onSuccess(binaryData);
                try {
                    File folder = new File(folderPath);
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    File file = new File(folderPath + fileName);
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(binaryData);
                    fos.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * 注册
     *
     * @param userName name
     * @param password pwd
     * @param handler  handler
     */
    public static void Register(final String userName, final String password, String tokenId, final Handler handler) {
        final RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.USERINFO_USERNAME, userName);
        params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.REGISTER, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("sign success ---- > " + jsonObject);
                handler.sendEmptyMessage(SIGN_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.out("status----->" + status);
                handler.obtainMessage(SIGN_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 发送验证码
     * SEND_SMS_VALIDATE_CODE
     * <p/>
     * note:手机号格式 “＋8615717737873”
     * appID:string，必填，appID
     * phone:string，必填，手机号，
     * language:string,选填,语言，默认为CN，可填写值：CN或EN，
     * msgType:string,选填，默认为register，可选值（forgotPassword,register）
     */
    public static void sendSMSValidateCode(final Handler handler, final String tokenId, String phone, String language, boolean isRegister) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PHONE, "+" + phone);
        params.put(Common.LANGUAGE, language.equals(Common.SIMPLE_CHINESE) ? "CN" : "EN");
        params.put(Common.MSG_TYPE, isRegister ? "register" : "forgotPassword");

        PictureAirLog.v(TAG, "sendSMSValidateCode params：" + params.toString());
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.SEND_SMS_VALIDATE_CODE, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.v(TAG, "sendSMSValidateCode onSuccess：" + jsonObject.toString());
                handler.obtainMessage(SEND_SMS_VALIDATE_CODE_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.v(TAG, "sendSMSValidateCode onFailure：" + status);
                handler.obtainMessage(SEND_SMS_VALIDATE_CODE_FAILED, status, 0).sendToTarget();
            }

        });
    }

    /**
     * 判断验证信息是否有效 验证码
     * validateCode
     *
     * tokenId:string，必填，tokenId
     * validateCode:string,必填，验证信息
     * sendTo:string,选填，email或mobile
     * msgType:string,选填，可选值（register,forgotPassword）
     */
    public static void validateCode(final Handler handler, final String tokenId, String validateCode, String phoneOremail, boolean isRegister) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.VALIDATE_CODE, validateCode);
        params.put(Common.SEND_TO, "+"+phoneOremail);
        params.put(Common.MSG_TYPE, isRegister ? "register" : "forgotPassword");

        PictureAirLog.v(TAG, "validateCode params：" + params.toString());
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.VALIDATE_CODE_URL, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.v(TAG, "validateCode onSuccess：" + jsonObject.toString());
                handler.obtainMessage(VALIDATECODE_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.v(TAG, "validateCode onFailure：" + status);
                handler.obtainMessage(VALIDATECODE_FAILED, status, 0).sendToTarget();
            }

        });
    }


    /**
     * 获取所有的地址信息
     *
     * @param context
     * @param handler
     */
    public static void getLocationInfo(final Context context, String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_ALL_LOCATIONS_OF_ALBUM_GROUP, params, new HttpCallback() {
            @Override
            public void onStart() {
                super.onStart();
                PictureAirLog.out("get location start-->");
            }

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                ACache.get(context).put(Common.LOCATION_INFO, jsonObject.toString(), ACache.TIME_DAY);
                handler.obtainMessage(GET_ALL_LOCATION_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.out("get location info failed----->" + status);
                handler.obtainMessage(GET_ALL_LOCATION_FAILED, status, 0).sendToTarget();
            }
        });
    }


    /**
     * 获取用户照片
     *
     * @param tokenId
     * @param handler
     * @param timeString 根据时间获取图片信息
     */
    public static void getPhotosByConditions(final String tokenId, final Handler handler, final String timeString) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LAST_UPDATE_TIME, timeString);
        PictureAirLog.out("the time of start get photos = " + timeString);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_PHOTOS_BY_CONDITIONS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                //成功获取照片信息
                if (null == timeString) {//获取全部照片
                    handler.obtainMessage(GET_ALL_PHOTOS_BY_CONDITIONS_SUCCESS, jsonObject).sendToTarget();
                } else {//获取当前照片
                    handler.obtainMessage(GET_REFRESH_PHOTOS_BY_CONDITIONS_SUCCESS, jsonObject).sendToTarget();
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                if (null == timeString) {//获取全部照片
                    handler.obtainMessage(GET_ALL_PHOTOS_BY_CONDITIONS_FAILED, status, 0).sendToTarget();
                } else {//获取当前照片
                    handler.obtainMessage(GET_REFRESH_PHOTOS_BY_CONDITIONS_FAILED, status, 0).sendToTarget();
                }
            }
        });
    }

    /**
     * 获取视频信息
     *
     * @param time 如果是null，则全部获取，如果不为null，则获取最新数据
     */
    public static void getVideoList(String tokenId, final String time, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LAST_UPDATE_TIME, time);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_VIDEO_LIST, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                if (time == null) {//全部数据
                    handler.obtainMessage(GET_ALL_VIDEO_LIST_SUCCESS, jsonObject).sendToTarget();
                } else {//刷新数据
                    handler.obtainMessage(GET_REFRESH_VIDEO_LIST_SUCCESS, jsonObject).sendToTarget();
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                if (time == null) {//全部数据
                    handler.obtainMessage(GET_ALL_VIDEO_LIST_FAILED, status, 0).sendToTarget();
                } else {//刷新数据
                    handler.obtainMessage(GET_REFRESH_VIDEO_LIST_FAILED, status, 0).sendToTarget();
                }
            }
        });

    }

    /**
     * 检查扫描的结果是否正确，并且返回是否已经被使用
     *
     * @param code
     * @param handler
     */
    public static void checkCodeAvailable(String code, String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.CODE, code);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.CHECK_CODE_AVAILABLE, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("check code success--->" + jsonObject.toString());
                handler.obtainMessage(CHECK_CODE_SUCCESS, jsonObject.getString("codeType")).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(CHECK_CODE_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 绑定扫描码到用户
     *
     * @param url
     * @param params
     * @param type
     * @param handler
     */
    public static void addScanCodeToUser(String url, RequestParams params, final String type, final Handler handler) {
        HttpUtil1.asyncPost(url, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("add scan code success---->" + type);
                if ("pp".equals(type)) {
                    handler.obtainMessage(ADD_PP_CODE_TO_USER_SUCCESS).sendToTarget();
                } else if ("ppp".equals(type)) {//ppp
                    handler.obtainMessage(ADD_PPP_CODE_TO_USER_SUCCESS).sendToTarget();
                } else {//coupon
                    handler.obtainMessage(ADD_PPP_CODE_TO_USER_SUCCESS, jsonObject).sendToTarget();
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(ADD_SCANE_CODE_FAIED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 删除网络图片
     * @param tokenId
     * @param handler
     */
    public static void removePhotosFromPP(String tokenId, JSONArray ids, String ppCode, final Handler handler){
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.SHARE_PHOTO_ID, ids.toJSONString());
        params.put(Common.PP, ppCode);
        PictureAirLog.out("param---->" + params.toString());
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.REMOVE_PHOTOS_FROME_PP, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("delete photos----->" + jsonObject);
                handler.sendEmptyMessage(DELETE_PHOTOS_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.out("delete photos failed--->" + status);
                handler.obtainMessage(DELETE_PHOTOS_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 获取已收藏的地点信息
     *
     * @param tokenId
     * @param handler
     */
    public static void getFavoriteLocations(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_FAVORITE_LOCATIONS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.d(TAG, "get favorite locations success" + jsonObject.toString());
                handler.obtainMessage(GET_FAVORITE_LOCATION_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_FAVORITE_LOCATION_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 收藏或者取消收藏地址获取已收藏的地点信息
     *
     * @param tokenId    必填，token
     * @param locationId locationId:string，必填，location的locationId
     * @param action     必填，操作（可选值：add，remove），收藏或取消收藏
     * @param handler
     */
    public static void editFavoriteLocations(String tokenId, String locationId,
                                             String action, final int position, final Handler handler) {
        final RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LOCATION_ID, locationId);
        params.put(Common.ACTION, action);
        HttpUtil1.asyncPost(
                Common.BASE_URL_TEST + Common.EDIT_FAVORITE_LOCATION, params,
                new HttpCallback() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        PictureAirLog.out("edit favorite location start-->");
                    }

                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        super.onSuccess(jsonObject);
                        PictureAirLog
                                .out("edit favorite location info success ----->"
                                        + jsonObject.toString());
                        handler.obtainMessage(EDIT_FAVORITE_LOCATION_SUCCESS,
                                position, 0, jsonObject).sendToTarget();

                    }

                    @Override
                    public void onFailure(int status) {
                        super.onFailure(status);
                        PictureAirLog
                                .out("get favorite location info failed----->"
                                        + status);
                        handler.obtainMessage(EDIT_FAVORITE_LOCATION_FAILED,
                                status, 0).sendToTarget();
                    }
                });
    }


    /**
     * 获取最新的边框以及饰品信息
     *
     * @param lastUpdateTime 上次更新时间
     * @param handler
     */
    public static void getLastContent(String lastUpdateTime, String tokenId, final Handler handler) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append(Common.BASE_URL_TEST);
        sBuffer.append(Common.GET_LASTEST_CONTENT);

        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LAST_UPDATE_TIME, lastUpdateTime);
        HttpUtil1.asyncGet(sBuffer.toString(), params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_LAST_CONTENT_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_LAST_CONTENT_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 获取有广告的地点
     *
     * @param handler
     */
    public static void getADLocations(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_AD_LOCATIONS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_AD_LOCATIONS_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_AD_LOCATIONS_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /***************************************我的模块 start**************************************/


    /**
     * 更新用户头像或头部背景图
     *
     * @param params
     * @param handler
     * @param position 修改图片的时候需要这个参数来定位
     * @throws FileNotFoundException
     */
    public static void updateUserImage(RequestParams params, final Handler handler, final int position, final CustomProgressBarPop diaBarPop) throws FileNotFoundException {
        // 需要更新服务器中用户背景图片信息
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.UPDATE_USER_IMAGE, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(UPDATE_USER_IMAGE_SUCCESS, position, 0, jsonObject).sendToTarget();

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(UPDATE_USER_IMAGE_FAILED, status, 0).sendToTarget();


            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                diaBarPop.setProgress(bytesWritten, totalSize);
            }
        });
    }


    /**
     * 上传个人图片信息，头像或背景图
     *
     * @param params
     * @param handler
     * @param position 修改图片的时候需要这个参数来定位
     * @throws FileNotFoundException
     */
    public static void SetPhoto(RequestParams params, final Handler handler, final int position, final CustomProgressBarPop diaBarPop) throws FileNotFoundException {
        // 需要更新服务器中用户背景图片信息
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.UPLOAD_PHOTOS, params, new HttpCallback() {
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
                diaBarPop.setProgress(bytesWritten, totalSize);
            }
        });
    }

    /**
     * 更新用户信息
     *
     * @param tokenId  tokenId
     * @param name     名字
     * @param birthday 生日
     * @param gender   性别
     * @param QQ       qq
     * @param handler  handler
     */
    public static void updateProfile(String tokenId, String name, String birthday, String gender, String country, String QQ, final int modifyType, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.USERINFO_NICKNAME, name);
        params.put(Common.USERINFO_COUNTRY, country);
        params.put(Common.USERINFO_QQ, QQ);
        params.put(Common.USERINFO_BIRTHDAY, birthday);
        params.put(Common.USERINFO_GENDER, gender);

        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.UPDATE_PROFILE, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(UPDATE_PROFILE_SUCCESS, modifyType, 0).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(UPDATE_PROFILE_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 获取订单信息 -- 有大改动
     */


    /**
     * 获取所有的PP
     *
     * @param handler handler
     */
    public static void getPPSByUserId(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_PPS_BY_USERID, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_PPS_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_PPS_FAILED, status, 0).sendToTarget();

            }
        });
    }

    /**
     * 获取账号下所有ppp
     *
     * @param tokenId tokenId
     * @param handler handler
     */
    public static ArrayList<PPPinfo> PPPlist = new ArrayList<>();

    public static void getPPPSByUserId(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_PPPS_BY_USERID, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("ppp--->" + jsonObject.toString());
                PPPlist = JsonUtil.getPPPSByUserId(jsonObject);
                handler.obtainMessage(GET_PPP_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_PPP_FAILED, status, 0).sendToTarget();

            }
        });
    }

    /**
     * 隐藏PP
     *
     * @param params  参数
     * @param handler handler
     */
    public static void hidePPs(RequestParams params, final Handler handler) {
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.HIDE_PPS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(HIDE_PP_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(HIDE_PP_FAILED, status, 0).sendToTarget();

            }
        });
    }

    /**
     * 将pp绑定到ppp
     *
     * @param tokenId  token
     * @param pps      pps
     * @param bindDate bind
     * @param ppp      ppp
     * @param handler  handler
     */
    public static void bindPPsToPPP(String tokenId, JSONArray pps, String bindDate, String ppp, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PPS, pps);
        params.put(Common.bindDate, bindDate);
        params.put(Common.ppp1, ppp);

        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.BIND_PPS_TO_PPP, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(BIND_PP_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(BIND_PP_FAILURE, status, 0).sendToTarget();
            }
        });

    }

    /**
     * 绑定PP卡到用户
     */
    public static void addCodeToUser(String tokenId, String ppCode, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.CUSTOMERID, ppCode);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.ADD_CODE_TO_USER, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(ADD_PP_CODE_TO_USER_SUCCESS);

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(ADD_CODE_TO_USER_FAILED, status, 0).sendToTarget();

            }
        });
    }


    /**
     * 扫描PPP并绑定用户
     *
     * @param params  params
     * @param handler handler
     */
    public static void bindPPPToUser(RequestParams params, final Handler handler) {
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.BIND_PPP_TO_USER, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(SCAN_PPP_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(SCAN_PPP_FAILED, status, 0).sendToTarget();
            }
        });
    }


    /**
     * 帮助
     *
     * @param handler
     */
    public static void getHelp(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.ME_HELP, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_HELP_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_HELP_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 修改密码或者忘记密码接口
     *
     * @param oldPwd  旧密码，修改的时候用到，如果是忘记密码的话，设为null
     * @param newPwd  新密码
     * @param handler
     */
    public static void modifyPwd(String tokenId, String oldPwd, String newPwd, final Handler handler) {
        final RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.NEW_PASSWORD, AppUtil.md5(newPwd));
        params.put(Common.OLD_PASSWORD, AppUtil.md5(oldPwd));
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.MODIFYPWD, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(MODIFY_PWD_SUCCESS);

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(MODIFY_PWD_FAILED, status, 0).sendToTarget();

            }
        });
    }

    /**
     * 使用体验卡绑定未购买的图片
     *
     * @param pppCode  体验卡卡号
     * @param photoIds 绑定的图片
     * @param handler
     */
    public static void useExperiencePPP(String tokenId, String pppCode, JSONArray photoIds, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.EPPP, pppCode);
        params.put(Common.EPPP_IDS, photoIds.toJSONString());
        PictureAirLog.out("photo ids --->" + photoIds);
        PictureAirLog.out("params--->" + params.toString());
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.USE_EXPERIENCE_PPP, params, new HttpCallback() {
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
    }

    /**
     * 从用户中移除pp
     *
     * @param ppCode   pp码
     * @param position
     * @param handler
     */
    public static void removePPFromUser(String tokenId, String ppCode, final int position, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.CUSTOMERID, ppCode);
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.REMOVE_PP_FROM_USER, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(REMOVE_PP_SUCCESS, position).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(REMOVE_PP_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /***************************************我的模块 end**************************************/


    /***************************************Shop模块 start**************************************/


    /**
     * 获取store编号,以此获取商品数据
     *
     * @param handler handler
     */
    public static void getStoreId(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.GET_STORE_BY_IP, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_STOREID_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_STOREID_FAILED, status, 0).sendToTarget();

            }
        });
    }


    /**
     * 获取全部商品
     *
     * @param handler handler
     */
    public static void getGoods(String tokenId, String language, final Handler handler) {
        PictureAirLog.v(TAG, "getGoods");
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LANGUAGE, language);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_GOODS, params, new HttpCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_GOODS_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_GOODS_FAILED, status, 0).sendToTarget();

            }
        });
    }


    /**
     * 获取用户购物车信息
     *
     * @param cartIdsArray
     * @param handler      handler
     */
    public static void getCarts(String tokenId, String language, JSONArray cartIdsArray, final Handler handler) {
        PictureAirLog.out("getCarts---》" + tokenId);
        final int flag;//表示请求类型： 初始化/选中取消选中
        RequestParams params = new RequestParams();
        if (cartIdsArray == null) {
            flag = -1;
        } else {
            if (cartIdsArray.size() > 0) {
                params.put("cartItemIds", cartIdsArray.toString());
            }
            flag = GET_CART_SUCCESS;
        }
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LANGUAGE, language);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_CART, params, new HttpCallback() {
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
    public static void addToCart(String tokenId, String goodsKey, int qty, Boolean isJustBuy, JSONArray embedPhotos, final Handler handler) {
        PictureAirLog.v(TAG, "addToCart");
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.GOODS_KEY, goodsKey);
        params.put(Common.IS_JUST_BUY, isJustBuy);
        params.put(Common.QTY, qty);
        if (embedPhotos != null) {
            params.put(Common.EMBEDPHOTOS, embedPhotos.toString());
        }
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.ADD_TO_CART, params, new HttpCallback() {
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
    public static void modifyCart(String tokenId, String cartId, String goodsKey, int qty, JSONArray embedPhotos, final Handler handler, final CustomProgressBarPop diaBarPop) {
        PictureAirLog.v(TAG, "modifyCart");
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.GOODS_KEY, goodsKey);
        if (embedPhotos != null) {
            params.put(Common.EMBEDPHOTOS, embedPhotos.toString());
        }
        params.put(Common.QTY, qty);
        String url = Common.BASE_URL_TEST + Common.MODIFY_TO_CART + "/" + cartId;
        HttpUtil1.asyncPut(url, params, new HttpCallback() {
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
    }


    /**
     * 移除用户购物车信息
     *
     * @param cartIdsArray 购物车项id参数(可选,不填时为移除全部)
     * @param handler      handler
     */
    public static void removeCartItems(String tokenId, JSONArray cartIdsArray, final Handler handler) {
        String url = Common.BASE_URL_TEST + Common.DELETE_TO_CART;
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (cartIdsArray != null && cartIdsArray.size() > 0) {
            params.put("cartIdsArray", cartIdsArray.toString());
        }
        PictureAirLog.v(TAG, "params" + params.toString());
        HttpUtil1.asyncDelete(url, params, new HttpCallback() {
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

    }

    /**
     * 提交订单
     *
     * @param cartItemIds  JSONArray
     * @param deliveryType 物流方式(必须，送货方式,物流(0)、自提(1)、直送(2),虚拟类商品无须快递(3))
     * @param outletId     门店编号门店主键(与addressId互斥,但不能都存在,若物流方式为3则无此条约束)
     * @param addressId    string用户地址id(与outletId互斥,但不能都存在)
     * @param handler      handler
     */
    public static void addOrder(String tokenId, JSONArray cartItemIds, int deliveryType, String outletId, String addressId, JSONArray couponCodes, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put("cartItemIds", cartItemIds.toString());
        params.put("deliveryType", deliveryType);
        params.put("coupons", couponCodes == null ? null : couponCodes.toString());
        if (deliveryType == 0) {
            //物流
            params.put("addressId", addressId);
        } else if (deliveryType == 1) {
            //自提
            params.put("outletId", outletId);
        }
        PictureAirLog.v(TAG, "addOrder params: " + params.toString());
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.ADD_ORDER, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(ADD_ORDER_SUCCESS, jsonObject).sendToTarget();

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(ADD_ORDER_FAILED, status, 0).sendToTarget();

            }
        });
    }


    /**
     * 获取订单信息
     *
     * @param handler handler
     */
    public static void getOrderInfo(String tokenId, String language, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LANGUAGE, language);
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.GET_ALL_ORDERS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_ORDER_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_ORDER_FAILED, status, 0).sendToTarget();

            }
        });
    }

    /**
     * 删除订单信息
     *
     * @param handler
     */
    public static void removeOrder(String tokenId, String orderId, final OrderInfo groupInfo, final ArrayList<CartItemInfo> childInfo, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.ORDER_ID, orderId);
        PictureAirLog.v(TAG, "removeOrder params：" + params);
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.DELETE_ORDER, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                Message msg = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putParcelable("group", groupInfo);
                b.putSerializable("child", childInfo);
                msg.what = DELETE_ORDER_SUCCESS;
                msg.setData(b);
                handler.sendMessage(msg);

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(DELETE_ORDER_FAILED, status, 0).sendToTarget();

            }
        });
    }


    /**
     * 购买单张照片
     * 一键放入数码商品至购物车信息
     *
     * @param photoId photoId
     * @param handler handler
     */
    public static void buyPhoto(String tokenId, String language, String photoId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PHOTO_ID, photoId);
        params.put(Common.LANGUAGE, language);
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.BUY_PHOTO, params,
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

    }

    /**
     * 获取门店地址信息
     *
     * @param handler handler
     */
    public static void getOutlets(String tokenId, String language, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LANGUAGE, language);
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.GET_OUTLET_ID, params, new HttpCallback() {
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

    }


    /***************************************Shop模块 end**************************************/


    /**
     * 上传照片到服务器合成视频
     *
     * @param photos
     * @param handler
     */
    public static void uploadPhotoMakeVideo(String tokenId, String photos, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PHOTOIDS, photos);
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.VIDEO_GENERATEVIDEO, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("uploadPhotoMakeVideo--->" + jsonObject.toString());
                handler.sendEmptyMessage(UPLOAD_PHOTO_MAKE_VIDEO_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(UPLOAD_PHOTO_MAKE_VIDEO_FAILED, status, 0).sendToTarget();
            }
        });
    }


    public final static String checkUpdateTestingString = "{'version': {'_id': '560245482cd4db6c0a3a21e3','appName': 'pictureAir',"
            + "'version': '2.1.2', 'createdOn': '2015-09-23T06:06:17.371Z', "
            + " 'mandatory': 'false',  '__v': 0, "
            + " 'versionOS': ['android'], "
            + " 'content': '1、新增修改密码功能；\n2、优化注册功能；\n3、调整部分界面UI；\n1、新增修改密码功能；\n2、优化注册功能；\n3、调整部分界面UI；',"
            + " 'content_EN': '1、Add password modification ;\n2、Improve register function ;\n3、Beautify UI design ;' ,'content_EN':'1、Addpasswordmodification;\n2、Improveregisterfunction;\n3、BeautifyUIdesign;',"
            + "'downloadChannel':[ {'channel':'360',"
            + "'downloadUrl':'http://gdown.baidu.com/data/wisegame/1f10e30a23693de1/baidushoujizhushou_16786079.apk'},"
            + " { 'channel':'tencent',"
            + "'downloadUrl':'http://mmgr.myapp.com/myapp/gjbig/packmanage/24/2/3/102027/tencentmobilemanager5.7.0_android_build3146_102027.apk'}]}}";


    /**
     * 获取最新的版本信息
     *
     * @param handler
     * @param thisVerName
     * @param language
     */
    public static void checkUpdate(String tokenId, Context context, final Handler handler, final String thisVerName, final String language) {
        final String channelStr = AppUtil.getMetaData(context, "UMENG_CHANNEL");
        PictureAirLog.out("channel------>" + channelStr);
        String verson = context.getSharedPreferences(Common.SHARED_PREFERENCE_APP, Context.MODE_PRIVATE).getString(Common.APP_VERSION_NAME, "");
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        params.put(Common.VERSION, verson);

        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.CHECK_VERSION, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {

                super.onSuccess(jsonObject);
                PictureAirLog.out("update---->" + jsonObject);
                /**
                 * 测试使用
                 */
//            jsonObject = JSONObject.parseObject(checkUpdateTestingString);

                if (jsonObject.getJSONObject("version").getJSONArray("versionOS").toString().contains("android")) {
                    //结果不为null，并且结果更新平台中有android，则需要更新
                    JSONObject versionObject = jsonObject.getJSONObject("version");
                    String versionName = versionObject.getString("version");
                    String mandatory = versionObject.getString("mandatory");
                    String content_EN = versionObject.getString("content_EN");
                    String content = versionObject.getString("content");
                    String channel = "";
                    String downloadUrl = "";

                    JSONArray array = versionObject.getJSONArray("downloadChannel");
                    for (int i = 0; i < array.size(); i++) {
                        channel = array.getJSONObject(i).getString("channel");
                        if (channelStr.equals(channel)) {
                            downloadUrl = array.getJSONObject(i).getString("downloadUrl");
                            break;
                        }
                    }

                    boolean flag = false;//为false则不更新
                    int[] number = CheckUpdateManager.verNameChangeInt(thisVerName);
                    int[] newNumber = CheckUpdateManager.verNameChangeInt(versionName);
                    for (int i = 0; i < number.length; i++) {
                        if (number[i] < newNumber[i]) {
                            //需要更新
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        //更新
                        String[] objsStrings = new String[4];
                        objsStrings[0] = versionName;
                        objsStrings[1] = mandatory;

                        objsStrings[3] = downloadUrl;
                        PictureAirLog.d("api update", language);

                        if (null != language && language.equals("en")) {
                            objsStrings[2] = content_EN;
                        } else {
                            objsStrings[2] = content;
                        }
                        Message message = new Message();
                        message.what = APK_NEED_UPDATE;
                        message.obj = objsStrings;
                        handler.sendMessage(message);
                    } else {
                        handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
                    }
                } else {
                    handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
                }

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.out("failed---->" + status);
                handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
            }
        });
    }

//    /**
//     * 下载apk文件
//     *
//     * @param downloadURL 下載路徑
//     * @param handler
//     */
//    public static void downloadAPK(final Context content,String downloadURL, final CustomProgressBarPop customProgressBarPop, final String version, final Handler handler) {
//
//        HttpUtil1.asyncDownloadBinaryData(downloadURL, new HttpCallback() {
//            @Override
//            public void onSuccess(byte[] binaryData) {
//                super.onSuccess(binaryData);
//                handler.obtainMessage(DOWNLOAD_APK_SUCCESS, binaryData).sendToTarget();
//            }
//
//            @Override
//            public void onFailure(int status) {
//                super.onFailure(status);
//                handler.sendEmptyMessage(DOWNLOAD_APK_FAILED);
//            }
//
//            @Override
//            public void onProgress(long bytesWritten, long totalSize) {
//                super.onProgress(bytesWritten, totalSize);
//                customProgressBarPop.setProgress(bytesWritten,totalSize);
//            }
//        });
//    }

//    /**
//     * 下载apk文件
//     * @param content
//     * @param threadInfo
//     * @param handler
//     */
//    public static void downloadAPK2(final Context content,final ThreadInfo threadInfo, final Handler handler) {
//        final Intent intent = new Intent(BreakpointDownloadService.ACTION_UPDATE);
//
//        HttpUtil1.asyncDownloadBinaryData(threadInfo.getUrl(), new HttpCallback() {
//            @Override
//            public void onSuccess(byte[] binaryData) {
//                super.onSuccess(binaryData);
//                handler.obtainMessage(DOWNLOAD_APK_SUCCESS, binaryData).sendToTarget();
//            }
//
//            @Override
//            public void onFailure(int status) {
//                super.onFailure(status);
//                handler.sendEmptyMessage(DOWNLOAD_APK_FAILED);
//            }
//
//            @Override
//            public void onProgress(long bytesWritten, long totalSize) {
//                super.onProgress(bytesWritten, totalSize);
//
//                intent.putExtra("bytesWritten", bytesWritten);
//                intent.putExtra("totalSize", totalSize);
//                content.sendBroadcast(intent);
//            }
//        });
//    }


    /***************************************推送 Start**************************************/
    /**
     * socket链接后处理方法
     */
    public static void noticeSocketConnect(String tokenId) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.APNS_CONNECT, params, new HttpCallback() {
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
    }


    /**
     * 手机端退出登录前调用
     */
    public static void noticeSocketDisConnect(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.APNS_DISCONNECT, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.v(TAG, "noticeSocketDisConnect 退出应用 socket 断开成功");
                handler.sendEmptyMessage(SOCKET_DISCONNECT_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.sendEmptyMessage(SOCKET_DISCONNECT_FAILED);
                PictureAirLog.v(TAG, "noticeSocketDisConnect 退出应用 socket 断开失败,状态码：" + status);
            }
        });
    }


    /**
     * 手机端接收到推送后，调用清空推送数据
     *
     * @param clearType
     */
    public static void clearSocketCachePhotoCount(String tokenId, String clearType) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.CLEAR_TYPE, clearType);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.CLEAR_PHOTO_COUNT, params, new HttpCallback() {
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
    }


    /**
     * 返回用户未接收到的推送消息
     */
    public static void getSocketData(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_SOCKET_DATA, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.v(TAG, "getSocketData onSuccess() jsonObject: " + jsonObject.toString());
                handler.obtainMessage(GET_SOCKET_DATA_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.v(TAG, "getSocketData onFailure() status: " + status);
                handler.obtainMessage(GET_SOCKET_DATA_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /***************************************
     * 推送 End
     **************************************/

    public static ArrayList<PPinfo> PPlist = new ArrayList<PPinfo>();

    /**
     * 根据PP+选择PP界面。  曾经根据日期选择，现在不需要日期。
     *
     * @param pppCode
     * @param handler
     */
    public static void getPPsByPPPAndDate(String tokenId, String pppCode, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PPPCode, pppCode);
        String url = Common.BASE_URL_TEST + Common.GET_PPS_BY_PPP_AND_DATE;
        HttpUtil1.asyncGet(url, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PPlist = JsonUtil.getPPSByPPP(jsonObject);
                handler.obtainMessage(GET_PPS_BY_PPP_AND_DATE_SUCCESS, jsonObject).sendToTarget();

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_PPS_BY_PPP_AND_DATE_FAILED, status, 0).sendToTarget();

            }
        });
    }


    /**
     * 选择PP+ 绑定PP。现在的逻辑： 一张PP+卡只能绑定一张PP卡的某一天。
     *
     * @param pps
     * @param pppCode
     * @param handler
     */
    public static void bindPPsDateToPPP(String tokenId, JSONArray pps, String pppCode, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PPS, pps.toString());
        params.put(Common.ppp1, pppCode);
        String url = Common.BASE_URL_TEST + Common.BIND_PPS_DATE_TO_PPP;
        HttpUtil1.asyncPost(url, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(BIND_PPS_DATE_TO_PP_SUCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(BIND_PPS_DATE_TO_PP_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 获取分享的URL
     *
     * @param photoID   id
     * @param shareType 视频还是照片
     * @param id        点击id
     * @param handler
     */
    public static void getShareUrl(String tokenId, String photoID, String shareType, final int id, final Handler handler) {
        RequestParams params = new RequestParams();
        JSONObject orgJSONObject = new JSONObject();
        try {
            orgJSONObject.put(Common.SHARE_MODE, shareType);
            orgJSONObject.put(Common.SHARE_PHOTO_ID, photoID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.SHARE_CONTENT, orgJSONObject.toString());
        params.put(Common.IS_USE_SHORT_URL, false);
        //BASE_URL_TEST2 测试成功
        PictureAirLog.out("get share url----------------" + params.toString());
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.GET_SHARE_URL, params, new HttpCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.e(TAG, "获取分享成功" + jsonObject.toString());
                handler.obtainMessage(GET_SHARE_URL_SUCCESS, id, 0, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.e(TAG, "获取分享失败" + status);
                handler.obtainMessage(GET_SHARE_URL_FAILED, status, 0).sendToTarget();
            }
        });
    }

    /**
     * 分享成功的回调，通知服务器已经成功分享
     *
     * @param shareId
     * @param platform
     */
    public static void shareCallBack(String tokenId, String shareId, String platform) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.SHARE_ID, shareId);
        params.put(Common.SHARE_PLATFORM, platform);

        PictureAirLog.e("----shareCallBack:", "" + params.toString());

        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.SHARE_CALL_BACK, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("call back success---->" + jsonObject);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
            }
        });
    }

    /**************************************下载图片 Start**************************************/
    /**
     * 下载图片的接口。
     *
     * @param handler
     * @param photoId
     */
    public static void downLoadPhotos(String tokenId, final Handler handler, String photoId) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PHOTOIDS, photoId);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.DOWNLOAD_PHOTO, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.e(TAG, "调用下载照片API成功");
                handler.obtainMessage(DOWNLOAD_PHOTO_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.e(TAG, "调用下载照片API失败：错误代码：" + status);
                handler.obtainMessage(DOWNLOAD_PHOTO_FAILED, status, 0).sendToTarget();
            }
        });
    }
    /**************************************下载图片 End**************************************/


    /**
     * 忘记密码
     *
     * @param handler
     * @param pwd
     * @param mobile
     */
    public static void findPwd(String tokenId, final Handler handler, String pwd, String mobile) {
        final RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.NEW_PASSWORD, AppUtil.md5(pwd));
        params.put(Common.USERINFO_USERNAME, mobile);

        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.FORGET_PWD, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(FIND_PWD_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(FIND_PWD_FAILED, status, 0).sendToTarget();

            }
        });
    }

    /**
     * 忘记密码
     *
     * @param handler
     * @param email
     * @param language
     */
    public static void findPwdEmail(final Handler handler, String email, String language, String tokenId) {
        final RequestParams params = new RequestParams();
        if (null != language) {
            params.put(Common.LANGUAGE, language.equals(Common.SIMPLE_CHINESE) ? "CN" : "EN");
        }
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.USERINFO_EMAIL, email);

        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.FORGET_PWD_EMAIL, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(FIND_PWD_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(FIND_PWD_FAILED, status, 0).sendToTarget();

            }
        });
    }


    /**
     * 获取unionpay的tn
     *
     * @param handler
     */
    public static void getUnionPayTN(String tokenId, String orderCode , final Handler handler){
        PictureAirLog.e(TAG, tokenId);
        final RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.ORDER_CODE, orderCode);

        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_UNIONPAY_TN , params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.e(TAG, "jsonObject" + jsonObject);
                handler.obtainMessage(UNIONPAY_GET_TN_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(UNIONPAY_GET_TN_FAILED, status, 0).sendToTarget();
            }
        });
    }


    /**
     * 根据商品查询所有可以使用的优惠卷
     * 1. tokenId
     * 2. cartItemIds:array<string>,用户选中的购物项(可选)
     */
    public static void getCartItemCoupons(String tokenId, final Handler handler, JSONArray cartItemIds) {
        final RequestParams params = new RequestParams();
        if (null != cartItemIds) {//订单页面发来的请求
            params.put(Common.CART_ITEM_IDS, cartItemIds);
        }
        if (null != tokenId){
            params.put(Common.USERINFO_TOKENID, tokenId);
        }
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_COUPONS, params, new HttpCallback() {
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
    public static void addCoupons(String tokenId, final Handler handler, String couponsCode, JSONArray cartItemIds) {
        final RequestParams params = new RequestParams();
        if (null != cartItemIds) {//订单页面发来的请求
            params.put(Common.CART_ITEM_IDS, cartItemIds);
        }
        params.put(Common.couponCode, couponsCode);
        params.put(Common.USERINFO_TOKENID, tokenId);
        PictureAirLog.e(TAG, tokenId);

        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.ADD_COUPONS, params, new HttpCallback() {
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
    }

    /**
     * 用户使用优惠码预览费用
     *
     * @param handler
     * @param couponCodes  优惠码
     * @param cartItemsIds 用户选中的购物项
     */
    public static void previewCoupon(String tokenId, final Handler handler, JSONArray couponCodes, JSONArray cartItemsIds) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put("couponCodes", couponCodes.toString());
        params.put("cartItemIds", cartItemsIds.toString());
        PictureAirLog.v(TAG, "previewCoupon params：" + params);
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.PREVIEW_COUPONS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(PREVIEW_COUPON_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(PREVIEW_COUPON_FAILED, status, 0).sendToTarget();
            }

        });
    }

    /**
     * 从me中进入查询抵用劵
     *
     * @param handler
     */
    public static void getCoupons(String tokenId, final Handler handler) {
        final RequestParams params = new RequestParams();

        params.put(Common.USERINFO_TOKENID, tokenId);

        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_ME_COUPONS, params, new HttpCallback() {
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
    }

    /**
     * 根据照片的拍摄时间获取PP+卡列表
     * 用于预览图片页面，“使用已有的迪斯尼乐拍通一卡通”
     * @param handler
     * @param shootDate
     */
    public static void getPPPsByShootDate(String tokenId, final Handler handler, String shootDate) {
        final RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.SHOOTDATE, shootDate);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_PPPS_BY_SHOOTDATE, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
//                PictureAirLog.e(TAG, "============" + jsonObject);
                PPPlist = JsonUtil.getPPPSByUserIdNHavedPPP(jsonObject);
                handler.obtainMessage(GET_PPPS_BY_SHOOTDATE_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
//                PictureAirLog.e(TAG, "============" + status);
                handler.obtainMessage(GET_PPPS_BY_SHOOTDATE_FAILED, status, 0).sendToTarget();
            }
        });
    }

}