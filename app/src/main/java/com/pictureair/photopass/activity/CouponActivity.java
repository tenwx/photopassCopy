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
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.CouponAdapter;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CouponTool;
import com.pictureair.photopass.util.DealCodeUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.CouponViewInterface;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PictureWorksDialog;

import java.util.ArrayList;
import java.util.List;

import com.pictureair.photopass.widget.CustomProgressDialog;

/**
 * 优惠卷view
 * bass
 */
public class CouponActivity extends BaseActivity implements CouponViewInterface {
    private final String TAG = "CouponActivity";

    private RecyclerView mRecyclerView;
    private LinearLayout llNoCoupon;
    private List<CouponInfo> mAllData;
    private List<CouponInfo> mSelectData;
    private ArrayList<String> mCouponCodeFromOrderPage;
    //    private CustomTextView mBtnSubmit, mBtnScan;
    private CustomProgressDialog customProgressDialog;
    private CouponAdapter couponAdapter;
    private Context context;
    private PWToast myToast;

    private CouponTool couponTool;
    private String whatPege = "";//是从什么页面进来的
    private PictureWorksDialog pictureWorksDialog;
    private DealCodeUtil dealCodeUtil;
    public static int PREVIEW_COUPON_CODE = 10000;
    private NoNetWorkOrNoCountView netWorkOrNoCountView;

    private Intent mOerderIntent = null;
    private CouponInfo newAddCoupon = null;//在订单页面进入优惠卷页面添加的新优惠卷

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        context = this;
        couponTool = new CouponTool(this);
        Intent intent = new Intent();
        intent.putExtra("type", "coupon");//只扫描coupon
        dealCodeUtil = new DealCodeUtil(this, intent, false, myHandler);
        initViews();
        mOerderIntent = getIntent();
        couponTool.getIntentActivity(mOerderIntent);
    }

    private void initViews() {
        myToast = new PWToast(context);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        setTopTitleShow("");
        setTopRightValueAndShow(R.drawable.add_righttop, true);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_coupon);
        llNoCoupon = (LinearLayout) findViewById(R.id.ll_no_coupon);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);

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
                    if (!data.isApplyThisProduct()) {
                        myToast.setTextAndShow(R.string.coupon_incalid, Common.TOAST_SHORT_TIME);
                        return;
                    }
                    if (data.getCpIsSelect()) {//取消选中
                        if (!mSelectData.isEmpty() && mSelectData.contains(data)) {
                            mSelectData.remove(data);
                        }
                        data.setCpIsSelect(false);
                        ((ImageView) view.findViewById(R.id.iv_select)).setImageResource(R.drawable.nosele);
                    } else {//选中
                        for (int i = 0; i < mAllData.size(); i++) {
                            mAllData.get(i).setCpIsSelect(false);
                        }
                        mSelectData.clear();
                        mSelectData.add(data);
                        data.setCpIsSelect(true);
                        couponAdapter.notifyDataSetChanged();
//                        ((ImageView) view.findViewById(R.id.iv_select)).setImageResource(R.drawable.sele);
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
                if (pictureWorksDialog == null) {
                    pictureWorksDialog = new PictureWorksDialog(context, null, null, getResources().getString(R.string.cancel1), getResources().getString(R.string.ok), false, R.layout.dialog_edittext, myHandler);
                }
                pictureWorksDialog.show();
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
            int availableCount = 0;
            for (int i = 0; i < mAllData.size(); i++) {
                if (mAllData.get(i).isApplyThisProduct()) {
                    availableCount++;
                }
            }
            PictureAirLog.out("array.toString()" + array.toString());
            intent.putExtra("couponCodes", array.toString());
            intent.putExtra("couponCount", availableCount);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case DialogInterface.BUTTON_POSITIVE://点击确定，添加code
                    if (msg.obj.toString().length() == 0) {
                        myToast.setTextAndShow(R.string.conpon_input_hint, Common.TOAST_SHORT_TIME);
                    } else {
                        showProgressBar();
                        dealCodeUtil.startDealCode(msg.obj.toString());
                    }
                    break;

                case DealCodeUtil.DEAL_CODE_FAILED:
                    goneProgressBar();
                    break;

                case DealCodeUtil.DEAL_CODE_SUCCESS:
                    Bundle bundle = (Bundle) msg.obj;
                    CouponInfo couponInfo = (CouponInfo) bundle.getSerializable("coupon");

                    if (couponInfo != null) {
                        if (whatPege.equals(CouponTool.ACTIVITY_ORDER)) {//从订单页面进行添加抵用劵
                            PictureAirLog.out("coupon----> add success （order page）");
                            newAddCoupon = couponInfo;
                            couponTool.getIntentActivity(mOerderIntent);//重新获取优惠卷
                        } else {//从me中进行添加抵用劵
                            PictureAirLog.out("coupon----> add success （me page）");
                            goneProgressBar();
                            PictureAirLog.out("coupon no null" + mAllData.size());
                            mAllData.add(0, couponInfo);
                            PictureAirLog.out("coupon no null" + mAllData.size());
                            sortCoupon(mAllData, false);
                        }
                    } else {
                        goneProgressBar();
                    }
                    break;
                case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
//                    if (null != refreshLayout && refreshLayout.isRefreshing()) {
//                        refreshLayout.setEnabled(true);
//                        refreshLayout.setRefreshing(false);
//                    }
                    //重新加载购物车数据
                    if (AppUtil.getNetWorkType(MyApplication.getInstance()) == 0) {
                        myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                        break;
                    }
                    couponTool.getIntentActivity(mOerderIntent);//重新获取优惠卷
                    netWorkOrNoCountView.setVisibility(View.GONE);
                    break;
                default:
                    break;

            }
            return false;
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        couponTool.onDestroyCouponTool();
        mAllData.clear();
        mAllData = null;
        mSelectData.clear();
        mSelectData = null;
        mCouponCodeFromOrderPage = null;
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
    public void sortCoupon(List<CouponInfo> sortDatas, boolean needClear) {
        PictureAirLog.out("start sort coupon" + sortDatas.size());
        couponAdapter.setPage(whatPege);//设置显示界面
        mRecyclerView.setVisibility(View.VISIBLE);
        llNoCoupon.setVisibility(View.GONE);

        if (whatPege.equals(CouponTool.ACTIVITY_ORDER)) {
            if (null != mCouponCodeFromOrderPage) {
                for (int i = 0; i < mCouponCodeFromOrderPage.size(); i++) {
                    for (int j = 0; j < sortDatas.size(); j++) {
                        if (sortDatas.get(j).getCpCode().equals(mCouponCodeFromOrderPage.get(i))) {
                            sortDatas.get(j).setCpIsSelect(true);
                            if (!mSelectData.contains(sortDatas.get(j))) {
                                mSelectData.add(sortDatas.get(j));
                            }
                        }
                    }
                }
            }
            if (null != newAddCoupon) {//订单页面中新添加的优惠卷
                for (int k = 0; k < sortDatas.size(); k++) {
                    if (newAddCoupon.getCpCode().equals(sortDatas.get(k).getCpCode())) {
                        sortDatas.remove(k);
                        newAddCoupon.setApplyThisProduct(true);//适用
                        break;
                    } else {
                        newAddCoupon.setApplyThisProduct(false);//不适用
                    }
                }
                sortDatas.add(0, newAddCoupon);
            }
        }
        if (needClear) {
            mAllData.clear();
            mAllData.addAll(sortDatas);
        }
        couponAdapter.notifyDataSetChanged();
    }

    @Override
    public String getCouponCode() {
        return "";
    }

    @Override
    public void noCoupon() {
        if (whatPege.equals(couponTool.ACTIVITY_ORDER) && null != newAddCoupon) {
            couponAdapter.setPage(whatPege);//设置显示界面
            newAddCoupon.setApplyThisProduct(false);//不适用
            mAllData.clear();
            mAllData.add(newAddCoupon);
            mRecyclerView.setVisibility(View.VISIBLE);
            llNoCoupon.setVisibility(View.GONE);
            couponAdapter.notifyDataSetChanged();
            return;
        }
        mRecyclerView.setVisibility(View.GONE);
        llNoCoupon.setVisibility(View.VISIBLE);
    }

    @Override
    public void noNetwork() {
        netWorkOrNoCountView.setVisibility(View.VISIBLE);
        netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, myHandler, true);
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
        if (whatPege.equals(couponTool.ACTIVITY_ME)) {//me页面
            setTopTitleShow(R.string.my_coupon);
        } else if (whatPege.equals(couponTool.ACTIVITY_ORDER)) {//订单页面
            setTopTitleShow(R.string.select_cpupon);
        } else {
        }
    }

    @Override
    public void showCouponFromOrderPage(JSONArray jsonArray) {
        mCouponCodeFromOrderPage = null;
        if (null != jsonArray && !jsonArray.toString().equals("")) {
            mCouponCodeFromOrderPage = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                mCouponCodeFromOrderPage.add(jsonArray.get(i).toString());
            }
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
