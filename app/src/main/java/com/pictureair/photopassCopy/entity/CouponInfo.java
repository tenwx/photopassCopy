package com.pictureair.photopassCopy.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 优惠卷
 * <p/>
 * 模拟测试用   需要等齐超把接口做好
 * Created by bass on 16/3/11.
 */
public class CouponInfo implements Serializable {

    private int cpId = 0;
    private String cpCode = "";//优惠卷code
    private String cpName = "";//优惠卷名字
    private String cpValidityPeriod = "";//有效期
    private String cpStatus = "";//（used,active,failure）已使用，可使用，已过期
    private String cpType = "";//优惠卷类型（discount,full,subtract）折扣，满，减
    private String cpDescribe = "";//优惠卷描述
    private int cpSort = 0;//序号 （排序）
    private String userId = "";
    private double cpNumber = 0;//显示价格或者折扣的数字（比如 减0.8元？ 还是打8折？（根据优惠类型而定））
    private boolean cpIsSelect = false;//是否选中 （选择优惠卷的时候，选择标识）
    private int cpColor = 0;//颜色  （目前没用到，可能以后用到优惠卷的颜色）
    private List<String> usingScope = null;//可用范围(齐超说待定)
    private boolean applyThisProduct = true;//该优惠卷适用当前商品？ true适用，false不适用

    public CouponInfo() {
    }

    public CouponInfo(int cpId, String cpCode, String cpValidityPeriod, String cpStatus, String cpType, String cpDescribe, int cpSort, String userId, double cpNumber, boolean cpIsSelect, int cpColor, String cpName) {
        this.cpId = cpId;
        this.cpCode = cpCode;
        this.cpName = cpName;
        this.cpValidityPeriod = cpValidityPeriod;
        this.cpStatus = cpStatus;
        this.cpType = cpType;
        this.cpDescribe = cpDescribe;
        this.cpSort = cpSort;
        this.userId = userId;
        this.cpNumber = cpNumber;
        this.cpIsSelect = cpIsSelect;
        this.cpColor = cpColor;
    }

    public boolean isApplyThisProduct() {
        return applyThisProduct;
    }

    public void setApplyThisProduct(boolean applyThisProduct) {
        this.applyThisProduct = applyThisProduct;
    }

    public String getCpName() {
        return cpName;
    }

    public void setCpName(String cpName) {
        this.cpName = cpName;
    }

    public int getCpId() {
        return cpId;
    }

    public void setCpId(int cpId) {
        this.cpId = cpId;
    }

    public String getCpCode() {
        return cpCode;
    }

    public void setCpCode(String cpCode) {
        this.cpCode = cpCode;
    }

    public String getCpValidityPeriod() {
        return cpValidityPeriod;
    }

    public void setCpValidityPeriod(String cpValidityPeriod) {
        this.cpValidityPeriod = cpValidityPeriod;
    }

    public String getCpStatus() {
        return cpStatus;
    }

    public void setCpStatus(String cpStatus) {
        this.cpStatus = cpStatus;
    }

    public String getCpType() {
        return cpType;
    }

    public void setCpType(String cpType) {
        this.cpType = cpType;
    }

    public String getCpDescribe() {
        return cpDescribe;
    }

    public void setCpDescribe(String cpDescribe) {
        this.cpDescribe = cpDescribe;
    }

    public int getCpSort() {
        return cpSort;
    }

    public void setCpSort(int cpSort) {
        this.cpSort = cpSort;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getCpNumber() {
        return cpNumber;
    }

    public void setCpNumber(double cpNumber) {
        this.cpNumber = cpNumber;
    }

    public boolean getCpIsSelect() {
        return cpIsSelect;
    }

    public void setCpIsSelect(boolean cpIsSelect) {
        this.cpIsSelect = cpIsSelect;
    }

    public int getCpColor() {
        return cpColor;
    }

    public void setCpColor(int cpColor) {
        this.cpColor = cpColor;
    }

}
