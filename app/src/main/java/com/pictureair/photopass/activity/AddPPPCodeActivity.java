package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.pictureair.photopass.R;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DealCodeUtil;
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;

/**
 * 手动输入条码的页面
 */
public class AddPPPCodeActivity extends BaseActivity implements OnClickListener, PWDialog.OnPWDialogClickListener{
    private Button ok;
    private PWToast newToast;
    private DealCodeUtil dealCodeUtil;

    private EditTextWithClear inputCodeEdit;

    private PWDialog pwDialog;

    private static final int TYPE_NOT_SAME_DIALOG = 333;

    private final Handler inputCodeHandler = new InputCodeHandler(this);

    private static class InputCodeHandler extends Handler {
        private final WeakReference<AddPPPCodeActivity> mActivity;

        public InputCodeHandler(AddPPPCodeActivity activity) {
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
            case DealCodeUtil.DEAL_CODE_FAILED:
                dismissPWProgressDialog();
                break;

            case DealCodeUtil.DEAL_CODE_SUCCESS:
                dismissPWProgressDialog();

                if (msg.obj != null) {
                    Bundle bundle = (Bundle) msg.obj;
                    if (bundle.getInt("status") == DealCodeUtil.STATE_RETURN_MSG) {//需要当前页面提示用户扫描结果
                        String result = bundle.getString("result");
                        if (result.equals("pppOK")) {//添加ppp成功
                            addPPPSuccess();

                        } else {//失败，卡片类型不一致
                            //初始化dialog
                            if (pwDialog == null) {
                                pwDialog = new PWDialog(this)
                                        .setOnPWDialogClickListener(this)
                                        .pwDialogCreate();
                            }
                            pwDialog.setPWDialogId(TYPE_NOT_SAME_DIALOG)
                                    .setPWDialogMessage(R.string.not_ppp_card)
                                    .setPWDialogNegativeButton(null)
                                    .setPWDialogPositiveButton(R.string.dialog_ok1)
                                    .setPWDialogContentCenter(true)
                                    .pwDilogShow();

                        }
//                    } else if (bundle.getInt("status") == DealCodeUtil.STATE_ADD_PPP_TO_USER_NOT_RETURN_SUCCESS) {//添加ppp成功
//                        addPPPSuccess();
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * 添加ppp成功
     */
    private void addPPPSuccess() {
        //进入ppp页面
        Intent intent2 = new Intent();
        intent2.setClass(AddPPPCodeActivity.this, MyPPPActivity.class);
        intent2.putExtra("upgradePP", true);
        API1.PPPlist.clear();
        startActivity(intent2);
        AppManager.getInstance().killActivity(MipCaptureActivity.class);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ppp_code);
        newToast = new PWToast(this);
        initview();

    }

    private void initview() {
        ok = (Button) findViewById(R.id.sure);
        inputCodeEdit = (EditTextWithClear) findViewById(R.id.input_manaul_edittext);

        ok.setOnClickListener(this);
        setTopLeftValueAndShow(R.drawable.back_blue, true);

        setTopTitleShow(R.string.active);
        dealCodeUtil = new DealCodeUtil(this, getIntent(), true, inputCodeHandler);
    }

    private void hideInputMethodManager(View v) {
        // TODO Auto-generated method stub
        /*隐藏软键盘*/
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.sure://手动输入页面的确定
                if ("".equals(inputCodeEdit.getText().toString())) {
                    newToast.setTextAndShow(R.string.http_error_code_6136, Common.TOAST_SHORT_TIME);
                } else {
                    //如果有键盘显示，把键盘取消掉
                    hideInputMethodManager(v);
                    showPWProgressDialog();
                    dealCodeUtil.startDealCode(inputCodeEdit.getText().toString().toUpperCase(), true);
                }
                break;

            default:
                break;
        }
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
        inputCodeHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {

    }

}
