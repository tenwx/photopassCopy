package com.pictureair.photopass.activity;

import android.content.Context;
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
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.ScanInfoEvent;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CouponTool;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.CouponViewInterface;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PWToast;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * 优惠券view
 * bass
 */
public class CouponActivity extends BaseActivity implements CouponViewInterface{
    private RecyclerView mRecyclerView;
    private LinearLayout llNoCoupon;
    private List<CouponInfo> mAllData;
    private List<CouponInfo> mSelectData;
    private ArrayList<String> mCouponCodeFromOrderPage;
    private CouponAdapter couponAdapter;
    private Context context;
    private PWToast myToast;

    private CouponTool couponTool;
    private String whatPege = "";//是从什么页面进来的
    private NoNetWorkOrNoCountView netWorkOrNoCountView;

    private Intent mOerderIntent = null;
    private CouponInfo newAddCoupon = null;//在订单页面进入优惠卷页面添加的新优惠卷

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        context = this;
        couponTool = new CouponTool(this);
        initViews();
        mOerderIntent = getIntent();
        couponTool.getIntentActivity(mOerderIntent);
    }

    private void initViews() {
        myToast = new PWToast(context);
        setTopLeftValueAndShow(R.drawable.back_blue, true);
        setTopTitleShow("");
        setTopRightValueAndShow(R.drawable.scan_blue, true);
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
                Intent intent = new Intent(CouponActivity.this, MipCaptureActivity.class);
                intent.putExtra("type", "coupon");//只扫描pp
                startActivity(intent);
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
                case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
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
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
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
        showPWProgressDialog();
    }

    @Override
    public void goneProgressBar() {
        dismissPWProgressDialog();
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
        if (whatPege.equals(CouponTool.ACTIVITY_ORDER) && null != newAddCoupon) {
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
        if (whatPege.equals(CouponTool.ACTIVITY_ME)) {//me页面
            setTopTitleShow(R.string.my_coupon);
        } else if (whatPege.equals(CouponTool.ACTIVITY_ORDER)) {//订单页面
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

    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        if (baseBusEvent instanceof ScanInfoEvent) {
            ScanInfoEvent scanInfoEvent = (ScanInfoEvent) baseBusEvent;
            CouponInfo couponInfo = scanInfoEvent.getCouponInfo();

            if (couponInfo != null) {
                if (whatPege.equals(CouponTool.ACTIVITY_ORDER)) {//从订单页面进行添加抵用劵
                    PictureAirLog.out("coupon----> add success （order page）");
                    newAddCoupon = couponInfo;
                    couponTool.getIntentActivity(mOerderIntent);//重新获取优惠卷
                } else {
                    PictureAirLog.out("coupon----> add success （from scan page）");
                    PictureAirLog.out("coupon no null" + mAllData.size());
                    mAllData.add(0, couponInfo);
                    PictureAirLog.out("coupon no null" + mAllData.size());
                    sortCoupon(mAllData, false);
                }
            }

            EventBus.getDefault().removeStickyEvent(scanInfoEvent);
        }
    }
}
