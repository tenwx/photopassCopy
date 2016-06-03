package com.pictureair.photopass.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;

public class ModifyPasswordActivity extends BaseActivity implements OnClickListener {
    private SharedPreferences sharedPreferences;
    private EditTextWithClear oldPassword, newPassword;
    private Button submit;
    private PWToast newToast;
    private boolean isSele = true;
    private ImageButton radio;
    private CustomProgressDialog dialog;

    private final Handler modifyPasswordHandler = new ModifyPasswordHandler(this);


    private static class ModifyPasswordHandler extends Handler{
        private final WeakReference<ModifyPasswordActivity> mActivity;

        public ModifyPasswordHandler(ModifyPasswordActivity activity){
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

            case API1.MODIFY_PWD_SUCCESS:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                newToast.setTextAndShow(R.string.modify_password_success,
                        Common.TOAST_SHORT_TIME);
                ModifyPasswordActivity.this.finish();
                break;

            case API1.MODIFY_PWD_FAILED:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            default:
                break;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);
        initView();
    }

    private void initView() {
        newToast = new PWToast(this);
        sharedPreferences = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);
        setTopLeftValueAndShow(R.drawable.back_white,true);
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
                    newToast.setTextAndShow(R.string.modify_password_empty_hint,
                            Common.TOAST_SHORT_TIME);
                } else {
                    //判断newPassword的合法性
                    switch (AppUtil.checkPwd(newPassword.getText().toString(),
                            newPassword.getText().toString())) {
                        case AppUtil.PWD_ALL_SAPCE:
                            newToast.setTextAndShow(R.string.pwd_no_all_space,
                                    Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_AVAILABLE:
                            // 密码可用
                            dialog = CustomProgressDialog.show(
                                    ModifyPasswordActivity.this,
                                    getString(R.string.connecting), false, null);
                            API1.modifyPwd(oldPassword.getText().toString(), newPassword.getText().toString(), modifyPasswordHandler);
                            break;

                        case AppUtil.PWD_EMPTY:// 空
                            newToast.setTextAndShow(
                                    R.string.modify_password_empty_hint,
                                    Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_INCONSISTENCY:// 不一致，当前页面不存在这情况
                            break;

                        case AppUtil.PWD_SHORT:// 小于6位
                            newToast.setTextAndShow(R.string.notify_password_hint,
                                    Common.TOAST_SHORT_TIME);
                            break;

                        case AppUtil.PWD_HEAD_OR_FOOT_IS_SPACE:// 密码首尾不能为空
                            newToast.setTextAndShow(R.string.pwd_head_or_foot_space,
                                    Common.TOAST_SHORT_TIME);
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
        modifyPasswordHandler.removeCallbacksAndMessages(null);
    }
}
