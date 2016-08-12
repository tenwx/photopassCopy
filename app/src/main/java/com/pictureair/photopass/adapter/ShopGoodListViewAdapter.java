package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;

import java.util.List;

/**
 * shop页商品列表的适配器
 *
 * @author bauer_bao
 */
public class ShopGoodListViewAdapter extends BaseAdapter {
    private final static String TAG = "ShopGoodListViewAdapter";
    private List<GoodsInfo> goodList;
    private LayoutInflater layoutInflater;
    private String currency;
    private int width = 0;
    private Context context;

    public ShopGoodListViewAdapter(List<GoodsInfo> list, Context c, String currency) {
        goodList = list;
        context = c;
        layoutInflater = LayoutInflater.from(c);
        this.currency = currency;
        width = ScreenUtil.getScreenWidth(c) - ScreenUtil.dip2px(c, 16);
    }

    /**
     * 更新数据
     *
     * @param list data
     */
    public void refresh(List<GoodsInfo> list) {
        this.goodList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return goodList.size();
    }

    @Override
    public Object getItem(int position) {
        return goodList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.good_listview_item, null);
            viewHolder.goodNameAlias = (TextView) convertView.findViewById(R.id.good_name);
            viewHolder.goodPrice = (TextView) convertView.findViewById(R.id.good_price);
            viewHolder.goodCurrency = (TextView) convertView.findViewById(R.id.good_currency);
            viewHolder.goodDetailIntroduce = (TextView) convertView.findViewById(R.id.goodDetailIntroduce);
            viewHolder.goodImageView = (ImageView) convertView.findViewById(R.id.goodImageView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ViewGroup.LayoutParams layoutParams = viewHolder.goodImageView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = width / 2;
        viewHolder.goodImageView.setLayoutParams(layoutParams);
        GoodsInfo goodsInfo = goodList.get(position);
        if (goodsInfo == null) {
            PictureAirLog.v(TAG, "getView goodInfo == null");
            return convertView;
        }
        PictureAirLog.v(TAG, "getView goodInfo name: " + goodsInfo.getName());
        //初始化数据
        viewHolder.goodNameAlias.setTypeface(MyApplication.getInstance().getFontBold());
        viewHolder.goodPrice.setTypeface(MyApplication.getInstance().getFontBold());
        viewHolder.goodCurrency.setTypeface(MyApplication.getInstance().getFontBold());

        viewHolder.goodNameAlias.setText(goodsInfo.getNameAlias());
        viewHolder.goodPrice.setText(goodsInfo.getPrice() + "");
        viewHolder.goodCurrency.setText(currency);
        viewHolder.goodDetailIntroduce.setText(goodsInfo.getCopywriter());
        if (goodsInfo.getPictures() != null && goodsInfo.getPictures().size() > 0) {
            if (viewHolder.goodImageView.getTag(R.id.glide_image_tag) == null || !viewHolder.goodImageView.getTag(R.id.glide_image_tag).equals(goodsInfo.getPictures().get(0).getUrl())) {
                GlideUtil.load(context, Common.PHOTO_URL + goodsInfo.getPictures().get(0).getUrl(), R.drawable.ic_discover_loading, R.drawable.ic_discover_failed, viewHolder.goodImageView);
                viewHolder.goodImageView.setTag(R.id.glide_image_tag, goodsInfo.getPictures().get(0).getUrl());
            }
        } else {
            GlideUtil.load(context, "", R.drawable.ic_discover_loading, R.drawable.ic_discover_failed, viewHolder.goodImageView);
            viewHolder.goodImageView.setTag(R.id.glide_image_tag, "");
        }
        return convertView;
    }

    private class ViewHolder {
        TextView goodNameAlias;//商品的别名
        TextView goodPrice;//商品的价格
        TextView goodCurrency;//商品的币种
        ImageView goodImageView;//商品的预览图片
        TextView goodDetailIntroduce;//商品的详细介绍
    }

}
