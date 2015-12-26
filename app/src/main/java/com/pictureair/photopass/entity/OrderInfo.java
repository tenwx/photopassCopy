package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 订单封装类
 *
 * @author bauer_bao
 */
public class OrderInfo implements Parcelable {
    public String orderId;
    public String orderTime;//订单下单时间
    public String orderNumber;//订单号
    public double orderTotalPrice;//订单总价 = 商品价格 + 运费
    public int orderPayMentMethod;//订单支付方式  支付类型  0 支付宝 1 银联  2 VISA信用卡 3 代付 4 分期 5 自提 6 paypal
    public int orderStatus;//订单当前状态 1等待买家付款，2买家已付款（等待卖家发货），3卖家已发货（等待买家确认），4交易成功，5交易关闭,订单冻结 , 6：已付款，且服务器返回仍未未付款状态（本地）
    public String orderIntroduce;//订单明细，支付宝下单的时候需要这个参数
    public int deliveryMethod;//送货方式,物流(0)、自提(1)、直送(2),虚拟类商品无须快递(3)
    public String deliveryCustomer;//快递收货人姓名
    public String deliveryHomeNumber;//快递固话
    public String deliveryPhoneNumber;//快递手机号
    public String deliveryAddress;//快递收货地址
    public double deliveryShipping;//快递费
    public String deliveryNumber;//快递号
    public String deliveryCompany;//快递公司
    public String deliveryPostNumber;//快递收货地址邮编
    public double productPrice;//商品价格

    public OrderInfo() {

    }

    public static final Parcelable.Creator<OrderInfo> CREATOR = new Creator<OrderInfo>() {

        @Override
        public OrderInfo[] newArray(int size) {
            return new OrderInfo[size];
        }

        @Override
        public OrderInfo createFromParcel(Parcel source) {
            return new OrderInfo(source);
        }
    };

    private OrderInfo(Parcel source) {
        orderId = source.readString();
        orderTime = source.readString();
        orderNumber = source.readString();
        orderTotalPrice = source.readDouble();
        orderPayMentMethod = source.readInt();
        orderStatus = source.readInt();
        orderIntroduce = source.readString();
        deliveryMethod = source.readInt();
        deliveryCustomer = source.readString();
        deliveryHomeNumber = source.readString();
        deliveryPhoneNumber = source.readString();
        deliveryAddress = source.readString();
        deliveryShipping = source.readDouble();
        deliveryNumber = source.readString();
        deliveryCompany = source.readString();
        deliveryPostNumber = source.readString();
        productPrice = source.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderId);
        dest.writeString(orderTime);
        dest.writeString(orderNumber);
        dest.writeDouble(orderTotalPrice);
        dest.writeInt(orderPayMentMethod);
        dest.writeInt(orderStatus);
        dest.writeString(orderIntroduce);
        dest.writeInt(deliveryMethod);
        dest.writeString(deliveryCustomer);
        dest.writeString(deliveryHomeNumber);
        dest.writeString(deliveryPhoneNumber);
        dest.writeString(deliveryAddress);
        dest.writeDouble(deliveryShipping);
        dest.writeString(deliveryNumber);
        dest.writeString(deliveryCompany);
        dest.writeString(deliveryPostNumber);
        dest.writeDouble(productPrice);

    }

}
