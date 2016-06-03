package com.pictureworks.android.util;

import com.pictureworks.android.entity.OrderInfo;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by milo on 15/12/24.
 * 订单列表按时间降序排序
 */
public class OrderInfoDateSortUtil implements Comparator<OrderInfo> {
    @Override
    public int compare(OrderInfo lhs, OrderInfo rhs) {
        int flag = 0;
        Date date1 = AppUtil.getDateFromStr(lhs.orderTime);
        Date date2 = AppUtil.getDateFromStr(rhs.orderTime);
        //降序排列
        if (date1 != null && date2 != null) {
            flag = date2.compareTo(date1);
        }
        return flag;
    }
}
