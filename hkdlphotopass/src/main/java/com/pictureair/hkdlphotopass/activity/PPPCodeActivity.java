package com.pictureair.hkdlphotopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.MyApplication;
import com.pictureair.hkdlphotopass.R;
import com.pictureworks.android.eventbus.ScanInfoEvent;
import com.pictureworks.android.util.API1;
import com.pictureworks.android.util.AppManager;
import com.pictureworks.android.util.AppUtil;
import com.pictureworks.android.util.Common;
import com.pictureworks.android.util.DealCodeUtil;
import com.pictureworks.android.util.PictureAirLog;
import com.pictureworks.android.widget.CustomProgressDialog;
import com.pictureworks.android.widget.MyToast;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;

/**
 * Created by Eric on 16/3/29.
 */
public class PPPCodeActivity extends BaseActivity implements View.OnClickListener, View.OnKeyListener {

    private String[] resultList;
    private TextView tvConfirmHint, tvManulInputIntro;

    private ImageView ivShowResult;
    private Button btnConfirmScanPppCode, btnReScanPppCode;
    private LinearLayout lvButtom;

    private Button ok;
    private EditText input1, input2, input3, input4;
    private SharedPreferences sp;
    private MyToast newToast;
    private String inputValue1, inputValue2, inputValue3, inputValue4;
    private DealCodeUtil dealCodeUtil;

    private CustomProgressDialog dialog;

    private LinearLayout comfirmPPPLayout;

    private final Handler pppCodeHandler = new PPPCodeHandler(this);

    /**
     * 统计已输入条码的个数
     */
    private int[] codeCount;


    private static class PPPCodeHandler extends Handler {
        private final WeakReference<PPPCodeActivity> mActivity;

        public PPPCodeHandler(PPPCodeActivity activity) {
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
                        intent2.setClass(PPPCodeActivity.this, MyPPPActivity.class);
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
        setContentView(R.layout.activity_ppp_code);
        newToast = new MyToast(this);
        initview();

    }

    private void initview() {
        tvConfirmHint = (TextView) findViewById(R.id.tv_confirm_hint);
        ivShowResult = (ImageView) findViewById(R.id.iv_show_result);
        btnConfirmScanPppCode = (Button) findViewById(R.id.btn_confirm_scan_ppp_code);
        btnConfirmScanPppCode.setOnClickListener(this);
        btnReScanPppCode = (Button) findViewById(R.id.btn_re_scan_ppp_code);
        btnReScanPppCode.setOnClickListener(this);
        lvButtom = (LinearLayout) findViewById(R.id.lv_buttom);

        sp = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);

        input1 = (EditText) findViewById(R.id.input1);
        input2 = (EditText) findViewById(R.id.input2);
        input3 = (EditText) findViewById(R.id.input3);
        input4 = (EditText) findViewById(R.id.input4);

        comfirmPPPLayout = (LinearLayout) findViewById(R.id.comfirm_ppp_layout);

        setTopLeftValueAndShow(R.drawable.back_white, true);

        codeCount = new int[]{0, 0, 0, 0};


        ivShowResult.setImageBitmap(MipCaptureActivity.tempBitmap);
        MipCaptureActivity.tempBitmap = null;
        setTopTitleShow(R.string.scan_ppp_code);
        resultList = getIntent().getStringExtra("text").split("-");
        input1.setText(resultList[0]);
        input2.setText(resultList[1]);
        input3.setText(resultList[2]);
        input4.setText(resultList[3]);
        codeCount[0] = resultList[0].length();
        codeCount[1] = resultList[1].length();
        codeCount[2] = resultList[2].length();
        codeCount[3] = resultList[3].length();

        input1.addTextChangedListener(new TextWatcher() {

                                          @Override
                                          public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                                          }

                                          @Override
                                          public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                                          }

                                          @Override
                                          public void afterTextChanged(Editable arg0) {
                                              codeCount[0] = arg0.length();
                                              if (!AppUtil.isInputCodeEditing(codeCount, input1.getSelectionStart(), 0)) {//非编辑
                                                  if (AppUtil.inputCodeEditJump(input1.getSelectionStart(), 0) == 1) {//往后
                                                      input2.setEnabled(true);
                                                      input2.requestFocus();
                                                      input1.clearFocus();
                                                  }
                                              }
                                          }
                                      }

        );

        input2.addTextChangedListener(new TextWatcher() {

                                          @Override
                                          public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                                          }

                                          @Override
                                          public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                                          }

                                          @Override
                                          public void afterTextChanged(Editable arg0) {
                                              codeCount[1] = arg0.length();
                                              if (!AppUtil.isInputCodeEditing(codeCount, input2.getSelectionStart(), 1)) {//非编辑
                                                  if (AppUtil.inputCodeEditJump(input2.getSelectionStart(), 1) == 1) {//往后
                                                      input3.setEnabled(true);
                                                      input3.requestFocus();
                                                      input2.clearFocus();
                                                  } else if (AppUtil.inputCodeEditJump(input2.getSelectionStart(), 1) == -1) {//往前
                                                      input1.setEnabled(true);
                                                      input1.requestFocus();
                                                      input1.setSelection(4);
                                                      input2.clearFocus();
                                                  }
                                              }
                                          }
                                      }

        );

        input3.addTextChangedListener(new TextWatcher() {

                                          @Override
                                          public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                                          }

                                          @Override
                                          public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                                          }

                                          @Override
                                          public void afterTextChanged(Editable arg0) {
                                              codeCount[2] = arg0.length();
                                              if (!AppUtil.isInputCodeEditing(codeCount, input3.getSelectionStart(), 2)) {//非编辑
                                                  if (AppUtil.inputCodeEditJump(input3.getSelectionStart(), 2) == 1) {//往后
                                                      input4.setEnabled(true);
                                                      input4.requestFocus();
                                                      input3.clearFocus();
                                                  } else if (AppUtil.inputCodeEditJump(input3.getSelectionStart(), 2) == -1) {//往前
                                                      input2.setEnabled(true);
                                                      input2.requestFocus();
                                                      input2.setSelection(4);
                                                      input3.clearFocus();
                                                  }
                                              }
                                          }
                                      }

        );

        input4.addTextChangedListener(new TextWatcher() {
                                          @Override
                                          public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                                          }

                                          @Override
                                          public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                                          }

                                          @Override
                                          public void afterTextChanged(Editable arg0) {
                                              codeCount[3] = arg0.length();
                                              if (!AppUtil.isInputCodeEditing(codeCount, input4.getSelectionStart(), 3)) {//非编辑
                                                  if (AppUtil.inputCodeEditJump(input4.getSelectionStart(), 3) == -1) {//往前
                                                      input3.setEnabled(true);
                                                      input3.requestFocus();
                                                      input3.setSelection(4);
                                                      input4.clearFocus();
                                                  }
                                              }
                                          }
                                      }

        );


        dealCodeUtil = new DealCodeUtil(this, getIntent(), true, MyApplication.getInstance().getLanguageType(), pppCodeHandler);

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        PictureAirLog.out("keycode-->" + keyCode);
        /**
         * 处理光标处于输入框最前面的时候的回删操作
         */
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            switch (v.getId()) {
                case R.id.input2:
                    if (!AppUtil.isInputCodeEditing(codeCount, 0, 1)) {
                        input1.setEnabled(true);
                        input1.requestFocus();
                        input1.setText(input1.getText().toString().substring(0, 3));
                        input1.setSelection(3);
                    }
                    break;

                case R.id.input3:
                    if (!AppUtil.isInputCodeEditing(codeCount, 0, 2)) {
                        input2.setEnabled(true);
                        input2.requestFocus();
                        input2.setText(input2.getText().toString().substring(0, 3));
                        input2.setSelection(3);
                    }
                    break;

                case R.id.input4:
                    if (!AppUtil.isInputCodeEditing(codeCount, 0, 3)) {
                        input3.setEnabled(true);
                        input3.requestFocus();
                        input3.setText(input3.getText().toString().substring(0, 3));
                        input3.setSelection(3);
                    }
                    break;

                default:
                    break;
            }
        }
        return false;
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
            case R.id.btn_confirm_scan_ppp_code://ppp确认页面的确定按钮
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
                    dealCodeUtil.startDealCode(inputValue1.trim().toUpperCase() + inputValue2.trim().toUpperCase() + inputValue3.trim().toUpperCase() + inputValue4.trim().toUpperCase(),
                            Common.APP_TYPE_HKDLPP);
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
        pppCodeHandler.removeCallbacksAndMessages(null);
    }
}
