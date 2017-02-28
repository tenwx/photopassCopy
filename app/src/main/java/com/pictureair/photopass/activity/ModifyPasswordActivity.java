package com.pictureair.photopass.activity;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.PWToast;
import com.trello.rxlifecycle.android.ActivityEvent;

import rx.android.schedulers.AndroidSchedulers;

public class ModifyPasswordActivity extends BaseActivity implements OnClickListener {
    private EditTextWithClear oldPassword, newPassword;
    private Button submit;
    private PWToast newToast;
    private boolean isSele = true;
    private ImageButton radio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);
        initView();
    }

    private void initView() {
        newToast = new PWToast(this);
        setTopLeftValueAndShow(R.drawable.back_blue,true);
        setTopTitleShow(R.string.modify);
        radio = (ImageButton) findViewById(R.id.modify_password_radio);
        oldPassword = (EditTextWithClear) findViewById(R.id.old_password);
        newPassword = (EditTextWithClear) findViewById(R.id.new_password);
        submit = (Button) findViewById(R.id.submit);
        submit.setTypeface(MyApplication.getInstance().getFontBold());

        radio.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.submit:
                if (!isNetWorkConnect(MyApplication.getInstance())) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                if (oldPassword.getText().toString().equals("")
                        || newPassword.getText().toString().equals("")) {
                    newToast.setTextAndShow(R.string.modify_password_empty_hint, Common.TOAST_SHORT_TIME);
                } else {
                    //判断newPassword的合法性
                    switch (AppUtil.checkPwd(newPassword.getText().toString(), newPassword.getText().toString())) {
                        case AppUtil.PWD_ALL_SAPCE:
                            newToast.setTextAndShow(R.string.pwd_no_all_space, Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_AVAILABLE:
                            // 密码可用
                            showPWProgressDialog();
                            modifyPassword(oldPassword.getText().toString(), newPassword.getText().toString());
                            break;

                        case AppUtil.PWD_EMPTY:// 空
                            newToast.setTextAndShow(R.string.modify_password_empty_hint, Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_INCONSISTENCY:// 不一致，当前页面不存在这情况
                            break;

                        case AppUtil.PWD_SHORT:// 小于6位
                            newToast.setTextAndShow(R.string.notify_password_hint, Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_HEAD_OR_FOOT_IS_SPACE:// 密码首尾不能为空
                            newToast.setTextAndShow(R.string.pwd_head_or_foot_space, Common.TOAST_SHORT_TIME);
                            break;

                        default:
                            break;
                    }
                }
                break;
            case R.id.modify_password_radio:
                PictureAirLog.out("onclick");
                if (isSele) {
                    PictureAirLog.out("sele");
                    radio.setImageResource(R.drawable.sele);
                    oldPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isSele = false;
                } else {
                    PictureAirLog.out("no sele");
                    radio.setImageResource(R.drawable.nosele);
                    oldPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    isSele = true;
                }

                break;

            default:
                break;
        }

    }

    private void modifyPassword(String pOldPwd, String pNewPwd) {
        API2.modifyPwd(pOldPwd, pNewPwd)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(R.string.modify_password_success, Common.TOAST_SHORT_TIME);
                        ModifyPasswordActivity.this.finish();

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
    }
}
