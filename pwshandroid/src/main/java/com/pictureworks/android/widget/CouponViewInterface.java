package com.pictureworks.android.widget;

import com.alibaba.fastjson.JSONArray;
import com.pictureworks.android.entity.CouponInfo;

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
    void sortCoupon(List<CouponInfo> sortDatas, boolean needClear);

    /**
     * 读取输入的code
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

    /**
     * 添加优惠卷失败
     * @param id 失败原因
     */
    void fail(int id);

    /**
     * 是什么页面进来的
     */
    void getWhatPege(String whatPege);

    /**
     * 从订单页面传入过来的优惠卷（是上一次选择过的）需要标记打勾
     */
    void showCouponFromOrderPage(JSONArray jsonArray);


}
