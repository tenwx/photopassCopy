package com.pictureair.photopass.util;

import android.content.Context;
import android.os.CountDownTimer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.widget.RegisterOrForgetCallback;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.RxActivity;

import java.lang.ref.WeakReference;
import rx.android.schedulers.AndroidSchedulers;


/**
 * Created by bass on 16/4/27.
 */
public class RegisterTool implements SignAndLoginUtil.OnLoginSuccessListener {
    private String tokenId;
    private Context context;
    private RegisterOrForgetCallback registerOrForgetView;
    private String languageType;
    private String phone = "";
    private String pwd = "";
    public static final String SIGN_ACTIVITY = "sign";
    public static final String FORGET_ACTIVITY = "forget";
    private String whatActivity = "";
    private SignAndLoginUtil signAndLoginUtil;
    private RegisterCountDownTimer timer;

    public void setWhatActivity(String whatActivity) {
        this.whatActivity = whatActivity;
    }

    /**
     * 验证码发送成功
     */
    private void sendValidateCodeSuccess() {
        registerOrForgetView.goneDialog();
        timer = new RegisterCountDownTimer(60 * 1000, 1000, this);
        timer.start();
    }

    private void validateSuccess() {
        if (whatActivity.equals(SIGN_ACTIVITY))
            signAndLoginUtil.start(phone, pwd, true, false, null, null, null, null, null, null);
        else if (whatActivity.equals(FORGET_ACTIVITY))
            signAndLoginUtil.start(phone, pwd, false, false, null, null, null, null, null, null);// 登录
    }

    public RegisterTool(String tokenId, Context context, RegisterOrForgetCallback registerView, String languageType) {
        this.tokenId = tokenId;
        this.context = context;
        this.registerOrForgetView = registerView;
        this.languageType = languageType;
        signAndLoginUtil = new SignAndLoginUtil(context, this);
    }

    public void submit(String validateCode, String phone, String pwd) {
        registerOrForgetView.showDialog();
        this.phone = phone;
        this.pwd = pwd;
        validateCode(validateCode);
    }

    public void sendSMSValidateCode(String phone) {
        registerOrForgetView.showDialog();
        this.phone = phone;
        if (null == tokenId) {
            getTokenId();
        } else {
            sendSMSCode();
        }
    }

    @Override
    public void loginSuccess() {
        registerOrForgetView.onSuccess();
    }

    public void onDestroy() {
        if (signAndLoginUtil != null) {
            signAndLoginUtil.destroy();
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        registerOrForgetView.goneDialog();
    }

    public void forgetPwd(String pwd) {
        registerOrForgetView.showDialog();
        this.pwd = pwd;
        findPwd();
    }


    private void findPwd() {
        String realPhone = AppUtil.getCorrectAccount(phone);
        API2.findPwd(pwd, realPhone)
                .compose(((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        registerOrForgetView.goneDialog();
                        validateSuccess();
                    }

                    @Override
                    public void _onError(int status) {
                        onRequestError(status);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });

    }


    private void getTokenId() {
        API2.getTokenId(context)
                .compose(((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {

                    @Override
                    public void onCompleted() {
                        sendSMSCode();
                    }

                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        try {
                            byte[] key = ACache.get(MyApplication.getInstance()).getAsBinary(Common.USERINFO_SALT);
                            if (key == null) {
                                ACache.get(context).put(Common.USERINFO_SALT, AESKeyHelper.secureByteRandom());
                            }
                            SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID,
                                    AESKeyHelper.encryptString(jsonObject.getString(Common.USERINFO_TOKENID), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0)));
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                        tokenId = MyApplication.getTokenId();
                    }

                    @Override
                    public void _onError(int status) {
                        onRequestError(status);
                    }
                });
    }

    private void onRequestError(int status) {
        registerOrForgetView.goneDialog();
        registerOrForgetView.onFai(status);
    }

    private void sendSMSCode() {
        String realPhone = AppUtil.getCorrectAccount(phone);
        API2.sendSMSValidateCode( tokenId, realPhone, languageType, true)
                .compose(((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {

                    }

                    @Override
                    public void _onError(int status) {
                        onRequestError(status);
                    }

                    @Override
                    public void onCompleted() {
                        sendValidateCodeSuccess();
                    }
                });
    }

    private void validateCode(String validateCode) {
        String realPhone = AppUtil.getCorrectAccount(phone);
        API2.validateCode(tokenId, validateCode, realPhone, true)
                .compose(((RxActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        registerOrForgetView.goneDialog();
                        if (whatActivity.equals(SIGN_ACTIVITY))
                            validateSuccess();//验证码ok
                        else if (whatActivity.equals(FORGET_ACTIVITY))
                            registerOrForgetView.nextPageForget();
                    }

                    @Override
                    public void _onError(int status) {
                        onRequestError(status);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void timeCountDown(int time) {
        registerOrForgetView.countDown(time);
    }

    private static class RegisterCountDownTimer extends CountDownTimer{
        private WeakReference<RegisterTool> registerToolWeakRef;

        public RegisterCountDownTimer(long millisInFuture, long countDownInterval, RegisterTool registerTool) {
            super(millisInFuture, countDownInterval);
            registerToolWeakRef = new WeakReference<>(registerTool);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int sec = 1000;
            int ss = (int)millisUntilFinished / sec;
            if (registerToolWeakRef.get() != null) {
                registerToolWeakRef.get().timeCountDown(ss);
                PictureAirLog.d("registerTool count down", ss + "s");
            }
        }

        @Override
        public void onFinish() {
            if (registerToolWeakRef.get() != null) {
                registerToolWeakRef.get().timeCountDown(0);
                PictureAirLog.d("registerTool count down", "finish");
            }
        }
    }

}
