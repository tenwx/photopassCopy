package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.eventbus.ScanInfoEvent;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DealCodeUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;

/**
 * 手动输入条码的页面
 */
public class InputCodeActivity extends BaseActivity implements OnClickListener,View.OnKeyListener{
    private String[] resultList;
    private TextView tvConfirmHint,tvManulInputIntro;
    private ImageView ivShowResult;
    private Button btnConfirmScanPppCode , btnReScanPppCode;
    private LinearLayout lvButtom;

    private Button ok;
    private EditText input1, input2, input3, input4;
    private SharedPreferences sp;
    private MyToast newToast;
    private String inputValue1, inputValue2, inputValue3, inputValue4;
    private DealCodeUtil dealCodeUtil;

    private CustomProgressDialog dialog;

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
        tvConfirmHint = (TextView) findViewById(R.id.tv_confirm_hint);
        ivShowResult = (ImageView) findViewById(R.id.iv_show_result);
        btnConfirmScanPppCode = (Button) findViewById(R.id.btn_confirm_scan_ppp_code);
        btnConfirmScanPppCode.setOnClickListener(this);
        btnReScanPppCode = (Button) findViewById(R.id.btn_re_scan_ppp_code);
        btnReScanPppCode.setOnClickListener(this);
        lvButtom = (LinearLayout) findViewById(R.id.lv_buttom);

        sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
        ok = (Button) findViewById(R.id.sure);

        input1 = (EditText) findViewById(R.id.input1);
        input2 = (EditText) findViewById(R.id.input2);
        input3 = (EditText) findViewById(R.id.input3);
        input4 = (EditText) findViewById(R.id.input4);

        ok.setOnClickListener(this);
        setTopLeftValueAndShow(R.drawable.back_white, true);


        if (getIntent().getStringExtra("text") == null) {
            lvButtom.setVisibility(View.GONE);
            btnConfirmScanPppCode.setVisibility(View.GONE);
            btnReScanPppCode.setVisibility(View.GONE);

            tvManulInputIntro.setVisibility(View.VISIBLE);
            ivShowResult.setVisibility(View.GONE);
            ok.setVisibility(View.VISIBLE);
            tvConfirmHint.setVisibility(View.INVISIBLE);
            setTopTitleShow(R.string.manual);
        } else {
            tvManulInputIntro.setVisibility(View.GONE);
            ivShowResult.setVisibility(View.VISIBLE);
            ivShowResult.setImageBitmap(MipCaptureActivity.tempBitmap);
            MipCaptureActivity.tempBitmap = null;

            ok.setVisibility(View.GONE);
            tvConfirmHint.setVisibility(View.VISIBLE);
            setTopTitleShow(R.string.confirm_ppp_code_title);
            resultList = getIntent().getStringExtra("text").split("-");
            input1.setText(resultList[0]);
            input2.setText(resultList[1]);
            input3.setText(resultList[2]);
            input4.setText(resultList[3]);
        }

        input2.setOnKeyListener(this);
        input3.setOnKeyListener(this);
        input4.setOnKeyListener(this);


        input1.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                PictureAirLog.out("========== input1 onTextChanged " + arg0.length());
                if (arg0.length() > 3) {
                    input2.setEnabled(true);
                    input2.requestFocus();
                }
                if (arg0.length() == 5){ // input1 中输入第5个字符，就让第5个字符  移动到input2 中
                    String text1= input1.getText().toString();
                    PictureAirLog.e("", "====:" + text1);
                    input1.setText(text1.substring(0, 4));
                    input2.setText(String.valueOf(text1.charAt(4)));
                    input2.setEnabled(true);
                    input2.requestFocus();
                    input2.setSelection(input2.getText().toString().length());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                PictureAirLog.out("========== input1 before " + arg0.length());
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                PictureAirLog.out("========== input1 after " + input1.getText().toString().length());
            }
        });

        input2.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                PictureAirLog.out("========== input2 onTextChanged " + arg0.length());
                if (arg0.length() > 3) {
                    input3.setEnabled(true);
                    input3.requestFocus();

                } else if (arg0.length() == 0) {
                    input1.setEnabled(true);
                    input1.requestFocus();
                    input1.setSelection(input1.getText().toString().length());
                }

                if (arg0.length() == 5){ // input2 中输入第5个字符，就让第5个字符  移动到input3 中
                    String text2= input2.getText().toString();
                    PictureAirLog.e("", "====:" + text2);
                    input2.setText(text2.substring(0, 4));
                    input3.setText(String.valueOf(text2.charAt(4)));
                    input3.setEnabled(true);
                    input3.requestFocus();
                    input3.setSelection(input3.getText().toString().length());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                PictureAirLog.out("========== input2  before " + arg0.length());
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                PictureAirLog.out("========== input2  after " + input2.getText().toString().length());
            }
        });

        input3.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                if (arg0.length() > 3) {
                    input4.setEnabled(true);
                    input4.requestFocus();
                } else if (arg0.length() == 0) {
                    input2.setEnabled(true);
                    input2.requestFocus();
                    input2.setSelection(input2.getText().toString().length());
                }
                if (arg0.length() == 5){ // input3 中输入第5个字符，就让第5个字符  移动到input4 中
                    String text3= input3.getText().toString();
                    PictureAirLog.e("", "====:" + text3);
                    input3.setText(text3.substring(0, 4));
                    input4.setText(String.valueOf(text3.charAt(4)));
                    input4.setEnabled(true);
                    input4.requestFocus();
                    input4.setSelection(input4.getText().toString().length());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        input4.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                if (arg0.length() == 0) {
                    input3.setEnabled(true);
                    input3.requestFocus();
                    input3.setSelection(input3.getText().toString().length());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        dealCodeUtil = new DealCodeUtil(this, getIntent(), inputCodeHandler);

    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        PictureAirLog.out("============ keyCode" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_DEL){
            PictureAirLog.out("============ input2.getText().toString().length()" + input2.getText().toString().length());
            if (input2.getText().toString().length() == 0 && input2.requestFocus()){
                input1.setEnabled(true);
                input1.requestFocus();
            }else if(input3.getText().toString().length() == 0 && input3.requestFocus()){
                input2.setEnabled(true);
                input2.requestFocus();
            }else if(input4.getText().toString().length() == 0 && input4.requestFocus()){
                input3.setEnabled(true);
                input3.requestFocus();
            }
            if (input1.getText().toString().length() == 0 && input2.getText().toString().length() == 0 && input4.getText().toString().length() > 0 && input3.requestFocus()){
                input3.setEnabled(true);
                input3.requestFocus();
                input1.clearFocus();
                input2.clearFocus();
                input4.clearFocus();
            }
            if (input1.getText().toString().length() == 0 && input2.getText().toString().length() == 0 && input3.getText().toString().length() > 0 &&  input4.getText().toString().length() > 0 && input4.requestFocus()){
                input4.setEnabled(true);
                input4.requestFocus();
                input1.clearFocus();
                input2.clearFocus();
                input3.clearFocus();
                if (input4.getText().toString().length() == 0 && input3.getText().toString().length() > 0 && input1.getText().toString().length() == 0 && input2.getText().toString().length() == 0 ){
                    input3.setEnabled(true);
                    input3.requestFocus();
                    input1.clearFocus();
                    input2.clearFocus();
                    input4.clearFocus();
                }
            }
            if(input1.getText().toString().length() == 0 && input2.getText().toString().length() == 0 && input3.getText().toString().length() == 0 && input4.requestFocus()){
                input4.setEnabled(true);
                input4.requestFocus();
                input1.clearFocus();
                input2.clearFocus();
                input3.clearFocus();
            }
            if(input1.getText().toString().length() == 0 && input2.getText().toString().length() == 0 && input4.getText().toString().length() == 0 && input3.requestFocus()){
                input3.setEnabled(true);
                input3.requestFocus();
                input1.clearFocus();
                input2.clearFocus();
                input4.clearFocus();
            }
            if(input1.getText().toString().length() > 0 && input2.getText().toString().length() == 0 && input3.getText().toString().length() > 0 && input4.getText().toString().length() >= 0 && input3.requestFocus()){
                input3.setEnabled(true);
                input3.requestFocus();
                input1.clearFocus();
                input2.clearFocus();
                input4.clearFocus();
            }
            if(input1.getText().toString().length() > 0 && input2.getText().toString().length() == 0 && input3.getText().toString().length() > 0 && input4.getText().toString().length() > 0 && input4.requestFocus()){
                input4.setEnabled(true);
                input4.requestFocus();
                input1.clearFocus();
                input2.clearFocus();
                input3.clearFocus();
            }
            if(input1.getText().toString().length() > 0 && input2.getText().toString().length() == 0 && input3.getText().toString().length() == 0 && input4.getText().toString().length() > 0 && input4.requestFocus()){
                input4.setEnabled(true);
                input4.requestFocus();
                input1.clearFocus();
                input2.clearFocus();
                input3.clearFocus();
            }
            if(input1.getText().toString().length() > 0 && input2.getText().toString().length() > 0 && input3.getText().toString().length() == 0 && input4.getText().toString().length() > 0 && input4.requestFocus()){
                input4.setEnabled(true);
                input4.requestFocus();
                input1.clearFocus();
                input2.clearFocus();
                input3.clearFocus();
            }
            if(input1.getText().toString().length() == 0 && input2.getText().toString().length() > 0 && input3.getText().toString().length() == 0 && input4.getText().toString().length() > 0 && input4.requestFocus()){
                input4.setEnabled(true);
                input4.requestFocus();
                input1.clearFocus();
                input2.clearFocus();
                input3.clearFocus();
            }
            if(input1.getText().toString().length() == 0 && input2.getText().toString().length() == 0 && input3.getText().toString().length() == 0 && input4.getText().toString().length() == 0 && input3.requestFocus()){
                input1.setEnabled(true);
                input1.requestFocus();
                input2.clearFocus();
                input3.clearFocus();
                input4.clearFocus();
            }

            if(input1.getText().toString().length() == 0 && input2.getText().toString().length() == 0 && input3.getText().toString().length() > 0 && input4.getText().toString().length() > 0 && input3.requestFocus()){
                input3.setEnabled(true);
                input3.requestFocus();
                input1.clearFocus();
                input2.clearFocus();
                input4.clearFocus();
            }

        }
        return false;
    }

    private void hideInputMethodManager(View v) {
        // TODO Auto-generated method stub
        /*隐藏软键盘*/
        InputMethodManager imm = (InputMethodManager) v
                .getContext().getSystemService(
                        INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(
                    v.getApplicationWindowToken(), 0);
        }
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_confirm_scan_ppp_code:
            case R.id.sure:
                inputValue1 = input1.getText().toString();
                inputValue2 = input2.getText().toString();
                inputValue3 = input3.getText().toString();
                inputValue4 = input4.getText().toString();

                if ("".equals(inputValue1 + inputValue2 + inputValue3 + inputValue4)) {
                    //				Toast.makeText(InputCodeAct.this, R.string.nocontext, Common.TOAST_SHORT_TIME).show();
                    newToast.setTextAndShow(R.string.nocontext, Common.TOAST_SHORT_TIME);
                } else if (inputValue1.trim().length() + inputValue2.trim().length() + inputValue3.trim().length() + inputValue4.trim().length() != 16) {//长度不一致
                    PictureAirLog.out("=======> length" + (inputValue1.trim().length() + inputValue2.trim().length() + inputValue3.trim().length() + inputValue4.trim().length()));
                    newToast.setTextAndShow(R.string.wrong_length, Common.TOAST_SHORT_TIME);
                } else {
                    //如果有键盘显示，把键盘取消掉
                    hideInputMethodManager(v);
                    dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
                    PictureAirLog.out("code is --->" + input1.getText().toString());
//				dealCodeUtil.startDealCode("DPPPRU6CC7M5J6MM");
                    dealCodeUtil.startDealCode(inputValue1.trim().toUpperCase() + inputValue2.trim().toUpperCase() + inputValue3.trim().toUpperCase() + inputValue4.trim().toUpperCase());
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
