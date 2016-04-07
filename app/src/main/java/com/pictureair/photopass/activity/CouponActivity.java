package com.pictureair.photopass.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONArray;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.CouponAdapter;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CouponTool;
import com.pictureair.photopass.widget.CouponViewInterface;
import com.pictureair.photopass.widget.CustomTextView;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.PictureWorksDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.smssdk.gui.CustomProgressDialog;

/**
 * 优惠卷view
 * bass
 */
public class CouponActivity extends BaseActivity implements CouponViewInterface{
    private final String TAG = "CouponActivity";

    private RecyclerView mRecyclerView;
    private LinearLayout llNoCoupon;
    private List<CouponInfo> mAllData;
    private List<CouponInfo> mSelectData;
//    private CustomTextView mBtnSubmit, mBtnScan;
    private CustomProgressDialog customProgressDialog;
    private CouponAdapter couponAdapter;
    private Context context;
    private MyToast myToast;

    private CouponTool couponTool;
    private String whatPege = "";//是从什么页面进来的

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        context = this;
        couponTool = new CouponTool(this);
        initViews();
        couponTool.getIntentActivity(getIntent());
    }

    private void initViews() {
        myToast = new MyToast(context);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        setTopTitleShow("");
        setTopRightValueAndShow(R.drawable.coupon_add2, true);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_coupon);
//        mBtnSubmit = (CustomTextView) findViewById(R.id.btn_submit);
//        mBtnScan = (CustomTextView) findViewById(R.id.btn_scan);
//        mBtnSubmit.setOnClickListener(this);
//        mBtnScan.setOnClickListener(this);
        llNoCoupon = (LinearLayout) findViewById(R.id.ll_no_coupon);

        mSelectData = new ArrayList<>();
        mAllData = new ArrayList<>();
        couponAdapter = new CouponAdapter(context, mAllData);
        mRecyclerView.setAdapter(couponAdapter);
        listview();
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        couponAdapter.setOnItemClickListener(new CouponAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, CouponInfo data) {
                if (whatPege.equals(CouponTool.ACTIVITY_ORDER) && data.getCpStatus().equals("active")) {//当订单页面进来 ，状态为可使用，取消注释
                    if (data.getCpIsSelect()) {//取消选中
                        if (!mSelectData.isEmpty() && mSelectData.contains(data)) {
                            mSelectData.remove(data);
                        }
                        data.setCpIsSelect(false);
                        ((ImageView)view.findViewById(R.id.iv_select)).setImageResource(R.drawable.nosele);
                    } else {//选中
                            mSelectData.add(data);
                            data.setCpIsSelect(true);
                            ((ImageView) view.findViewById(R.id.iv_select)).setImageResource(R.drawable.sele);
                    }
                }
            }
        });
    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                onBackPressed();
                break;
            case R.id.topRightView:
                new PictureWorksDialog(context,null,null,getResources().getString(R.string.cancel1),getResources().getString(R.string.ok),false,R.layout.dialog_edittext,myHandler).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        myToast.setTextAndShow(mSelectData.size() + "", Common.TOAST_SHORT_TIME);
        if (whatPege.equals(CouponTool.ACTIVITY_ME)) {
        } else if (whatPege.equals(CouponTool.ACTIVITY_ORDER)) {//返回到订单页面 ，给jsonArray的优惠code
            Intent intent = new Intent();
            JSONArray array = new JSONArray();
            for (int i = 0; i < mSelectData.size(); i++) {
                array.add(mSelectData.get(i).getCpCode());
            }
            intent.putExtra("couponCodes", array.toString());
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private final Handler myHandler = new MyHandler(this);


    private static class MyHandler extends Handler {
        private final WeakReference<CouponActivity> mActivity;

        public MyHandler(CouponActivity activity) {
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


    private void dealHandler(Message msg) {
        switch (msg.what){
            case DialogInterface.BUTTON_POSITIVE:
                couponTool.insertCoupon(""+msg.obj);
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        couponTool.onDestroyCouponTool();
        mSelectData.clear();
        mSelectData = null;
        myHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void showProgressBar() {
        if (null == customProgressDialog) {
            customProgressDialog = CustomProgressDialog.show(context, context.getString(R.string.is_loading), false, null);
        } else {
            if (!customProgressDialog.isShowing()) {
                customProgressDialog.show();
            }
        }
    }

    @Override
    public void goneProgressBar() {
        if (null != customProgressDialog && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
            customProgressDialog = null;
        }
    }

    @Override
    public void sortCoupon(List<CouponInfo> sortDatas) {
        couponAdapter.setPage(whatPege);//设置显示界面
        mRecyclerView.setVisibility(View.VISIBLE);
        llNoCoupon.setVisibility(View.GONE);
        if (couponAdapter == null) {
            couponAdapter = new CouponAdapter(context, sortDatas);
            mRecyclerView.setAdapter(couponAdapter);
            listview();
        } else {
            couponAdapter.setDatas(sortDatas);
            couponAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public String getCouponCode() {
        return "";
    }

    @Override
    public void noCoupon() {
        mRecyclerView.setVisibility(View.GONE);
        llNoCoupon.setVisibility(View.VISIBLE);
    }

    @Override
    public void noNetwork() {
        myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
    }

    @Override
    public void fail(String str) {
        myToast.setTextAndShow(str, Common.TOAST_SHORT_TIME);

    }

    @Override
    public void fail(int id) {
        myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
    }

    @Override
    public void getWhatPege(String whatPege) {
        this.whatPege = whatPege;
        if(whatPege.equals(couponTool.ACTIVITY_ME)){//me页面
            setTopTitleShow(R.string.my_coupon);
        }
        else if (whatPege.equals(couponTool.ACTIVITY_ORDER)){//订单页面
            setTopTitleShow(R.string.select_cpupon);
        }else{
        }
    }

    /**
     * ListView样式
     */
    private void listview() {
        //设置RecycerView的布局管理
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }
}
