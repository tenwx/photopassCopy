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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.wheelview.OnWheelScrollListener;
import com.pictureair.photopass.widget.wheelview.WheelView;
import com.pictureair.photopass.widget.wheelview.adapter.NumericWheelAdapter;

import java.util.Calendar;

/**
 * 个人信息页面
 */
public class ProfileActivity extends BaseActivity implements OnClickListener {
    private TextView tvNickName, tvGender, tvBirthday, tvQq;
    private RelativeLayout nn, g, bd, q, item_password;
    private LinearLayout timePickerLinearLayout;
    private Button cancelTime, submitTime;
    private ImageView back;
    private SharedPreferences sp;
    private int year, month, day;
    private MyToast newToast;
    private String birthdayString;
    private WheelView yearWheelView, monthWheelView, dayWheelView;
    private Animation timePickerInAnimation, timePickerOutAnimation;

    private RelativeLayout isSelectMale, isSelectFemale;
    private ImageView iVisSelectMale, iVisSelectFemale;
    private AlertDialog mySexDialog;
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
                    String sex = (String) msg.obj;
                    if (sex.equals("male") || sex.equals("男")) {
                        tvGender.setText(R.string.male);
                    } else {
                        tvGender.setText(R.string.female);
                    }

                    tvGender.invalidate();
                    API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""), sp.getString(Common.USERINFO_BIRTHDAY, ""), sex.toLowerCase(), sp.getString(Common.USERINFO_COUNTRY, ""), sp.getString(Common.USERINFO_QQ, ""), handler);
                    break;
                case R.id.time_picker:
                    String birthString = (String) msg.obj;
                    tvBirthday.setText(birthString);
                    tvBirthday.invalidate();
                    API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""), birthString, sp.getString(Common.USERINFO_GENDER, "").toLowerCase(), sp.getString(Common.USERINFO_COUNTRY, ""), sp.getString(Common.USERINFO_QQ, ""), handler);
                    break;

                case API.UPDATE_PROFILE_FAILED:
                    System.out.println("-------failed");
                    if (msg.obj == null) {//网络连接错误
                        newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                    } else {//修改失败
                        newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                    }
                    break;

                case API.UPDATE_PROFILE_SUCCESS:
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
                    e.commit();
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
        timePickerLinearLayout = (LinearLayout) findViewById(R.id.time_picker);
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
        timePickerInAnimation = AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.slide_in_from_bottom);
        timePickerOutAnimation = AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.slide_out_to_bottom);
        if ("".equals(birthdayString.trim())) {
            birthdayString = "1991-01-01";
        }
        tvBirthday.setText(birthdayString);

        tvQq.setText(sp.getString(Common.USERINFO_QQ, ""));

        System.out.println("birthday------>" + birthdayString);

        String[] time = birthdayString.split("-");
        year = Integer.valueOf(time[0]);
        month = Integer.valueOf(time[1]) - 1;
        day = Integer.valueOf(time[2]);
        timePickerLinearLayout.addView(getDataPick());
        timePickerLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        Message msg = handler.obtainMessage();
        switch (v.getId()) {
            case R.id.item_nickname:
                if (timePickerLinearLayout.isShown()) {
                    timePickerLinearLayout.startAnimation(timePickerOutAnimation);
                    timePickerLinearLayout.setVisibility(View.GONE);
                }
                intent = new Intent(ProfileActivity.this, UpdateUserinfoActivity.class);
                intent.putExtra(Common.USERINFOTYPE, Common.NICKNAMETYPE);
                startActivityForResult(intent, 0);
                break;

            case R.id.item_gender:
                if (timePickerLinearLayout.isShown()) {
                    timePickerLinearLayout.startAnimation(timePickerOutAnimation);
                    timePickerLinearLayout.setVisibility(View.GONE);
                }
                if (tvGender.getText().toString().equals(getString(R.string.male))) {
                    diaLogSex(true);
                } else {
                    diaLogSex(false);
                }
                break;

            case R.id.item_birth:
                // 弹出出生年月日
                if (!timePickerLinearLayout.isShown()) {
                    timePickerLinearLayout.startAnimation(timePickerInAnimation);
                    timePickerLinearLayout.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.item_qq:
                if (timePickerLinearLayout.isShown()) {
                    timePickerLinearLayout.startAnimation(timePickerOutAnimation);
                    timePickerLinearLayout.setVisibility(View.GONE);
                }
                intent = new Intent(ProfileActivity.this, UpdateUserinfoActivity.class);
                intent.putExtra(Common.USERINFOTYPE, Common.QQTYPE);
                startActivityForResult(intent, 1);
                break;

            case R.id.back:
                if (timePickerLinearLayout.isShown()) {
                    timePickerLinearLayout.startAnimation(timePickerOutAnimation);
                    timePickerLinearLayout.setVisibility(View.GONE);
                }
                finish();
                break;

            case R.id.item_modify:
                //跳转新页面。
                if (timePickerLinearLayout.isShown()) {
                    timePickerLinearLayout.startAnimation(timePickerOutAnimation);
                    timePickerLinearLayout.setVisibility(View.GONE);
                }
                intent = new Intent(ProfileActivity.this, ModifyPasswordActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_c_date://取消时间
                timePickerLinearLayout.startAnimation(timePickerOutAnimation);
                timePickerLinearLayout.setVisibility(View.GONE);
                break;

            case R.id.btn_s_date://提交时间
                msg.what = R.id.time_picker;
                msg.obj = birthdayString;
                handler.sendMessage(msg);
                timePickerLinearLayout.startAnimation(timePickerOutAnimation);
                timePickerLinearLayout.setVisibility(View.GONE);
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
                String name = data.getStringExtra("nickName");
                tvNickName.setText(name);
                tvNickName.invalidate();
                API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), name, sp.getString(Common.USERINFO_BIRTHDAY, ""), sp.getString(Common.USERINFO_GENDER, "").toLowerCase(), sp.getString(Common.USERINFO_COUNTRY, ""), sp.getString(Common.USERINFO_QQ, ""), handler);
                break;
            case 2://更改QQ的标识
                String qq = data.getStringExtra("qq");
                tvQq.setText(qq);
                tvQq.invalidate();
                API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""), sp.getString(Common.USERINFO_BIRTHDAY, ""), sp.getString(Common.USERINFO_GENDER, "").toLowerCase(), sp.getString(Common.USERINFO_COUNTRY, ""), qq, handler);
                break;
            default:
                break;
        }
    }


    private View getDataPick() {
        Calendar c = Calendar.getInstance();
        int norYear = c.get(Calendar.YEAR);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wheel_date_picker, null);

        // 得到按钮
        cancelTime = (Button) view.findViewById(R.id.btn_c_date);
        submitTime = (Button) view.findViewById(R.id.btn_s_date);

        cancelTime.setOnClickListener(this);
        submitTime.setOnClickListener(this);

        yearWheelView = (WheelView) view.findViewById(R.id.year);
        NumericWheelAdapter numericWheelAdapter1 = new NumericWheelAdapter(
                this, 1950, norYear); // 最小
        numericWheelAdapter1.setLabel("");// 这里改变
        yearWheelView.setViewAdapter(numericWheelAdapter1);
        yearWheelView.setCyclic(true);// 是否可循环滑动
        yearWheelView.addScrollingListener(scrollListener);

        monthWheelView = (WheelView) view.findViewById(R.id.month);
        NumericWheelAdapter numericWheelAdapter2 = new NumericWheelAdapter(
                this, 1, 12, "%02d");
        numericWheelAdapter2.setLabel("");
        monthWheelView.setViewAdapter(numericWheelAdapter2);
        monthWheelView.setCyclic(true);
        monthWheelView.addScrollingListener(scrollListener);

        dayWheelView = (WheelView) view.findViewById(R.id.day);
        initDay(year, month);
        dayWheelView.setCyclic(true);
        dayWheelView.addScrollingListener(scrollListener);

        yearWheelView.setVisibleItems(4);// 设置显示行数
        monthWheelView.setVisibleItems(4);
        dayWheelView.setVisibleItems(4);

        yearWheelView.setCurrentItem(year - 1950);
        monthWheelView.setCurrentItem(month);
        dayWheelView.setCurrentItem(day - 1);

        return view;
    }

    private OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {
        }

        @Override
        public void onScrollingFinished(WheelView wheel) {
            int n_year = yearWheelView.getCurrentItem() + 1950;// 年
            int n_month = monthWheelView.getCurrentItem() + 1;// 月

            initDay(n_year, n_month);

            birthdayString = new StringBuilder()
                    .append((yearWheelView.getCurrentItem() + 1950))
                    .append("-")
                    .append((monthWheelView.getCurrentItem() + 1) < 10 ? "0"
                            + (monthWheelView.getCurrentItem() + 1) : (monthWheelView
                            .getCurrentItem() + 1))
                    .append("-")
                    .append(((dayWheelView.getCurrentItem() + 1) < 10) ? "0"
                            + (dayWheelView.getCurrentItem() + 1) : (dayWheelView
                            .getCurrentItem() + 1)).toString();

        }
    };

    private void initDay(int arg1, int arg2) {
        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(this,
                1, AppUtil.getDay(arg1, arg2), "%02d");
        numericWheelAdapter.setLabel("");
        dayWheelView.setViewAdapter(numericWheelAdapter);
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
