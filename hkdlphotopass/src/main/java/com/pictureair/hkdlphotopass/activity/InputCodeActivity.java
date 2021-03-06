package com.pictureair.hkdlphotopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureworks.android.eventbus.ScanInfoEvent;
import com.pictureworks.android.util.API1;
import com.pictureworks.android.util.AppManager;
import com.pictureworks.android.util.Common;
import com.pictureworks.android.util.DealCodeUtil;
import com.pictureworks.android.util.PictureAirLog;
import com.pictureworks.android.widget.CustomProgressDialog;
import com.pictureworks.android.widget.EditTextWithClear;
import com.pictureworks.android.widget.MyToast;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;

/**
 * 手动输入条码的页面
 */
public class InputCodeActivity extends BaseActivity implements OnClickListener{
    private String[] resultList;
    private TextView tvConfirmHint, tvManulInputIntro;

    private ImageView ivShowResult;
    private Button btnConfirmScanPppCode, btnReScanPppCode;
    private LinearLayout lvButtom;

    private Button ok;
    private SharedPreferences sp;
    private MyToast newToast;
    private String inputValue1, inputValue2, inputValue3, inputValue4;
    private DealCodeUtil dealCodeUtil;

    private CustomProgressDialog dialog;

    private EditTextWithClear inputCodeEdit;
    private LinearLayout comfirmPPPLayout;

    private final Handler inputCodeHandler = new InputCodeHandler(this);

    /**
     * 统计已输入条码的个数
     */
    private int[] codeCount;


    private static class InputCodeHandler extends Handler {
        private final WeakReference<InputCodeActivity> mActivity;

        public InputCodeHandler(InputCodeActivity activity) {
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
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                break;

            case DealCodeUtil.DEAL_CODE_SUCCESS:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                if (msg.obj != null) {//从ppp过来
                    Intent intent2 = new Intent();
                    Bundle bundle = (Bundle) msg.obj;
                    if (bundle.getInt("status") == 1) {
                        EventBus.getDefault().post(new ScanInfoEvent(0, bundle.getString("result"), false));
                    } else if (bundle.getInt("status") == 2) {//将pp码返回
                        EventBus.getDefault().post(new ScanInfoEvent(0, bundle.getString("result"), bundle.getBoolean("hasBind")));
                    } else if (bundle.getInt("status") == 3) {
                        intent2.setClass(InputCodeActivity.this, MyPPPActivity.class);
                        API1.PPPlist.clear();
                        startActivity(intent2);
                    } else if (bundle.getInt("status") == 4) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean(Common.NEED_FRESH, true);
                        editor.putInt(Common.PP_COUNT, sp.getInt(Common.PP_COUNT, 0) + 1);
                        editor.commit();
                    } else if (bundle.getInt("status") == 5) {
                        EventBus.getDefault().post(new ScanInfoEvent(0, bundle.getString("result"), false));
                        PictureAirLog.out("scan ppp success and start back");
                    }
                }
                AppManager.getInstance().killActivity(MipCaptureActivity.class);
                finish();
                break;

            default:
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputcode);
        newToast = new MyToast(this);
        initview();

    }

    private void initview() {
        tvManulInputIntro = (TextView) findViewById(R.id.tv_manul_input_intro);

        sp = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);
        ok = (Button) findViewById(R.id.sure);

        inputCodeEdit = (EditTextWithClear) findViewById(R.id.input_manaul_edittext);

        ok.setOnClickListener(this);
        setTopLeftValueAndShow(R.drawable.back_white, true);

        setTopTitleShow(R.string.manual);
        dealCodeUtil = new DealCodeUtil(this, getIntent(), true, MyApplication.getInstance().getLanguageType(), inputCodeHandler);
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
                    newToast.setTextAndShow(R.string.nocontext, Common.TOAST_SHORT_TIME);
                } else {
                    //如果有键盘显示，把键盘取消掉
                    hideInputMethodManager(v);
                    dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
                    dealCodeUtil.startDealCode(inputCodeEdit.getText().toString().toUpperCase(), Common.APP_TYPE_HKDLPP);
                }
                break;

            case R.id.btn_re_scan_ppp_code:
                this.finish();
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

}
