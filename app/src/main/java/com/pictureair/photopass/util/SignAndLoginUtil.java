package com.pictureair.photopass.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfoJson;
import com.pictureair.photopass.widget.PWProgressDialog;
import com.pictureair.photopass.widget.PWToast;

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
    private PWToast myToast;
    private Context context;
    private PWProgressDialog pwProgressDialog;
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

    public SignAndLoginUtil(Context c, OnLoginSuccessListener onLoginSuccessListener) {
        this.context = c;
        this.onLoginSuccessListener = onLoginSuccessListener;
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case API1.GET_TOKEN_ID_FAILED://获取tokenId失败
                dismissPWProgressDialog();
                myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                break;

            case API1.GET_TOKEN_ID_SUCCESS://获取tokenId成功
                PictureAirLog.out("start sign or login");
                if (isSign) {
                    API1.Register(account, pwd,
                            AESKeyHelper.decryptString(
                                    SPUtils.getString(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, null),
                                    PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0)), handler);
                } else {
                    API1.Login(context, account, pwd, handler);
                }
                break;

            case API1.LOGIN_FAILED://登录失败
                switch (msg.arg1) {
                    case 6035://token过期
                        id = R.string.http_error_code_401;
                        PictureAirLog.v(TAG, "tokenExpired");
                        SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, null);
                        break;

                    case 6031://用户名不存在
                    case 6033://密码错误
                        id = ReflectionUtil.getStringId(context, msg.arg1);
                        break;

                    default:
                        id = ReflectionUtil.getStringId(context, msg.arg1);
                        break;
                }
                dismissPWProgressDialog();
                SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, null);
                myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                break;

            case API1.LOGIN_SUCCESS://登录成功
                String headUrl = SPUtils.getString(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_HEADPHOTO, null);
                if (headUrl != null) {//头像不为空，下载头像文件
                    API1.downloadHeadFile(Common.PHOTO_URL + headUrl, Common.USER_PATH, Common.HEADPHOTO_PATH);
                }
                String bgUrl = SPUtils.getString(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_BGPHOTO, null);
                if (bgUrl != null) {//背景不为空，下载背景文件
                    API1.downloadHeadFile(Common.PHOTO_URL + bgUrl, Common.USER_PATH, Common.BGPHOTO_PAHT);
                }

                if (needModifyInfo) {//需要修改个人信息
                    API1.updateProfile(AESKeyHelper.decryptString(
                            SPUtils.getString(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, ""),
                            PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0)),
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
                dismissPWProgressDialog();
                SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, null);
                myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                break;

            case API1.SIGN_SUCCESS://注册成功
                API1.Login(context, account, pwd, handler);
                break;


            case API1.UPDATE_PROFILE_FAILED://修改个人信息失败
            case API1.GET_CART_FAILED://获取购物车失败
            case API1.GET_STOREID_FAILED://获取storeId失败
                id = ReflectionUtil.getStringId(context, msg.arg1);
                dismissPWProgressDialog();
                SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, null);
                myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                break;

            //修改个人信息成功
            case API1.UPDATE_PROFILE_SUCCESS:
                if (needModifyInfo) {
                    //需要将个人信息保存部分
                    SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_COUNTRY, country);
                    SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_GENDER, gender);
                    SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_BIRTHDAY, birthday);
                    SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, name);
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
                SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, cartCount);
                PictureAirLog.out("start get pp");
                //获取StoreId
                API1.getStoreId(handler);
                break;

            case API1.GET_STOREID_SUCCESS://获取storeId成功
                dismissPWProgressDialog();
                PictureAirLog.v(TAG, "get storeid success");
                JSONObject jsonObject = JSONObject.parseObject(msg.obj.toString());
                SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CURRENCY, jsonObject.getString("currency"));
                SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.STORE_ID, jsonObject.getString("storeId"));
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
        dismissPWProgressDialog();
        onLoginSuccessListener.loginSuccess();
    }

    public void start(String account, String pwdStr, boolean isSign, boolean needModifyInfo,
                            String name, String birthday, String gender, String country) {
        this.account = account;
        this.pwd = pwdStr;
        this.isSign = isSign;
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
        this.country = country;
        this.needModifyInfo = needModifyInfo;
        PictureAirLog.out("account---->" + account + ",pwd---->" + AppUtil.md5(pwdStr));
        myToast = new PWToast(context);
        pwProgressDialog = new PWProgressDialog(context)
                .setPWProgressDialogMessage(R.string.is_loading)
                .pwProgressDialogCreate();
        pwProgressDialog.pwProgressDialogShow();

        handler = new Handler(this);
        if (null == SPUtils.getString(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, null)) {
            PictureAirLog.v(TAG, "no tokenid");
            API1.getTokenId(context, handler);
        } else {
            PictureAirLog.v(TAG, "has tokenid");
            handler.sendEmptyMessage(API1.GET_TOKEN_ID_SUCCESS);
        }
    }

    public void destroy(){
        dismissPWProgressDialog();
        pwProgressDialog = null;
    }

    /**
     * dismiss pwProgressDialog
     */
    private void dismissPWProgressDialog(){
        if (null != pwProgressDialog) {
            pwProgressDialog.pwProgressDialogDismiss();
        }
    }

    public interface OnLoginSuccessListener {
        void loginSuccess();
    }

}
