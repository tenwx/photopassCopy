package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SignAndLoginUtil;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.wheelview.SelectDateWeidget;

import cn.smssdk.gui.EditTextWithClear;

public class OtherRegisterActivity extends BaseActivity implements
        OnClickListener, LoginCallBack{
    // 声明控件
    private EditTextWithClear etEmail, etPwd, etPwd2, etName;
    private TextView etYear, etMonth, etDay, etCounry;
    private RadioGroup rg;
    private RadioButton rbMan, rbWoman;
    private Button btn_submit_sign;
    private MyToast myToast;
    // 变量
    private String sex = "";// 性别
    private String country = "";
    private String birthday = "";

    // 日期探矿选择器
    private LinearLayout ll_brith;
    private String mYear_Str = "1996";// 初始化
    private String mMonth_Str = "01";
    private String mDay_Str = "01";
    View view = null;

    private SelectDateWeidget selectDateWeidget;

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

    // 消息机制
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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
                    System.out.println("birthday " + birthday);
                    break;


                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_register);


        initview();// 初始化
    }

    private void initview() {
        myToast = new MyToast(OtherRegisterActivity.this);
//		getDateYMD();
        setTopLeftValueAndShow(R.drawable.back_white,true);
        setTopTitleShow(R.string.smssdk_regist);
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
//        rbMan.setChecked(true);
        // 日期选择器
        ll_brith = (LinearLayout) findViewById(R.id.ll_birth);

        ll_brith.setOnClickListener(this);
        btn_submit_sign.setOnClickListener(this);
        etCounry.setOnClickListener(this);
        etYear.setOnClickListener(this);
        etMonth.setOnClickListener(this);
        etDay.setOnClickListener(this);
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
            case R.id.btn_other_sign_submit:
			/*
			 * 1.先提交用户名和密码 2.根据修改用户来提交 个人信息。
			 */
                String email = etEmail.getText().toString().trim();
                String pwd = etPwd.getText().toString();
                String pwd2 = etPwd2.getText().toString();
                String name = etName.getText().toString().trim();
                // String year = etYear.getText().toString();
                // String month = etMonth.getText().toString();
                // String day = etDay.getText().toString();
                country = etCounry.getText().toString();

                if (email.isEmpty()) {
                    myToast.setTextAndShow(R.string.email_is_empty,
                            Common.TOAST_SHORT_TIME);
                } else if (!AppUtil.isEmail(email)) {
                    myToast.setTextAndShow(R.string.email_error,
                            Common.TOAST_SHORT_TIME);
                } else if (name.isEmpty()) {
                    myToast.setTextAndShow(R.string.name_is_empty,
                            Common.TOAST_SHORT_TIME);
                } else {
                    // 比较密码合法性
                    switch (AppUtil.checkPwd(pwd, pwd2)) {
                        case AppUtil.PWD_ALL_SAPCE:// 全部为空格
                            myToast.setTextAndShow(R.string.pwd_no_all_space,
                                    Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_AVAILABLE:// 密码可用
                            new SignAndLoginUtil(OtherRegisterActivity.this, email, pwd, true, true,
                                    name, birthday, sex, country, OtherRegisterActivity.this);
                            break;

                        case AppUtil.PWD_EMPTY:// 空
                            myToast.setTextAndShow(R.string.pwd_is_empty,
                                    Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_INCONSISTENCY:// 不一致
                            myToast.setTextAndShow(R.string.pw_is_inconsistency,
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
                    selectDateWeidget = new SelectDateWeidget(this, ll_brith, handler);
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
                intent.setClass(this, NationalListSelectionActivity.class);
                intent.putExtra("isCountrycode", "isCountryCode");
                startActivityForResult(intent, 0);
                break;

            default:
                break;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 222://修改国家
                PictureAirLog.out("country---->" + data.getStringExtra("country"));
                etCounry.setText(data.getStringExtra("country"));// 国家名称
                break;

            default:
                break;
        }
    }

    @Override
    public void loginSuccess() {
        Intent i = new Intent();
        i.setClass(OtherRegisterActivity.this, MainTabActivity.class);
        startActivity(i);
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

}
