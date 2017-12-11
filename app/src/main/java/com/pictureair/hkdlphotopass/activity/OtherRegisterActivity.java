package com.pictureair.hkdlphotopass.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.util.AppManager;
import com.pictureair.hkdlphotopass.util.AppUtil;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.PictureAirLog;
import com.pictureair.hkdlphotopass.util.SignAndLoginUtil;
import com.pictureair.hkdlphotopass.widget.EditTextWithClear;
import com.pictureair.hkdlphotopass.widget.PWToast;
import com.pictureair.hkdlphotopass.widget.wheelview.SelectDateWeidget;

import java.lang.ref.WeakReference;

/**
 * 邮箱注册页面
 */
public class OtherRegisterActivity extends BaseActivity implements
        OnClickListener, SignAndLoginUtil.OnLoginSuccessListener {
    // 声明控件
    private TextView tvAgreement;
    private EditTextWithClear etEmail, etPwd, etPwd2, etName;
    private TextView etYear, etMonth, etDay, etCounry;
    private RadioGroup rg;
    private RadioButton rbMan, rbWoman;
    private Button btn_submit_sign;
    private PWToast myToast;
    // 变量
    private String sex = "";// 性别
    private String country = "";
    private String countryCode = "";
    private String birthday = "";

    // 日期探矿选择器
    private LinearLayout ll_brith;
    private String mYear_Str = "1996";// 初始化
    private String mMonth_Str = "01";
    private String mDay_Str = "01";
    private View view = null;

    private SelectDateWeidget selectDateWeidget;

    private ImageView agreeIv;
    private boolean isAgree = false;
    private int isAgree2 = 0;

    private SignAndLoginUtil signAndLoginUtil;

    /*
     * 监听性别 性别只能是两种 male 或female
     */
    private RadioGroup.OnCheckedChangeListener mChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup rg, int id) {
            if (id == rbMan.getId()) {
                sex = "male";
            } else if (id == rbWoman.getId()) {
                sex = "female";
            }
        }
    };


    private final Handler otherRegisterHandler = new OtherRegisterHandler(this);
    private ImageView agreeIv2;
    private TextView tvAgreement2;


    private static class OtherRegisterHandler extends Handler{
        private final WeakReference<OtherRegisterActivity> mActivity;

        public OtherRegisterHandler(OtherRegisterActivity activity){
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
        switch (msg.what) {
            case SelectDateWeidget.SUBMIT_SELECT_DATE://确认日期
                Bundle bundle = (Bundle) msg.obj;
                mYear_Str = bundle.getString("year");
                mMonth_Str = bundle.getString("month");
                mDay_Str = bundle.getString("day");
                etYear.setText(mYear_Str);
                etMonth.setText(mMonth_Str);
                etDay.setText(mDay_Str);
                birthday = mYear_Str + "-" + mMonth_Str + "-" + mDay_Str;
                PictureAirLog.out("birthday " + birthday);
                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode!=0 && requestCode == SelectCountryActivity.requestCountry) {
            String[] strs = data.getExtras().getStringArray("country");
//            Toast.makeText(getContext(),"国家名称：" + strs[0] + "\n" + "国家区号：" + strs[1] + "\n" + "国家简码：" + strs[4],Toast.LENGTH_SHORT).show();
            if (null != strs) {
                country = strs[0];
                countryCode = strs[4];
                etCounry.setText(country);// 国家名称
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_other_register);
        initview();// 初始化
    }

    private void initview() {
        agreeIv = (ImageView) findViewById(R.id.iv_agreement);
        tvAgreement = (TextView) findViewById(R.id.tv_agreement);
        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = tvAgreement.getText();
        if (text instanceof Spannable) {
            int end = text.length();
            Spannable sp = (Spannable) tvAgreement.getText();
            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.clearSpans();// should clear old spans
            for (URLSpan url : urls) {
                MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                style.setSpan(myURLSpan, sp.getSpanStart(url), sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            tvAgreement.setText(style);
        }

        agreeIv2 = (ImageView)findViewById(R.id.iv_agreement_personal);
        tvAgreement2 = (TextView) findViewById(R.id.tv_explain_personal);
        tvAgreement2.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text2 = tvAgreement2.getText();
        //新增个人资料收集声明
        if (text2 instanceof Spannable) {
            int end = text2.length();
            Spannable sp = (Spannable) tvAgreement2.getText();
            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
            SpannableStringBuilder style = new SpannableStringBuilder(text2);
            style.clearSpans();// should clear old spans
            for (URLSpan url : urls) {
                MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                style.setSpan(myURLSpan, sp.getSpanStart(url), sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            tvAgreement2.setText(style);
        }

        myToast = new PWToast(OtherRegisterActivity.this);
        signAndLoginUtil = new SignAndLoginUtil(this, this);
//		getDateYMD();
        setTopLeftValueAndShow(R.drawable.back_white,true);
        etEmail = (EditTextWithClear) findViewById(R.id.other_sign_email);
        etPwd = (EditTextWithClear) findViewById(R.id.other_sign_password);
        etPwd2 = (EditTextWithClear) findViewById(R.id.other_sign_password2);
        etName = (EditTextWithClear) findViewById(R.id.other_sign_name);
        etCounry = (TextView) findViewById(R.id.other_sign_country);
        etYear = (TextView) findViewById(R.id.other_sign_year);
        etMonth = (TextView) findViewById(R.id.other_sign_month);
        etDay = (TextView) findViewById(R.id.other_sign_day);

        btn_submit_sign = (Button) findViewById(R.id.btn_other_sign_submit);

        rg = (RadioGroup) findViewById(R.id.rg_sex);// 获取RadioGroup控件
        rbMan = (RadioButton) findViewById(R.id.rb_btn_man);// 获取RadioButton控件;
        rbWoman = (RadioButton) findViewById(R.id.rb_btn_woman);// 获取RadioButton控件;
        rg.setOnCheckedChangeListener(mChangeListener);// 单选框的改变事件

//        btn_submit_sign.setTypeface(MyApplication.getInstance().getFontBold());

//        rbMan.setChecked(true);
        // 日期选择器
        ll_brith = (LinearLayout) findViewById(R.id.ll_birth);

        agreeIv.setOnClickListener(this);
        agreeIv2.setOnClickListener(this);
        ll_brith.setOnClickListener(this);
        btn_submit_sign.setOnClickListener(this);
        etCounry.setOnClickListener(this);
        etYear.setOnClickListener(this);
        etMonth.setOnClickListener(this);
        etDay.setOnClickListener(this);
        etName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int cou = 0;
                cou = before + count;
                String editable = etName.getText().toString();
                String str = AppUtil.inputTextFilter(editable); //过滤特殊字符
                if (!editable.equals(str)) {
                    etName.setText(str);
                }
                etName.setSelection(etName.length());
                cou = etName.length();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 点击键盘之外，隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (AppUtil.isShouldHideInput(v, ev)) {
                // if (!password.hasFocus() && !userName.hasFocus()) {
                hideInputMethodManager(v);
                // }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    /**
     * 隐藏软键盘
     */
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
            case R.id.iv_agreement:
                if (isAgree) {
                    isAgree = false;
                    agreeIv.setImageResource(R.drawable.login_check_unselect);
                } else {
                    isAgree = true;
                    agreeIv.setImageResource(R.drawable.login_check_select);
                }
                break;
            case R.id.iv_agreement_personal:
                if (isAgree2 == 1) {
                    isAgree2 = 0;
                    agreeIv2.setImageResource(R.drawable.login_check_unselect);
                } else {
                    isAgree2 = 1;
                    agreeIv2.setImageResource(R.drawable.login_check_select);
                }
                break;

            case R.id.btn_other_sign_submit:
			/*
			 * 1.先提交用户名和密码 2.根据修改用户来提交 个人信息。
			 */
                String email = etEmail.getText().toString().trim();
                String pwd = etPwd.getText().toString();
                String pwd2 = etPwd2.getText().toString();
                String name = etName.getText().toString().trim();
                country = etCounry.getText().toString();

                if (email.isEmpty()) {
                    myToast.setTextAndShow(R.string.email_is_empty,
                            Common.TOAST_SHORT_TIME);
                } else if (!AppUtil.isEmail(email)) {
                    myToast.setTextAndShow(R.string.email_error,
                            Common.TOAST_SHORT_TIME);
                } else {
                    // 比较密码合法性
                    switch (AppUtil.checkPwd(pwd, pwd2)) {
                        case AppUtil.PWD_ALL_SAPCE:// 全部为空格
                            myToast.setTextAndShow(R.string.pwd_no_all_space,
                                    Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_AVAILABLE:// 密码可用
                            if (name.isEmpty()) {
                                myToast.setTextAndShow(R.string.name_is_empty,
                                        Common.TOAST_SHORT_TIME);
                            } else if (isAgree) {
                                signAndLoginUtil.start(email, pwd, true, true, name, birthday, sex, countryCode,null, null, isAgree2);
                            } else {
                                myToast.setTextAndShow(R.string.please_agree, Common.TOAST_SHORT_TIME);
                            }
                            break;

                        case AppUtil.PWD_EMPTY:// 空
                            myToast.setTextAndShow(R.string.modify_password_empty_hint,
                                    Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_INCONSISTENCY:// 不一致
                            myToast.setTextAndShow(R.string.smssdk_pw_is_inconsistency,
                                    Common.TOAST_SHORT_TIME);
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
                }
                break;

            case R.id.other_sign_month:
            case R.id.other_sign_day:
            case R.id.other_sign_year:
            case R.id.ll_birth:

                // 弹出出生年月日
                if (selectDateWeidget == null) {
                    selectDateWeidget = new SelectDateWeidget(this, ll_brith, otherRegisterHandler);
                    selectDateWeidget.showPopupWindow();
                } else {
                    selectDateWeidget.showPopupWindow();
                }
                etEmail.clearFocus();
                etPwd.clearFocus();
                etPwd2.clearFocus();
                etName.clearFocus();
                break;

            case R.id.other_sign_country:

                Intent intent = new Intent();
                intent.setClass(OtherRegisterActivity.this, SelectCountryActivity.class);
                startActivityForResult(intent, SelectCountryActivity.requestCountry);

                break;

            default:
                break;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        otherRegisterHandler.removeCallbacksAndMessages(null);
        if (signAndLoginUtil != null) {
            signAndLoginUtil.destroy();
        }
    }

    @Override
    public void loginSuccess() {
        Intent i = new Intent();
        i.setClass(OtherRegisterActivity.this, MainTabActivity.class);
        startActivity(i);
        AppManager.getInstance().killActivity(LoginActivity.class);
        finish();
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


    private class MyURLSpan extends ClickableSpan {
        private String mUrl;
        MyURLSpan(String url) {
            mUrl = url;
        }

        @Override
        public void onClick(View widget) {
            Intent intent = new Intent();
            if (Integer.valueOf(mUrl) == 6) {
                intent.setAction("android.intent.action.VIEW");
                Uri target = Uri.parse(getCmrUrl());
                intent.setData(target);
                startActivity(intent);
            } else {
                intent.putExtra("key", Integer.valueOf(mUrl));
                intent.setClass(OtherRegisterActivity.this, WebViewActivity.class);
                startActivity(intent);
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ContextCompat.getColor(OtherRegisterActivity.this, R.color.pp_red));
        }
    }

    //个人资料收集声明
    public String getCmrUrl() {
        String language = MyApplication.getInstance().getLanguageType();
        String url;
        if (language.equals(Common.TRADITIONAL_CHINESE)) {
            url = Common.PERSONAL_INFORMATION_COLLECT_HK;
        } else if (language.equals(Common.SIMPLE_CHINESE)) {
            url = Common.PERSONAL_INFORMATION_COLLECT_CN;
        } else {
            url = Common.PERSONAL_INFORMATION_COLLECT_EN;
        }
        return url;
    }
}
