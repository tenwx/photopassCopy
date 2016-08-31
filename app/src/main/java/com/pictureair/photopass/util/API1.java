package com.pictureair.photopass.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.OrderProductInfo;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.SendAddress;
import com.pictureair.photopass.fragment.DownLoadingFragment;
import com.pictureair.photopass.http.CallTask;
import com.pictureair.photopass.widget.CustomProgressBarPop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;


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

    public static final int GET_NEW_PHOTOS_INFO_FAILED = 2110;
    public static final int GET_NEW_PHOTOS_INFO_SUCCESS = 2111;

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

    public static final int BATCH_ADD_TO_CARTS_SUCCESS = 4121;
    public static final int BATCH_ADD_TO_CARTS_FAILED = 4120;

    public static final int ADD_ADDRESS_LIST_FAILED = 4130;
    public static final int ADD_ADDRESS_LIST_SUCCESS = 4131;

    public static final int MODIFY_ADDRESS_LIST_FAILED = 4140;
    public static final int MODIFY_ADDRESS_LIST_SUCCESS = 4141;

    public static final int DELETE_ADDRESS_LIST_FAILED = 4150;
    public static final int DELETE_ADDRESS_LIST_SUCCESS = 4151;

    public static final int GET_INVOICE_FAILED = 4160;
    public static final int GET_INVOICE_SUCCESS = 4161;

    public static final int ADDRESS_LIST_FAILED = 4170;
    public static final int ADDRESS_LIST_SUCCESS = 4171;

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
    public static final int UPLOAD_PHOTO_PROGRESS = 5062;

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


    public static final int GET_UPDATE_SUCCESS = 6001;
    public static final int GET_UPDATE_FAILED = 6000;

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
    public static CallTask getTokenId(final Context context, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.TERMINAL, "android");
        String id = Installation.id(context);
        if (id != null) {
            params.put(Common.UUID, id);
        }
        params.put(Common.APP_ID, AppUtil.md5(PWJniUtil.getAPPKey(Common.APP_TYPE_SHDRPP) + PWJniUtil.getAppSecret(Common.APP_TYPE_SHDRPP)));
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_TOKENID, params, new HttpCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                super.onSuccess(jsonObject);
                try {
                    SharedPreferences sp = context.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
                    Editor e = sp.edit();
                    e.putString(Common.USERINFO_TOKENID, AESKeyHelper.encryptString(jsonObject.getString(Common.USERINFO_TOKENID), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0)));
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

        return task;
    }


    /**
     * 登录
     *
     * @param context
     * @param userName
     * @param password
     * @param handler
     */
    public static CallTask Login(final Context context, final String userName, String password, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        PictureAirLog.v("MyApplication.getTokenId()", MyApplication.getTokenId());
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (userName != null) {
            params.put(Common.USERINFO_USERNAME, userName);
        }
        params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.LOGIN, params, new HttpCallback() {
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

        return task;
    }

    /**
     * 登出账号
     *
     * @param handler handler
     */
    public static CallTask Logout(final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        PictureAirLog.v("MyApplication.getTokenId()", MyApplication.getTokenId());
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.LOGOUT, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 下载头像或者背景文件
     *
     * @param downloadUrl
     * @param folderPath
     * @param fileName
     */
    public static CallTask downloadHeadFile(String downloadUrl, final String folderPath, final String fileName) {
        CallTask task = HttpUtil1.asyncDownloadBinaryData(downloadUrl, new HttpCallback() {
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

        return task;
    }

    /**
     * 注册
     *
     * @param userName name
     * @param password pwd
     * @param handler  handler
     */
    public static CallTask Register(final String userName, final String password, String tokenId, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (userName != null) {
            params.put(Common.USERINFO_USERNAME, userName);
        }
        params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.REGISTER, params, new HttpCallback() {
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

        return task;
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
    public static CallTask sendSMSValidateCode(final Handler handler, final String tokenId, String phone, String language, boolean isRegister) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PHONE, "+" + phone);
        params.put(Common.LANGUAGE, language.equals(Common.SIMPLE_CHINESE) ? "CN" : "EN");
        params.put(Common.MSG_TYPE, isRegister ? "register" : "forgotPassword");

        PictureAirLog.v(TAG, "sendSMSValidateCode params：" + params.toString());
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.SEND_SMS_VALIDATE_CODE, params, new HttpCallback() {
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
        return task;
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
    public static CallTask validateCode(final Handler handler, final String tokenId, String validateCode, String phoneOremail, boolean isRegister) {

        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (validateCode != null) {
            params.put(Common.VALIDATE_CODE, validateCode);
        }
        params.put(Common.SEND_TO, "+"+phoneOremail);
        params.put(Common.MSG_TYPE, isRegister ? "register" : "forgotPassword");

        PictureAirLog.v(TAG, "validateCode params：" + params.toString());
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.VALIDATE_CODE_URL, params, new HttpCallback() {
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
        return task;
    }


    /**
     * 获取所有的地址信息
     *
     * @param context
     * @param handler
     */
    public static CallTask getLocationInfo(final Context context, String tokenId, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_ALL_LOCATIONS_OF_ALBUM_GROUP, params, new HttpCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                ACache.get(context).put(Common.DISCOVER_LOCATION, jsonObject.toString());
                handler.obtainMessage(GET_ALL_LOCATION_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.out("get location info failed----->" + status);
                handler.obtainMessage(GET_ALL_LOCATION_FAILED, status, 0).sendToTarget();
            }
        });
        return task;
    }


    /**
     * 获取用户照片
     *
     * @param tokenId
     * @param handler
     * @param timeString 根据时间获取图片信息
     */
    public static CallTask getPhotosByConditions(final String tokenId, final Handler handler, final String timeString, String ppCode) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (timeString != null) {
            params.put(Common.LAST_UPDATE_TIME, timeString);
        }
        if (!TextUtils.isEmpty(ppCode)) {
            params.put(Common.CUSTOMERID, ppCode);
        }
        PictureAirLog.out("the time of start get photos = " + timeString);
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_PHOTOS_BY_CONDITIONS, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 获取照片的最新数据
     *
     * @param tokenId
     * @param handler
     */
    public static CallTask getNewPhotosInfo(String tokenId, String photoId, final int id, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        JSONArray ids = new JSONArray();
        ids.add(photoId);
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (ids!= null) {
            params.put(Common.EPPP_IDS, ids.toJSONString());
        }

        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_PHOTOS_BY_CONDITIONS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("jsonobject---->" + jsonObject.toString());
                JSONArray photos = jsonObject.getJSONArray("photos");
                if (photos.size() > 0) {
                    PhotoInfo photoInfo = JsonUtil.getPhoto(photos.getJSONObject(0));
                    PictureAirLog.out("jsonobject---->" + photoInfo.photoThumbnail_1024);
                    handler.obtainMessage(GET_NEW_PHOTOS_INFO_SUCCESS, id, 0, photoInfo.photoThumbnail_1024).sendToTarget();

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
     * 获取视频信息
     *
     * @param time 如果是null，则全部获取，如果不为null，则获取最新数据
     */
    public static CallTask getVideoList(final String time, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (time != null) {
            params.put(Common.LAST_UPDATE_TIME, time);
        }
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_VIDEO_LIST, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 检查扫描的结果是否正确，并且返回是否已经被使用
     *
     * @param code
     * @param handler
     */
    public static CallTask checkCodeAvailable(String code, String tokenId, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (code != null) {
            params.put(Common.CODE, code);
        }
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.CHECK_CODE_AVAILABLE, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 绑定扫描码到用户
     *
     * @param url
     * @param params
     * @param type
     * @param handler
     */
    public static CallTask addScanCodeToUser(String url, Map params, final String type, final Handler handler) {
        CallTask task = HttpUtil1.asyncPost(url, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 删除网络图片
     * @param tokenId
     * @param handler
     */
    public static CallTask removePhotosFromPP(String tokenId, JSONArray ids, String ppCode, final Handler handler){
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (ids != null) {
            params.put(Common.SHARE_PHOTO_ID, ids.toJSONString());
        }
        if (ppCode != null) {
            params.put(Common.PP, ppCode);
        }
        PictureAirLog.out("param---->" + params.toString());
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.REMOVE_PHOTOS_FROME_PP, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 获取已收藏的地点信息
     *
     * @param tokenId
     * @param handler
     */
    public static CallTask getFavoriteLocations(String tokenId, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_FAVORITE_LOCATIONS, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 收藏或者取消收藏地址获取已收藏的地点信息
     *
     * @param tokenId    必填，token
     * @param locationId locationId:string，必填，location的locationId
     * @param action     必填，操作（可选值：add，remove），收藏或取消收藏
     * @param handler
     */
    public static CallTask editFavoriteLocations(String tokenId, String locationId,
                                             String action, final int position, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (locationId != null) {
            params.put(Common.LOCATION_ID, locationId);
        }
        if (action != null) {
            params.put(Common.ACTION, action);
        }
        CallTask task = HttpUtil1.asyncPost(
                Common.BASE_URL_TEST + Common.EDIT_FAVORITE_LOCATION, params,
                new HttpCallback() {

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
        return task;
    }


    /**
     * 获取最新的边框以及饰品信息
     *
     * @param lastUpdateTime 上次更新时间
     * @param handler
     */
    public static CallTask getLastContent(String lastUpdateTime, final Handler handler) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append(Common.BASE_URL_TEST);
        sBuffer.append(Common.GET_LASTEST_CONTENT);

        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (lastUpdateTime != null) {
            params.put(Common.LAST_UPDATE_TIME, lastUpdateTime);
        }
        CallTask task = HttpUtil1.asyncGet(sBuffer.toString(), params, new HttpCallback() {
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
        return task;
    }

    /**
     * 获取有广告的地点
     *
     * @param handler
     */
    public static CallTask getADLocations(final int oldPosition, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());

        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_AD_LOCATIONS, params, new HttpCallback() {
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
     * 更新用户头像或头部背景图
     *
     * @param params
     * @param handler
     * @param position 修改图片的时候需要这个参数来定位
     * @throws FileNotFoundException
     */
    public static CallTask updateUserImage(Map<String,RequestBody> params, final Handler handler, final int position) throws FileNotFoundException {
        // 需要更新服务器中用户背景图片信息
        CallTask task = HttpUtil1.asyncUpload(Common.BASE_URL_TEST + Common.UPDATE_USER_IMAGE, params, new HttpCallback() {
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
     * 上传个人图片信息，头像或背景图
     *
     * @param params
     * @param handler
     * @param position 修改图片的时候需要这个参数来定位
     * @throws FileNotFoundException
     */
    public static CallTask SetPhoto(Map<String,RequestBody> params, final Handler handler, final int position) throws FileNotFoundException {
        // 需要更新服务器中用户背景图片信息
        CallTask task = HttpUtil1.asyncUpload(Common.BASE_URL_TEST + Common.UPLOAD_PHOTOS, params, new HttpCallback() {
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
     * 更新用户信息
     *
     * @param tokenId  tokenId
     * @param name     名字
     * @param birthday 生日
     * @param gender   性别
     * @param QQ       qq
     * @param handler  handler
     */
    public static CallTask updateProfile(String tokenId, String name, String birthday, String gender, String country, String QQ, final int modifyType, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (name != null) {
            params.put(Common.USERINFO_NICKNAME, name);
        }
        if (country != null) {
            params.put(Common.USERINFO_COUNTRY, country);
        }
        if (QQ != null) {
            params.put(Common.USERINFO_QQ, QQ);
        }
        if (birthday != null) {
            params.put(Common.USERINFO_BIRTHDAY, birthday);
        }
        if (gender != null) {
            params.put(Common.USERINFO_GENDER, gender);
        }

        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.UPDATE_PROFILE, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 获取订单信息 -- 有大改动
     */


    /**
     * 获取所有的PP
     *
     * @param handler handler
     */
    public static CallTask getPPSByUserId(final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_PPS_BY_USERID, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 获取账号下所有ppp
     *
     * @param tokenId tokenId
     * @param handler handler
     */
    public static ArrayList<PPPinfo> PPPlist = new ArrayList<>();

    public static CallTask getPPPSByUserId(String tokenId, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_PPPS_BY_USERID, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 隐藏PP
     *
     * @param params  参数
     * @param handler handler
     */
    public static CallTask hidePPs(Map params, final Handler handler) {
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.HIDE_PPS, params, new HttpCallback() {
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
        return task;
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
    public static CallTask bindPPsToPPP(String tokenId, JSONArray pps, String bindDate, String ppp, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PPS, pps);
        if (bindDate != null) {
            params.put(Common.bindDate, bindDate);
        }
        if (ppp != null) {
            params.put(Common.ppp1,ppp);
        }

        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.BIND_PPS_TO_PPP, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 绑定PP卡到用户
     */
    public static CallTask addCodeToUser(String ppCode, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (ppCode != null) {
            params.put(Common.CUSTOMERID, ppCode);
        }
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.ADD_CODE_TO_USER, params, new HttpCallback() {
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
        return task;
    }


    /**
     * 扫描PPP并绑定用户
     *
     * @param params  params
     * @param handler handler
     */
    public static CallTask bindPPPToUser(Map params, final Handler handler) {
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.BIND_PPP_TO_USER, params, new HttpCallback() {
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
        return task;
    }


    /**
     * 帮助
     *
     * @param handler
     */
    public static CallTask getHelp(final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.ME_HELP, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 修改密码或者忘记密码接口
     *
     * @param oldPwd  旧密码，修改的时候用到，如果是忘记密码的话，设为null
     * @param newPwd  新密码
     * @param handler
     */
    public static CallTask modifyPwd(String oldPwd, String newPwd, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.NEW_PASSWORD, AppUtil.md5(newPwd));
        params.put(Common.OLD_PASSWORD, AppUtil.md5(oldPwd));
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.MODIFYPWD, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 使用体验卡绑定未购买的图片
     *
     * @param pppCode  体验卡卡号
     * @param photoIds 绑定的图片
     * @param handler
     */
    public static CallTask useExperiencePPP(String pppCode, JSONArray photoIds, final Handler handler) {
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
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.USE_EXPERIENCE_PPP, params, new HttpCallback() {
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

    /**
     * 从用户中移除pp
     *
     * @param ppCode   pp码
     * @param position
     * @param handler
     */
    public static CallTask removePPFromUser(String ppCode, final int position, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (ppCode != null) {
            params.put(Common.CUSTOMERID,ppCode);
        }
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.REMOVE_PP_FROM_USER, params, new HttpCallback() {
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
        return task;
    }

    /***************************************我的模块 end**************************************/


    /***************************************Shop模块 start**************************************/


    /**
     * 获取store编号,以此获取商品数据
     *
     * @param handler handler
     */
    public static CallTask getStoreId(final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.GET_STORE_BY_IP, params, new HttpCallback() {
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
        return task;
    }


    /**
     * 获取全部商品
     *
     * @param handler handler
     */
    public static CallTask getGoods(final Handler handler) {
        PictureAirLog.v(TAG, "getGoods");
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_GOODS, params, new HttpCallback() {

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
    public static CallTask getCarts(JSONArray cartIdsArray, final Handler handler) {
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
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_CART, params, new HttpCallback() {
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
    public static CallTask addToCart(String goodsKey, int qty, Boolean isJustBuy, JSONArray embedPhotos, final Handler handler) {
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
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.ADD_TO_CART, params, new HttpCallback() {
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
    public static CallTask batchAddToCarts(String tokenId, String goods, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (goods != null) {
            params.put(Common.GOODS, goods);
        }
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.BATCH_ADD_TO_CART, params, new HttpCallback() {
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
    public static CallTask modifyCart(String cartId, String goodsKey, int qty, JSONArray embedPhotos, final Handler handler, final CustomProgressBarPop diaBarPop) {
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
        CallTask task = HttpUtil1.asyncPut(url, params, new HttpCallback() {
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
    public static CallTask removeCartItems(JSONArray cartIdsArray, final Handler handler) {
        String url = Common.BASE_URL_TEST + Common.DELETE_TO_CART;
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (cartIdsArray != null && cartIdsArray.size() > 0) {
            params.put("cartIdsArray", cartIdsArray.toString());
        }
        PictureAirLog.v(TAG, "params" + params.toString());
        CallTask task = HttpUtil1.asyncDelete(url, params, new HttpCallback() {
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
     * 提交订单
     *
     * @param cartItemIds  JSONArray
     * @param deliveryType 物流方式(必须，送货方式,物流(0)、自提(1)、直送(2),虚拟类商品无须快递(3))
     * @param outletId     门店编号门店主键(与addressId互斥,但不能都存在,若物流方式为3则无此条约束)
     * @param addressId    string用户地址id(与outletId互斥,但不能都存在)
     * @param handler      handler
     */
    public static CallTask addOrder(JSONArray cartItemIds, int deliveryType, String outletId, String addressId,
                                JSONArray couponCodes,JSONObject invoice,
                                String channelId, String uid, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (cartItemIds != null) {
            params.put("cartItemIds", cartItemIds.toString());
        }
        params.put("deliveryType", deliveryType);
        params.put("coupons", couponCodes == null ? "" : couponCodes.toString());
        if (deliveryType == 0) {
            //物流
            if (addressId != null) {
                params.put("addressId", addressId);
            }
        } else if (deliveryType == 1) {
            //自提
            if (outletId != null) {
                params.put("outletId", outletId);
            }
        }
        if(null != invoice)
            params.put("invoiceInfo",invoice);

        if (!TextUtils.isEmpty(channelId)) {
            params.put("channelId", channelId);
            params.put("uId", uid);
        }
        PictureAirLog.out("addorder params ------------>"+params.toString());
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.ADD_ORDER, params, new HttpCallback() {
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
        return task;
    }


    /**
     * 获取订单信息
     *
     * @param handler handler
     */
    public static CallTask getOrderInfo(final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.GET_ALL_ORDERS, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 删除订单信息
     *
     * @param handler
     */
    public static CallTask removeOrder(String orderId, final OrderInfo groupInfo, final OrderProductInfo childInfo, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (orderId != null) {
            params.put(Common.ORDER_ID, orderId);
        }
        PictureAirLog.v(TAG, "removeOrder params：" + params);
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.DELETE_ORDER, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 获得发票的所有地址列表
     *
     * @param handler
     */
    public static CallTask getInvoiceAddressList(final Handler handler ){
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, new HttpCallback() {
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
     * 获取用户购物车带发票的信息
     *
     * @param cartIdsArray
     * @param handler      handler
     */
    public static CallTask getCartsWithInvoice(JSONArray cartIdsArray,boolean isNeedInvoice, final Handler handler) {
        PictureAirLog.out("getCartsInvoice---》" + MyApplication.getTokenId());
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
        params.put("isNeedInvoice", isNeedInvoice);
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_CART, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_INVOICE_SUCCESS, flag, flag, jsonObject).sendToTarget();

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_INVOICE_FAILED, status, 0).sendToTarget();

            }
        });
        return task;
    }

    /**
     * 添加发票的地址
     *
     * @param handler
     */
    public static CallTask addInvoiceAddress(final Handler handler , SendAddress address){
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
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, new HttpCallback() {

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
    public static CallTask modifyInvoiceAddress(final Handler handler , SendAddress address){
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
        CallTask task = HttpUtil1.asyncPut(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, new HttpCallback() {

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
    public static CallTask deleteInvoiceAddress(final Handler handler , String[] ids){
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put("addressesIds", ids);
        CallTask task = HttpUtil1.asyncDelete(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, new HttpCallback() {
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
    public static CallTask buyPhoto(String photoId, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (photoId != null) {
            params.put(Common.PHOTO_ID, photoId);
        }
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.BUY_PHOTO, params,
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
    public static CallTask getOutlets(final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.GET_OUTLET_ID, params, new HttpCallback() {
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


    /***************************************Shop模块 end**************************************/


    /**
     * 上传照片到服务器合成视频
     *
     * @param photos
     * @param handler
     */
    public static CallTask uploadPhotoMakeVideo(String photos, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (photos != null) {
            params.put(Common.PHOTOIDS, photos);
        }
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.VIDEO_GENERATEVIDEO, params, new HttpCallback() {
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
        return task;
    }


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


    /**
     * 获取最新的版本信息
     *
     * @param handler
     */
    public static CallTask checkUpdate(Context context, final Handler handler) {
        if (context == null) {
            handler.sendEmptyMessage(GET_UPDATE_FAILED);
            return null;
        }
        final Context c = context;
        String verson = c.getSharedPreferences(Common.SHARED_PREFERENCE_APP, Context.MODE_PRIVATE).getString(Common.APP_VERSION_NAME, "");
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        params.put(Common.VERSION, verson);

        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.CHECK_VERSION, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                /**
                 * 测试使用
                 */
//                jsonObject = JSONObject.parseObject(checkUpdateTestingString);
                PictureAirLog.out("update---->" + jsonObject);
                ACache.get(c).put(Common.UPDATE_INFO, jsonObject.toString());
                handler.obtainMessage(GET_UPDATE_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.out("failed---->" + status);
                handler.sendEmptyMessage(GET_UPDATE_FAILED);
            }
        });
        return task;
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
    public static CallTask noticeSocketConnect() {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.APNS_CONNECT, params, new HttpCallback() {
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
     * 手机端退出登录前调用
     */
    public static CallTask noticeSocketDisConnect(final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.APNS_DISCONNECT, params, new HttpCallback() {
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
        return task;
    }


    /**
     * 手机端接收到推送后，调用清空推送数据
     *
     * @param clearType
     */
    public static CallTask clearSocketCachePhotoCount(String clearType) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (clearType != null) {
            params.put(Common.CLEAR_TYPE, clearType);
        }
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.CLEAR_PHOTO_COUNT, params, new HttpCallback() {
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


    /**
     * 返回用户未接收到的推送消息
     */
    public static CallTask getSocketData(final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_SOCKET_DATA, params, new HttpCallback() {
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
        return task;
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
    public static CallTask getPPsByPPPAndDate(String pppCode, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (pppCode != null) {
            params.put(Common.PPPCode, pppCode);
        }
        String url = Common.BASE_URL_TEST + Common.GET_PPS_BY_PPP_AND_DATE;
        CallTask task = HttpUtil1.asyncGet(url, params, new HttpCallback() {
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
        return task;
    }


    /**
     * 选择PP+ 绑定PP。现在的逻辑： 一张PP+卡只能绑定一张PP卡的某一天。
     *
     * @param pps
     * @param pppCode
     * @param handler
     */
    public static CallTask bindPPsDateToPPP(JSONArray pps, String pppCode, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (pps != null) {
            params.put(Common.PPS,pps.toString());
        }
        if (pppCode != null) {
            params.put(Common.ppp1, pppCode);
        }
        String url = Common.BASE_URL_TEST + Common.BIND_PPS_DATE_TO_PPP;
        CallTask task = HttpUtil1.asyncPost(url, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 获取分享的URL
     *
     * @param photoID   id
     * @param shareType 视频还是照片
     * @param id        点击id
     * @param handler
     */
    public static CallTask getShareUrl(String photoID, String shareType, final int id, final Handler handler) {
        Map<String,Object> params = new HashMap<>();
        JSONObject orgJSONObject = new JSONObject();
        try {
            orgJSONObject.put(Common.SHARE_MODE, shareType);
            orgJSONObject.put(Common.SHARE_PHOTO_ID, photoID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.SHARE_CONTENT, orgJSONObject.toString());
        params.put(Common.IS_USE_SHORT_URL, false);
        //BASE_URL_TEST2 测试成功
        PictureAirLog.out("get share url----------------" + params.toString());
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.GET_SHARE_URL, params, new HttpCallback() {

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
        return task;
    }

    /**
     * 分享成功的回调，通知服务器已经成功分享
     *
     * @param shareId
     * @param platform
     */
    public static CallTask shareCallBack(String shareId, String platform) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (shareId != null) {
            params.put(Common.SHARE_ID,shareId);
        }
        if (platform != null) {
            params.put(Common.SHARE_PLATFORM, platform);
        }

        PictureAirLog.e("----shareCallBack:", "" + params.toString());

        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.SHARE_CALL_BACK, params, new HttpCallback() {
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
        return task;
    }

    /**************************************下载图片 Start**************************************/
    /**
     * 下载图片的接口。
     *
     * @param handler
     * @param fileStatus
     */
    public static CallTask downLoadPhotos(final Handler handler, final DownloadFileStatus fileStatus, final Handler adapterHandler) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (fileStatus.getPhotoId() != null) {
            params.put(Common.PHOTOIDS, fileStatus.getPhotoId());
        }
        params.put("fileType", "jpg");
        CallTask task = HttpUtil1.asyncDownloadBinaryData(Common.BASE_URL_TEST + Common.DOWNLOAD_PHOTO, params, new HttpCallback() {
            long startTime = System.currentTimeMillis();
            long lastTime = startTime;
            @Override
            public void onSuccess(byte[] binaryData) {
                super.onSuccess(binaryData);
                PictureAirLog.e(TAG, "调用下载照片API成功");
                Message msg =  handler.obtainMessage();
                msg.what = DOWNLOAD_PHOTO_SUCCESS;
                Bundle bundle = new Bundle();
                fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FINISH;
                bundle.putParcelable("url",fileStatus);
                bundle.putByteArray("binaryData",binaryData);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.e(TAG, "调用下载照片API失败：错误代码：" + status);
                Message msg =  handler.obtainMessage();
                msg.what = DOWNLOAD_PHOTO_FAILED;
                Bundle bundle = new Bundle();
                fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
                if (TextUtils.isEmpty(fileStatus.getFailedTime())) {
                    String failedTime = AppUtil.getFormatCurrentTime();
                    fileStatus.setFailedTime(failedTime);
                }
                bundle.putParcelable("url",fileStatus);
                bundle.putInt("status",status);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                double currentSize = bytesWritten/1000d/1000d;
                double total = totalSize/1000d/1000d;
                String c = AppUtil.formatData(currentSize);
                String t = AppUtil.formatData(total);
                fileStatus.setCurrentSize(c);
                fileStatus.setTotalSize(t);
                long currentTime = System.currentTimeMillis();
                float usedTime = (currentTime-lastTime)/1000f;
                float keepTime = (currentTime-startTime)/1000f;
                if (usedTime > 0.2) {
                    lastTime = currentTime;
                    double downSpeed = (bytesWritten / 1000d) / keepTime;
                    String ds = AppUtil.formatData(downSpeed);
                    fileStatus.setLoadSpeed(ds);
                    if (adapterHandler != null) {
                        adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
                    }
                }
            }
        });
        return task;
    }
    /**************************************下载图片 End**************************************/


    /**************************************下载图片 Start**************************************/
    /**
     * 下载图片的接口。
     *
     * @param handler
     * @param photoId
     */
//    public static void downLoadPhotos(final Handler handler, String photoId) {
//        RequestParams params = new RequestParams();
//        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
//        params.put(Common.PHOTOIDS, photoId);
//        params.put("fileType", "jpg");
//        HttpUtil1.asyncDownloadBinaryData(Common.BASE_URL_TEST + Common.DOWNLOAD_PHOTO, params, new HttpCallback() {
//            @Override
//            public void onSuccess(byte[] binaryData) {
//                super.onSuccess(binaryData);
//                PictureAirLog.e(TAG, "调用下载照片API成功");
//                handler.obtainMessage(DOWNLOAD_PHOTO_SUCCESS, binaryData).sendToTarget();
//            }
//
//            @Override
//            public void onFailure(int status) {
//                super.onFailure(status);
//                PictureAirLog.e(TAG, "调用下载照片API失败：错误代码：" + status);
//                handler.obtainMessage(DOWNLOAD_PHOTO_FAILED, status, 0).sendToTarget();
//            }
//
//            @Override
//            public void onProgress(long bytesWritten, long totalSize) {
//                super.onProgress(bytesWritten, totalSize);
//            }
//        });
//    }
    /**************************************下载图片 End**************************************/


    /**
     * 忘记密码
     *
     * @param handler
     * @param pwd
     * @param mobile
     */
    public static CallTask findPwd(final Handler handler, String pwd, String mobile) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.NEW_PASSWORD, AppUtil.md5(pwd));
        if (mobile != null) {
            params.put(Common.USERINFO_USERNAME, mobile);
        }

        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.FORGET_PWD, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 忘记密码
     *
     * @param handler
     * @param email
     * @param language
     */
    public static CallTask findPwdEmail(final Handler handler, String email, String language, String tokenId) {
        Map<String,Object> params = new HashMap<>();
        if (null != language) {
            params.put(Common.LANGUAGE, language.equals(Common.SIMPLE_CHINESE) ? "CN" : "EN");
        }
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (email != null) {
            params.put(Common.USERINFO_EMAIL, email);
        }

        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.FORGET_PWD_EMAIL, params, new HttpCallback() {
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
        return task;
    }


    /**
     * 获取unionpay的tn
     *
     * @param handler
     */
    public static CallTask getUnionPayTN(String orderCode , final Handler handler){
        PictureAirLog.e(TAG, MyApplication.getTokenId());
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (orderCode != null) {
            params.put(Common.ORDER_CODE, orderCode);
        }
        PictureAirLog.e(TAG, MyApplication.getTokenId());

        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_UNIONPAY_TN , params, new HttpCallback() {
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
        return task;
    }


    /**
     * 根据商品查询所有可以使用的优惠卷
     * 1. tokenId
     * 2. cartItemIds:array<string>,用户选中的购物项(可选)
     */
    public static CallTask getCartItemCoupons(final Handler handler, JSONArray cartItemIds) {
        Map<String,Object> params = new HashMap<>();
        if (null != cartItemIds) {//订单页面发来的请求
            params.put(Common.CART_ITEM_IDS, cartItemIds);
        }
        if (null != MyApplication.getTokenId()){
            params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        }
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_COUPONS, params, new HttpCallback() {
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
    public static CallTask addCoupons(final Handler handler, String couponsCode, JSONArray cartItemIds) {
        Map<String,Object> params = new HashMap<>();
        if (null != cartItemIds) {//订单页面发来的请求
            params.put(Common.CART_ITEM_IDS, cartItemIds);
        }
        if (couponsCode != null) {
            params.put(Common.couponCode, couponsCode);
        }
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        PictureAirLog.e(TAG, MyApplication.getTokenId());

        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.ADD_COUPONS, params, new HttpCallback() {
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
     * 用户使用优惠码预览费用
     *
     * @param handler
     * @param couponCodes  优惠码
     * @param cartItemsIds 用户选中的购物项
     */
    public static CallTask previewCoupon(final Handler handler, JSONArray couponCodes, JSONArray cartItemsIds) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (couponCodes != null) {
            params.put("couponCodes", couponCodes.toString());
        }
        if (cartItemsIds != null) {
            params.put("cartItemIds", cartItemsIds.toString());
        }
        PictureAirLog.v(TAG, "previewCoupon params：" + params);
        CallTask task = HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.PREVIEW_COUPONS, params, new HttpCallback() {
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
        return task;
    }

    /**
     * 从me中进入查询抵用劵
     *
     * @param handler
     */
    public static CallTask getCoupons(final Handler handler) {
        Map<String,Object> params = new HashMap<>();

        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        PictureAirLog.e(TAG, "===========" + MyApplication.getTokenId());

        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_ME_COUPONS, params, new HttpCallback() {
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

    /**
     * 根据照片的拍摄时间获取PP+卡列表
     * 用于预览图片页面，“使用已有的迪斯尼乐拍通一卡通”
     * @param handler
     * @param shootDate
     */
    public static CallTask getPPPsByShootDate(final Handler handler, String shootDate) {
        Map<String,Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (shootDate != null) {
            params.put(Common.SHOOTDATE,shootDate);
        }
        CallTask task = HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_PPPS_BY_SHOOTDATE, params, new HttpCallback() {
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
        return task;
    }
}