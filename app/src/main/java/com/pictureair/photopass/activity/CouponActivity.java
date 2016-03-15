package com.pictureair.photopass.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.CouponAdapter;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CouponTool;
import com.pictureair.photopass.widget.CouponViewInterface;
import com.pictureair.photopass.widget.MyToast;

import java.util.ArrayList;
import java.util.List;

import cn.smssdk.gui.CustomProgressDialog;
import cn.smssdk.gui.EditTextWithClear;

/**
 * 优惠卷view
 * bass
 */
public class CouponActivity extends BaseActivity implements CouponViewInterface, View.OnClickListener {
    private final String TAG = "CouponActivity";

    private RecyclerView mRecyclerView;
    private List<CouponInfo> mAllData;
    private List<CouponInfo> mSelectData;
    private EditTextWithClear mEditTextWithClear;
    private Button mBtnSubmit, mBtnScan;
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
        setTopTitleShow(R.string.my_coupon);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_coupon);
        mEditTextWithClear = (EditTextWithClear) findViewById(R.id.et_userinfo_text);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);
        mBtnScan = (Button) findViewById(R.id.btn_scan);
        mBtnSubmit.setOnClickListener(this);
        mBtnScan.setOnClickListener(this);

        mSelectData = new ArrayList<>();
        mAllData = new ArrayList<>();
        couponAdapter = new CouponAdapter(context, mAllData);
        mRecyclerView.setAdapter(couponAdapter);
        listview();
        couponAdapter.setOnItemClickListener(new CouponAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, CouponInfo data) {
                if (whatPege.equals(CouponTool.ACTIVITY_ORDER)) {//当订单页面进来 ，取消注释
                    if (data.getCpIsSelect()) {//取消选中
                        if (!mSelectData.isEmpty() && mSelectData.contains(data)) {
                            mSelectData.remove(data);
                        }
                        data.setCpIsSelect(false);
                        (view.findViewById(R.id.iv_select)).setVisibility(View.INVISIBLE);
                    } else {//选中
                        mSelectData.add(data);
                        data.setCpIsSelect(true);
                        (view.findViewById(R.id.iv_select)).setVisibility(View.VISIBLE);
                    }
//                    myToast.setTextAndShow(data.getCpId()+"",Common.TOAST_SHORT_TIME);
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
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        myToast.setTextAndShow(mSelectData.size() + "", Common.TOAST_SHORT_TIME);
        if (whatPege.equals(CouponTool.ACTIVITY_ME)) {
        } else if (whatPege.equals(CouponTool.ACTIVITY_ORDER)) {//返回到订单页面 ，给jsonArray的优惠code
            Intent intent = new Intent();
            JSONArray array = new JSONArray();
            for (int i =0; i < mSelectData.size(); i++){
                array.add(mSelectData.get(i));
            }
            intent.putExtra(couponTool.ACTIVITY_ORDER_CART_DATAS,array);
        }
        finish();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                couponTool.insertCoupon(getCouponCode());
                break;

            case R.id.btn_scan:
                //进入扫描页面
                //扫到码之后调用 couponTool.insertCoupon(优惠卷码);
                myToast.setTextAndShow("扫描页面", Common.TOAST_SHORT_TIME);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        couponTool.onDestroyCouponTool();
        mSelectData.clear();
        mSelectData = null;
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
        return mEditTextWithClear.getText().toString();
    }

    @Override
    public void noCoupon() {
        mRecyclerView.setVisibility(View.GONE);
        myToast.setTextAndShow("无优惠卷", Common.TOAST_SHORT_TIME);
    }

    @Override
    public void noNetwork() {
        myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
    }

    @Override
    public void fail(String str) {
        myToast.setTextAndShow("处理失败：" + str, Common.TOAST_SHORT_TIME);

    }

    @Override
    public void fail(int id) {
        myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
    }

    @Override
    public void getWhatPege(String whatPege) {
        this.whatPege = whatPege;
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
