package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.eventbus.ScanInfoEvent;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DealCodeUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;

/**
 * 手动输入条码的页面
 */
public class InputCodeActivity extends BaseActivity implements OnClickListener{
    private Button ok;
    private SharedPreferences sp;
    private PWToast newToast;
    private DealCodeUtil dealCodeUtil;

    private EditTextWithClear inputCodeEdit;

    private TextView wordSpaceTextView;

    private final Handler inputCodeHandler = new InputCodeHandler(this);

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
                dismissPWProgressDialog();
                break;

            case DealCodeUtil.DEAL_CODE_SUCCESS:
                dismissPWProgressDialog();

                if (msg.obj != null) {
                    Intent intent2 = new Intent();
                    Bundle bundle = (Bundle) msg.obj;
                    if (bundle.getInt("status") == DealCodeUtil.STATE_RETURN_MSG) {
                        EventBus.getDefault().post(new ScanInfoEvent(0, bundle.getString("result"), false, getIntent().getStringExtra("type")));

                    } else if (bundle.getInt("status") == DealCodeUtil.STATE_ADD_CODE_TO_USER_NOT_RETURN_SUCCESS) {
                        intent2.setClass(InputCodeActivity.this, MyPPPActivity.class);
                        API1.PPPlist.clear();
                        startActivity(intent2);

                    } else if (bundle.getInt("status") == DealCodeUtil.STATE_ADD_PP_TO_USER_NOT_RETURN_SUCCESS) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean(Common.NEED_FRESH, true);
                        editor.putInt(Common.PP_COUNT, sp.getInt(Common.PP_COUNT, 0) + 1);
                        editor.commit();

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
        newToast = new PWToast(this);
        initview();

    }

    private void initview() {
        sp = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);
        ok = (Button) findViewById(R.id.sure);

        inputCodeEdit = (EditTextWithClear) findViewById(R.id.input_manaul_edittext);
        wordSpaceTextView = (TextView) findViewById(R.id.scancodetextview);
        wordSpaceTextView.setTypeface(MyApplication.getInstance().getFontBold());
        wordSpaceTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        ok.setOnClickListener(this);
        setTopLeftValueAndShow(R.drawable.back_white, true);

        setTopTitleShow(R.string.manual);
        dealCodeUtil = new DealCodeUtil(this, getIntent(), true, inputCodeHandler);

        inputCodeEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                String editString = "";
                for (int i = 0; i < s.length(); i++) {
                    editString += " " + s.charAt(i);
                }
                PictureAirLog.out("editString---->" + editString);
                if (0 == inputCodeEdit.getText().toString().length()) {
                    if (wordSpaceTextView.isShown()) {
                        wordSpaceTextView.scrollTo(0, 0);//保证放大的textview正常显示
                        wordSpaceTextView.setVisibility(View.INVISIBLE);
                        wordSpaceTextView.setText(editString.trim());
                    }
                }else {
                    if (!wordSpaceTextView.isShown()) {
                        wordSpaceTextView.setVisibility(View.VISIBLE);
                    }
                    wordSpaceTextView.setText(editString.trim());
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
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
                    dealCodeUtil.startDealCode(inputCodeEdit.getText().toString().toUpperCase());
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
