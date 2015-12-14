package com.pictureair.photopass.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.entity.PPPinfo;
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

    public static final int CHECK_CODE_SUCCESS = 341;
    public static final int CHECK_CODE_FAILED = 340;

    public static final int GET_PPS_SUCCESS = 351;
    public static final int GET_PPS_FAILED = 350;

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

    /**
     * Story
     */
    public static final int GET_ALL_LOCATION_FAILED = 2000;
    public static final int GET_ALL_LOCATION_SUCCESS = 2001;

    public static final int GET_ALL_PHOTOS_BY_CONDITIONS_FAILED = 2010;
    public static final int GET_ALL_PHOTOS_BY_CONDITIONS_SUCCESS = 2011;

    public static final int GET_REFRESH_PHOTOS_BY_CONDITIONS_FAILED = 2020;
    public static final int GET_REFRESH_PHOTOS_BY_CONDITIONS_SUCCESS = 2021;


    //我的模块 start
    public static final int BIND_PP_FAILURE = 5000;
    public static final int BIND_PP_SUCCESS = 5001;

    public static final int UPDATE_PROFILE_SUCCESS = 5011;
    public static final int UPDATE_PROFILE_FAILED = 5010;

    public static final int SCAN_PPP_FAILED = 5400;
    public static final int SCAN_PPP_SUCCESS = 5401;


    //我的模块 end


    static {
        HttpUtil1.setBaseUrl(Common.BASE_URL_TEST);
    }


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
        HttpUtil1.asyncGet(Common.GET_TOKENID, params, new HttpCallback() {

            @Override
            public void onSuccess(JSONObject jsonObject) {

                super.onSuccess(jsonObject);
                try {
                    SharedPreferences sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
                    Editor e = sp.edit();
                    e.putString(Common.USERINFO_TOKENID, jsonObject.getString(Common.USERINFO_TOKENID));
                    PictureAirLog.out("tokenid---->" + jsonObject.getString(Common.USERINFO_TOKENID));
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
        final SharedPreferences sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        RequestParams params = new RequestParams();
        String tokenId = sp.getString(Common.USERINFO_TOKENID, null);
        params.put(Common.USERINFO_USERNAME, userName);
        params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
        params.put(Common.USERINFO_TOKENID, tokenId);


        HttpUtil1.asyncPost(Common.LOGIN, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("Login--->" + jsonObject.toString());
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
    public static void Register(final String userName, final String password, final Handler handler) {
        final RequestParams params = new RequestParams();
        params.put(Common.USERINFO_USERNAME, userName);
        params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
        HttpUtil1.asyncPost(Common.REGISTER, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PictureAirLog.out("sign success ---- > " + jsonObject);
                handler.obtainMessage(SIGN_SUCCESS).sendToTarget();
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
     * @param context
     * @param handler
     */
    public static void getLocationInfo(final Context context, final Handler handler) {
        HttpUtil1.asyncPost(Common.GET_ALL_LOCATIONS_OF_ALBUM_GROUP, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                ACache.get(context).put(Common.LOCATION_INFO, jsonObject.toString());
                handler.obtainMessage(GET_ALL_LOCATION_SUCCESS, jsonObject).sendToTarget();
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_ALL_LOCATION_FAILED, status, 0).sendToTarget();
            }
        });
    }


    /**
     * 获取用户照片
     * @param tokenId
     * @param handler
     * @param timeString 根据时间获取图片信息
     */
    public static void getPhotosByConditions(final String tokenId,final Handler handler,final String timeString) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LAST_UPDATE_TIME, timeString);
        PictureAirLog.out("the time of start get photos = " + timeString);
        HttpUtil1.asyncGet(Common.GET_PHOTOS_BY_CONDITIONS, params, new HttpCallback() {
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
     * 上传个人图片信息，头像或背景图
     *
     * @param url
     * @param params
     * @param handler
     * @param position 修改图片的时候需要这个参数来定位
     * @throws FileNotFoundException
     */
    public static void SetPhoto(String url, RequestParams params, final Handler handler, final int position, final CustomProgressBarPop diaBarPop) throws FileNotFoundException {
        // 需要更新服务器中用户背景图片信息
        HttpUtil1.asyncPost(url, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(UPLOAD_PHOTO_SUCCESS, position, 0, jsonObject);

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                PictureAirLog.v("SetPhoto onFailure", "status: " + status);
                handler.obtainMessage(UPLOAD_PHOTO_FAILED, status,0);


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

        HttpUtil1.asyncPost(Common.UPDATE_PROFILE, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(UPDATE_PROFILE_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(UPDATE_PROFILE_FAILED, status,0).sendToTarget();
            }
        });
    }

    /**
     * 获取订单信息 -- 有大改动
     */


    /**
     * 获取所有的P
     *
     * @param tokenId tokenId
     * @param handler handler
     */
    public static void getPPSByUserId(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);


        HttpUtil1.asyncPost(Common.GET_PPS_BY_USERID, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(GET_PPS_SUCCESS, jsonObject);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_PPS_FAILED, status);

            }
        });
    }

    /**
     * 获取账号下所有ppp
     *
     * @param tokenId tokenId
     * @param handler handler
     */
    public static ArrayList<PPPinfo> PPPlist;

    public static void getPPPSByUserId(String tokenId, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.USERINFO_TOKENID, tokenId);
        HttpUtil1.asyncPost(Common.GET_PPPS_BY_USERID, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                PPPlist = JsonUtil.getPPPSByUserId(jsonObject);

                handler.obtainMessage(GET_PPP_SUCCESS, jsonObject);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(GET_PPP_FAILED, status);

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
        HttpUtil1.asyncPost(Common.HIDE_PPS, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(HIDE_PP_SUCCESS, jsonObject);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(HIDE_PP_FAILED, status);

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

        HttpUtil1.asyncPost(Common.BIND_PPS_TO_PPP, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(BIND_PP_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(BIND_PP_FAILURE, status, 0);


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
        HttpUtil1.asyncPost(Common.BIND_PPP_TO_USER, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(SCAN_PPP_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.sendEmptyMessage(SCAN_PPP_FAILED);
            }
        });
    }

    /**
     * 检查扫描的结果是否正确，并且返回是否已经被使用
     *
     * @param code
     * @param handler
     */
    public static void checkCodeAvailable(String code, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put(Common.CODE, code);
        HttpUtil1.asyncPost(Common.CHECK_CODE_AVAILABLE, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.obtainMessage(CHECK_CODE_SUCCESS, jsonObject);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(CHECK_CODE_FAILED, status, 0);

            }
        });
    }


    /***************************************我的模块 end**************************************/


}
