package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpUtil;
import com.pictureair.photopass.util.Installation;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.wheelview.WheelView;
import com.pictureair.photopass.widget.wheelview.SelectDateWeidget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.smssdk.gui.EditTextWithClear;
import cz.msebera.android.httpclient.Header;

/**
 * 其他注册实现规则 先调用注册的接口（ 返回成功后再调用修改个人信息的接口 ） 修改个人信息，API.updateProfile这个接口
 * <p/>
 * 如果修改日期颜色。在com.byl.datepicker.wheelview.WheelView中
 *
 * @author bass
 */
public class OtherRegisterActivity extends BaseActivity implements
        OnClickListener {
    // 声明控件
    private ImageView back;
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
    private String name = "";

    private SharedPreferences sp;// SharedPreferences
    // 日期探矿选择器
    private LinearLayout ll_brith;
    private LayoutInflater inflater = null;
    private WheelView year;
    private WheelView month;
    private WheelView day;
    private int mYear = 1996;// 初始化
    private int mMonth = 0;
    private int mDay = 1;
    private String mYear_Str = "1996";// 初始化
    private String mMonth_Str = "01";
    private String mDay_Str = "01";
    LinearLayout ll;
    TextView tv1, tv2;
    View view = null;
    boolean isMonthSetted = false, isDaySetted = false;
    private static final int GET_IP_SUCCESS = 2;
    private static final int GET_IP_FAILED = 3;

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

                case API.SUCCESS:// sign成功
                    System.out.println("login success-------------注册成功，现在进行保存个人信息");
                    System.out.println("swx:" + sex);
                    // &&&&&&&&&&&&&& 调用个人信息里的进行添加个人资料
                    API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""),
                            name, birthday, sex, country, "", handler);
                    // finish();
                    break;
                case API.UPDATE_PROFILE_SUCCESS:
                    // 获取购物车数量
                    API.getcartcount(OtherRegisterActivity.this,
                            sp.getString(Common.USERINFO_ID, ""), handler);

                    break;
                case API.UPDATE_PROFILE_FAILED:
                    /**
                     * 注册成功，但是保存个人信息失败的话，提示注册成功，保存个人信息失败。应该跳转到登录页面，
                     * 并且将信息保存在sharedpreference中
                     */
                    myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                    Intent intent = new Intent(OtherRegisterActivity.this,
                            OtherLoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case API.FAILURE:
                case API.GET_CART_COUNT_FAILED:
                case API.GET_PPP_FAILED:
                case API.GET_STOREID_FAILED:

                    myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                    break;

                case API.SIGN_FAILED:
                    try {
                        JSONObject infoJsonObject = (JSONObject) msg.obj;
                        if (infoJsonObject.has("type")) {
                            if (infoJsonObject.getString("type").equals(
                                    "shortPassword")) {
                                myToast.setTextAndShow(R.string.pwd_is_short,
                                        Common.TOAST_SHORT_TIME);

                            } else if (infoJsonObject.getString("type").equals(
                                    "existedEmail")) {
                                myToast.setTextAndShow(R.string.email_exist,
                                        Common.TOAST_SHORT_TIME);

                            }
                        } else {
                            myToast.setTextAndShow(R.string.http_failed,
                                    Common.TOAST_SHORT_TIME);
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;

                case API.GET_CART_COUNT_SUCCESS:
                    System.out.println("get cart count success------------");
                    API.getPPSByUserId(sp.getString(Common.USERINFO_TOKENID, null),
                            handler);// 获取pp列表
                    break;

                case GET_IP_SUCCESS:
                    API.getStoreIdbyIP(msg.obj.toString(), handler);
                    break;

                case API.GET_PPS_SUCCESS:// 获取pp列表成功
                    /**
                     * 获取pp成功之后，需要放入sharedPrefence中
                     */
                    JSONObject ppsJsonObject = (JSONObject) msg.obj;
                    // Log.d(TAG, "pps===" + ppsJsonObject);
                    if (ppsJsonObject.has("PPList")) {
                        try {
                            JSONArray pplists = ppsJsonObject
                                    .getJSONArray("PPList");
                            Editor editor = sp.edit();
                            editor.putInt(Common.PP_COUNT, pplists.length());
                            editor.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Log.d(TAG, "pp size == 0");
                    }
                    new Thread() {
                        public void run() {
                            // String netIP = AppUtil.GetNetIp();
                            String netIP = "211.95.27.34";
                            System.out.println("netIP-----------> " + netIP);
                            if (netIP.equals("")) {// 获取失败
                                handler.sendEmptyMessage(GET_IP_FAILED);
                            } else {// 获取成功
                                Message message = handler.obtainMessage();
                                message.what = GET_IP_SUCCESS;
                                message.obj = netIP;
                                handler.sendMessage(message);
                            }

                        }

                        ;
                    }.start();
                    // API.getStoreIdbyIP("140.206.125.195", handler);
                    break;

                case GET_IP_FAILED:
                case API.GET_PPS_FAILED:// 获取pp列表失败
                    // dialog.dismiss();
                    myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                    break;

                case API.GET_STOREID_SUCCESS:
                    System.out.println("get storeid success----------------");
                    JSONObject obj = (JSONObject) msg.obj;
                    try {
                        Editor editor = sp.edit();
                        editor.putString(Common.CURRENCY, obj.getString("currency")
                                .toString());
                        editor.putString(Common.STORE_ID, obj.getString("storeId")
                                .toString());
                        editor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//				Intent intent2 = new Intent();
//				intent2.setAction("com.receiver.AlertManagerRecriver");
//				sendBroadcast(intent2);
                    Intent i = new Intent();
                    i.setClass(OtherRegisterActivity.this, MainTabActivity.class);
                    startActivity(i);
                    finish();
                    break;

                case API.MODIFY_PWD_FAILED:
                    System.out.println("signorfotget------modify pwd failed");
                    // 提示错误
                    break;

                case API.MODIFY_PWD_SUCCESS:
                    System.out.println("signorforget------modify pwd success");
                    // 跳转至登录界面
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
        back = (ImageView) findViewById(R.id.login_back);// 返回按键
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
        rbMan.setChecked(true);
        // 日期选择器
        ll_brith = (LinearLayout) findViewById(R.id.ll_birth);

        ll_brith.setOnClickListener(this);
        back.setOnClickListener(this);
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

        sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);

        switch (v.getId()) {
            case R.id.login_back:
                finish();
                break;

            case R.id.btn_other_sign_submit:
			/*
			 * 1.先提交用户名和密码 2.根据修改用户来提交 个人信息。
			 */
                String email = etEmail.getText().toString().trim();
                String pwd = etPwd.getText().toString().trim();
                String pwd2 = etPwd2.getText().toString().trim();
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
                    switch (AppUtil.checkPwd(etPwd.getText().toString(), etPwd2
                            .getText().toString())) {
                        case AppUtil.PWD_ALL_SAPCE:// 全部为空格
                            myToast.setTextAndShow(R.string.pwd_no_all_space,
                                    Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_AVAILABLE:// 密码可用
                            sign(email, pwd);// 注册 用户名和 密码。成功后将保存个人信息
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

    /**
     * 注册
     */
    private void sign(final String email, final String pwd) {
        // 注册请求
        if (null == sp.getString(Common.USERINFO_TOKENID, null)) {// 需要重新获取一次tokenid
            System.out.println("no tokenid, need to obtain one");
            final StringBuffer sb = new StringBuffer();
            sb.append(Common.BASE_URL).append(Common.GET_TOKENID);// 获取地址

            RequestParams params = new RequestParams();
            params.put(Common.TERMINAL, "android");
            params.put(Common.UUID, Installation.id(this));

            HttpUtil.get(sb.toString(), params, new JsonHttpResponseHandler() {

                // 成功
                public void onSuccess(int statusCode, Header[] headers,
                                      JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        System.out.println("tokenid==" + response);
                        Editor e = sp.edit();
                        if (response.has(Common.USERINFO_TOKENID)) {
                            System.out.println("add tokenid=============");
                            e.putString(Common.USERINFO_TOKENID,
                                    response.getString(Common.USERINFO_TOKENID));
                        }
                        e.commit();
                        API.Sign(OtherRegisterActivity.this, email, pwd,
                                handler);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }

                // 失败
                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      Throwable throwable, JSONObject errorResponse) {
                    // TODO Auto-generated method stub
                    super.onFailure(statusCode, headers, throwable,
                            errorResponse);
                    myToast.setTextAndShow(R.string.http_failed,
                            Common.TOAST_SHORT_TIME);
                }
            });
        } else {
            API.Sign(this, email, pwd, handler);
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
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
}
