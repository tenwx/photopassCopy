package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.MyToast;

import cn.smssdk.gui.EditTextWithClear;

public class UpdateUserinfoActivity extends BaseActivity implements OnClickListener {
    private ImageView back;  // back btn
    private Button ibSave;   // save btn

    private TextView tvHeader;
    private EditTextWithClear etUserInfo;
    private int type;
    private MyToast myToast;

    private RelativeLayout bgUpdateInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_userinfo);
        init();
    }

    private void init() {
        bgUpdateInfo = (RelativeLayout) findViewById(R.id.bg_update_info);
        tvHeader = (TextView) findViewById(R.id.tvHeader);
        etUserInfo = (EditTextWithClear) findViewById(R.id.et_userinfo_text);
        myToast = new MyToast(this);

        //判断是从哪个页面跳转过来的。
        Intent intent = getIntent();
        type = intent.getIntExtra(Common.USERINFOTYPE, 0);
        switch (type) {
            case Common.NICKNAMETYPE:
                tvHeader.setText(R.string.nn);
                etUserInfo.setHint(R.string.hint_text_nickname);
                break;
            case Common.EMAILTYPE:
                tvHeader.setText(R.string.title_update_email);
                etUserInfo.setHint(R.string.hint_text_email);
                break;
            case Common.QQTYPE:
                tvHeader.setText(R.string.title_update_qq);
                etUserInfo.setHint(R.string.hint_text_qq);
                break;

            default:
                break;
        }
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        ibSave = (Button) findViewById(R.id.submit);
        ibSave.setOnClickListener(this);
        bgUpdateInfo.setOnClickListener(this);
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
            case R.id.back:
                this.finish();
                break;
            case R.id.submit:
                Intent mIntent;
                String userInfo = etUserInfo.getText().toString().trim();
                switch (type) {
                    case Common.NICKNAMETYPE:
                        if (TextUtils.isEmpty(etUserInfo.getText().toString().trim())) {
                            myToast.setTextAndShow(R.string.nick_name_empty, Common.TOAST_SHORT_TIME);
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

}
