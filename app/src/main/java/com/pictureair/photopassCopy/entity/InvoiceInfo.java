package com.pictureair.photopassCopy.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 发票实体类
 * Created by jiaxing on 16/6/13.
 */
public class InvoiceInfo implements Parcelable {
    public static final int NONE = 100;
    public static final int PERSONAL = 101;
    public static final int COMPANY = 102;
    public static final int PHOTO = 103;
    public static final int PHOTO_SERVICE = 104;
    private int type = 0;//发票类型,0,纸质发票,1 电子发票
    private int title;//发票抬头,0 个人 1 单位
    private String companyName;//公司名称、或者个人姓名
    private String companyId;//公司id，或者个人身份证Id
    private int content;//发票内容0 固定为影像服务
    private boolean needInvoice = true;//true 开发票,false 不开发票
    private SendAddress address;

    protected InvoiceInfo(Parcel in) {
        type = in.readInt();
        title = in.readInt();
        companyName = in.readString();
        companyId = in.readString();
        content = in.readInt();
        needInvoice = in.readByte() != 0;
        address = in.readParcelable(SendAddress.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(title);
        dest.writeString(companyName);
        dest.writeString(companyId);
        dest.writeInt(content);
        dest.writeByte((byte) (needInvoice ? 1 : 0));
        dest.writeParcelable(address, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InvoiceInfo> CREATOR = new Creator<InvoiceInfo>() {
        @Override
        public InvoiceInfo createFromParcel(Parcel in) {
            return new InvoiceInfo(in);
        }

        @Override
        public InvoiceInfo[] newArray(int size) {
            return new InvoiceInfo[size];
        }
    };

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public InvoiceInfo() {
        address = new SendAddress();
    }

    public SendAddress getAddress() {
        return address;
    }

    public void setAddress(SendAddress address) {
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getContent() {
        return content;
    }

    public void setContent(int content) {
        this.content = content;
    }

    public boolean isNeedInvoice() {
        return needInvoice;
    }

    public void setNeedInvoice(boolean needInvoice) {
        this.needInvoice = needInvoice;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
