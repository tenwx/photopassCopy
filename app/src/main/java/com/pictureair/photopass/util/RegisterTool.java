package com.pictureair.photopass.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.RegisterOrForgetCallback;


/**
 * Created by bass on 16/4/27.
 */
public class RegisterTool implements SignAndLoginUtil.OnLoginSuccessListener {
    private String tokenId;
    private Context context;
    private RegisterOrForgetCallback registerOrForgetView;
    private String languageType;
    private final int SEND_TIME = 0X1;
    private int time = 60;
    private String phone = "";
    private String pwd = "";
    public static final String SIGN_ACTIVITY = "sign";
    public static final String FORGET_ACTIVITY = "forget";
    private String whatActivity = "";
    private SignAndLoginUtil signAndLoginUtil;
    private PWToast myToast;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case API1.FIND_PWD_SUCCESS:
                    registerOrForgetView.goneDialog();
                    validateSuccess();
                    break;
                case API1.VALIDATECODE_SUCCESS:
                    registerOrForgetView.goneDialog();
                    if (whatActivity.equals(SIGN_ACTIVITY))
                        validateSuccess();//验证码ok
                    else if (whatActivity.equals(FORGET_ACTIVITY))
                        registerOrForgetView.nextPageForget();
                    break;

                case API1.SEND_SMS_VALIDATE_CODE_SUCCESS://验证码发送成功
                    sendValidateCodeSuccess();
                    break;
                case API1.FIND_PWD_FAILED:
                case API1.VALIDATECODE_FAILED:
                case API1.SEND_SMS_VALIDATE_CODE_FAILED:
                    registerOrForgetView.goneDialog();
                    registerOrForgetView.onFai(msg.arg1);
                    break;
                case SEND_TIME:
                    registerOrForgetView.countDown(time);
                    break;
                case API1.GET_TOKEN_ID_SUCCESS:
                    tokenId = MyApplication.getTokenId();
                    API1.sendSMSValidateCode(handler, tokenId, phone, languageType, true);
                    break;

                case API1.GET_TOKEN_ID_FAILED://获取tokenId失败
                    registerOrForgetView.goneDialog();
                    myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    break;
                default:
                    break;
            }
        }
    };

    public void setWhatActivity(String whatActivity) {
        this.whatActivity = whatActivity;
    }

    /**
     * 验证码发送成功
     */
    private void sendValidateCodeSuccess() {
        registerOrForgetView.goneDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                time = 60;
                try {
                    while (time > 0) {
                        Thread.sleep(1000);
                        --time;
                        handler.sendEmptyMessage(SEND_TIME);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
        myToast = new PWToast(context);
    }

    public void submit(String validateCode, String phone, String pwd) {
        registerOrForgetView.showDialog();
        this.phone = phone;
        this.pwd = pwd;
        API1.validateCode(handler, tokenId, validateCode, phone, true);
    }

    public void sendSMSValidateCode(String phone) {
        registerOrForgetView.showDialog();
        if (null == tokenId) {
            API1.getTokenId(context, handler);
        } else {
            handler.sendEmptyMessage(API1.GET_TOKEN_ID_SUCCESS);
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
        registerOrForgetView.goneDialog();
    }

    public void forgetPwd(String pwd) {
        registerOrForgetView.showDialog();
        this.pwd = pwd;
        API1.findPwd(handler, pwd, phone);
    }
}
