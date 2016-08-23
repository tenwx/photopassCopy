package com.pictureair.photopass.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
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
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.SignAndLoginUtil;
import com.pictureair.photopass.widget.CheckUpdateManager;
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
    private final int START_AGREEMENT_WEBVIEW = 22;
    private static final int START_CHECK_UPDATE = 33;
    // 申明其他类
    private PWToast myToast;
    // 区号,国家
    private String countryCode = "86";
    private String country = "";
    private CheckUpdateManager checkUpdateManager;// 自动检查更新
    private String forGetphoto;
    private String forGetPwd;

    private List<String> permissionList;
    private static final int REQUEST_ASK_PERMISSION = 1;
    private boolean mIsAskPermission = false;

    private SignAndLoginUtil signAndLoginUtil;

    private final Handler loginHandler = new LoginHandler(this);

    private static class LoginHandler extends Handler {
        private final WeakReference<LoginActivity> mActivity;

        public LoginHandler(LoginActivity activity) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode!=0 && requestCode == SelectCountryActivity.requestCountry) {
            String[] strs = data.getExtras().getStringArray("country");
//            Toast.makeText(getContext(),"国家名称：" + strs[0] + "\n" + "国家区号：" + strs[1] + "\n" + "国家简码：" + strs[4],Toast.LENGTH_SHORT).show();
            if (null != strs) {
                countryCode = strs[1];
                country = strs[0];
                tv_country.setText(country);
                tv_country_num.setText("+" + countryCode);
            }
        }
    }

    /**
     * 处理Message
     *
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case START_OTHER_REGISTER_ACTIVITY:
                // 其他注册的按钮//
                PictureAirLog.v(TAG, "other way on click----------");
                startActivity(new Intent(LoginActivity.this,
                        OtherRegisterActivity.class));
                break;

            case API1.FIND_PWD_FAILED:
                int id = 0;
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
                signAndLoginUtil.start(forGetphoto, forGetPwd, false, false, null, null, null, null);// 登录
                break;

            case START_AGREEMENT_WEBVIEW:
                Intent intent = new Intent();
                intent.putExtra("key", msg.arg1);
                intent.setClass(LoginActivity.this, WebViewActivity.class);
                startActivity(intent);
                break;

            case START_CHECK_UPDATE:
                checkUpdateManager.startCheck();
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
        myToast = new PWToast(LoginActivity.this);// 获取toast
        signAndLoginUtil = new SignAndLoginUtil(this, this);
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
                SPUtils.getString(this, Common.SHARED_PREFERENCE_APP, Common.LANGUAGE_TYPE, Common.ENGLISH));

        new Thread(new Runnable() {
            @Override
            public void run() {
                checkUpdateManager.init();
                loginHandler.sendEmptyMessage(START_CHECK_UPDATE);
            }
        }).start();

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
                startActivity(new Intent(LoginActivity.this,
                        RegisterOrForgetActivity.class).putExtra("activity","forget"));

                break;
            case R.id.rl_country:

                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, SelectCountryActivity.class);
                startActivityForResult(intent, SelectCountryActivity.requestCountry);
                break;

            case R.id.login:
                hideInputMethodManager(v);
                if (userName.getText().toString().trim().isEmpty()) {
                    myToast.setTextAndShow(R.string.input_mobile,
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

                    case AppUtil.PWD_SHORT:// 小于6位
                    case AppUtil.PWD_AVAILABLE:// 密码可用
                        signAndLoginUtil.start(countryCode + userName.getText().toString().trim(),
                                password.getText().toString(), false, false, null, null, null, null);// 登录
                        break;

                    case AppUtil.PWD_EMPTY:// 空
                        myToast.setTextAndShow(R.string.modify_password_empty_hint,
                                Common.TOAST_SHORT_TIME);
                        break;

                    case AppUtil.PWD_INCONSISTENCY:// 不一致
                        // myToast.setTextAndShow(R.string.pw_is_inconsistency,
                        // Common.TOAST_SHORT_TIME);
                        break;

                    case AppUtil.PWD_HEAD_OR_FOOT_IS_SPACE:// 密码首尾不能为空格
                        myToast.setTextAndShow(R.string.pwd_head_or_foot_space,
                                Common.TOAST_SHORT_TIME);
                        break;
                }
                break;

            case R.id.sign:
                PictureAirLog.v(TAG, "tap sign");
                startActivity(new Intent(LoginActivity.this,
                        RegisterOrForgetActivity.class).putExtra("activity","sign"));

                break;

            case R.id.otherLogin:
                PictureAirLog.v(TAG, "tap other login 其他方式登录");
                Intent intent2 = new Intent(LoginActivity.this,
                        OtherLoginActivity.class);
                startActivity(intent2);
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - i > 1000) {
            myToast.setTextAndShow(R.string.exit, Common.TOAST_SHORT_TIME);
            i = System.currentTimeMillis();
        } else {
            myToast.cancelShow();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (checkUpdateManager != null) {
            checkUpdateManager.onDestroy();
        }
        loginHandler.removeCallbacksAndMessages(null);
        if (signAndLoginUtil != null) {
            signAndLoginUtil.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void loginSuccess() {
        Intent intent = new Intent();
        intent.setClass(this, MainTabActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean addPermission(List<String> permissionList, String permission) {
        if (!AppUtil.checkPermission(getApplicationContext(), permission)) {
            permissionList.add(permission);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsAskPermission) {
            mIsAskPermission = false;
            return;
        }
        requesPermission();
    }

    private void requesPermission() {
        if (permissionList != null) {
            permissionList.clear();
        } else {
            permissionList = new ArrayList<>();
        }

        addPermission(permissionList, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        addPermission(permissionList, Manifest.permission.READ_EXTERNAL_STORAGE);
        addPermission(permissionList, Manifest.permission.CAMERA);
        addPermission(permissionList, Manifest.permission.ACCESS_FINE_LOCATION);
        addPermission(permissionList, Manifest.permission.ACCESS_COARSE_LOCATION);
        addPermission(permissionList, Manifest.permission.READ_PHONE_STATE);

        if (permissionList.size() > 0) {
            mIsAskPermission = true;
            ActivityCompat.requestPermissions(LoginActivity.this, permissionList.toArray(new String[permissionList.size()]), REQUEST_ASK_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
