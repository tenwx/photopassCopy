package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.pictureair.photopass.util.PictureAirLog;

/**
 * Created by jiaxing on 16/5/19.
 */
public class SendAddress implements Parcelable, Comparable<SendAddress> {
    private String addressId;
    private String area;
    private String name;
    private String zip;
    private String mobilePhone;
    private String telePhone;
    private String detailAddress;
    private String province;
    private String city;
    private String country;
    private boolean isSelected;

    public SendAddress() {

    }

    public SendAddress(String address, String name, String phone) {
        this.detailAddress = address;
        this.name = name;
        this.mobilePhone = phone;
    }
    public SendAddress(String address, String name, String phone,boolean isSelected) {
        this.detailAddress = address;
        this.name = name;
        this.mobilePhone = phone;
        this.isSelected=isSelected;
    }


    protected SendAddress(Parcel in) {
        addressId = in.readString();
        area = in.readString();
        name = in.readString();
        zip = in.readString();
        mobilePhone = in.readString();
        telePhone = in.readString();
        detailAddress = in.readString();
        province = in.readString();
        city = in.readString();
        country = in.readString();
        isSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(addressId);
        dest.writeString(area);
        dest.writeString(name);
        dest.writeString(zip);
        dest.writeString(mobilePhone);
        dest.writeString(telePhone);
        dest.writeString(detailAddress);
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(country);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SendAddress> CREATOR = new Creator<SendAddress>() {
        @Override
        public SendAddress createFromParcel(Parcel in) {
            return new SendAddress(in);
        }

        @Override
        public SendAddress[] newArray(int size) {
            return new SendAddress[size];
        }
    };

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getTelePhone() {
        return telePhone;
    }

    public void setTelePhone(String telePhone) {
        this.telePhone = telePhone;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public int compareTo(SendAddress another) {
        if (this.isSelected() && !another.isSelected()){
            PictureAirLog.out("order--->-1-1-1-1-1");
            return -1;//排后面
        } else if (!this.isSelected() && another.isSelected()) {
            PictureAirLog.out("order--->11111111");
            return 1;
        } else {
            return 0;
        }
    }
}
