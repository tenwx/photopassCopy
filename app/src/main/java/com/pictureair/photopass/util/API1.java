package com.pictureair.photopass.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;

import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.widget.CustomProgressBarPop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;


/**
 * 所有与后台的交互都封装到此类
 */
public class API1 {
    private static final String TAG = "API";
    public static final int SUCCESS = 111;
    public static final int FAILURE = 222;//失败需分情况判断，是网络未打开还是IP地址无法连接亦或是没有授予网络权限
    public static final int GET_TOP_GOODS_SUCCESS = 666;
    public static final int GET_BANNER_GOODS_SUCCESS = 777;
    public static final int GET_ORDER_NO_SUCCESS = 999;
    public static final int DELETE_ADDRESS_SUCCESS = 11;
    public static final int ADD_ADDRESS_SUCCESS = 12;
    public static final int MODIFY_ADDRESS_SUCCESS = 13;
    public static final int GET_TOKENID_SUCCESS = 16;
    public static final int GET_ORDER_NO_FAILED = 17;
    public static final int UPLOADING_PHOTO = 512;
    public static final int SIGN_FAILED = 5220;
    public static final int DELETE_ORDER_SUCCESS = 14;
    public static final int BIND_PP_FAILURE = 6666;


    public static final int GET_LOCATION_SUCCESS = 301;
    public static final int GET_LOCATION_FAILED = 300;

    public static final int GET_PHOTOS_SUCCESS = 311;
    public static final int GET_PHOTOS_FAILED = 310;

    public static final int GET_REFRESH_PHOTOS_SUCCESS = 321;
    public static final int GET_REFRESH_PHOTOS_FAILED = 320;

    public static final int LOGOUT_SUCCESS = 331;
    public static final int LOGOUT_FAILED = 330;

    public static final int CHECK_CODE_SUCCESS = 341;
    public static final int CHECK_CODE_FAILED = 340;

    public static final int GET_PPS_SUCCESS = 351;
    public static final int GET_PPS_FAILED = 350;

    public static final int GET_CART_COUNT_SUCCESS = 361;
    public static final int GET_CART_COUNT_FAILED = 360;

    public static final int GET_PPP_SUCCESS = 371;
    public static final int GET_PPP_FAILED = 370;

    public static final int GET_STOREID_SUCCESS = 381;
    public static final int GET_STOREID_FAILED = 380;

    public static final int MODIFY_PWD_SUCCESS = 391;
    public static final int MODIFY_PWD_FAILED = 390;

    public static final int GET_DETAIL_GOOD_SUCCESS = 401;
    public static final int GET_DETAIL_GOOD_FAILED = 400;

    public static final int GET_ORDER_SUCCESS = 411;
    public static final int GET_ORDER_FAILED = 410;

    public static final int GET_ADDRESS_SUCCESS = 421;
    public static final int GET_ADDRESS_FAILED = 420;

    public static final int UPDATE_PROFILE_SUCCESS = 431;
    public static final int UPDATE_PROFILE_FAILED = 430;

    public static final int GET_ALL_GOODS_SUCCESS = 441;
    public static final int GET_ALL_GOODS_FAILED = 440;

    public static final int ADD_TO_CART_SUCCESS = 451;
    public static final int ADD_TO_CART_FAILED = 450;

    public static final int GET_PHOTOPASSPLUS_SUCCESS = 461;
    public static final int GET_PHOTOPASSPLUS_FAILED = 460;

    public static final int GET_FAVORITE_LOCATION_SUCCESS = 471;
    public static final int GET_FAVORITE_LOCATION_FAILED = 470;

    public static final int EDIT_FAVORITE_LOCATION_SUCCESS = 481;
    public static final int EDIT_FAVORITE_LOCATION_FAILED = 480;

    public static final int GET_CART_SUCCESS = 491;
    public static final int GET_CART_FAILED = 490;

    public static final int DELETE_CART_SUCCESS = 501;
    public static final int DELETE_CART_FAILED = 500;

    public static final int UPLOAD_PHOTO_SUCCESS = 511;
    public static final int UPLOAD_PHOTO_FAILED = 510;

    public static final int GET_PP_SUCCESS = 521;
    public static final int GET_PP_FAILED = 520;

    public static final int HIDE_PP_SUCCESS = 531;
    public static final int HIDE_PP_FAILED = 530;

    public static final int GET_LAST_CONTENT_SUCCESS = 541;
    public static final int GET_LAST_CONTENT_FAILED = 540;

    public static final int APK_NEED_UPDATE = 551;
    public static final int APK_NEED_NOT_UPDATE = 550;

    public static final int DOWNLOAD_APK_SUCCESS = 561;
    public static final int DOWNLOAD_APK_FAILED = 560;

    /**
     * 根据状态码返回提示语
     *
     * @param status 状态码
     * @return String
     */
    public static String getStringByStatus(int status) {
        String str = "";
        //.......

        return str;

    }


    static {
        HttpUtil1.setBaseUrl(Common.BASE_URL_TEST);
    }

    /**
     * 查询手机号是否已经被注册
     *
     * @param context
     * @param phone
     * @param handler
     */
    public static void findPhone(Context context, String phone, final Handler handler) {
        RequestParams params = new RequestParams();
        params.put("phone", phone);
        HttpUtil1.asyncPost(Common.IS_EXIST_PHONE, params, new HttpCallback() {
            @Override
            public void onSuccess(String result) {
                super.onSuccess(result);
                //成功
                handler.sendEmptyMessage(SUCCESS);

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                //失败
                handler.obtainMessage(FAILURE, getStringByStatus(status));
            }
        });
    }


    /**
     * 发送设备ID获取tokenId
     *
     * @param context
     */
    public static void getTokenId(final Context context) {
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
                    e.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
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
                try {
                    JsonUtil.getUserInfo(context, jsonObject, handler);
                    handler.sendEmptyMessage(SUCCESS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                //根据返回的status 获取对应的字符串
                handler.obtainMessage(FAILURE, getStringByStatus(status));
            }
        });

    }


    /**
     * 退出账号
     *
     * @param context 上下文
     * @param handler handler
     */
    public static void Logout(final Context context, final Handler handler) {
        final SharedPreferences sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        RequestParams params = new RequestParams();
        String tokenId = sp.getString(Common.USERINFO_TOKENID, null);//如果为空 怎出处理？
        params.put(Common.USERINFO_TOKENID, tokenId);

        HttpUtil1.asyncPost(Common.LOGOUT, params, new HttpCallback() {
            @Override
            public void onSuccess(String result) {
                super.onSuccess(result);
                handler.sendEmptyMessage(LOGOUT_SUCCESS);

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.sendEmptyMessage(LOGOUT_FAILED);


            }
        });
    }


    /**
     * 注册
     *
     * @param context 上下文
     * @param userName name
     * @param password pwd
     * @param handler handler
     */
    public static void Sign(final Context context, final String userName, final String password, final Handler handler) {
        final SharedPreferences sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        final RequestParams params = new RequestParams();
        params.put(Common.USERINFO_USERNAME, userName);
        params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
        params.put(Common.USERINFO_TOKENID, sp.getString(Common.USERINFO_TOKENID, null));
        HttpUtil1.asyncPost(Common.REGISTER, params, new HttpCallback() {
            @Override
            public void onSuccess(String result) {
                super.onSuccess(result);
                // 注册成功直接跳转到登录页面自动登录
                StringBuffer sb = new StringBuffer();
                sb.append(Common.BASE_URL).append(Common.LOGIN);
                params.put(Common.USERINFO_TOKENID, sp.getString(Common.USERINFO_TOKENID, null));
                params.put(Common.USERINFO_USERNAME, userName);
                params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
                //注册成功进入登录界面
                HttpUtil1.asyncPost(Common.LOGIN, params, new HttpCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        super.onSuccess(jsonObject);
                        try {
                            JsonUtil.getUserInfo(context, jsonObject, handler);
                            handler.sendEmptyMessage(SUCCESS);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int status) {
                        super.onFailure(status);
                        handler.obtainMessage(SIGN_FAILED, getStringByStatus(status));

                    }
                });
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(SIGN_FAILED, getStringByStatus(status));
            }
        });
    }


    /**
     * 修改密码或者忘记密码接口
     * @param context
     * @param oldPwd 旧密码，修改的时候用到，如果是忘记密码的话，设为null
     * @param newPwd 新密码
     * @param type 判断是否是修改密码（null）还是忘记密码（forget）
     * @param handler
     */
    public static void modifyPwd(Context context, String oldPwd, String newPwd, String type, final Handler handler) {
        SharedPreferences sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        RequestParams params = new RequestParams();
        params.put(Common.NEW_PASSWORD, AppUtil.md5(newPwd));
        params.put(Common.USERINFO_TOKENID, sp.getString(Common.USERINFO_TOKENID, null));
        if (type.equals("forget")) {//忘记密码，不需要填写oldpassword
            params.put(Common.MODIFY_OR_FORGET, type);
        }else {//修改密码操作，type不要填写
            params.put(Common.OLD_PASSWORD, AppUtil.md5(oldPwd));
        }

        HttpUtil1.asyncPost(Common.MODIFYPWD, params, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                handler.sendEmptyMessage(MODIFY_PWD_SUCCESS);
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(MODIFY_PWD_FAILED,getStringByStatus(status));

            }
        });
    }


    /**
     *上传个人图片信息，头像或背景图
     * @param url
     * @param params
     * @param handler
     * @param position 修改图片的时候需要这个参数来定位
     * @throws FileNotFoundException
     */
    public static void SetPhoto(String url , RequestParams params,final Handler handler,final int position, final CustomProgressBarPop diaBarPop) throws FileNotFoundException {
        // 需要更新服务器中用户背景图片信息

        HttpUtil1.asynUploadFile(url, params, MyApplication.getInstance(), new HttpCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                Message message = handler.obtainMessage(UPLOAD_PHOTO_SUCCESS);
                message.arg1 = position;
                message.obj = jsonObject;
                handler.sendMessage(message);

            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                handler.obtainMessage(UPLOAD_PHOTO_FAILED,getStringByStatus(status));
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                diaBarPop.setProgress(bytesWritten, totalSize);
            }
        });
    }

}
