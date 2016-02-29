package com.pictureair.photopass.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.wheelview.SelectDateWeidget;

import java.lang.ref.WeakReference;

import cn.smssdk.SMSSDK;
import cn.smssdk.gui.CountryPage;
import cn.smssdk.gui.CustomProgressDialog;

/**
 * 个人信息页面
 */
public class ProfileActivity extends BaseActivity implements OnClickListener {
    private TextView tvNickName, tvGender, tvBirthday, countryTv, accountTv;
    private RelativeLayout nn, g, bd, countryRL, item_password;
    private SharedPreferences sp;
    private MyToast newToast;
    private String nickNameString, genderString, birthdayString, countryString;
    private RelativeLayout isSelectMale, isSelectFemale;
    private ImageView iVisSelectMale, iVisSelectFemale;
    private AlertDialog mySexDialog;

    private SelectDateWeidget selectDateWeidget;

    private CustomProgressDialog dialog;
    private String countryCode;//国家简码
//    private CountryPage countryPage;

    private final Handler profileHandler = new ProfileHandler(this);


    private static class ProfileHandler extends Handler{
        private final WeakReference<ProfileActivity> mActivity;

        public ProfileHandler(ProfileActivity activity){
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
            case 1://国家
                String[] countrys = (String[])msg.obj;
                /**
                 * countrys[1] 手机区号
                 * countrys[0] 国家名称
                 * countrys[4] 国家简码
                 */
                countryString = countrys[0];
                countryCode = countrys[4];

                if (!isNetWorkConnect(this)) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                dialog = CustomProgressDialog.show(this,
                        getString(R.string.connecting), false, null);
//                if (null != countryPage){
//                    countryPage.finish();
//                }
                API1.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""),
                        sp.getString(Common.USERINFO_BIRTHDAY, ""), sp.getString(Common.USERINFO_GENDER, "").toLowerCase(),
                        countryCode, "", API1.UPDATE_PROFILE_COUNTRY, profileHandler);
                break;
            case SelectDateWeidget.SUBMIT_SELECT_DATE://确认日期
                if (!isNetWorkConnect(MyApplication.getInstance())) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                Bundle bundle = (Bundle) msg.obj;
                birthdayString = bundle.getString("year") + "-" + bundle.getString("month") + "-" + bundle.getString("day");
                dialog = CustomProgressDialog.show(ProfileActivity.this,
                        getString(R.string.connecting), false, null);
                API1.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""), birthdayString,
                        sp.getString(Common.USERINFO_GENDER, "").toLowerCase(), sp.getString(Common.USERINFO_COUNTRY, ""),
                        "", API1.UPDATE_PROFILE_BIRTHDAY, profileHandler);
                break;

            case R.id.item_gender:
                if (!isNetWorkConnect(MyApplication.getInstance())) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                dialog = CustomProgressDialog.show(ProfileActivity.this,
                        getString(R.string.connecting), false, null);
                API1.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""),
                        sp.getString(Common.USERINFO_BIRTHDAY, ""), genderString,
                        sp.getString(Common.USERINFO_COUNTRY, ""), "", API1.UPDATE_PROFILE_GENDER, profileHandler);
                break;


            case API1.UPDATE_PROFILE_FAILED:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.UPDATE_PROFILE_SUCCESS:
                switch (msg.arg1) {
                    case API1.UPDATE_PROFILE_NAME:
                        tvNickName.setText(nickNameString);
                        tvNickName.setTextColor(getResources().getColor(R.color.pp_blue));
                        break;

                    case API1.UPDATE_PROFILE_GENDER:
                        if (genderString.equals("male")) {
                            tvGender.setText(R.string.male);
                        } else if (genderString.equals("female")) {
                            tvGender.setText(R.string.female);
                        }
                        tvGender.setTextColor(getResources().getColor(R.color.pp_blue));
                        break;

                    case API1.UPDATE_PROFILE_BIRTHDAY:
                        PictureAirLog.out("birthday--->" + birthdayString);
                        tvBirthday.setText(birthdayString);
                        tvBirthday.setTextColor(getResources().getColor(R.color.pp_blue));
                        break;

                    case API1.UPDATE_PROFILE_COUNTRY:
                        countryTv.setText(countryString);
                        break;

                    default:
                        break;
                }

                SharedPreferences.Editor e = sp.edit();
                e.putString(Common.USERINFO_BIRTHDAY, birthdayString);
                e.putString(Common.USERINFO_NICKNAME, nickNameString);
                e.putString(Common.USERINFO_COUNTRY, countryCode);
                e.putString(Common.USERINFO_GENDER, genderString);
                e.commit();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_profile);
        sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
        newToast = new MyToast(this);
        initView();
        initData();
    }

    private void initView() {
        setTopLeftValueAndShow(R.drawable.back_white, true);
        setTopTitleShow(R.string.profile);
        tvNickName = (TextView) findViewById(R.id.nick_name);
        tvGender = (TextView) findViewById(R.id.sex);
        tvBirthday = (TextView) findViewById(R.id.birthday);
        countryTv = (TextView) findViewById(R.id.country_tv);
        accountTv = (TextView) findViewById(R.id.account_tv);


        nn = (RelativeLayout) findViewById(R.id.item_nickname);
        g = (RelativeLayout) findViewById(R.id.item_gender);
        bd = (RelativeLayout) findViewById(R.id.item_birth);
        countryRL = (RelativeLayout) findViewById(R.id.item_country);
        item_password = (RelativeLayout) findViewById(R.id.item_modify);

        item_password.setOnClickListener(this);
        nn.setOnClickListener(this);
        g.setOnClickListener(this);
        bd.setOnClickListener(this);
        countryRL.setOnClickListener(this);
        initSSMSSDK();
    }

    /**
     * 初始化SMSSDK才能使用国家
     */
    private void initSSMSSDK() {
        SMSSDK.initSDK(this, Common.SMS_APPKEY, Common.SMS_APPSECRET);
    }

    private void initData() {
        nickNameString = sp.getString(Common.USERINFO_NICKNAME, "");
        if (!"".equals(nickNameString)) {
            tvNickName.setText(nickNameString);
            tvNickName.setTextColor(getResources().getColor(R.color.pp_blue));
        }

        genderString = sp.getString(Common.USERINFO_GENDER, "").toLowerCase();
        if (genderString.equals("male") || genderString.equals("男")) {
            tvGender.setText(R.string.male);
            tvGender.setTextColor(getResources().getColor(R.color.pp_blue));
        } else if (genderString.equals("female") || genderString.equals("女")){
            tvGender.setText(R.string.female);
            tvGender.setTextColor(getResources().getColor(R.color.pp_blue));
        }

        birthdayString = sp.getString(Common.USERINFO_BIRTHDAY, "");
        if (!"".equals(birthdayString.trim())) {
            tvBirthday.setText(birthdayString);
            tvBirthday.setTextColor(getResources().getColor(R.color.pp_blue));
        }

        // 设置国家
        countryString = sp.getString(Common.USERINFO_COUNTRY, "");
        if (null != countryString && !countryString.equals("")){
            countryString = AppUtil.getCountryByCountryCode(countryString, this);
            countryTv.setText(countryString);
        }

        if (!sp.getString(Common.USERINFO_ACCOUNT, "").equals("")) {// email
            accountTv.setText(sp.getString(Common.USERINFO_ACCOUNT, ""));
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        Message msg = profileHandler.obtainMessage();
        switch (v.getId()) {
            case R.id.item_nickname:
                intent = new Intent(ProfileActivity.this, UpdateUserinfoActivity.class);
                intent.putExtra(Common.USERINFOTYPE, Common.NICKNAMETYPE);
                startActivityForResult(intent, 0);
                break;

            case R.id.item_gender:
                if (tvGender.getText().toString().equals(getString(R.string.male))) {
                    diaLogSex(true);
                } else {
                    diaLogSex(false);
                }
                break;

            case R.id.item_birth:
                // 弹出出生年月日
                if (selectDateWeidget == null) {
                    selectDateWeidget = new SelectDateWeidget(this, bd, profileHandler);
                    selectDateWeidget.showPopupWindow();
                } else {
                    selectDateWeidget.showPopupWindow();
                }
                break;

            case R.id.item_country:
                if (null != countryString && !countryString.trim().equals("")) {
                    return;
                }
                CountryPage countryPage = new CountryPage();
                countryPage.setMHandler(profileHandler);
                countryPage.show(this,null);
                break;

            case R.id.item_modify:
                //跳转新页面。
                intent = new Intent(ProfileActivity.this, ModifyPasswordActivity.class);
                startActivity(intent);
                break;

            case R.id.isSelectMale:
                iVisSelectMale.setImageResource(R.drawable.sele);
                iVisSelectFemale.setImageResource(R.drawable.nosele);
                msg.what = R.id.item_gender;
                genderString = "male";
                mySexDialog.dismiss();
                profileHandler.sendMessage(msg);
                break;

            case R.id.isSelectFemale:
                iVisSelectMale.setImageResource(R.drawable.nosele);
                iVisSelectFemale.setImageResource(R.drawable.sele);
                msg.what = R.id.item_gender;
                genderString = "female";
                mySexDialog.dismiss();
                profileHandler.sendMessage(msg);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("resultCode", " " + resultCode);
        switch (resultCode) {
            case 1://更改昵称的标识
                if (!isNetWorkConnect(this)) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                nickNameString = data.getStringExtra("nickName");
                dialog = CustomProgressDialog.show(this,
                        getString(R.string.connecting), false, null);
                API1.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), nickNameString,
                        sp.getString(Common.USERINFO_BIRTHDAY, ""), sp.getString(Common.USERINFO_GENDER, "").toLowerCase(),
                        sp.getString(Common.USERINFO_COUNTRY, ""), "", API1.UPDATE_PROFILE_NAME, profileHandler);
                break;

            default:
                break;
        }
    }

    /**
     * 弹出对话框
     *
     * @param male 男性
     */
    private void diaLogSex(boolean male) {
        View v = LayoutInflater.from(this).inflate(
                R.layout.custom_dialog_select_sex, null);
        AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
        mySexDialog = myBuilder.create();
        mySexDialog.show();
        mySexDialog.getWindow().setContentView(v);
        isSelectMale = (RelativeLayout) v.findViewById(R.id.isSelectMale);
        isSelectFemale = (RelativeLayout) v.findViewById(R.id.isSelectFemale);
        iVisSelectMale = (ImageView) v.findViewById(R.id.iVisSelectMale);
        iVisSelectFemale = (ImageView) v.findViewById(R.id.iVisSelectFemale);
        if (male) {
            iVisSelectMale.setImageResource(R.drawable.sele);
            iVisSelectFemale.setImageResource(R.drawable.nosele);
        } else {
            iVisSelectMale.setImageResource(R.drawable.nosele);
            iVisSelectFemale.setImageResource(R.drawable.sele);
        }
        isSelectMale.setOnClickListener(this);
        isSelectFemale.setOnClickListener(this);
        mySexDialog.setCancelable(true);
        myBuilder.setCancelable(true);
        mySexDialog.setCanceledOnTouchOutside(true);
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
    protected void onDestroy() {
        super.onDestroy();
        profileHandler.removeCallbacksAndMessages(null);
    }
}
