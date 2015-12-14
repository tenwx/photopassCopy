package com.pictureair.photopass.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.LoginCallBack;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 接受loginActicity中传来的手机号和密码
 * 1.首先验证手机号是否注册过
 * 2.进行注册
 * 3.自动登录到主界面
 */
public class SignAndLoginUtil {
    private String pwd;
    private String account;
    private String name, birthday, gender, country;
    private SharedPreferences sp;
    private Editor editor;
    private MyToast myToast;
    private Context context;
    private CustomProgressDialog customProgressDialog;
    private LoginCallBack loginCallBack;
    /**
     * 注册
     */
    private boolean isSign;

    /**
     * 修改信息
     */
    private boolean needModifyInfo;

    private static final int GET_IP_SUCCESS = 1;
    private static final int GET_IP_FAILED = 2;
    private static final String TAG = "SignAndLoginUtil";
    private int id = 0;

    public SignAndLoginUtil(Context c, String account, String pwdStr, boolean isSign, boolean needModifyInfo,
                            String name, String birthday, String gender, String country, LoginCallBack loginCallBack) {
        this.context = c;
        this.account = account;
        this.pwd = pwdStr;
        this.isSign = isSign;
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
        this.country = country;
        this.needModifyInfo = needModifyInfo;
        this.loginCallBack = loginCallBack;
        start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case API1.GET_TOKEN_ID_FAILED://获取tokenId失败
                    if (customProgressDialog.isShowing()) {
                        customProgressDialog.dismiss();
                    }
                    myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
                    break;

                case API1.GET_TOKEN_ID_SUCCESS://获取tokenId成功
                    PictureAirLog.out("start sign or login");
                    if (isSign) {
                        API1.Register(account, pwd, handler);
                    } else {
                        API1.Login(context, account, pwd, handler);
                    }
                    break;

                case API1.LOGIN_FAILED://登录失败
                    switch (msg.arg1) {
                        case 6035://token过期
                            id = R.string.http_failed;
                            PictureAirLog.v(TAG, "tokenExpired");
                            editor = sp.edit();
                            editor.putString(Common.USERINFO_TOKENID, null);
                            editor.commit();
                            break;

                        case 6031://用户名不存在
                        case 6033://密码错误
                            id = ReflectionUtil.getStringId(context, msg.arg1);
                            break;

                        default:
                            id = ReflectionUtil.getStringId(context, msg.arg1);
                            break;
                    }
                    if (customProgressDialog.isShowing()) {
                        customProgressDialog.dismiss();
                    }
                    myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                    break;

                case API1.LOGIN_SUCCESS://登录成功
                    String headUrl = sp.getString(Common.USERINFO_HEADPHOTO, null);
                    if (headUrl != null) {//头像不为空，下载头像文件
                        API1.downloadHeadFile(Common.PHOTO_URL + headUrl, Common.USER_PATH, Common.HEADPHOTO_PATH);
                    }
                    String bgUrl = sp.getString(Common.USERINFO_BGPHOTO, null);
                    if (bgUrl != null) {//背景不为空，下载背景文件
                        API1.downloadHeadFile(Common.PHOTO_URL + bgUrl, Common.USER_PATH, Common.BGPHOTO_PAHT);
                    }

                    if (needModifyInfo) {//需要修改个人信息
                        API1.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""),
                                name, birthday, gender, country, "", handler);
                    } else {
                        handler.sendEmptyMessage(API1.UPDATE_PROFILE_SUCCESS);
                    }
                    break;

                case API1.SIGN_FAILED://注册失败
                    PictureAirLog.out("msg --->" + msg.arg1);
                    switch (msg.arg1) {
                        case 6029://邮箱已经存在
                        case 6030://手机号已经存在
                            id = ReflectionUtil.getStringId(context, msg.arg1);
                            break;

                        default:
                            id = ReflectionUtil.getStringId(context, msg.arg1);
                            break;
                    }
                    if (customProgressDialog.isShowing()) {
                        customProgressDialog.dismiss();
                    }
                    myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                    break;

                case API1.SIGN_SUCCESS://注册成功
                    API1.Login(context, account, pwd, handler);
                    break;

                //修改个人信息失败
                case API1.UPDATE_PROFILE_FAILED:
                    id = ReflectionUtil.getStringId(context, msg.arg1);
                    if (customProgressDialog.isShowing()) {
                        customProgressDialog.dismiss();
                    }
                    myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                    break;

                //修改个人信息成功
                case API1.UPDATE_PROFILE_SUCCESS:
                    PictureAirLog.out("start get pp");
                    API.getPPSByUserId(MyApplication.getTokenId(), handler);// 获取pp列表

                    break;


                case API.GET_PPP_FAILED:
                case API.GET_STOREID_FAILED:
                    customProgressDialog.dismiss();
                    myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                    break;
                case API.GET_PPS_SUCCESS:// 获取pp列表成功
                    /**
                     * 获取pp成功之后，需要放入sharedPrefence中
                     */
                    JSONObject ppsJsonObject = (JSONObject) msg.obj;
                    //				Log.d(TAG, "pps===" + ppsJsonObject);
                    if (ppsJsonObject.has("PPList")) {
                        try {
                            JSONArray pplists = ppsJsonObject
                                    .getJSONArray("PPList");
                            Editor editor = sp.edit();
                            editor.putInt(Common.PP_COUNT, pplists.length());
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    //获取StoreId
                    API1.getStoreId(handler);
                    break;

                case API.GET_PPS_FAILED:// 获取pp列表失败
                    customProgressDialog.dismiss();
                    myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                    break;

                case API1.GET_STOREID_SUCCESS:
                    customProgressDialog.dismiss();
                    PictureAirLog.v(TAG, "get storeid success----------------");
                    com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject) msg.obj;
                    Editor editor = sp.edit();
                    editor.putString(Common.CURRENCY, jsonObject.getString("currency"));
                    editor.putString(Common.STORE_ID, jsonObject.getString("storeId"));
                    editor.apply();

                    loginsuccess();
                    break;

                case API.MODIFY_PWD_FAILED:
                    customProgressDialog.dismiss();
                    PictureAirLog.v(TAG, "signorfotget------modify pwd failed");
                    //提示错误
                    break;

                case API.MODIFY_PWD_SUCCESS:
                    customProgressDialog.dismiss();
                    PictureAirLog.v(TAG, "signorforget------modify pwd success");
                    //跳转至登录界面
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 登录成功之后的跳转
     */
    private void loginsuccess() {
        PictureAirLog.v(TAG, "loginsuccess----------------");
        if (customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
        loginCallBack.loginSuccess();
    }

    private void start() {
        PictureAirLog.out("start login or sign------->");
        myToast = new MyToast(context);
        customProgressDialog = CustomProgressDialog.show(context, context.getString(R.string.is_loading), false, null);
        sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        if (null == sp.getString(Common.USERINFO_TOKENID, null)) {
            PictureAirLog.v(TAG, "no tokenid");
            API1.getTokenId(context, handler);
        } else {
            PictureAirLog.v(TAG, "has tokenid");
            handler.sendEmptyMessage(API1.GET_TOKEN_ID_SUCCESS);
        }
    }

}
