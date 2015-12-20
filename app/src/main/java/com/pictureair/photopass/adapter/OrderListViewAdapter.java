package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.PaymentOrderActivity;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ScreenUtil;

import java.util.ArrayList;

public class OrderListViewAdapter extends BaseExpandableListAdapter {
    private LayoutInflater mInflater;
    private Context context;
    private String currency;
    private ArrayList<OrderInfo> grouplist;//group信息
    private ArrayList<ArrayList<CartItemInfo>> childlist;//child信息
    private ArrayList<ImageView> gridlayoutList;
    private ImageLoader imageLoader;

    private int screenWight;

    private Handler handler;

    public OrderListViewAdapter(Context context, ArrayList<OrderInfo> list, ArrayList<ArrayList<CartItemInfo>> orderChildlist, String currency, Handler handler) {
        this.context = context;
        this.currency = currency;
        this.handler = handler;
        grouplist = list;
        childlist = orderChildlist;
        mInflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        screenWight = ScreenUtil.getScreenWidth(context) / 3 - 40;
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return grouplist.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        System.out.println("getChildrenCount===" + groupPosition + "===child size==" + childlist.size() + ",group size = " + grouplist.size());
        return childlist.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return grouplist.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childlist.get(groupPosition).get(childPosition);
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
        GroupHolderView groupHolderView = null;
        // 布局混乱。每次都重新加载。
        if (groupPosition != -1) {
            convertView = null;
        }
        if (convertView == null) {
            groupHolderView = new GroupHolderView();
            convertView = mInflater.inflate(R.layout.order_item, null);
            //找控件
            groupHolderView.orderStatesTextView = (TextView) convertView.findViewById(R.id.order_states);
            groupHolderView.orderTimeTextView = (TextView) convertView.findViewById(R.id.order_time);
            groupHolderView.orderNumberTextView = (TextView) convertView.findViewById(R.id.order_number);
            groupHolderView.totalPriceTextView = (TextView) convertView.findViewById(R.id.order_total_price);
            groupHolderView.paymentButton = (Button) convertView.findViewById(R.id.order_payment1);
            groupHolderView.currencyTextView = (TextView) convertView.findViewById(R.id.order_price_currency);
            convertView.setTag(groupHolderView);
        } else {
            groupHolderView = (GroupHolderView) convertView.getTag();
        }
        //初始化group
        groupHolderView.orderTimeTextView.setText(grouplist.get(groupPosition).orderTime.substring(0, 10));
        groupHolderView.orderNumberTextView.setText(grouplist.get(groupPosition).orderNumber);
        groupHolderView.totalPriceTextView.setText((int) grouplist.get(groupPosition).orderTotalPrice + "");
        switch (grouplist.get(groupPosition).orderStatus) {
            case 1://1等待买家付款
                groupHolderView.paymentButton.setVisibility(View.VISIBLE);
                groupHolderView.paymentButton.setText(R.string.payment_order_btn);
//			groupHolderView.paymentButton.setBackgroundColor(R.color.orange1);
                groupHolderView.paymentButton.setTextColor(context.getResources().getColor(R.color.white));
                groupHolderView.orderStatesTextView.setText(R.string.order_unpaid);
                break;

            case 2://2买家已付款（等待卖家发货），3卖家已发货（等待买家确认）
            case 3:
                groupHolderView.paymentButton.setVisibility(View.VISIBLE);
                groupHolderView.paymentButton.setTextColor(context.getResources().getColor(R.color.orange));
                groupHolderView.paymentButton.setText(R.string.delete_order_btn);
                groupHolderView.paymentButton.setBackgroundColor(context.getResources().getColor(R.color.white));
                groupHolderView.orderStatesTextView.setText(R.string.order_paid);
                break;

            case 4://4交易成功，5交易关闭,订单冻结
            case 5:
                groupHolderView.paymentButton.setVisibility(View.GONE);
//			groupHolderView.paymentButton.setText(R.string.buy_again);
                break;

            default:
                break;
        }
        groupHolderView.paymentButton.setOnClickListener(new orderOnlickListener(grouplist.get(groupPosition).orderStatus, groupPosition));
        groupHolderView.currencyTextView.setText(currency);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ChildHolderView hView = null;
        gridlayoutList = new ArrayList<ImageView>();
        if (convertView == null) {
            hView = new ChildHolderView();
            convertView = mInflater.inflate(R.layout.order_product_detail_item, null);
            hView.goodsImageView = (ImageView) convertView.findViewById(R.id.order_imageView_pd);
            hView.goodsName = (TextView) convertView.findViewById(R.id.order_textView_name);
            hView.goodsCount = (TextView) convertView.findViewById(R.id.order_editText_count);
            hView.currency = (TextView) convertView.findViewById(R.id.order_textview_currency2);
            hView.priceTextView = (TextView) convertView.findViewById(R.id.order_textView_pr);
            hView.gridLayout = (GridLayout) convertView.findViewById(R.id.order_grid_photo);
            convertView.setTag(hView);
        } else {
            hView = (ChildHolderView) convertView.getTag();
        }
        //初始化控件值
        if (childlist.get(groupPosition).get(childPosition).cart_productImageUrl.contains("http")) {
            imageLoader.displayImage(childlist.get(groupPosition).get(childPosition).cart_productImageUrl, hView.goodsImageView);
        } else {
            imageLoader.displayImage(Common.BASE_URL_TEST + childlist.get(groupPosition).get(childPosition).cart_productImageUrl, hView.goodsImageView);
        }
        hView.goodsName.setText(childlist.get(groupPosition).get(childPosition).cart_productName);
        hView.goodsCount.setText(childlist.get(groupPosition).get(childPosition).cart_quantity + "");
        hView.currency.setText(currency);
        hView.priceTextView.setText((int) childlist.get(groupPosition).get(childPosition).cart_promotionPrice + "");
        //初始化添加的图片信息
        if (childlist.get(groupPosition).get(childPosition).cart_photoUrls == null || childlist.get(groupPosition).get(childPosition).cart_photoUrls.size() <= 0) {
            hView.gridLayout.setVisibility(View.GONE);
        } else {
            hView.gridLayout.removeAllViews();
            //依次添加照片
            for (int i = 0; i < childlist.get(groupPosition).get(childPosition).cart_photoUrls.size(); i++) {
                ImageView imageView = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = screenWight;
                params.height = screenWight;
                imageView.setLayoutParams(params);
                imageLoader.displayImage(Common.PHOTO_URL + childlist.get(groupPosition).get(childPosition).cart_photoUrls.get(i).cart_photoUrl, imageView);
                imageView.setScaleType(ScaleType.CENTER_CROP);
                imageView.setId(childPosition * 10 + i);//给添加的imageview添加id
                imageView.setFocusable(false);
                imageView.setClickable(false);
                gridlayoutList.add(imageView);
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

    private class GroupHolderView {
        TextView orderStatesTextView;//订单状态
        TextView orderTimeTextView;//订单时间
        TextView orderNumberTextView;//订单号
        TextView currencyTextView;//币种
        TextView totalPriceTextView;//订单总价
        Button paymentButton;//支付按钮，快递按钮，再次购买按钮，三个功能
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
        private int type;
        private int position;

        public orderOnlickListener(int type, int position) {
            // TODO Auto-generated constructor stub
            this.type = type;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (type) {
                case 1://1等待买家付款
                    System.out.println("payment");
                    intent = new Intent(context, PaymentOrderActivity.class);
                    intent.putExtra("flag", "order");
                    intent.putExtra("deliveryInfo", grouplist.get(position));
                    context.startActivity(intent);
                    break;

                case 2://2买家已付款（等待卖家发货），3卖家已发货（等待买家确认）
                case 3:
                    API1.removeOrder(grouplist.get(position).orderId, grouplist.get(position), childlist.get(position), handler);
                    break;

                case 4://4交易成功，5交易关闭,订单冻结,暂时这个功能不开放
                case 5:
                    System.out.println("buyagain");
                    break;

                default:
                    break;
            }
        }

    }


}
