package com.pictureair.photopassCopy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.entity.CartItemInfo;
import com.pictureair.photopassCopy.entity.CartPhotosInfo;
import com.pictureair.photopassCopy.util.AppUtil;
import com.pictureair.photopassCopy.util.Common;
import com.pictureair.photopassCopy.util.GlideUtil;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.pictureair.photopassCopy.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单提交
 */
public class SubmitOrderListViewAdapter extends BaseAdapter {
    private ArrayList<CartItemInfo> arrayList;
    private Context context;
    private LayoutInflater layoutInflater;
    private List<CartPhotosInfo> gridviewlist;
    private String currency;
    private static final String TAG = "SubmitOrderListViewAdapter";

    public SubmitOrderListViewAdapter(Context context, ArrayList<CartItemInfo> list, String currency) {
        this.context = context;
        this.currency = currency;
        this.arrayList = list;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.submitorder_listview_item, parent, false);
            viewHolder.goodImageView = (ImageView) convertView.findViewById(R.id.imageView_pd);
            viewHolder.goodNameTextView = (TextView) convertView.findViewById(R.id.textView_name);
            viewHolder.currencyTextView = (TextView) convertView.findViewById(R.id.textView_currency);
            viewHolder.goodPriceTextView = (TextView) convertView.findViewById(R.id.textView_pr);
            viewHolder.goodQuentityTextView = (TextView) convertView.findViewById(R.id.editText_count1);
            viewHolder.goodPhotosGridLayout = (GridLayout) convertView.findViewById(R.id.gridView_cartphoto);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        gridviewlist = arrayList.get(position).getEmbedPhotos();
        if (gridviewlist == null) {
            gridviewlist = new ArrayList<>();
        }

        //初始化数据
        if (Common.ppp.equals(arrayList.get(position).getProductName()) || Common.SINGLE_PPP.equals(arrayList.get(position).getProductName())) {//ppp产品 或 拼单
            PictureAirLog.v(TAG, "ppp product");
            viewHolder.goodPhotosGridLayout.setVisibility(View.GONE);
        } else {//为其他产品
            PictureAirLog.v(TAG, "other procut");

            //设置gridlayout,先清除所有的view，再重新添加view
            viewHolder.goodPhotosGridLayout.setVisibility(View.VISIBLE);
            viewHolder.goodPhotosGridLayout.removeAllViews();
            for (int i = 0; i < gridviewlist.size(); i++) {
                ImageView imageView = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 100)) / 4;
                params.height = params.width;
                imageView.setLayoutParams(params);

                if (gridviewlist.size() == 0 || gridviewlist.get(i).getPhotoUrl() == null || gridviewlist.get(i).getPhotoUrl().equals("")) {
                    imageView.setImageResource(R.drawable.empty);
                } else {
                    String photoUrl;
                    if (gridviewlist.get(i).getPhotoUrl().contains("http")) {
                        photoUrl = gridviewlist.get(i).getPhotoUrl();
                    } else {
                        photoUrl = Common.PHOTO_URL + gridviewlist.get(i).getPhotoUrl();
                    }
                    PictureAirLog.v(TAG, "getPhotoUrl() != null" + photoUrl);
                    GlideUtil.load(context, photoUrl, AppUtil.isEncrypted(gridviewlist.get(i).getIsEncrypted()), imageView);
                }
                imageView.setScaleType(ScaleType.CENTER_CROP);
                //imageview设置监听 订单确认界面无法选图片 和修改数量
                viewHolder.goodPhotosGridLayout.addView(imageView, params);
                viewHolder.goodPhotosGridLayout.setVisibility(View.VISIBLE);
            }
        }
        if (arrayList.get(position).getPictures() != null && arrayList.get(position).getPictures().length > 0) {
            GlideUtil.load(context, Common.PHOTO_URL + arrayList.get(position).getPictures()[0], viewHolder.goodImageView);
        }
        viewHolder.goodQuentityTextView.setText("x" + arrayList.get(position).getQty());//数量
        viewHolder.currencyTextView.setText(currency);//单位
        viewHolder.goodPriceTextView.setText(arrayList.get(position).getUnitPrice() + "");//单价
        viewHolder.goodNameTextView.setText(arrayList.get(position).getProductNameAlias());
        return convertView;
    }

    private class ViewHolder {
        ImageView goodImageView;
        TextView goodNameTextView;
        TextView currencyTextView;
        TextView goodPriceTextView;
        TextView goodQuentityTextView;
        GridLayout goodPhotosGridLayout;
    }
}
