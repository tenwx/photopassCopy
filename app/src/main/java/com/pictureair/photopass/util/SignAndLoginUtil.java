package com.pictureair.photopass.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.keygenerator.PWJniUtil;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfoJson;
import com.pictureair.photopass.widget.MyToast;

import com.pictureair.photopass.widget.CustomProgressDialog;


/**
 * 接受loginActicity中传来的手机号和密码
 * 1.首先验证手机号是否注册过
 * 2.进行注册
 * 3.自动登录到主界面
 */
public class SignAndLoginUtil implements Handler.Callback {
    private String pwd;
    private String account;
    private String name, birthday, gender, country;
    private SharedPreferences sp;
    private Editor editor;
    private MyToast myToast;
    private Context context;
    private CustomProgressDialog customProgressDialog;
    private OnLoginSuccessListener onLoginSuccessListener;
    private Handler handler;
    /**
     * 注册
     */
    private boolean isSign;

    /**
     * 修改信息
     */
    private boolean needModifyInfo;

    private static final String TAG = "SignAndLoginUtil";
    private int id = 0;

    public SignAndLoginUtil(Context c, String account, String pwdStr, boolean isSign, boolean needModifyInfo,
                            String name, String birthday, String gender, String country, OnLoginSuccessListener onLoginSuccessListener) {
        this.context = c;
        this.account = account;
        this.pwd = pwdStr;
        this.isSign = isSign;
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
        this.country = country;
        this.needModifyInfo = needModifyInfo;
        this.onLoginSuccessListener = onLoginSuccessListener;
        PictureAirLog.out("account---->" + account + ",pwd---->" + AppUtil.md5(pwdStr));
        start();
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case API1.GET_TOKEN_ID_FAILED://获取tokenId失败
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
                myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                break;

            case API1.GET_TOKEN_ID_SUCCESS://获取tokenId成功
                PictureAirLog.out("start sign or login");
                if (isSign) {
                    API1.Register(account, pwd, AESKeyHelper.decryptString(sp.getString(Common.USERINFO_TOKENID, null), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP)), handler);
                } else {
                    API1.Login(context, account, pwd, handler);
                }
                break;

            case API1.LOGIN_FAILED://登录失败
                switch (msg.arg1) {
                    case 6035://token过期
                        id = R.string.http_error_code_401;
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
                editor = sp.edit();
                editor.putString(Common.USERINFO_TOKENID, null);
                editor.commit();
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
                    API1.updateProfile(AESKeyHelper.decryptString(sp.getString(Common.USERINFO_TOKENID, ""), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP)),
                            name, birthday, gender, country, "", API1.UPDATE_PROFILE_ALL, handler);
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
                editor = sp.edit();
                editor.putString(Common.USERINFO_TOKENID, null);
                editor.commit();
                myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                break;

            case API1.SIGN_SUCCESS://注册成功
                API1.Login(context, account, pwd, handler);
                break;


            case API1.UPDATE_PROFILE_FAILED://修改个人信息失败
            case API1.GET_CART_FAILED://获取购物车失败
            case API1.GET_STOREID_FAILED://获取storeId失败
                id = ReflectionUtil.getStringId(context, msg.arg1);
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
                editor = sp.edit();
                editor.putString(Common.USERINFO_TOKENID, null);
                editor.commit();
                myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                break;

            //修改个人信息成功
            case API1.UPDATE_PROFILE_SUCCESS:
                if (needModifyInfo) {
                    //需要将个人信息保存部分
                    editor = sp.edit();
                    editor.putString(Common.USERINFO_COUNTRY, country);
                    editor.putString(Common.USERINFO_GENDER, gender);
                    editor.putString(Common.USERINFO_BIRTHDAY, birthday);
                    editor.putString(Common.USERINFO_NICKNAME, name);
                    editor.apply();
                }
                PictureAirLog.v(TAG, "start get cart");
                PictureAirLog.out("start get cart");
                API1.getCarts(null,handler);
                break;

            case API1.GET_CART_SUCCESS://获取购物车成功
                PictureAirLog.out("get cart count success");
                int cartCount = 0;
                CartItemInfoJson cartItemInfoJson = JsonTools.parseObject((JSONObject) msg.obj, CartItemInfoJson.class);//CartItemInfoJson.getString()
                if (cartItemInfoJson != null && cartItemInfoJson.getItems() != null && cartItemInfoJson.getItems().size() > 0) {
                    cartCount = cartItemInfoJson.getTotalCount();
                }
                Editor ed = sp.edit();
                ed.putInt(Common.CART_COUNT, cartCount);
                ed.commit();
                PictureAirLog.out("start get pp");
                //获取StoreId
                API1.getStoreId(handler);
                break;

            case API1.GET_STOREID_SUCCESS://获取storeId成功
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
                PictureAirLog.v(TAG, "get storeid success");
                JSONObject jsonObject = JSONObject.parseObject(msg.obj.toString());
                Editor editor = sp.edit();
                editor.putString(Common.CURRENCY, jsonObject.getString("currency"));
                editor.putString(Common.STORE_ID, jsonObject.getString("storeId"));
                editor.commit();
                //登录成功，跳转界面
                loginsuccess();
                break;

            default:
                break;
        }
        return false;
    }

    /**
     * 登录成功之后的跳转
     */
    private void loginsuccess() {
        if (customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
        onLoginSuccessListener.loginSuccess();
    }

    /**
     * 开始登录
     */
    private void start() {
        PictureAirLog.v(TAG, "start login or sign");
        myToast = new MyToast(context);
        customProgressDialog = CustomProgressDialog.show(context, context.getString(R.string.is_loading), false, null);
        sp = context.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        handler = new Handler(this);
        if (null == sp.getString(Common.USERINFO_TOKENID, null)) {
            PictureAirLog.v(TAG, "no tokenid");
            API1.getTokenId(context, handler);
        } else {
            PictureAirLog.v(TAG, "has tokenid");
            handler.sendEmptyMessage(API1.GET_TOKEN_ID_SUCCESS);
        }
    }

    public interface OnLoginSuccessListener {
        void loginSuccess();
    }

}
