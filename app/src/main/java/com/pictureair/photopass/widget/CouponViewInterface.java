package com.pictureair.photopass.widget;

import com.pictureair.photopass.entity.CouponInfo;

import java.util.List;

/**
 * 提供给CouponTool使用的接口，主要松视图和数据业务逻辑的耦合
 * Created by bass on 16/3/11.
 */
public interface CouponViewInterface {
    /**
     * 显示进度条
     */
    void showProgressBar();

    /**
     * 隐藏进度条
     */
    void goneProgressBar();

    /**
     * 给UI显示的数据，排序后的数据
     */
    void sortCoupon(List<CouponInfo> sortDatas);

    /**
     * 读取code
     */
    String getCouponCode();

    /**
     * 没有优惠卷
     */
    void noCoupon();

    /**
     * 没有网络
     */
    void noNetwork();

    /**
     * 添加优惠卷失败
     * @param str 失败原因
     */
    void fail(String str);

}
