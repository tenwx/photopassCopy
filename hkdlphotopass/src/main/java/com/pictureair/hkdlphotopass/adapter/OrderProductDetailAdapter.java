package com.pictureair.hkdlphotopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.hkdlphotopass.R;
import com.pictureworks.android.entity.CartItemInfo;
import com.pictureworks.android.util.Common;
import com.pictureworks.android.util.ScreenUtil;

import java.util.ArrayList;

public class OrderProductDetailAdapter extends BaseAdapter {
    private ArrayList<CartItemInfo> list;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private String currency;
    private Context context;
    private int screenWight;
    private ArrayList<ImageView> gridlayoutList;

    public OrderProductDetailAdapter(Context context, ArrayList<CartItemInfo> list, String currency) {
        this.list = list;
        inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        this.currency = currency;
        this.context = context;
        screenWight = ScreenUtil.getScreenWidth(context) / 3 - 40;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OrderHolderView hView = null;
        gridlayoutList = new ArrayList<ImageView>();
        if (convertView == null) {
            hView = new OrderHolderView();
            convertView = inflater.inflate(R.layout.order_product_detail_item, null);
            hView.goodsImageView = (ImageView) convertView.findViewById(R.id.order_imageView_pd);
            hView.goodsName = (TextView) convertView.findViewById(R.id.order_textView_name);
            hView.goodsName.setTextColor(context.getResources().getColor(R.color.pp_dark_blue));
            hView.goodsCount = (TextView) convertView.findViewById(R.id.order_editText_count);
            hView.currency = (TextView) convertView.findViewById(R.id.order_textview_currency2);
            hView.priceTextView = (TextView) convertView.findViewById(R.id.order_textView_pr);
            hView.gridLayout = (GridLayout) convertView.findViewById(R.id.order_grid_photo);
            convertView.setTag(hView);
        } else {
            hView = (OrderHolderView) convertView.getTag();
        }
        //初始化控件值
        if (list.get(position).getCartProductImageUrl().contains("http")) {
            imageLoader.displayImage(list.get(position).getCartProductImageUrl(), hView.goodsImageView);
        } else {
            imageLoader.displayImage(Common.PHOTO_URL + list.get(position).getCartProductImageUrl(), hView.goodsImageView);
        }
        hView.goodsName.setText(list.get(position).getProductName());
        hView.goodsCount.setText(list.get(position).getQty() + "");
        hView.currency.setText(currency);
        hView.priceTextView.setText((int) list.get(position).getUnitPrice() + "");

        //初始化添加的图片信息
        if (list.get(position).getEmbedPhotos() == null || list.get(position).getEmbedPhotos().size() == 0) {
            hView.gridLayout.setVisibility(View.GONE);
        } else {
            hView.gridLayout.setVisibility(View.VISIBLE);
            hView.gridLayout.removeAllViews();
            //依次添加照片
            for (int i = 0; i < list.get(position).getEmbedPhotos().size(); i++) {
                ImageView imageView = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 25)) / 4;
                params.height = params.width;
                imageView.setLayoutParams(params);

                imageLoader.displayImage(Common.PHOTO_URL + list.get(position).getEmbedPhotos().get(i).getPhotoUrl(), imageView);
                imageView.setScaleType(ScaleType.CENTER_CROP);
                imageView.setId(position * 10 + i);//给添加的imageview添加id
                imageView.setFocusable(false);
                imageView.setClickable(false);
                gridlayoutList.add(imageView);
                //imageview设置监听
                hView.gridLayout.addView(imageView, params);
            }
        }

        return convertView;
    }

    private class OrderHolderView {
        ImageView goodsImageView;//商品预览图
        TextView goodsName;//商品名字
        TextView goodsCount;//商品数量
        TextView currency;//货币
        TextView priceTextView;//商品价格
        GridLayout gridLayout;//商品所添加的图片
    }

}
