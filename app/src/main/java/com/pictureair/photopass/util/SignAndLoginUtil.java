package com.pictureair.photopass.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfoJson;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.widget.PWProgressDialog;
import com.pictureair.photopass.widget.PWToast;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.RxActivity;

import rx.android.schedulers.AndroidSchedulers;

/**
 * 接受loginActicity中传来的手机号和密码
 * 1.首先验证手机号是否注册过
 * 2.进行注册
 * 3.自动登录到主界面
 */
public class SignAndLoginUtil implements Handler.Callback {
    private String pwd;
    private String account;
    private String name, birthday, gender, country, loginType, verificationCode;
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
                            String name, String birthday, String gender, String country, String loginType, String verificationCode) {
        this.account = account;
        this.pwd = pwdStr;
        this.isSign = isSign;
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
        this.country = country;
        this.needModifyInfo = needModifyInfo;
        this.loginType = loginType;
        this.verificationCode = verificationCode;
        if (loginType == null) {
            PictureAirLog.out("account---->" + account + ",pwd---->" + AppUtil.md5(pwdStr));
        }
        myToast = new PWToast(context);
        pwProgressDialog = new PWProgressDialog(context)
                .setPWProgressDialogMessage(R.string.is_loading)
                .pwProgressDialogCreate();
        pwProgressDialog.pwProgressDialogShow();

        handler = new Handler(this);
        if (null == SPUtils.getString(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, null)) {
            PictureAirLog.v(TAG, "no tokenid");
            getTokenId();
        } else {
            PictureAirLog.v(TAG, "has tokenid");
            goSignOrLogin();
        }
    }


    private void getTokenId(){
        API2.getTokenId(context)
                .compose(((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        try {
                            byte[] key = ACache.get(MyApplication.getInstance()).getAsBinary(Common.USERINFO_SALT);
                            if (key == null) {
                                ACache.get(context).put(Common.USERINFO_SALT, AESKeyHelper.secureByteRandom());
                            }
                            SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID,
                                    AESKeyHelper.encryptString(jsonObject.getString(Common.USERINFO_TOKENID), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0)));
                            PictureAirLog.out("start sign or login");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {
                        goSignOrLogin();
                        PictureAirLog.e("getTokenId", "onCompleted end");
                    }
                });

    }

    private void goSignOrLogin() {
        if (isSign) {
            register();
        } else {
            login();
        }
    }

    private void register() {
        PictureAirLog.e("register", "register start");
        API2.Register(account, pwd)
                .compose(((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {

                    }

                    @Override
                    public void _onError(int status) {
                        switch (status) {
                            case 6029://邮箱已经存在
                            case 6030://手机号已经存在
                                id = ReflectionUtil.getStringId(context, status);
                                break;

                            default:
                                id = ReflectionUtil.getStringId(context, status);
                                break;
                        }
                        dismissPWProgressDialog();
                        SPUtils.remove(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID);
                        myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {
                        login();
                        PictureAirLog.e("register", "onCompleted end");
                    }
                });
    }

    private void login() {
        PictureAirLog.e("login", "login start");
        API2.Login(account, pwd, loginType, verificationCode)
                .compose(((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {

                        JsonUtil.getUserInfo(context, jsonObject, account, null);

                    }

                    @Override
                    public void _onError(int status) {
                        switch (status) {
                            case 6035://token过期
                                id = R.string.http_error_code_401;
                                PictureAirLog.v(TAG, "tokenExpired");
                                break;

                            case 6031://用户名不存在
                            case 6033://密码错误
                                id = ReflectionUtil.getStringId(context, status);
                                break;

                            default:
                                id = ReflectionUtil.getStringId(context, status);
                                break;
                        }
                        dismissPWProgressDialog();
                        SPUtils.remove(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID);
                        myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {
                        String headUrl = SPUtils.getString(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_HEADPHOTO, null);
                        if (headUrl != null) {//头像不为空，下载头像文件
                            API1.downloadHeadFile(Common.PHOTO_URL + headUrl, Common.USER_PATH, Common.HEADPHOTO_PATH, handler);
                        }
                        String bgUrl = SPUtils.getString(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_BGPHOTO, null);
                        if (bgUrl != null) {//背景不为空，下载背景文件
                            API1.downloadHeadFile(Common.PHOTO_URL + bgUrl, Common.USER_PATH, Common.BGPHOTO_PAHT, handler);
                        }

                        if (needModifyInfo) {//需要修改个人信息
                            updateProfile();
                        } else {
                            updateProfileSuccess();
                        }

                        PictureAirLog.e("login", "onCompleted end");
                    }
                });
    }


    private void updateProfile() {
        PictureAirLog.e("updateProfile", "updateProfile start");
        API2.updateProfile(AESKeyHelper.decryptString(
                SPUtils.getString(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, ""),
                PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0)),
                name, birthday, gender, country, "")//, API1.UPDATE_PROFILE_ALL
         .compose(((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
         .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {

                    }

                    @Override
                    public void _onError(int status) {
                        profileOrCartsFailed(status);
                    }

                    @Override
                    public void onCompleted() {
                        updateProfileSuccess();
                        PictureAirLog.e("updateProfile", "onCompleted end");
                    }
                });
    }


    private void getCarts() {
        PictureAirLog.e("getCarts", "getCarts start");
        API2.getCarts(null)
                .compose(((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.out("get cart count success");
                        int cartCount = 0;
                        CartItemInfoJson cartItemInfoJson = JsonTools.parseObject(jsonObject, CartItemInfoJson.class);//CartItemInfoJson.getString()
                        if (cartItemInfoJson != null && cartItemInfoJson.getItems() != null && cartItemInfoJson.getItems().size() > 0) {
                            cartCount = cartItemInfoJson.getTotalCount();
                        }
                        SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, cartCount);
                        PictureAirLog.out("start get pp");

                    }

                    @Override
                    public void _onError(int status) {
                        profileOrCartsFailed(status);
                    }

                    @Override
                    public void onCompleted() {
                        //登录成功，跳转界面
                        loginsuccess();
                        PictureAirLog.e("getCarts", "onCompleted end");
                    }
                });
    }

    private void updateProfileSuccess() {
        if (needModifyInfo) {
            //需要将个人信息保存部分
            SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_COUNTRY, country);
            SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_GENDER, gender);
            SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_BIRTHDAY, birthday);
            SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, name);
        }
        PictureAirLog.v(TAG, "start get cart");
        PictureAirLog.out("start get cart");
        getCarts();
    }

    private void profileOrCartsFailed(int status) {
        id = ReflectionUtil.getStringId(context, status);
        dismissPWProgressDialog();
        SPUtils.remove(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID);
        myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
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
