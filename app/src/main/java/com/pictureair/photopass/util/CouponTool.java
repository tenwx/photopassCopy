package com.pictureair.photopass.util;

import android.content.Intent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.widget.CouponViewInterface;
import com.trello.rxlifecycle.components.RxActivity;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

/**
 * 此类负责与Coupon的UI对接与数据逻辑
 * Created by bass on 16/3/11.
 *
 * 需求改动：
 * 2016/4/7 从me中进入（获取抵用劵访问lisa接口，字段与之前不一样）
 */

public class CouponTool {
    private CouponViewInterface couponView;
    private List<CouponInfo> mDatas;
    public static final String ACTIVITY_ME = "activity_me";
    public static final String ACTIVITY_ORDER = "activity_order";
    public static final String ACTIVITY_ORDER_CART_DATAS = "activity_order_cart_datas";//从订单页面传来的 cartItemIds:array<string>,用户选中的购物项
    private JSONArray cartItemIds = null;
    private String whatPege = "";//是从什么页面进来的

    public CouponTool(CouponViewInterface couponView) {
        this.couponView = couponView;
    }

    /**
     * 判断是从什么页面进来的
     */
    public void getIntentActivity(Intent getIntent) {
        if (null == getIntent) {
            return;
        }
        if (!getTokenID()) {
            return;
        }
        if (getIntent.getExtras().getString(ACTIVITY_ME, "").equals(ACTIVITY_ME)) {
            //从me中进入
            //根据tokenID
            whatPege = ACTIVITY_ME;
            couponView.getWhatPege(ACTIVITY_ME);
            if (!getNetwork()) {//无网络
                couponView.noNetwork();
                return;
            }
            queryCouponMePage();
        } else if (getIntent.getExtras().getString(ACTIVITY_ORDER, "").equals(ACTIVITY_ORDER)) {//从订单页面进来的
            whatPege = ACTIVITY_ORDER;
            couponView.getWhatPege(ACTIVITY_ORDER);
            cartItemIds = JSONArray.parseArray(getIntent.getExtras().getString(ACTIVITY_ORDER_CART_DATAS));
            couponView.showCouponFromOrderPage(JSONArray.parseArray(getIntent.getExtras().getString("couponCodes")));
            if (null == cartItemIds) {
                return;
            }
            if (!getNetwork()) {//无网络
                couponView.noNetwork();
                return;
            }
            queryCouponOrderPage(cartItemIds);
        }
    }

    /**
     * 从订单页面进
     */
    public void queryCouponOrderPage(JSONArray cartItemIds) {
        couponView.showProgressBar();
        getCounpon(cartItemIds);
    }

    /**
     * 从Me界面进入
     * 查询当前用户的所有优惠卷
     * 也可作为刷新数据用
     * 1. 需要TokenId
     * LISA接口
     */
    public void queryCouponMePage() {
        couponView.showProgressBar();
        getCounpon(null);
    }

    /**
     * 获取抵用券
     * @param cartItemIds
     */
    private void getCounpon(JSONArray cartItemIds) {
        API2.getCartItemCoupons(cartItemIds)
                .compose(((RxActivity) couponView).<JSONObject>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        getApiReturnDatas(jsonObject);
                    }

                    @Override
                    public void _onError(int status) {
                        if (couponView != null) {
                            couponView.goneProgressBar();
                            couponView.noNetwork();
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 拿到API查到的数据
     * 1. 拿到json
     * 2. 解析到实体类中
     * 3. 排序
     * 4. 交给view
     */
    private void getApiReturnDatas(JSONObject jsonObject) {
        mDatas = null;
        if (whatPege.equals(ACTIVITY_ME)){//从me中进来的
            mDatas = JsonUtil.getCouponListFromJson2(jsonObject);
        }else{//从订单进来
            mDatas = JsonUtil.getCouponListFromJson(jsonObject);
        }
        couponView.goneProgressBar();
        if (null == mDatas || mDatas.size() <= 0) {
            couponView.noCoupon();//无优惠卷
        } else {
            couponView.sortCoupon(mDatas, true);
        }
    }

    /**
     * 读取tokend
     */
    private boolean getTokenID() {
        if (MyApplication.getTokenId() == null) {
            couponView.fail("get token fail");
            return false;
        }
        return true;
    }

    /**
     * 是否有网络
     */
    private boolean getNetwork() {
        return AppUtil.NETWORKTYPE_INVALID != AppUtil.getNetWorkType(MyApplication.getInstance().getApplicationContext());
    }

    /**
     * 结束后，回收
     */
    public void onDestroyCouponTool() {
        cartItemIds = null;
        whatPege = "";
        mDatas = null;
        couponView = null;
    }

}
