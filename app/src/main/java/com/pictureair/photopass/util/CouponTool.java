package com.pictureair.photopass.util;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.widget.CouponViewInterface;

import java.util.List;

/**
 * 此类负责与Coupon的UI对接与数据逻辑
 * Created by bass on 16/3/11.
 *
 * 需求改动：
 * 2016/4/7 从me中进入（获取抵用劵访问lisa接口，字段与之前不一样）
 */
public class CouponTool {
    private static final String TAG = "CouponTool";
    private CouponViewInterface couponView;
    private List<CouponInfo> mDatas;
    public static final String ACTIVITY_ME = "activity_me";
    public static final String ACTIVITY_ORDER = "activity_order";
    public static final String ACTIVITY_ORDER_CART_DATAS = "activity_order_cart_datas";//从订单页面传来的 cartItemIds:array<string>,用户选中的购物项
    private JSONArray cartItemIds = null;
    private String whatPege = "";//是从什么页面进来的

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case API1.GET_COUPON_FAILED://获取所有优惠卷失败
                case API1.INSERT_COUPON_FAILED://添加一张优惠卷失败
                    int id = ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1);
                    if (couponView != null) {
                        couponView.goneProgressBar();
                        couponView.fail(id);
                    }
                    break;

                case API1.GET_COUPON_SUCCESS://从订单页面获取所有优惠卷成功
                    getApiReturnDatas((JSONObject) msg.obj);
                    break;

                case API1.INSERT_COUPON_SUCCESS://添加一张优惠卷成功
                    insertCouponSuccess((JSONObject) msg.obj);
                    break;

                default:
                    break;
            }
            return false;
        }
    });


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
        } else {
            return;
        }
    }

    /**
     * 从订单页面进
     */
    public void queryCouponOrderPage(JSONArray cartItemIds) {
        couponView.showProgressBar();
        API1.getCartItemCoupons(mHandler, cartItemIds);
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
        PictureAirLog.out("从Me界面进入queryCouponMePage");
        API1.getCoupons(mHandler);//从me中进来的
        /**
         * test
         */
//        couponView.sortCoupon(testData());
//        couponView.goneProgressBar();
    }

    /**
     * 添加优惠卷，访问服务器
     * 1. 需要TokenId
     * 2. 优惠卷code
     */
    public void insertCoupon(String cpCode) {
        if (!getTokenID()) {
            return;
        }
        if (!getNetwork()) {//无网络
            couponView.noNetwork();
            return;
        }
        couponView.showProgressBar();

        if (whatPege.equals(ACTIVITY_ORDER)) {
            API1.addCoupons(mHandler, cpCode, cartItemIds);
        } else if (whatPege.equals(ACTIVITY_ME)) {
            API1.addCoupons(mHandler, cpCode, null);
        } else {
            couponView.goneProgressBar();
            couponView.fail("insert coupon fail");
        }
    }

    /**
     * 拿到API查到的数据
     * <p/>
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
     * 对拿到的优惠卷进行排序
     */
    private List<CouponInfo> sort(List<CouponInfo> mDatas) {
        CouponInfo temp;
        for (int i = 0; i < mDatas.size() - 1; i++) {
            for (int j = 0; j < mDatas.size() - i - 1; j++) {
                if (mDatas.get(j).getCpSort() > mDatas.get(j + 1).getCpSort()) {
                    temp = mDatas.get(j);
                    mDatas.set(j, mDatas.get(j + 1));// 对象交换
                    mDatas.set(j + 1, temp);
                }
            }
        }
        return mDatas;
    }

    /**
     * 是否有网络
     */
    private boolean getNetwork() {
        return AppUtil.NETWORKTYPE_INVALID != AppUtil.getNetWorkType(MyApplication.getInstance().getApplicationContext());
    }

    /**
     * 添加优惠卷成功,后拿到json
     * 1. 拿到json
     * 2. 解析到实体类中
     * 3. 排序
     * 4. 交给view
     */
    private void insertCouponSuccess(JSONObject jsonObject) {
        getApiReturnDatas(jsonObject);//暂时这里的业务是一样的
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
