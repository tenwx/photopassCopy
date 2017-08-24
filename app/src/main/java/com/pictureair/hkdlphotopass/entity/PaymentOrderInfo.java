package com.pictureair.photopass.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 存放已经支付但未收到支付完成推送的订单
 * Created by bauer_bao on 16/11/30.
 */

@Entity
public class PaymentOrderInfo {
    @Id
    private Long id;//自增长id
    private String orderId;
    private String userId;//和用户绑定的userid
    @Generated(hash = 851006768)
    public PaymentOrderInfo(Long id, String orderId, String userId) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
    }
    @Generated(hash = 779247414)
    public PaymentOrderInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getOrderId() {
        return this.orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
