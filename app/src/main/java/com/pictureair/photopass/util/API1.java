package com.pictureair.photopass.util;

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
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.widget.CheckUpdateManager;
import com.pictureair.photopass.widget.CustomProgressBarPop;

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
    private static final String APP_KEY = "photoPass";
    private static final String APP_SECRET = "pictureworks";

    public static final int GET_PPP_SUCCESS = 371;
    public static final int GET_PPP_FAILED = 370;


    public static final int UPLOAD_PHOTO_SUCCESS = 511;
    public static final int UPLOAD_PHOTO_FAILED = 510;

    public static final int HIDE_PP_SUCCESS = 531;
    public static final int HIDE_PP_FAILED = 530;

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
    public static final int GET_PPS_SUCCESS = 1031;
    public static final int GET_PPS_FAILED = 1030;

    /**
     * Story
     */
    public static final int GET_ALL_LOCATION_FAILED = 2000;
    public static final int GET_ALL_LOCATION_SUCCESS = 2001;

    public static final int GET_ALL_PHOTOS_BY_CONDITIONS_FAILED = 2010;
    public static final int GET_ALL_PHOTOS_BY_CONDITIONS_SUCCESS = 2011;

    public static final int GET_REFRESH_PHOTOS_BY_CONDITIONS_FAILED = 2020;
    public static final int GET_REFRESH_PHOTOS_BY_CONDITIONS_SUCCESS = 2021;

    public static final int UPLOAD_PHOTO_MAKE_VIDEO_FAILED = 2030;
    public static final int UPLOAD_PHOTO_MAKE_VIDEO_SUCCESS = 2031;

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

    public static final int GET_GOODS_FAILED = 4100;
    public static final int GET_GOODS_SUCCESS = 4101;

    public static final int GET_SINGLE_GOOD_FAILED = 4200;
    public static final int GET_SINGLE_GOOD_SUCCESS = 4201;

    public static final int GET_CART_FAILED = 4300;
    public static final int GET_CART_SUCCESS = 4301;

    public static final int ADD_TO_CART_FAILED = 4310;
    public static final int ADD_TO_CART_SUCCESS = 4311;

    public static final int MODIFY_CART_FAILED = 4320;
    public static final int MODIFY_CART_SUCCESS = 4321;

    public static final int DELETE_CART_FAILED = 4330;
    public static final int DELETE_CART_SUCCESS = 4331;

    public static final int ADD_ORDER_FAILED = 4340;
    public static final int ADD_ORDER_SUCCESS = 4341;

    public static final int GET_ORDER_SUCCESS = 4350;
    public static final int GET_ORDER_FAILED = 4351;

    public static final int BUY_PHOTO_FAILED = 4360;
    public static final int BUY_PHOTO_SUCCESS = 4361;

    public static final int GET_OUTLET_ID_FAILED = 4370;
    public static final int GET_OUTLET_ID_SUCCESS = 4371;

    public static final int DELETE_ORDER_FAILED = 4380;
    public static final int DELETE_ORDER_SUCCESS = 4381;


    //Shop模块 end

    //我的模块 start
    public static final int BIND_PP_FAILURE = 5000;
    public static final int BIND_PP_SUCCESS = 5001;

    public static final int UPDATE_PROFILE_SUCCESS = 5011;
    public static final int UPDATE_PROFILE_FAILED = 5010;

    public static final int SCAN_PPP_FAILED = 5400;
    public static final int SCAN_PPP_SUCCESS = 5401;

    public static final int GET_HELP_SUCCESS = 5021;
    public static final int GET_HELP_FAILED = 5020;
    //我的模块 end


    public static final int APK_NEED_UPDATE = 6001;
    public static final int APK_NEED_NOT_UPDATE = 6000;

    public static final int DOWNLOAD_APK_SUCCESS = 6011;
    public static final int DOWNLOAD_APK_FAILED = 6010;


    // 推送
    public static final int SOCKET_DISCONNECT_FAILED = 5800;
    public static final int SOCKET_DISCONNECT_SUCCESS = 5801;

    //PP & PP＋模块
    public static final int GET_PPS_BY_PPP_AND_DATE_FAILED = 5500;
    public static final int GET_PPS_BY_PPP_AND_DATE_SUCCESS = 5501;
    //分享
    public static final int GET_SHARE_URL_SUCCESS = 6021;
    public static final int GET_SHARE_URL_FAILED = 6020;

    public static final int BIND_PPS_DATE_TO_PP_FAILED = 5600;
    public static final int BIND_PPS_DATE_TO_PP_SUCESS = 5601;

    /**
     * 发送设备ID获取tokenId
     *
     * @param context
     * @param handler
     */
    public static void getTokenId(final Context context, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.TERMINAL, "android");
        params.put(Common.UUID, Installation.id(context));
        params.put(Common.APP_ID, AppUtil.md5(APP_KEY + APP_SECRET));
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_TOKENID, params, new HttpCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                super.onSuccess(jsonObject);
                try {
                    SharedPreferences sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
                    Editor e = sp.edit();
                    e.putString(Common.USERINFO_TOKENID, jsonObject.getString(Common.USERINFO_TOKENID));
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
    public static void Login(final Context context, String userName, String password, final Handler handler) {
        RequestParams params = new RequestParams();
        PictureAirLog.v("MyApplication.getTokenId()", MyApplication.getTokenId());
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.USERINFO_USERNAME, userName);
        params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.LOGIN, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                try {
                    JsonUtil.getUserInfo(context, jsonObject, handler);
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
    public static void Logout(final Handler handler) {
        RequestParams params = new RequestParams();
        PictureAirLog.v("MyApplication.getTokenId()", MyApplication.getTokenId());
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
        HttpUtil1.asynDownloadBinaryData(downloadUrl, new HttpCallback() {
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
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
//                PictureAirLog.out("get location info success ----->" + jsonObject.toString());
                ACache.get(context).put(Common.LOCATION_INFO, jsonObject.toString());
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
                PictureAirLog.out("getphotos--------" + jsonObject);
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
     * @param time 如果是null，则全部获取，如果不为null，则获取最新数据
     */
    public static void getVideoList(final String time, final Handler handler){
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
                if ("ppp".equals(type)) {
                    handler.obtainMessage(ADD_PPP_CODE_TO_USER_SUCCESS).sendToTarget();
                } else if ("pp".equals(type)) {
                    handler.obtainMessage(ADD_PP_CODE_TO_USER_SUCCESS).sendToTarget();
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


    /***************************************我的模块 start**************************************/

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
    public static void updateProfile(String tokenId, String name, String birthday, String gender, String country, String QQ, final Handler handler) {
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
                handler.sendEmptyMessage(UPDATE_PROFILE_SUCCESS);
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
     * @param tokenId tokenId
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
    public static void getHelp(final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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

    /***************************************我的模块 end**************************************/


    /***************************************Shop模块 start**************************************/


    /**
     * 获取store编号,以此获取商品数据
     *
     * @param handler handler
     */
    public static void getStoreId(final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
    public static void getGoods(final Handler handler) {
        PictureAirLog.v(TAG, "getGoods");
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
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
     * @param handler handler
     */
    public static void getCarts(final Handler handler) {
        PictureAirLog.out("getCarts---》" + MyApplication.getTokenId());
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.GET_CART, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_CART_SUCCESS, jsonObject).sendToTarget();

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
    public static void addToCart(String goodsKey, int qty, Boolean isJustBuy, JSONArray embedPhotos, final Handler handler) {
        PictureAirLog.v(TAG, "addToCart");
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
    public static void modifyCart(String cartId, String goodsKey, int qty, JSONArray embedPhotos, final Handler handler, final CustomProgressBarPop diaBarPop) {
        PictureAirLog.v(TAG, "modifyCart");
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
    public static void removeCartItems(JSONArray cartIdsArray, final Handler handler) {
        String url = Common.BASE_URL_TEST + Common.DELETE_TO_CART;
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
    public static void addOrder(JSONArray cartItemIds, int deliveryType, String outletId, String addressId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put("cartItemIds", cartItemIds.toString());
        params.put("deliveryType", deliveryType);
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
    public static void getOrderInfo(final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
    public static void removeOrder(String orderId, final OrderInfo groupInfo, final ArrayList<CartItemInfo> childInfo, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.ORDER_ID, orderId);

        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.DELETE_ORDER, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                Message msg = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putParcelable("group", groupInfo);
                b.putParcelableArrayList("child", childInfo);
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
    public static void buyPhoto(String photoId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.PHOTO_ID, photoId);
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
    public static void getOutlets(final Handler handler) {
        RequestParams params = new RequestParams();
        params.put("tokenId", MyApplication.getTokenId());
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
    public static void uploadPhotoMakeVideo(String photos, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
    public static void checkUpdate(final Handler handler, final String thisVerName, final String language) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.CHECK_VERSION, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                jsonObject = jsonObject.getJSONObject("version");
                String versionName = jsonObject.getString("version");
                String mandatory = jsonObject.getString("mandatory");
                String content_EN = jsonObject.getString("content_EN");
                String content = jsonObject.getString("content");
                JSONArray array = jsonObject.getJSONArray("downloadChannel");
                String downloadUrl = null;
                for (int i = 0; i < array.size(); i++) {
                    String channel = array.getJSONObject(i).getString("channel");
                    if (Common.UMENG_CHANNEL.equals(channel)) {
                        downloadUrl = array.getJSONObject(i).getString("downloadUrl");
                        break;
                    }
                }
                boolean flag = false;// 为false则不更新
                int[] number = CheckUpdateManager.verNameChangeInt(thisVerName);
                int[] newNumber = CheckUpdateManager.verNameChangeInt(versionName);
                for (int i = 0; i < number.length; i++) {
                    if (number[i] < newNumber[i]) {
                        // 需要更新
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    // 更新
                    String[] resultArray = new String[4];
                    resultArray[0] = versionName;
                    resultArray[1] = mandatory;
                    resultArray[3] = downloadUrl;

                    if (null != language && language.equals("en")) {
                        resultArray[2] = content_EN;
                    } else {
                        resultArray[2] = content;
                    }
                    handler.obtainMessage(APK_NEED_UPDATE, resultArray).sendToTarget();
                } else {
                    handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
            }
        });
    }

    /**
     * 下载apk文件
     *
     * @param downloadURL 下載路徑
     * @param handler
     */
    public static void downloadAPK(String downloadURL, final CustomProgressBarPop customProgressBarPop, final String version, final Handler handler) {
        HttpUtil1.asynDownloadBinaryData(downloadURL, new HttpCallback() {
            @Override
            public void onSuccess(byte[] binaryData) {
                super.onSuccess(binaryData);
                handler.obtainMessage(DOWNLOAD_APK_SUCCESS, binaryData).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.sendEmptyMessage(DOWNLOAD_APK_FAILED);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                customProgressBarPop.setProgress(bytesWritten, totalSize);
            }
        });
    }


    /***************************************推送 Start**************************************/
    /**
     * socket链接后处理方法
     */
    public static void noticeSocketConnect() {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.APNS_CONNECT, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.e(TAG, "socket 链接成功");
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.e(TAG, "socket 链接失败,状态码：" + status);
            }
        });
    }


    /**
     * 手机端退出登录前调用
     */
    public static void noticeSocketDisConnect(final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.APNS_DISCONNECT, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.e(TAG, "退出应用 socket 断开成功");
                handler.sendEmptyMessage(SOCKET_DISCONNECT_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.sendEmptyMessage(SOCKET_DISCONNECT_FAILED);
                PictureAirLog.e(TAG, "退出应用 socket 断开失败,状态码：" + status);
            }
        });
    }


    /**
     * 手机端接收到推送后，调用清空推送数据
     *
     * @param clearType
     */
    public static void clearSocketCachePhotoCount(String clearType) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.CLEAR_TYPE, clearType);
        HttpUtil1.asyncGet(Common.BASE_URL_TEST + Common.CLEAR_PHOTO_COUNT, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.e(TAG, "收到推送 清空服务器消息成功");
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.e(TAG, "收到推送 清空服务器消息失败,状态码：" + status);
            }
        });
    }

    /***************************************推送 End**************************************/

    public static ArrayList<PPinfo> PPlist = new ArrayList<PPinfo>();
    /**
     * 根据PP+选择PP界面。  曾经根据日期选择，现在不需要日期。
     * @param pppCode
     * @param handler
     */
    public static void getPPsByPPPAndDate(String pppCode, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
     * @param pps
     * @param pppCode
     * @param handler
     */
    public static void bindPPsDateToPPP(JSONArray pps, String pppCode, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
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
     */
    public static void getShareUrl(String photoID, String shareType, final Handler handler) {
        RequestParams params = new RequestParams();
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
        HttpUtil1.asyncPost(Common.BASE_URL_TEST + Common.GET_SHARE_URL, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.e(TAG, "获取分享成功");
                handler.obtainMessage(GET_SHARE_URL_SUCCESS, jsonObject).sendToTarget();

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.e(TAG, "获取分享失败" + status);
                handler.obtainMessage(GET_SHARE_URL_FAILED, status, 0).sendToTarget();
            }
        });
    }
}