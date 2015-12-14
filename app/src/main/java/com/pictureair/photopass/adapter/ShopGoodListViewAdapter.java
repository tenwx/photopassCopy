package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.GoodsInfo1;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.UniversalImageLoadTool;

import java.util.List;

/**
 * shop页商品列表的适配器
 *
 * @author bauer_bao
 */
public class ShopGoodListViewAdapter extends BaseAdapter {
    private final static String TAG = "ShopGoodListViewAdapter";
    private List<GoodsInfo1> goodList;
    private LayoutInflater layoutInflater;
    private String currency;
    private int width = 0;
    private DisplayImageOptions options;

    public ShopGoodListViewAdapter(List<GoodsInfo1> list, Context c, String currency) {
        goodList = list;
        layoutInflater = LayoutInflater.from(c);
        this.currency = currency;
        width = ScreenUtil.getScreenWidth(c) - ScreenUtil.dip2px(c, 10);
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_discover_loading).
                showImageOnFail(R.drawable.ic_discover_failed).cacheInMemory(true).cacheOnDisk(true).build();
    }

    /**
     * 更新数据
     * @param list data
     */
    public void refresh(List<GoodsInfo1> list){
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
        LayoutParams layoutParams = viewHolder.goodImageView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = width / 2;
        GoodsInfo1 goodsInfo1 = goodList.get(position);
        if (goodsInfo1 == null) {
            PictureAirLog.v(TAG,"getView goodInfo == null");
            return convertView;
        }
        PictureAirLog.v(TAG,"getView goodInfo name: " + goodsInfo1.getName());
        //初始化数据
        viewHolder.goodNameAlias.setText(goodsInfo1.getNameAlias());
        viewHolder.goodPrice.setText(goodsInfo1.getPrice()+"");
        viewHolder.goodCurrency.setText(currency);
        viewHolder.goodDetailIntroduce.setText(goodsInfo1.getDescription());

        if (goodsInfo1.getPrictures() != null && goodsInfo1.getPrictures().size() > 0) {
            UniversalImageLoadTool.loadDiscoverImage(Common.BASE_URL_TEST + goodsInfo1.getPrictures().get(0).getUrl(), viewHolder.goodImageView, options);

        } else {
            UniversalImageLoadTool.loadDiscoverImage(null, viewHolder.goodImageView, options);
            viewHolder.goodImageView.setTag("null");
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
