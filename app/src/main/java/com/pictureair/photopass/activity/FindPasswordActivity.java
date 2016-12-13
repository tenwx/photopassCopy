package com.pictureair.photopass.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.PWToast;

import com.pictureair.photopass.widget.CustomButtonFont;

/**
 * 忘记密码：2个页面
 * 输入邮箱页面
 * 提示页面
 *
 * @author Bass
 */
public class FindPasswordActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "FindPasswordActivity";
    private Context context;
    private PWToast myToast;
    //输入邮箱页面
    private LinearLayout llFindPwdContent;
    private EditTextWithClear etEmail;
    private CustomButtonFont btnSendEmail;
    //提示页面
    private LinearLayout llContenHint;
    private CustomButtonFont btnBackLoginPage;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            dismissPWProgressDialog();
            switch (msg.what) {
                case API1.FIND_PWD_SUCCESS:
                    llFindPwdContent.setVisibility(View.GONE);
                    llContenHint.setVisibility(View.VISIBLE);
                    break;

                case API1.FIND_PWD_FAILED:
                    int id;
                    switch (msg.arg1) {
                        case 6031://用户名不存在
                            id = ReflectionUtil.getStringId(FindPasswordActivity.this, msg.arg1);
                            break;

                        default:
                            id = ReflectionUtil.getStringId(FindPasswordActivity.this, msg.arg1);
                            break;
                    }
                    myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
                    break;

                default:
                    break;

            }
            return false;
        }
    });

    /**
     * 点击键盘之外，隐藏键盘
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pwd);
        context = this;
        initview();// 初始化

    }

    private void initview() {
        myToast = new PWToast(context);
        setTopLeftValueAndShow(R.drawable.back_blue, true);
        setTopTitleShow(R.string.reset_pwd);

        llFindPwdContent = (LinearLayout) findViewById(R.id.ll_find_pwd_content);
        etEmail = (EditTextWithClear) findViewById(R.id.et_email);
        btnSendEmail = (CustomButtonFont) findViewById(R.id.btn_send_email);
        //hint page
        llContenHint = (LinearLayout) findViewById(R.id.ll_conten_hint);
        btnBackLoginPage = (CustomButtonFont) findViewById(R.id.btn_back_login);

        btnSendEmail.setTypeface(MyApplication.getInstance().getFontBold());

        btnSendEmail.setOnClickListener(this);
        btnBackLoginPage.setOnClickListener(this);

        etEmail.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
				/* 判断是否是“GO”键 */
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    hideInputMethodManager(v);
                    btnSendEmail.performClick();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back_login:
                backLogin();
                break;

            case R.id.btn_send_email:
                hideInputMethodManager(v);

                String etEmailStr = etEmail.getText().toString().trim();
                if (etEmailStr.isEmpty()) {
                    myToast.setTextAndShow(R.string.email_is_empty,
                            Common.TOAST_SHORT_TIME);
                    return;
                }
                if (!AppUtil.isEmail(etEmailStr)) {
                    myToast.setTextAndShow(R.string.email_error,
                            Common.TOAST_SHORT_TIME);
                    return;
                }

                showPWProgressDialog();
                API1.findPwdEmail(mHandler, etEmailStr, MyApplication.getInstance().getLanguageType(),MyApplication.getTokenId());
                break;

            default:
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (AppUtil.isShouldHideInput(v, ev)) {
                hideInputMethodManager(v);
            }
            return super.dispatchTouchEvent(ev);
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
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
    protected void onDestroy() {
        super.onDestroy();
        if (null != mHandler)
            mHandler.removeCallbacksAndMessages(null);
    }

    private void backLogin() {
        finish();
    }

}
