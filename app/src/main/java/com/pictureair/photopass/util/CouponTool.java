package com.pictureair.photopass.util;

import android.os.Handler;
import android.os.Message;

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
    private CouponViewInterface couponView;
    private List<CouponInfo> mDatas;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case API1.GET_COUPON_FAILED://获取所有优惠卷失败
                    couponView.goneProgressBar();
                    couponView.fail("获取所有优惠卷失败");
                    break;
                case API1.GET_COUPON_SUCCESS://获取所有优惠卷成功
                    /**
                     * 这里用的测试数据
                     * 1. 拿到json
                     * 2. 解析到实体类中
                     * 3. 排序
                     * 4. 交给view
                     *
                     * 注意：如果没有优惠卷直接使用  couponView.noCoupon();
                     */
                    mDatas = testData();
                    mDatas = sort(mDatas);
                    couponView.goneProgressBar();
                    couponView.sortCoupon(mDatas);
                    break;
                case API1.INSERT_COUPON_FAILED://添加一张优惠卷失败
                    couponView.fail("添加一张优惠卷失败");
                    break;
                case API1.INSERT_COUPON_SUCCESS://添加一张优惠卷成功
                    //1. 添加一张优惠卷成功之后需要重新获取排序
                    //这里用的测试数据
                    queryCoupon();//重新获取所有数据
                    break;
            }
        }
    };


    public CouponTool(CouponViewInterface couponView) {
        this.couponView = couponView;
    }

    /**
     * 查询当前用户的所有优惠卷
     * 也可作为刷新数据用
     * 1. 需要TokenId
     */
    public void queryCoupon() {
        if (!getTokenID()) {
            return;
        }
        if (!getNetwork()){//无网络
            couponView.noNetwork();
            return;
        }
        couponView.showProgressBar();
        /*
         *  API1。网络请求当前用户的所有优惠卷
         */

        //测试
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(API1.GET_COUPON_SUCCESS);
            }
        }, 2000);

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
        if (!getNetwork()){//无网络
            couponView.noNetwork();
            return;
        }
        couponView.showProgressBar();
        /*
         *  API1。网络请求当前用户添加优惠卷
         */

        //测试
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(API1.INSERT_COUPON_SUCCESS);
            }
        }, 2000);
    }

    /**
     * 读取tokend
     */
    private boolean getTokenID(){
        if (MyApplication.getTokenId() == null) {
            couponView.fail("获取token失败");
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
    private boolean getNetwork(){
        if (AppUtil.NETWORKTYPE_INVALID == AppUtil.getNetWorkType(MyApplication.getInstance().getApplicationContext())){
            return false;
        }
        return true;
    }


    /**
     * 测试数据
     */
    public List<CouponInfo> testData() {
        List<CouponInfo> datas = new ArrayList<>();
        CouponInfo couponInfo = new CouponInfo(1, "1234-1333-2333-2222", "有效期 20130405～2017年", 1, 1, "描述：pp＋卡专用", 1, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo2 = new CouponInfo(2, "1234-1333-2333-2222", "有效期 20130405～2016年", 0, 1, "描述：pp＋卡专用", 3, "1246527681767538712653671253875", 8, true, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo3 = new CouponInfo(3, "1234-1333-2333-2222", "有效期 20130405～2017年", 2, 1, "描述：pp＋卡专用", 4, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo4 = new CouponInfo(4, "1234-1333-2333-2222", "有效期 20130405～2018年", 2, 1, "描述：pp＋卡专用", 5, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo5 = new CouponInfo(5, "1234-1333-2333-2222", "有效期 20130405～2014年", 3, 1, "描述：pp＋卡专用", 7, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo6 = new CouponInfo(6, "1234-1333-2333-2222", "有效期 20130405～2013年", 2, 1, "描述：pp＋卡专用", 10, "1246527681767538712653671253875", 8, true, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo7 = new CouponInfo(7, "1234-1333-2333-2222", "有效期 20130405～2012年", 1, 1, "描述：pp＋卡专用", 11, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo8 = new CouponInfo(8, "1234-1333-2333-2222", "有效期 20130405～2011年", 0, 1, "描述：pp＋卡专用", 14, "1246527681767538712653671253875", 7, true, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo9 = new CouponInfo(9, "1234-1333-2333-2222", "有效期 20130405～2019年", 1, 1, "描述：pp＋卡专用", 17, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo10 = new CouponInfo(10, "1234-1333-2333-2222", "有效期 20130405～2017年", 1, 1, "描述：pp＋卡专用", 19, "1246527681767538712653671253875", 3, true, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo11 = new CouponInfo(11, "1234-1333-2333-2222", "有效期 20130405～2017年", 1, 1, "描述：pp＋卡专用", 21, "1246527681767538712653671253875", 100, false, 0, "pp＋卡优惠卷");
        CouponInfo couponInfo12 = new CouponInfo(12, "1234-1333-2333-2222", "有效期 20130405～2017年", 1, 1, "描述：pp＋卡专用", 23, "1246527681767538712653671253875", 2, true, 0, "pp＋卡优惠卷");

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
