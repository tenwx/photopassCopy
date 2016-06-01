package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.MyToast;

import com.pictureair.photopass.widget.EditTextWithClear;

public class UpdateUserinfoActivity extends BaseActivity implements OnClickListener {
    private RelativeLayout bgUpdateInfo;
    private Button ibSave;   // save btn
    private EditTextWithClear etUserInfo;
    private int type;
    private MyToast myToast;
    private String nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_userinfo);
        init();
    }

    private void init() {
        setTopLeftValueAndShow(R.drawable.back_white, true);
        etUserInfo = (EditTextWithClear) findViewById(R.id.et_userinfo_text);
        bgUpdateInfo = (RelativeLayout) findViewById(R.id.bg_update_info);
        myToast = new MyToast(this);
        //判断是从哪个页面跳转过来的。
        Intent intent = getIntent();
        type = intent.getIntExtra(Common.USERINFOTYPE, 0);
        switch (type) {
            case Common.NICKNAMETYPE:
                setTopTitleShow(R.string.nn);
                nickName = intent.getStringExtra("nickname");
                etUserInfo.setHint(R.string.nn);
                if (!nickName.equals("")) {
                    etUserInfo.setText(nickName);
                }
                break;
            default:
                break;
        }
        ibSave = (Button) findViewById(R.id.submit);
        ibSave.setTypeface(MyApplication.getInstance().getFontBold());
        ibSave.setOnClickListener(this);
        bgUpdateInfo.setOnClickListener(this);
        etUserInfo.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int cou = 0;
                cou = before + count;
                String editable = etUserInfo.getText().toString();
                String str = AppUtil.inputTextFilter(editable); //过滤特殊字符
                if (!editable.equals(str)) {
                    etUserInfo.setText(str);
                }
                etUserInfo.setSelection(etUserInfo.length());
                cou = etUserInfo.length();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void hideInputMethodManager(View v) {
        /* 隐藏软键盘 */
        InputMethodManager imm = (InputMethodManager) v.getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.bg_update_info:
                hideInputMethodManager(v);
                break;
            case R.id.submit:
                Intent mIntent;
                String userInfo = etUserInfo.getText().toString().trim();
                switch (type) {
                    case Common.NICKNAMETYPE:
                        if (TextUtils.isEmpty(etUserInfo.getText().toString().trim())) {
                            myToast.setTextAndShow(R.string.name_is_empty, Common.TOAST_SHORT_TIME);
                            return;
                        }
                        mIntent = new Intent();
                        mIntent.putExtra("nickName", userInfo);
                        this.setResult(1, mIntent);
                        break;
                    case Common.QQTYPE:
                        if (TextUtils.isEmpty(etUserInfo.getText().toString().trim())) {

                            return;
                        }
                        mIntent = new Intent();
                        mIntent.putExtra("qq", userInfo);
                        this.setResult(2, mIntent);
                        break;

                    default:
                        break;
                }
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

}
