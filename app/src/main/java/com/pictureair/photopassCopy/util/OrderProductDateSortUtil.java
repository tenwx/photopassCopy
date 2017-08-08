package com.pictureair.photopassCopy.util;

import com.pictureair.photopassCopy.entity.OrderProductInfo;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by milo on 15/12/24.
 * 订单列表按时间降序排序
 */
public class OrderProductDateSortUtil implements Comparator<OrderProductInfo> {
    @Override
    public int compare(OrderProductInfo lhs, OrderProductInfo rhs) {
        int flag = 0;
        Date date1 = AppUtil.getDateFromStr(lhs.getOrderTime());
        Date date2 = AppUtil.getDateFromStr(rhs.getOrderTime());
        //降序排列
        if (date1 != null && date2 != null) {
            flag = date2.compareTo(date1);
        }
        return flag;
    }
}
