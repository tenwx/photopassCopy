package com.pictureair.photopass.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

public class SubmitOrderListViewAdapter extends BaseAdapter {
    private ArrayList<CartItemInfo> arrayList;
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<ImageView> imageViews;
    private List<CartPhotosInfo> gridviewlist;
    private ArrayList<ArrayList<ImageView>> gridLayoutLists;
    private Handler handler;
    private String currency;
    private static final String TAG = "SubmitOrderListViewAdapter";

    public SubmitOrderListViewAdapter(Context context, ArrayList<CartItemInfo> list, String currency, Handler handler) {
        this.context = context;
        this.handler = handler;
        this.currency = currency;
        this.arrayList = list;
        layoutInflater = LayoutInflater.from(context);
        gridLayoutLists = new ArrayList<>();
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
            convertView = layoutInflater.inflate(R.layout.submitorder_listview_item, null);
            viewHolder.goodImageView = (ImageView) convertView.findViewById(R.id.imageView_pd);
            viewHolder.goodNameTextView = (TextView) convertView.findViewById(R.id.textView_name);
            viewHolder.currencyTextView = (TextView) convertView.findViewById(R.id.textView_currency);
            viewHolder.goodPriceTextView = (TextView) convertView.findViewById(R.id.textView_pr);
            viewHolder.goodQuentityTextView = (TextView) convertView.findViewById(R.id.editText_count1);
            viewHolder.goodPhotosGridLayout = (GridLayout) convertView.findViewById(R.id.gridView_cartphoto);
            viewHolder.goodRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.submitOrderRelativeLayout);
//            viewHolder.goodDashedLineView = (DashedLineView) convertView.findViewById(R.id.submitLine1);
            viewHolder.showOrHidePhotoImageView = (ImageView) convertView.findViewById(R.id.showOrHeight);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        gridviewlist = arrayList.get(position).getEmbedPhotos();
        if (gridviewlist == null) {
            gridviewlist = new ArrayList<>();
        }
        imageViews = new ArrayList<>();

        //初始化数据
//        if (Common.GOOD_NAME_SINGLE_DIGITAL.equals(arrayList.get(position).getProductName())) {//照片产品
//            PictureAirLog.v(TAG, "pp product");
//            if (arrayList.get(position).getPictures()[0].contains("http")) {
//                imageLoader.displayImage(arrayList.get(position).getPictures()[0], viewHolder.goodImageView);
//            } else {
//                imageLoader.displayImage(Common.BASE_URL_TEST + arrayList.get(position).getPictures()[0], viewHolder.goodImageView);
//            }
//            viewHolder.goodPhotosGridLayout.setVisibility(View.GONE);
//            viewHolder.goodDashedLineView.setVisibility(View.GONE);
//            viewHolder.showOrHidePhotoImageView.setVisibility(View.GONE);
//        } else
        if (Common.ppp.equals(arrayList.get(position).getProductName())) {//ppp产品
            PictureAirLog.v(TAG, "ppp product");
            viewHolder.goodPhotosGridLayout.setVisibility(View.GONE);
//            viewHolder.goodDashedLineView.setVisibility(View.GONE);
            viewHolder.showOrHidePhotoImageView.setVisibility(View.GONE);
            viewHolder.goodRelativeLayout.setClickable(false);
        } else {//为其他产品
            PictureAirLog.v(TAG, "other procut");

            //设置gridlayout,先清除所有的view，再重新添加view
            viewHolder.goodPhotosGridLayout.setVisibility(View.VISIBLE);
            viewHolder.goodPhotosGridLayout.removeAllViews();
            for (int i = 0; i < gridviewlist.size(); i++) {
                ImageView imageView = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 25)) / 4;
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
                imageView.setId(position * 10 + i);//给添加的imageview添加id
                imageViews.add(imageView);
                //imageview设置监听 订单确认界面无法选图片 和修改数量
//                imageView.setOnClickListener(new SubmitOrderOnClickListener(viewHolder, arrayList.get(position)));
                viewHolder.goodPhotosGridLayout.addView(imageView, params);
                viewHolder.goodPhotosGridLayout.setVisibility(View.VISIBLE);
//                viewHolder.goodDashedLineView.setVisibility(View.VISIBLE);
            }
            //是否显示图片

//            if (arrayList.get(position).getShowPhotos() == 1) {
//                viewHolder.goodPhotosGridLayout.setVisibility(View.VISIBLE);
//                viewHolder.goodDashedLineView.setVisibility(View.VISIBLE);
//                viewHolder.showOrHidePhotoImageView.setVisibility(View.VISIBLE);
//                viewHolder.showOrHidePhotoImageView.setImageResource(R.drawable.good_hide_photo);
//            } else {
//                viewHolder.showOrHidePhotoImageView.setImageResource(R.drawable.good_show_photo);
//                viewHolder.showOrHidePhotoImageView.setVisibility(View.VISIBLE);
//                viewHolder.goodPhotosGridLayout.setVisibility(View.GONE);
//                viewHolder.goodDashedLineView.setVisibility(View.GONE);
//            }
        }
        if (arrayList.get(position).getPictures() != null && arrayList.get(position).getPictures().length > 0) {
            GlideUtil.load(context, Common.PHOTO_URL + arrayList.get(position).getPictures()[0], viewHolder.goodImageView);
        }
        gridLayoutLists.add(imageViews);
        viewHolder.goodQuentityTextView.setText("x" + arrayList.get(position).getQty());//数量
        viewHolder.currencyTextView.setText(currency);//单位
        viewHolder.goodPriceTextView.setText(arrayList.get(position).getUnitPrice() + "");//单价
        viewHolder.goodNameTextView.setText(arrayList.get(position).getProductNameAlias());
        viewHolder.goodRelativeLayout.setClickable(true);
        //PP+商品不需要显示照片
//        if (arrayList.get(position).getCartProductType() != 3) {
//            viewHolder.goodRelativeLayout.setOnClickListener(new SubmitOrderOnClickListener(viewHolder, arrayList.get(position)));
//        }
        return convertView;
    }

    private class ViewHolder {
        ImageView goodImageView;
        TextView goodNameTextView;
        TextView currencyTextView;
        TextView goodPriceTextView;
        TextView goodQuentityTextView;
        GridLayout goodPhotosGridLayout;
        RelativeLayout goodRelativeLayout;
        //        DashedLineView goodDashedLineView;
        ImageView showOrHidePhotoImageView;
    }

    private class SubmitOrderOnClickListener implements OnClickListener {
        private ViewHolder viewHolder;
        private CartItemInfo cartItemInfo;

        public SubmitOrderOnClickListener(ViewHolder viewHolder, CartItemInfo cartItemInfo) {
            this.cartItemInfo = cartItemInfo;
            this.viewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.submitOrderRelativeLayout) {
                PictureAirLog.v(TAG, "Item onclick");
                if (cartItemInfo.getShowPhotos() == 1) {
                    viewHolder.goodPhotosGridLayout.setVisibility(View.GONE);
//                    viewHolder.goodDashedLineView.setVisibility(View.GONE);
                    viewHolder.showOrHidePhotoImageView.setImageResource(R.drawable.good_show_photo);
                    cartItemInfo.setShowPhotos(0);
                } else {
                    viewHolder.goodPhotosGridLayout.setVisibility(View.VISIBLE);
//                    viewHolder.goodDashedLineView.setVisibility(View.VISIBLE);
                    viewHolder.showOrHidePhotoImageView.setImageResource(R.drawable.good_hide_photo);
                    cartItemInfo.setShowPhotos(1);
                }

            } else {
                PictureAirLog.v(TAG, "photo click " + v.getId() / 10 + "_" + v.getId() % 10);
                Message message = handler.obtainMessage();
                message.what = 1;
                message.arg1 = v.getId();
                handler.sendMessage(message);

            }
        }
    }

}
