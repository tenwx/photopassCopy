package com.pictureair.photopass.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.wheelview.SelectDateWeidget;
import com.trello.rxlifecycle.android.ActivityEvent;

import java.lang.ref.WeakReference;

import rx.android.schedulers.AndroidSchedulers;

/**
 * 个人信息页面
 */
public class ProfileActivity extends BaseActivity implements OnClickListener {
    private TextView tvNickName, tvGender, tvBirthday, countryTv, accountTv;
    private RelativeLayout nn, g, bd, countryRL, item_password;
    private PWToast newToast;
    private String nickNameString, genderString, birthdayString, countryString;
    private RelativeLayout isSelectMale, isSelectFemale;
    private ImageView iVisSelectMale, iVisSelectFemale;
    private AlertDialog mySexDialog;

    private SelectDateWeidget selectDateWeidget;

    private String countryCode;//国家简码

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
            case SelectDateWeidget.SUBMIT_SELECT_DATE://确认日期
                if (!isNetWorkConnect(MyApplication.getInstance())) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                Bundle bundle = (Bundle) msg.obj;
                birthdayString = bundle.getString("year") + "-" + bundle.getString("month") + "-" + bundle.getString("day");
                showPWProgressDialog();
                updateProfile(MyApplication.getTokenId(),
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, ""), birthdayString,
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_GENDER, "").toLowerCase(),
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_COUNTRY, ""),
                        "", API2.UPDATE_PROFILE_BIRTHDAY);
                break;

            case R.id.item_gender:
                if (!isNetWorkConnect(MyApplication.getInstance())) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                showPWProgressDialog();
                updateProfile(MyApplication.getTokenId(),
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, ""),
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_BIRTHDAY, ""), genderString,
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_COUNTRY, ""), "",
                        API2.UPDATE_PROFILE_GENDER);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile);
        newToast = new PWToast(this);
        initView();
        initData();
    }

    private void initView() {
        setTopLeftValueAndShow(R.drawable.back_blue, true);
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
    }

    private void initData() {
        nickNameString = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, "");
        if (!"".equals(nickNameString)) {
            tvNickName.setText(nickNameString);
            tvNickName.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));
        }

        genderString = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_GENDER, "").toLowerCase();
        if (genderString.equals("male") || genderString.equals("男")) {
            tvGender.setText(R.string.male);
            tvGender.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));
        } else if (genderString.equals("female") || genderString.equals("女")){
            tvGender.setText(R.string.female);
            tvGender.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));
        }

        birthdayString = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_BIRTHDAY, "");
        if (!"".equals(birthdayString.trim())) {
            tvBirthday.setText(birthdayString);
            tvBirthday.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));
        }

        // 设置国家
        countryString = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_COUNTRY, "");
        PictureAirLog.out("coutry----->" + countryString);
        if (null != countryString && !countryString.equals("")){
            countryString = AppUtil.getCountryByCountryCode(countryString, this);
            countryTv.setText(countryString);
            countryRL.setEnabled(false);
        }

        if (!SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ACCOUNT, "").equals("")) {// email
            accountTv.setText(SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ACCOUNT, ""));
            accountTv.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));
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
                intent.putExtra("nickname", nickNameString);
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



                intent = new Intent(ProfileActivity.this, SelectCountryActivity.class);
                startActivityForResult(intent, SelectCountryActivity.requestCountry);
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
        super.onActivityResult(requestCode, resultCode, data);
        PictureAirLog.d("resultCode", " " + resultCode);

        if (resultCode!=0 && requestCode == SelectCountryActivity.requestCountry) {
            String[] strs = data.getExtras().getStringArray("country");
            if (null != strs) {
                /**
                 * countrys[1] 手机区号
                 * countrys[0] 国家名称
                 * countrys[4] 国家简码
                 */
                countryString = strs[0];
                countryCode = strs[4];

                if (!isNetWorkConnect(this)) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                showPWProgressDialog();
                updateProfile(MyApplication.getTokenId(),
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, ""),
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_BIRTHDAY, ""),
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_GENDER, "").toLowerCase(),
                        countryCode, "", API2.UPDATE_PROFILE_COUNTRY);
            }
        }

        switch (resultCode) {
            case 1://更改昵称的标识
                if (!isNetWorkConnect(this)) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                nickNameString = data.getStringExtra("nickName");
                showPWProgressDialog();
                updateProfile(MyApplication.getTokenId(), nickNameString,
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_BIRTHDAY, ""),
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_GENDER, "").toLowerCase(),
                        SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_COUNTRY, ""), "", API2.UPDATE_PROFILE_NAME);
                break;

            default:
                break;
        }

    }

    private void updateProfile(String pTokenId, String pName, String pBirthday, String pGender, String pCountry, String pQQ, final int pModifyType) {
        API2.updateProfile(pTokenId, pName, pBirthday, pGender, pCountry, pQQ)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        updateProfileSuccess(pModifyType);
                        dismissPWProgressDialog();
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void updateProfileSuccess(int modifyType) {

        switch (modifyType) {
            case API2.UPDATE_PROFILE_NAME:
                tvNickName.setText(nickNameString);
                tvNickName.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));
                break;

            case API2.UPDATE_PROFILE_GENDER:
                if (genderString.equals("male")) {
                    tvGender.setText(R.string.male);
                } else if (genderString.equals("female")) {
                    tvGender.setText(R.string.female);
                }
                tvGender.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));
                break;

            case API2.UPDATE_PROFILE_BIRTHDAY:
                PictureAirLog.out("birthday--->" + birthdayString);
                tvBirthday.setText(birthdayString);
                tvBirthday.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));
                break;

            case API2.UPDATE_PROFILE_COUNTRY:
                countryTv.setText(countryString);
                countryRL.setEnabled(false);
                break;

            default:
                break;
        }

        SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_BIRTHDAY, birthdayString);
        SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, nickNameString);
        SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_COUNTRY, countryCode);
        SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_GENDER, genderString);
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
