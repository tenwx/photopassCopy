package com.pictureair.photopass.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.wheelview.SelectDateWeidget;

/**
 * 个人信息页面
 */
public class ProfileActivity extends BaseActivity implements OnClickListener {
    private TextView tvNickName, tvGender, tvBirthday, tvQq;
    private RelativeLayout nn, g, bd, q, item_password;
    private ImageView back;
    private SharedPreferences sp;
    private MyToast newToast;
    private String birthdayString;
    private RelativeLayout isSelectMale, isSelectFemale;
    private ImageView iVisSelectMale, iVisSelectFemale;
    private AlertDialog mySexDialog;

    private SelectDateWeidget selectDateWeidget;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case R.id.item_nickname:
                    String nn = (String) msg.obj;
                    tvNickName.setText(nn);
                    tvNickName.invalidate();
                    break;
                case R.id.item_gender:
                    if (!isNetWorkConnect(MyApplication.getInstance())){
                        newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
                        return;
                    }
                    String sex = (String) msg.obj;
                    if (sex.equals("male") || sex.equals("男")) {
                        tvGender.setText(R.string.male);
                    } else {
                        tvGender.setText(R.string.female);
                    }

                    tvGender.invalidate();
                    API1.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""), sp.getString(Common.USERINFO_BIRTHDAY, ""), sex.toLowerCase(), sp.getString(Common.USERINFO_COUNTRY, ""), sp.getString(Common.USERINFO_QQ, ""), handler);
                    break;
                case SelectDateWeidget.SUBMIT_SELECT_DATE://确认日期
                    if (!isNetWorkConnect(MyApplication.getInstance())){
                        newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
                        return;
                    }
                    Bundle bundle = (Bundle) msg.obj;
                    birthdayString = bundle.getString("year") + "-" + bundle.getString("month") + "-" + bundle.getString("day");
                    tvBirthday.setText(birthdayString);
                    tvBirthday.invalidate();
                    API1.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""), birthdayString, sp.getString(Common.USERINFO_GENDER, "").toLowerCase(), sp.getString(Common.USERINFO_COUNTRY, ""), sp.getString(Common.USERINFO_QQ, ""), handler);
                    break;

                case API1.UPDATE_PROFILE_FAILED:
                    newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(),msg.arg1),Common.TOAST_SHORT_TIME);
                    break;

                case API1.UPDATE_PROFILE_SUCCESS:
                    Editor e = sp.edit();
                    e.putString(Common.USERINFO_NICKNAME, tvNickName.getText().toString());
                    e.putString(Common.USERINFO_BIRTHDAY, tvBirthday.getText().toString());
                    String gender = tvGender.getText().toString().toLowerCase();
                    if (gender.equals("男") || gender.equals("male")) {
                        gender = "male";
                    } else {
                        gender = "female";
                    }

                    e.putString(Common.USERINFO_GENDER, gender);
                    e.putString(Common.USERINFO_QQ, tvQq.getText().toString());
                    e.apply();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

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
        tvNickName = (TextView) findViewById(R.id.nick_name);
        tvGender = (TextView) findViewById(R.id.sex);
        tvBirthday = (TextView) findViewById(R.id.birthday);
        tvQq = (TextView) findViewById(R.id.qq);

        nn = (RelativeLayout) findViewById(R.id.item_nickname);
        g = (RelativeLayout) findViewById(R.id.item_gender);
        bd = (RelativeLayout) findViewById(R.id.item_birth);
        q = (RelativeLayout) findViewById(R.id.item_qq);
        item_password = (RelativeLayout) findViewById(R.id.item_modify);
        back = (ImageView) findViewById(R.id.back);

        item_password.setOnClickListener(this);
        nn.setOnClickListener(this);
        g.setOnClickListener(this);
        bd.setOnClickListener(this);
        q.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private void initData() {
        tvNickName.setText(sp.getString(Common.USERINFO_NICKNAME, "pictureAir"));
        String genderStr = sp.getString(Common.USERINFO_GENDER, "male").toLowerCase();
        if (genderStr.equals("male") || genderStr.equals("男")) {
            tvGender.setText(R.string.male);
        } else {
            tvGender.setText(R.string.female);
        }
        birthdayString = sp.getString(Common.USERINFO_BIRTHDAY, "1991-01-01");
        if ("".equals(birthdayString.trim())) {
            birthdayString = "1991-01-01";
        }
        tvBirthday.setText(birthdayString);
        tvQq.setText(sp.getString(Common.USERINFO_QQ, ""));
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        Message msg = handler.obtainMessage();
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
                    selectDateWeidget = new SelectDateWeidget(this, bd, handler);
                    selectDateWeidget.showPopupWindow();
                } else {
                    selectDateWeidget.showPopupWindow();
                }
                break;

            case R.id.item_qq:
                intent = new Intent(ProfileActivity.this, UpdateUserinfoActivity.class);
                intent.putExtra(Common.USERINFOTYPE, Common.QQTYPE);
                startActivityForResult(intent, 1);
                break;

            case R.id.back:
                finish();
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
                msg.obj = "male";
                mySexDialog.dismiss();
                handler.sendMessage(msg);
                break;

            case R.id.isSelectFemale:
                iVisSelectMale.setImageResource(R.drawable.nosele);
                iVisSelectFemale.setImageResource(R.drawable.sele);
                msg.what = R.id.item_gender;
                msg.obj = "female";
                mySexDialog.dismiss();
                handler.sendMessage(msg);
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
                if (!isNetWorkConnect(this)){
                    newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
                    return;
                }
                String name = data.getStringExtra("nickName");
                tvNickName.setText(name);
                tvNickName.invalidate();
                API1.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), name, sp.getString(Common.USERINFO_BIRTHDAY, ""), sp.getString(Common.USERINFO_GENDER, "").toLowerCase(), sp.getString(Common.USERINFO_COUNTRY, ""), sp.getString(Common.USERINFO_QQ, ""), handler);
                break;
            case 2://更改QQ的标识
                if (!isNetWorkConnect(this)){
                    newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
                    return;
                }
                String qq = data.getStringExtra("qq");
                tvQq.setText(qq);
                tvQq.invalidate();
                API1.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""), sp.getString(Common.USERINFO_BIRTHDAY, ""), sp.getString(Common.USERINFO_GENDER, "").toLowerCase(), sp.getString(Common.USERINFO_COUNTRY, ""), qq, handler);
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
