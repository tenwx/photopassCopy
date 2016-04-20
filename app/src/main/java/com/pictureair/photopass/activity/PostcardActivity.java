package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.MyToast;

import java.lang.ref.WeakReference;


/**
 * Created by Eric on 16/4/15.
 */
public class PostcardActivity extends BaseActivity implements View.OnClickListener {

    private final static String TAG = "PostcardActivity";
    //申明控件
    private ImageView ivCart;
    private TextView tvCartCount;
    private Button btnPreview;

    //申明实例类
    private SharedPreferences sharedPreferences;
    private Editor editor;
    private MyToast myToast;

    //申明变量
    private int recordcount = 0; //记录数据库中有几条记录

    private final Handler detailPostcardHandler = new DetailPostcardHandler(this);


    private static class DetailPostcardHandler extends Handler{
        private final WeakReference<PostcardActivity> mActivity;

        public DetailPostcardHandler(PostcardActivity activity){
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
            case API1.ADD_TO_CART_SUCCESS:
                editor = sharedPreferences.edit();
                editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0) + 1);
                editor.commit();
                break;

            case API1.ADD_TO_CART_FAILED:
                myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postcard);
        initview();

    }

    private void initview() {

        //加载控件
        ivCart = (ImageView) findViewById(R.id.button_cart);
        tvCartCount = (TextView)findViewById(R.id.textview_cart_count);
        btnPreview = (Button) findViewById(R.id.btn_preview_goods);

        //绑定监听
        tvCartCount.setOnClickListener(this);
        ivCart.setOnClickListener(this);
        btnPreview.setOnClickListener(this);

        //初始化数据
        myToast = new MyToast(this);
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.textview_cart_count:
            case R.id.button_cart:
                if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                    myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                intent = new Intent(PostcardActivity.this, CartActivity.class);
                PostcardActivity.this.startActivity(intent);

                break;

            case  R.id.btn_preview_goods:


            default:
                break;
        }
    }

    /**
     * 更新购物车数量
     */
    private void updateCartCount() {
        // TODO Auto-generated method stub
        recordcount = sharedPreferences.getInt(Common.CART_COUNT, 0);
        if (recordcount <= 0) {
            tvCartCount.setVisibility(View.INVISIBLE);
        } else {
            tvCartCount.setVisibility(View.VISIBLE);
            tvCartCount.setText(recordcount + "");
        }
    }
}
