package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SignAndLoginUtil;
import com.pictureair.photopass.widget.CheckUpdateManager;
import com.pictureair.photopass.widget.MyToast;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.CountryPage;
import cn.smssdk.gui.CustomProgressDialog;
import cn.smssdk.gui.EditTextWithClear;
import cn.smssdk.gui.RegisterPage;

/**
 * 登录页面 点击登录按钮之后，需要触发几个接口 1.登录接口 2.登录成功之后，需要获取一些信息，会调用获取购物车数量，获取storeId，获取PP列表
 * 3.全部获取之后，需要确认之前有扫描过pp或者ppp，如果有，则自动绑定
 */

public class LoginActivity extends BaseActivity implements OnClickListener, SignAndLoginUtil.OnLoginSuccessListener {
    private static final String TAG = "LoginActivity";
    // 申明控件
    private RelativeLayout parentRelativeLayout;
    private TextView tv_country, tv_country_num;// 国家，区号
    private TextView otherLogin;// 其他方式登录
    private Button login, sign;
    private TextView forgot;
    private EditTextWithClear userName, password;
    private LinearLayout rl_country;// 国家
    // 返回按键 反馈
    long i = 0;
    // 申明变量
    private static final int START_OTHER_REGISTER_ACTIVITY = 11;// 启动 其他注册的侧面
    // 申明其他类
    private SharedPreferences appPreferences;
    private MyToast myToast;
    // 区号,国家
    private String countryCode = "86";
    private String country = "";
    private RegisterPage registerPage;
    private CheckUpdateManager checkUpdateManager;// 自动检查更新
    private CustomProgressDialog customProgressDialog;
    private String forGetphoto;
    private String forGetPwd;

    private final Handler loginHandler = new LoginHandler(this);

    private static class LoginHandler extends Handler{
        private final WeakReference<LoginActivity> mActivity;

        public LoginHandler(LoginActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    /**
     * 处理Message
     * @param msg
     */
    private void dealHandler(Message msg) {
        if (null != customProgressDialog && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
        switch (msg.what) {
            case START_OTHER_REGISTER_ACTIVITY:
                // 其他注册的按钮//
                PictureAirLog.v(TAG, "other way on click----------");
                startActivity(new Intent(LoginActivity.this,
                        OtherRegisterActivity.class));
                break;

            case 1://国家
                String[] countrys = (String[])msg.obj;
                countryCode = countrys[1];
                country = countrys[0];
                tv_country.setText(country);
                tv_country_num.setText("+" + countryCode);
                break;
            case API1.FIND_PWD_FAILED:
                int id = 0 ;
                switch (msg.arg1) {
                    case 6031://用户名不存在
                        id = ReflectionUtil.getStringId(LoginActivity.this, msg.arg1);
                        break;

                    default:
                        id = ReflectionUtil.getStringId(LoginActivity.this, msg.arg1);
                        break;
                }

                myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                break;
            case API1.FIND_PWD_SUCCESS:
                new SignAndLoginUtil(LoginActivity.this, forGetphoto,
                        forGetPwd, false, false, null, null, null, null, LoginActivity.this);// 登录
                break;

            default:
                break;
        }
    }

    /**
     * 点击键盘之外，隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (AppUtil.isShouldHideInput(v, ev)) {
                hideInputMethodManager(v);
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initview();// 初始化

    }

    private void initview() {
        appPreferences = getSharedPreferences(Common.APP, MODE_PRIVATE);// userInfo

        myToast = new MyToast(LoginActivity.this);// 获取toast
        parentRelativeLayout = (RelativeLayout) findViewById(R.id.login_parent);
        login = (Button) findViewById(R.id.login);// 登录按钮
        sign = (Button) findViewById(R.id.sign);// 注册按钮
        userName = (EditTextWithClear) findViewById(R.id.login_username);// 文本框
        password = (EditTextWithClear) findViewById(R.id.login_password);// 密码框
        forgot = (TextView) findViewById(R.id.forgot);// 忘记密码？
        otherLogin = (TextView) findViewById(R.id.otherLogin);// 其他方式登录
        rl_country = (LinearLayout) findViewById(R.id.rl_country);// 国家
        tv_country = (TextView) findViewById(R.id.tv_country);
        tv_country_num = (TextView) findViewById(R.id.tv_country_num);

        login.setTypeface(MyApplication.getInstance().getFontBold());
        sign.setTypeface(MyApplication.getInstance().getFontBold());

        rl_country.setOnClickListener(this);
        login.setOnClickListener(this);
        sign.setOnClickListener(this);
        forgot.setOnClickListener(this);
        otherLogin.setOnClickListener(this);

        // 自动检查更新
        checkUpdateManager = new CheckUpdateManager(this,
                appPreferences.getString(Common.LANGUAGE_TYPE, ""),
                parentRelativeLayout);
        checkUpdateManager.startCheck();

        userName.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    /* 隐藏软键盘 */
                    userName.clearFocus();
                    password.requestFocus();
                    return true;
                }
                return false;
            }
        });
        password.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                // TODO Auto-generated method stub
				/* 判断是否是“GO”键 */
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    hideInputMethodManager(v);//
                    login.performClick(); //
                    return true;
                }
                return false;
            }
        });
        initSSMSSDK();

    }

    private void hideInputMethodManager(View v) {
		/* 隐藏软键盘 */
        InputMethodManager imm = (InputMethodManager) v.getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgot://忘记密码
                sendSMS(1);
                break;
            case R.id.rl_country:

                CountryPage countryPage = new CountryPage();
                countryPage.setMHandler(loginHandler);
                countryPage.show(this,null);
                break;

            case R.id.login:
                hideInputMethodManager(v);
                if (userName.getText().toString().trim().isEmpty()) {
                    myToast.setTextAndShow(R.string.username_null,
                            Common.TOAST_SHORT_TIME);
                    break;
                }
                String pwd = password.getText().toString();
                // 比较密码合法性
                switch (AppUtil.checkPwd(pwd, pwd)) {
                    case AppUtil.PWD_ALL_SAPCE:// 全部为空格
                        myToast.setTextAndShow(R.string.pwd_no_all_space,
                                Common.TOAST_SHORT_TIME);
                        break;

                    case AppUtil.PWD_AVAILABLE:// 密码可用
                        new SignAndLoginUtil(LoginActivity.this, countryCode + userName.getText().toString().trim(),
                                password.getText().toString(), false, false, null, null, null, null, this);// 登录
                        break;

                    case AppUtil.PWD_EMPTY:// 空
                        myToast.setTextAndShow(R.string.pwd_is_empty,
                                Common.TOAST_SHORT_TIME);
                        break;

                    case AppUtil.PWD_INCONSISTENCY:// 不一致
                        // myToast.setTextAndShow(R.string.pw_is_inconsistency,
                        // Common.TOAST_SHORT_TIME);
                        break;

                    case AppUtil.PWD_SHORT:// 小于6位
                        myToast.setTextAndShow(R.string.notify_password_hint,
                                Common.TOAST_SHORT_TIME);

                        break;

                    case AppUtil.PWD_HEAD_OR_FOOT_IS_SPACE:// 密码首尾不能为空格
                        myToast.setTextAndShow(R.string.pwd_head_or_foot_space,
                                Common.TOAST_SHORT_TIME);
                        break;
                }
                break;

            case R.id.sign:
                PictureAirLog.v(TAG, "tap sign");
                sendSMS(0);
                break;

            case R.id.otherLogin:
                PictureAirLog.v(TAG, "tap other login 其他方式登录");
                Intent intent = new Intent(LoginActivity.this,
                        OtherLoginActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }


    /**
     * 初始化发短信 *
     */
    private void initSSMSSDK() {
        SMSSDK.initSDK(this, Common.SMS_APPKEY, Common.SMS_APPSECRET);
    }

    /**
     * 第三方短信验证方法
     */
    private void sendSMS(final int type) {
        registerPage = new RegisterPage(type, loginHandler);
        registerPage.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                PictureAirLog.v(TAG, "type ---- >" + type + ",result--->"+ result + "data-->" + data);
                // 解析注册结果
                if (result == SMSSDK.RESULT_COMPLETE) {
                    HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                    String pwd = phoneMap.get("pwd").toString();
                    String phone = phoneMap.get("phone").toString();
                    // 把手机号发送到服务器判断账号是否存在，存在则跳转到重置密码页面
                    if (type == 0) {
						/*
						 * 服务器返回手机号不存在，注册 将验证都再smssdk中区完成 然后回调上来参数 参数有：phone,pwd
						 * 拿到值。就直接注册。 注册成功：跳转到主页ok
						 */
                        PictureAirLog.v(TAG, "phone:" + phone);
                        PictureAirLog.v(TAG, "pwd:" + pwd);
                        new SignAndLoginUtil(LoginActivity.this, phone, pwd, true, false,
                                null, null, null, null, LoginActivity.this);

                    } else if (type == 1) {
                        // 忘记密码
//                        请求API
                        customProgressDialog = CustomProgressDialog.show(LoginActivity.this, getString(R.string.is_loading), false, null);
                        forGetphoto = phone;
                        forGetPwd = pwd;
                        API1.findPwd(loginHandler, pwd, phone);
//                        myToast.setTextAndShow("phone:"+phone+"\n"+"PWD:"+pwd,Common.TOAST_SHORT_TIME);
                    }
                }
            }
        });
        registerPage.show(this);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - i > 2000) {
            myToast.setTextAndShow(R.string.exit, Common.TOAST_SHORT_TIME);
            i = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registerPage != null) {
            PictureAirLog.v(TAG, "logout onDestroy, need finish registerPage");
            registerPage.finish();
        }
        loginHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void loginSuccess() {
		Intent intent = new Intent();
		intent.setClass(this, MainTabActivity.class);
		startActivity(intent);
		finish();
    }

}
