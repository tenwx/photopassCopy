package com.pictureair.photopass.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;


public class ConfirmActivity extends BaseActivity {
    private TextView tvConfirmHint;
    private ImageView ivShowResult;
    String[] resultList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comfirm);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        setTopTitleShow(R.string.confirm_ppp_code_title);
        getIntent().getStringExtra("text");
        tvConfirmHint = (TextView) findViewById(R.id.tv_confirm_hint);
        String text = getIntent().getStringExtra("text");
        resultList = text.split("-");  //获取数组

        ivShowResult = (ImageView) findViewById(R.id.iv_show_result);
        ivShowResult.setImageBitmap(MipCaptureActivity.tempBitmap);
        MipCaptureActivity.tempBitmap = null;

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
