package com.pictureair.photopass.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.RegisterTool;
import com.pictureair.photopass.widget.CustomButtonFont;
import com.pictureair.photopass.widget.CustomFontManager;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.CustomTextView;
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.PictureWorksDialog;
import com.pictureair.photopass.widget.RegisterOrForgetCallback;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 手机注册
 * Created by bass on 16/4/27.
 */
public class RegisterOrForgetActivity extends BaseActivity implements RegisterOrForgetCallback, View.OnClickListener, TextWatcher {
    private static final String TAG = "RegisterOrForgetActivity";
    private RegisterTool registerTool;
    private String tokenId;
    private String languageType;
    private PWToast myToast;
    private Context context;
    private CustomProgressDialog customProgressDialog;
    private LinearLayout rlCountry, ll_pwd_centen, ll_mobile_centen, ll_put_identify_centen;
    private CustomTextView tvCountry, tvCountryNum, tv_otherRegistered, tv_explain,dialogTvPhone;//country textview
    private EditTextWithClear et_write_phone, pwd, pwd_again, et_put_identify;
    private CustomButtonFont btn_next, sure;
    private String currentCode = "86"; //国家区号
    private String phoneStr = "", pwdStr = "", pwdAgainStr = "", identifyStr = "";
    private Typeface typeface;
    private boolean countDownFinish = true;
    private String whatActivity;
    private final String signActivity = "sign", forgetActivity = "forget";
    private boolean isNext = false;
    private ImageView agreeIv;
    private boolean isAgree = false;
    private PictureWorksDialog pictureWorksDialog;

    private final Handler myHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<RegisterOrForgetActivity> mActivity;

        public MyHandler(RegisterOrForgetActivity activity) {
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
     *
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case DialogInterface.BUTTON_POSITIVE://点击确定
                registerTool.sendSMSValidateCode(currentCode + phoneStr);
                dismiss();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        context = this;
        tokenId = MyApplication.getTokenId();
        languageType = MyApplication.getInstance().getLanguageType();
        registerTool = new RegisterTool(tokenId, context, this, languageType);
        initView();
        initData();
    }


    private void initView() {
        whatActivity = getIntent().getExtras().getString("activity");
        registerTool.setWhatActivity(whatActivity);

        setTopLeftValueAndShow(R.drawable.back_white, true);
        rlCountry = (LinearLayout) findViewById(R.id.rl_country);
        tvCountry = (CustomTextView) findViewById(R.id.tv_country);
        tvCountryNum = (CustomTextView) findViewById(R.id.tv_country_num);
        et_write_phone = (EditTextWithClear) findViewById(R.id.et_write_phone);
        pwd = (EditTextWithClear) findViewById(R.id.pwd);
        pwd_again = (EditTextWithClear) findViewById(R.id.pwd_again);
        et_put_identify = (EditTextWithClear) findViewById(R.id.et_put_identify);
        btn_next = (CustomButtonFont) findViewById(R.id.btn_next);
        sure = (CustomButtonFont) findViewById(R.id.sure);
        tv_otherRegistered = (CustomTextView) findViewById(R.id.tv_otherRegistered);
        tv_explain = (CustomTextView) findViewById(R.id.tv_explain);
        ll_pwd_centen = (LinearLayout) findViewById(R.id.ll_pwd_centen);
        ll_mobile_centen = (LinearLayout) findViewById(R.id.ll_mobile_centen);
        ll_put_identify_centen = (LinearLayout) findViewById(R.id.ll_put_identify_centen);

        tv_otherRegistered.setOnClickListener(this);
        rlCountry.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        sure.setOnClickListener(this);
        et_write_phone.addTextChangedListener(this);
        pwd.addTextChangedListener(this);
        pwd_again.addTextChangedListener(this);
        et_put_identify.addTextChangedListener(this);
        btn_next.setEnabled(false);
        sure.setEnabled(false);

        tv_explain.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = tv_explain.getText();
        if (text instanceof Spannable) {
            int end = text.length();
            Spannable sp = (Spannable) tv_explain.getText();
            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.clearSpans();// should clear old spans
            for (URLSpan url : urls) {
                MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                style.setSpan(myURLSpan, sp.getSpanStart(url), sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            tv_explain.setText(style);
        }

        agreeIv = (ImageView) findViewById(R.id.iv_agreement);
        agreeIv.setOnClickListener(this);

        if (signActivity.equals(whatActivity)) {
            setTopTitleShow(R.string.smssdk_regist);
        } else if (forgetActivity.equals(whatActivity)) {
            tv_explain.setVisibility(View.GONE);
            ll_pwd_centen.setVisibility(View.GONE);
            tv_otherRegistered.setVisibility(View.GONE);
            sure.setText(R.string.smssdk_next);
            setTopTitleShow(R.string.reset_pwd);
            agreeIv.setVisibility(View.GONE);
            isAgree = true;
        }
        if (CustomFontManager.IS_CUSOTM_FONT) {
            typeface = Typeface.createFromAsset(context.getAssets(), CustomFontManager.CUSOTM_FONT_BOLD_NAME);
            btn_next.setTypeface(typeface);
            sure.setTypeface(typeface);
        }
    }

    private void initData() {
        myToast = new PWToast(context);// 获取toast
        tvCountry.setText(R.string.china);
    }

    @Override
    protected void onDestroy() {
        if (null != registerTool)
            registerTool.onDestroy();
        registerTool = null;
        myHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void goneDialog() {
        if (null != customProgressDialog && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
    }

    @Override
    public void showDialog() {
        if (null == customProgressDialog) {
            customProgressDialog = CustomProgressDialog.create(context, getString(R.string.connecting), false, null);
        }
        if (!customProgressDialog.isShowing()) {
            customProgressDialog.show();
        }
    }

    @Override
    public void onFai(int StringId) {
        myToast.setTextAndShow(ReflectionUtil.getStringId(context, StringId), Common.TOAST_SHORT_TIME);
    }

    @Override
    public void onFai(String StringId) {
        myToast.setTextAndShow(StringId, Common.TOAST_SHORT_TIME);
    }

    @Override
    public void onSuccess() {
        startActivity(new Intent(this, MainTabActivity.class));
        finish();
    }

    @Override
    public void countDown(int time) {
        if (time == 0) {
            countDownFinish = true;
            btn_next.setText(R.string.smssdk_send_verification_code);// 再次发送验证码
            if (phoneStr.length() > 0) {
                btn_next.setEnabled(true);
            } else {
                btn_next.setEnabled(false);
            }
        } else {
//            PictureAirLog.out("------>倒计时");
            countDownFinish = false;
            String unReceive = context.getString(R.string.smssdk_receive_msg, time);// 倒计时
            btn_next.setText(Html.fromHtml(unReceive));
            btn_next.setEnabled(false);
        }
    }

    @Override
    public void nextPageForget() {
        isNext = true;
        ll_put_identify_centen.setVisibility(View.GONE);
        rlCountry.setVisibility(View.GONE);
        ll_mobile_centen.setVisibility(View.GONE);
        ll_pwd_centen.setVisibility(View.VISIBLE);
        sure.setText(R.string.reset_pwd_ok);
        sure.setEnabled(false);
        pwd.setHint(R.string.smssdk_pwd_forget);
        pwd_again.setHint(R.string.smssdk_pwd2);
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
            case R.id.iv_agreement:
                if (isAgree) {
                    isAgree = false;
                    agreeIv.setImageResource(R.drawable.gender_normal);
                } else {
                    isAgree = true;
                    agreeIv.setImageResource(R.drawable.gender_sele);
                }
                break;

            case R.id.rl_country:
                Intent intent = new Intent();
                intent.setClass(context, SelectCountryActivity.class);
                startActivityForResult(intent, SelectCountryActivity.requestCountry);
                break;
            case R.id.btn_next:
                if (!checkPhoneNumber()) {
                    myToast.setTextAndShow(R.string.smssdk_write_right_mobile_phone, Common.TOAST_SHORT_TIME);
                    return;
                }
                showPwDialog();
//                registerTool.sendSMSValidateCode(currentCode + phoneStr);
                break;
            case R.id.sure:
                if (!isNext) {//SIGN
                    if (!isAgree) {
                        myToast.setTextAndShow(R.string.please_agree, Common.TOAST_SHORT_TIME);
                        return;
                    }
                    submitEvent();
                } else {
                    if (!checkPwd())
                        return;
                    registerTool.forgetPwd(pwdStr);
                }
                break;
            case R.id.tv_otherRegistered:
                startActivity(new Intent(context, OtherRegisterActivity.class));
                break;
            default:
                break;
        }
    }

    /**
     * 点击发送验证码过后 检查电话号码
     */
    private boolean checkPhoneNumber() {
        boolean tem = true;
        if (currentCode.equals("86")) {
//            Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
            Pattern p = Pattern.compile("^1(3|5|7|8|4)\\d{9}");
            Matcher matcher = p.matcher(phoneStr);
            tem = matcher.matches();
        }
        return tem;
    }

    /**
     * 提交
     */
    private void submitEvent() {
        if (whatActivity.equals(signActivity)) {
            if (!checkPhoneNumber()) {
                myToast.setTextAndShow(R.string.smssdk_write_right_mobile_phone, Common.TOAST_SHORT_TIME);
                return;
            }
            if (!checkPwd()) {
                return;
            } else if (!isAgree) {
                myToast.setTextAndShow(R.string.please_agree, Common.TOAST_SHORT_TIME);
                return;
            }
        } else {
            if (!checkPhoneNumber()) {
                myToast.setTextAndShow(R.string.smssdk_write_right_mobile_phone, Common.TOAST_SHORT_TIME);
                return;
            }
        }
        registerTool.submit(identifyStr, currentCode + phoneStr, pwdStr);
    }

    /**
     * 检查密码
     */
    public boolean checkPwd() {
        //判断密码，必须按照这个顺序
        if (pwdStr.isEmpty()) {
            // 密码为空
            myToast.setTextAndShow(R.string.modify_password_empty_hint, 100);
            return false;
        } else if (!pwdStr.isEmpty() && pwdStr.trim().isEmpty()) {
            // 密码全部为空格
            myToast.setTextAndShow(R.string.pwd_no_all_space, 100);
            return false;
        } else if (pwdStr.trim().length() < pwdStr.length()) {
            // 密码首尾有空格
            myToast.setTextAndShow(R.string.pwd_no_all_space, 100);
            return false;
        } else if (pwdStr.length() < 6) {
            // 密码小于6位
            myToast.setTextAndShow(R.string.notify_password_hint, 100);
            return false;
        } else if (!pwdStr.equals(pwdAgainStr)) {
            // 密码两次不一致
            myToast.setTextAndShow(R.string.smssdk_pw_is_inconsistency, 100);
            return false;
        }
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        phoneStr = et_write_phone.getText().toString().trim();
        pwdStr = pwd.getText().toString();
        pwdAgainStr = pwd_again.getText().toString();
        identifyStr = et_put_identify.getText().toString().trim();
        isSubmitAvailable();
        isSendCodeAvailable();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    /**
     * 设置提交注册按钮是否可用
     */
    public void isSubmitAvailable() {
        if (whatActivity.equals(signActivity)) {
            if (phoneStr.length() > 0 && pwdStr.length() > 0 && pwdAgainStr.length() > 0 && identifyStr.length() > 0) {
                sure.setEnabled(true);
            } else {
                sure.setEnabled(false);
            }
        } else if (whatActivity.equals(forgetActivity)) {
            if (!isNext) {
                if (phoneStr.length() > 0 && identifyStr.length() > 0) {
                    sure.setEnabled(true);
                } else {
                    sure.setEnabled(false);
                }
            } else {
                if (pwdStr.length() > 0 && pwdAgainStr.length() > 0) {
                    sure.setEnabled(true);
                } else {
                    sure.setEnabled(false);
                }
            }
        }
    }

    /**
     * 设置验证码按钮是否可用
     */
    public void isSendCodeAvailable() {
        if (countDownFinish)
            if (phoneStr.length() > 0) {
                btn_next.setEnabled(true);
            } else {
                btn_next.setEnabled(false);
            }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0 && requestCode == SelectCountryActivity.requestCountry) {
            String[] strs = data.getExtras().getStringArray("country");
//            Toast.makeText(getContext(),"国家名称：" + strs[0] + "\n" + "国家区号：" + strs[1] + "\n" + "国家简码：" + strs[4],Toast.LENGTH_SHORT).show();
            if (null != strs) {
                goneDialog();
                currentCode = strs[1];
                tvCountryNum.setText("+" + currentCode);
                tvCountry.setText(strs[0]);
            }
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
            intent.putExtra("key", Integer.valueOf(mUrl));
            intent.setClass(context, WebViewActivity.class);
            startActivity(intent);
        }
    }

    private void showPwDialog() {
        String dialogMsg = "+" + currentCode + " " + phoneStr;
        dialogMsg = context.getString(R.string.smssdk_make_sure_mobile_detail,dialogMsg);
        PictureAirLog.out("diamsg--->" + dialogMsg);

        /**
         * 每次弹框有可能手机号不同，dialog目前不支持多次改动数据，所以只能每次都重新new dialog
         */
        pictureWorksDialog = new PictureWorksDialog(context, null, dialogMsg, getResources().getString(R.string.cancel1), getResources().getString(R.string.ok), false, myHandler);
        pictureWorksDialog.show();
    }

    private void dismiss() {
        if (pictureWorksDialog != null) {
            pictureWorksDialog.dismiss();
        }
    }

}
