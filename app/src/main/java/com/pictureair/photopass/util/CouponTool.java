package com.pictureair.photopass.util;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.widget.CouponViewInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * 此类负责与Coupon的UI对接与数据逻辑
 * Created by bass on 16/3/11.
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
                    couponView.goneProgressBar();
                    int id = ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1);
                    couponView.fail(id);
                    break;

                case API1.GET_COUPON_SUCCESS://获取所有优惠卷成功
                    getApiReturnDatas((JSONObject) msg.obj);
                    break;

                case API1.INSERT_COUPON_SUCCESS://添加一张优惠卷成功
                    insertCouponSuccess((JSONObject) msg.obj);
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
        if (!getNetwork()) {//无网络
            couponView.noNetwork();
            return;
        }
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
            queryCouponMePage();
        } else if (getIntent.getExtras().getString(ACTIVITY_ORDER, "").equals(ACTIVITY_ORDER)) {//从订单页面进来的
            whatPege = ACTIVITY_ORDER;
            couponView.getWhatPege(ACTIVITY_ORDER);
            cartItemIds = JSONArray.parseArray(getIntent.getExtras().getString(ACTIVITY_ORDER_CART_DATAS));
            if (null == cartItemIds) {
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
        API1.getCoupons(mHandler, cartItemIds);
    }

    /**
     * 从Me界面进入
     * 查询当前用户的所有优惠卷
     * 也可作为刷新数据用
     * 1. 需要TokenId
     */
    public void queryCouponMePage() {
        couponView.showProgressBar();
        PictureAirLog.out("从Me界面进入queryCouponMePage");
        API1.getCoupons(mHandler, null);//从me中进来的

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
        mDatas = getJsonToObj(jsonObject);
        couponView.goneProgressBar();
        if (null == mDatas || mDatas.size() < 0) {
            couponView.noCoupon();//无优惠卷
        } else {
            couponView.sortCoupon(mDatas);
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
     * 解析优惠卷的json
     */
    private List<CouponInfo> getJsonToObj(JSONObject jsonObject) {
        PictureAirLog.e(TAG, "解析优惠卷的json" + jsonObject);
        int amount = jsonObject.getIntValue("amount");
        if (amount == 0) {
            return null;
        }
        List<CouponInfo> list = new ArrayList<>();
        JSONArray array = jsonObject.getJSONArray("data");
        CouponInfo couponInfo = null;
        String effectiveTime;
        String failureTime;
        for (int i = 0; i < array.size(); i++) {
            couponInfo = new CouponInfo();

            couponInfo.setCpStatus(array.getJSONObject(i).getString("status"));
            couponInfo.setCpCode(array.getJSONObject(i).getString("code"));
            couponInfo.setCpNumber(array.getJSONObject(i).getDouble("money"));
            couponInfo.setCpType(array.getJSONObject(i).getString("genre"));//优惠卷类型（discount,full,subtract）折扣，满，减
            couponInfo.setCpDescribe(array.getJSONObject(i).getString("description"));//描述
            couponInfo.setCpName(array.getJSONObject(i).getString("name"));//优惠卷名称
            //有效期
            effectiveTime = array.getJSONObject(i).getString("effectiveTime");//有效开始时间
            failureTime = array.getJSONObject(i).getString("failureTime");//有效结束时间
            couponInfo.setCpValidityPeriod(effectiveTime + "～" + failureTime);//有效期时间间隔
            list.add(couponInfo);
        }
        return list;
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

    /**
     * 测试数据
     */
    public List<CouponInfo> testData() {
        List<CouponInfo> datas = new ArrayList<>();
        CouponInfo couponInfo = new CouponInfo(1, "1234-1333-2333-2222", "有效期 20130405～2017年", "active", "subtract", "描述：pp＋卡专用", 1, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo2 = new CouponInfo(2, "1234-1333-2333-2222", "有效期 20130405～2016年", "failure", "subtract", "描述：pp＋卡专用", 3, "1246527681767538712653671253875", 8, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo3 = new CouponInfo(3, "1234-1333-2333-2222", "有效期 20130405～2017年", "active", "discount", "描述：pp＋卡专用", 4, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo4 = new CouponInfo(4, "1234-1333-2333-2222", "有效期 20130405～2018年", "active", "subtract", "描述：pp＋卡专用", 5, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo5 = new CouponInfo(5, "1234-1333-2333-2222", "有效期 20130405～2014年", "active", "subtract", "描述：pp＋卡专用", 7, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo6 = new CouponInfo(6, "1234-1333-2333-2222", "有效期 20130405～2013年", "active", "discount", "描述：pp＋卡专用", 10, "1246527681767538712653671253875", 8, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo7 = new CouponInfo(7, "1234-1333-2333-2222", "有效期 20130405～2012年", "failure", "subtract", "描述：pp＋卡专用", 11, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo8 = new CouponInfo(8, "1234-1333-2333-2222", "有效期 20130405～2011年", "active", "discount", "描述：pp＋卡专用", 14, "1246527681767538712653671253875", 7, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo9 = new CouponInfo(9, "1234-1333-2333-2222", "有效期 20130405～2019年", "failure", "subtract", "描述：pp＋卡专用", 17, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo10 = new CouponInfo(10, "1234-1333-2333-2222", "有效期 20130405～2017年", "active", "discount", "描述：pp＋卡专用", 19, "1246527681767538712653671253875", 3, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo11 = new CouponInfo(11, "1234-1333-2333-2222", "有效期 20130405～2017年", "used", "discount", "描述：pp＋卡专用", 21, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo12 = new CouponInfo(12, "1234-1333-2333-2222", "有效期 20130405～2017年", "used", "discount", "描述：pp＋卡专用", 23, "1246527681767538712653671253875", 2, false, 0, "pp＋卡优惠卷");

        datas.add(couponInfo);
        datas.add(couponInfo2);
        datas.add(couponInfo3);
        datas.add(couponInfo4);
        datas.add(couponInfo5);
        datas.add(couponInfo6);
        datas.add(couponInfo7);
        datas.add(couponInfo8);
        datas.add(couponInfo9);
        datas.add(couponInfo10);
        datas.add(couponInfo11);
        datas.add(couponInfo12);
        return datas;
    }


}
