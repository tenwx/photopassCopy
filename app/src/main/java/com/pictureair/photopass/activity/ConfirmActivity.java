package com.pictureair.photopass.activity;

import android.os.Bundle;
import android.view.View;

import com.pictureair.photopass.R;

public class ConfirmActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comfirm);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        setTopTitleShow(R.string.auto);
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
