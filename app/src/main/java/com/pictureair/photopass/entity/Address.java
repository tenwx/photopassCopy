package com.pictureair.photopass.entity;

import java.io.Serializable;

/**
 * Created by milo on 15/12/19.
 * 收货地址实体类
 */
public class Address implements Serializable {
    private String outletId = "";
    private String address = "";
    private boolean isSelect = false;

    public Address() {
    }

    public Address(String outletId, String address, boolean isSelect) {
        this.outletId = outletId;
        this.address = address;
        this.isSelect = isSelect;
    }

    public boolean getIsSelect() {
        return isSelect;
    }

    public void setIsSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public String getOutletId() {
        return outletId;
    }

    public void setOutletId(String outletId) {
        this.outletId = outletId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
