package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.PaymentOrderActivity;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.OrderProductInfo;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.PWToast;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

/**
 * 订单item
 */
public class OrderListViewAdapter extends BaseExpandableListAdapter implements PWDialog.OnPWDialogClickListener {
    private LayoutInflater mInflater;
    private Context context;
    private String currency;
    private ArrayList<OrderInfo> grouplist;//group信息
    private List<OrderProductInfo> childlist;//child信息
    private GroupHolderView groupHolderView;
    private ChildHolderView hView;
    private PWDialog pictureWorksDialog;

    private int screenWight;
    private int tab;

    private Handler handler;

    private OrderInfo deleteOrderInfo;
    private OrderProductInfo deleteOrderProductInfo;
    private RemoveOrderItemListener removeOrderItemListener;

    public OrderListViewAdapter(Context context, ArrayList<OrderInfo> list, List<OrderProductInfo> orderChildlist, String currency, Handler handler, int tab) {
        this.context = context;
        this.currency = currency;
        this.handler = handler;
        this.grouplist = list;
        this.childlist = orderChildlist;
        this.tab = tab;
        mInflater = LayoutInflater.from(context);
        screenWight = ScreenUtil.getScreenWidth(context) / 3 - 40;
    }

    public void setRemoveOrderItemListener(RemoveOrderItemListener listener) {
        this.removeOrderItemListener = listener;
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return grouplist.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        PictureAirLog.out("getChildrenCount===" + groupPosition + "===child size==" + childlist.size() + ",group size = " + grouplist.size());
        return childlist.get(groupPosition).getCartItemInfos().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return grouplist.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childlist.get(groupPosition).getCartItemInfos().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            groupHolderView = new GroupHolderView();
            convertView = mInflater.inflate(R.layout.order_item, parent, false);
            //找控件
            groupHolderView.orderStatesTextView = (TextView) convertView.findViewById(R.id.order_states);
            groupHolderView.orderTimeTextView = (TextView) convertView.findViewById(R.id.order_time);
            groupHolderView.orderNumberTextView = (TextView) convertView.findViewById(R.id.order_number);
            groupHolderView.totalPriceTextView = (TextView) convertView.findViewById(R.id.order_total_price);
            groupHolderView.paymentButton = (TextView) convertView.findViewById(R.id.order_payment1);
            groupHolderView.deleteButton = (TextView) convertView.findViewById(R.id.order_delete);
            groupHolderView.currencyTextView = (TextView) convertView.findViewById(R.id.order_price_currency);
            groupHolderView.orderCancled = (TextView) convertView.findViewById(R.id.order_tv_cancel);
            convertView.setTag(groupHolderView);
        } else {
            groupHolderView = (GroupHolderView) convertView.getTag();
        }
        //初始化group
        groupHolderView.orderTimeTextView.setText(grouplist.get(groupPosition).orderTime.substring(0, 19));
        groupHolderView.orderNumberTextView.setText(grouplist.get(groupPosition).orderNumber);
        groupHolderView.totalPriceTextView.setText((int) grouplist.get(groupPosition).orderTotalPrice + "");
        switch (grouplist.get(groupPosition).orderStatus) {
            case -3:
                groupHolderView.orderStatesTextView.setText(R.string.order_refund);
                groupHolderView.paymentButton.setVisibility(View.GONE);
                groupHolderView.deleteButton.setVisibility(View.VISIBLE);
                groupHolderView.paymentButton.setEnabled(true);
                groupHolderView.deleteButton.setEnabled(true);
                break;

            case -1:
                groupHolderView.paymentButton.setVisibility(View.GONE);
                groupHolderView.orderCancled.setVisibility(View.VISIBLE);
                groupHolderView.deleteButton.setVisibility(View.VISIBLE);
                groupHolderView.orderStatesTextView.setText(R.string.order_cancled);
                groupHolderView.deleteButton.setEnabled(true);
                break;

            case -2:
            case 1:
                //1等待买家付款
                groupHolderView.paymentButton.setVisibility(View.VISIBLE);
                groupHolderView.deleteButton.setVisibility(View.VISIBLE);
                groupHolderView.orderStatesTextView.setText(R.string.order_unpaid);
                groupHolderView.paymentButton.setEnabled(true);
                groupHolderView.deleteButton.setEnabled(true);
                break;

            case 2://2买家已付款（等待卖家发货），3卖家已发货（等待买家确认）
            case 3:
            case 4://4交易成功，5交易关闭,订单冻结
            case 5:
                if (grouplist.get(groupPosition).productEntityType == 0) {
                    //3为虚拟类商品无须快递
                    groupHolderView.orderStatesTextView.setText(R.string.order_completed);
                    groupHolderView.paymentButton.setVisibility(View.GONE);
                    groupHolderView.deleteButton.setVisibility(View.VISIBLE);
                } else {
                    groupHolderView.orderStatesTextView.setText(R.string.order_paid);
                    groupHolderView.paymentButton.setVisibility(View.GONE);
                    groupHolderView.deleteButton.setVisibility(View.GONE);
                }
                groupHolderView.paymentButton.setEnabled(true);
                groupHolderView.deleteButton.setEnabled(true);
                break;
            case 0:
            case 6:
                //已付款，服务器未返回推送
                groupHolderView.paymentButton.setVisibility(View.VISIBLE);
                groupHolderView.deleteButton.setVisibility(View.VISIBLE);
                groupHolderView.orderStatesTextView.setText(R.string.order_pending);
                groupHolderView.paymentButton.setEnabled(false);
                groupHolderView.deleteButton.setEnabled(false);

                break;

            default:
                break;
        }
        groupHolderView.paymentButton.setOnClickListener(new orderOnlickListener(true, groupPosition));
        groupHolderView.deleteButton.setOnClickListener(new orderOnlickListener(false, groupPosition));
        groupHolderView.currencyTextView.setText(currency);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            hView = new ChildHolderView();
            convertView = mInflater.inflate(R.layout.order_product_detail_item, parent, false);
            hView.goodsImageView = (ImageView) convertView.findViewById(R.id.order_imageView_pd);
            hView.goodsName = (TextView) convertView.findViewById(R.id.order_textView_name);
            hView.goodsName.setTypeface(MyApplication.getInstance().getFontBold());
            hView.goodsCount = (TextView) convertView.findViewById(R.id.order_editText_count);
            hView.currency = (TextView) convertView.findViewById(R.id.order_textview_currency2);
            hView.priceTextView = (TextView) convertView.findViewById(R.id.order_textView_pr);
            hView.gridLayout = (GridLayout) convertView.findViewById(R.id.order_grid_photo);
            convertView.setTag(hView);
        } else {
            hView = (ChildHolderView) convertView.getTag();
        }
        //初始化控件值
        if (childlist.get(groupPosition).getCartItemInfos().get(childPosition).getCartProductImageUrl().contains("http")) {
            GlideUtil.load(context, childlist.get(groupPosition).getCartItemInfos().get(childPosition).getCartProductImageUrl(), hView.goodsImageView);
        } else {
            GlideUtil.load(context, Common.PHOTO_URL + childlist.get(groupPosition).getCartItemInfos().get(childPosition).getCartProductImageUrl(), hView.goodsImageView);
        }
        hView.goodsName.setText(childlist.get(groupPosition).getCartItemInfos().get(childPosition).getProductName());
        hView.goodsCount.setText(childlist.get(groupPosition).getCartItemInfos().get(childPosition).getQty() + "");
        hView.currency.setText(currency);
        hView.priceTextView.setText(childlist.get(groupPosition).getCartItemInfos().get(childPosition).getUnitPrice() + "");
        //初始化添加的图片信息
        if (childlist.get(groupPosition).getCartItemInfos().get(childPosition).getEmbedPhotos() == null || childlist.get(groupPosition).getCartItemInfos().get(childPosition).getEmbedPhotos().size() <= 0) {
            hView.gridLayout.setVisibility(View.GONE);
        } else {
            hView.gridLayout.removeAllViews();
            //依次添加照片
            for (int i = 0; i < childlist.get(groupPosition).getCartItemInfos().get(childPosition).getEmbedPhotos().size(); i++) {
                ImageView imageView = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = screenWight;
                params.height = screenWight;
                imageView.setLayoutParams(params);
                GlideUtil.load(context, Common.PHOTO_URL + childlist.get(groupPosition).getCartItemInfos().get(childPosition).getEmbedPhotos().get(i).getPhotoUrl(), imageView);
                imageView.setScaleType(ScaleType.CENTER_CROP);
                imageView.setId(childPosition * 10 + i);//给添加的imageview添加id
                imageView.setFocusable(false);
                imageView.setClickable(false);
                //imageview设置监听
                hView.gridLayout.addView(imageView, params);
            }
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            removeOrder(deleteOrderInfo.orderId, deleteOrderInfo, deleteOrderProductInfo);
        }
    }

    private class GroupHolderView {
        TextView orderStatesTextView;//订单状态
        TextView orderTimeTextView;//订单时间
        TextView orderNumberTextView;//订单号
        TextView currencyTextView;//币种
        TextView totalPriceTextView;//订单总价
        TextView orderCancled;
        TextView paymentButton;//支付按钮，快递按钮，再次购买按钮，三个功能
        TextView deleteButton;
    }

    private class ChildHolderView {
        ImageView goodsImageView;//商品预览图
        TextView goodsName;//商品名字
        TextView goodsCount;//商品数量
        TextView currency;//货币
        TextView priceTextView;//商品价格
        GridLayout gridLayout;//商品所添加的图片
    }

    private class orderOnlickListener implements OnClickListener {
        private boolean isPay;
        private int position;

        public orderOnlickListener(boolean isPay, int position) {
            // TODO Auto-generated constructor stub
            this.isPay = isPay;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (isPay) {//支付
                checkOrder(grouplist.get(position).orderNumber, grouplist.get(position), childlist.get(position));

            } else {//删除
                if (tab == 0) {//未付款
                    deleteOrderInfo = grouplist.get(position);
                    deleteOrderProductInfo = childlist.get(position);
                    if (pictureWorksDialog == null) {
                        pictureWorksDialog = new PWDialog(context)
                                .setPWDialogMessage(R.string.order_delete_msg)
                                .setPWDialogNegativeButton(R.string.button_cancel)
                                .setPWDialogPositiveButton(R.string.button_ok)
                                .setOnPWDialogClickListener(OrderListViewAdapter.this)
                                .pwDialogCreate();
                    }
                    pictureWorksDialog.pwDilogShow();

                } else if (tab == 2) {//已取
                    PictureAirLog.out("start delete order");
                    removeOrder(grouplist.get(position).orderId, grouplist.get(position), childlist.get(position));
                }
            }
        }

    }

    private void removeOrder(String orderId, final OrderInfo orderInfo, final OrderProductInfo orderProductInfo) {
        API2.removeOrder(orderId)
                .compose(((RxAppCompatActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        if (removeOrderItemListener != null) {
                            removeOrderItemListener.removeOrderSuccess(orderInfo, orderProductInfo);
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        if (removeOrderItemListener != null) {
                            removeOrderItemListener.removeOrderFailed(status);
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void checkOrder(final String orderCode, final OrderInfo orderInfo, final OrderProductInfo orderProductInfo) {
        API2.checkOrder(orderCode)
                .compose(((RxAppCompatActivity)context).<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.d("check order---> " + jsonObject.toString());
                        if (jsonObject.containsKey("orderStatus")) {
                            JSONArray result = jsonObject.getJSONArray("orderStatus");
                            if (result.size() > 0) {
                                JSONObject orderResultObj = result.getJSONObject(0);
                                if (orderResultObj.containsKey("orderCode") && orderCode.equals(orderResultObj.getString("orderCode"))//返回的订单号和传入的订单号一致
                                        && orderResultObj.containsKey("status") && orderResultObj.getIntValue("status") == 1) {//订单状态为待支付
                                    startPay(orderInfo, orderProductInfo);//开始支付

                                } else {//没有此订单
                                    PWToast.getInstance(context).setTextAndShow(R.string.order_error);

                                }

                            } else {//没有此订单
                                PWToast.getInstance(context).setTextAndShow(R.string.order_error);

                            }
                        } else {
                            _onError(401);
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        PWToast.getInstance(context).setTextAndShow(R.string.http_error_code_401);

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void startPay(OrderInfo orderInfo, OrderProductInfo orderProductInfo) {
        Intent intent = new Intent(context, PaymentOrderActivity.class);

        intent.putExtra("flag", "order");
        intent.putExtra("deliveryInfo", orderInfo);
        //childlist
        String orderName, orderIntroduce = null;
        if (orderProductInfo.getCartItemInfos().size() == 1) {
            orderName = orderProductInfo.getCartItemInfos().get(0).getProductName();
            orderIntroduce = orderProductInfo.getCartItemInfos().get(0).getProductName()
                    + orderProductInfo.getCartItemInfos().get(0).getUnitPrice() + "*" + orderProductInfo.getCartItemInfos().get(0).getQty();
        } else {
            orderName = context.getString(R.string.multi_goods);
            for (int i = 0; i < orderProductInfo.getCartItemInfos().size(); i++) {
                if (i == 0) {
                    orderIntroduce = orderProductInfo.getCartItemInfos().get(i).getProductName()
                            + orderProductInfo.getCartItemInfos().get(i).getUnitPrice() + "*" + orderProductInfo.getCartItemInfos().get(i).getQty();
                } else {
                    orderIntroduce += "," + orderProductInfo.getCartItemInfos().get(i).getProductName()
                            + orderProductInfo.getCartItemInfos().get(i).getUnitPrice() + "*" + orderProductInfo.getCartItemInfos().get(i).getQty();
                }
            }
        }
        PictureAirLog.out("orderIntroduce---->" + orderIntroduce);
        intent.putExtra("name", orderName);
        intent.putExtra("introduce", orderIntroduce);
        context.startActivity(intent);
    }

    public interface RemoveOrderItemListener {
        void removeOrderSuccess(OrderInfo orderInfo, OrderProductInfo orderProductInfo);
        void removeOrderFailed(int status);
    }

}
