package com.pictureair.photopass.adapter;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfo1;
import com.pictureair.photopass.entity.CartPhotosInfo1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpCallback;
import com.pictureair.photopass.util.HttpUtil1;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.ListViewImageView;
import com.pictureair.photopass.widget.MyToast;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车页面的ExpandableListView的适配器
 *
 * @author bauer_bao
 */
public class CartInfoAdapter extends BaseAdapter {
    private List<CartPhotosInfo1> gridviewlist;
    private LayoutInflater layoutInflater;
    private ArrayList<ImageView> gridlayoutList;
    private ArrayList<CartItemInfo1> goodArrayList;
    private Context context;
    private Handler handler;
    private String currency;
    private String userId;
    private ImageLoader imageLoader;
    private ArrayList<ArrayList<ImageView>> gridLayoutLists;
    private MyToast myToast;

    public static final int MINUSCOUNT = 0;// 减少数量
    public static final int ADDCOUNT = 1;// 增加数量
    public static final int SELECTED = 3;// 选中item
    public static final int NOSELECTED = 4;// 取消选中item
    public static final int CHANGE_PHOTO = 5;//更改照片
    private static final String TAG = "CartInfoAdapter";


    public CartInfoAdapter(Context context, String currency, ArrayList<CartItemInfo1> goodArrayList, String userId, Handler handler) {
        this.context = context;
        this.goodArrayList = goodArrayList;
        this.handler = handler;
        this.currency = currency;
        this.userId = userId;
        layoutInflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        gridLayoutLists = new ArrayList<>();
        myToast = new MyToast(context);
    }

    /**
     * 更新list
     *
     * @param cartInfoList
     */
    public void refresh(ArrayList<CartItemInfo1> cartInfoList) {
        this.goodArrayList = cartInfoList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return goodArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return goodArrayList.get(position);
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
            convertView = layoutInflater.inflate(R.layout.cart_listview_item, null);
            viewHolder.selectedImageView = (ImageView) convertView.findViewById(R.id.cartSelectImageView);
            viewHolder.cartGoodImageView = (ImageView) convertView.findViewById(R.id.cartProductImageView);
            viewHolder.cartGoodNameTextView = (TextView) convertView.findViewById(R.id.cartProductName);
            viewHolder.cartCurrencyTextView = (TextView) convertView.findViewById(R.id.cartCurrency);
            viewHolder.cartGoodPriceTextView = (TextView) convertView.findViewById(R.id.cartPrice);
            viewHolder.cartGoodProductQuentityTextView = (TextView) convertView.findViewById(R.id.cartCount);
            viewHolder.cartReduceImageView = (ListViewImageView) convertView.findViewById(R.id.cartRemoveImageView);
            viewHolder.cartAddImageView = (ListViewImageView) convertView.findViewById(R.id.cartAddImageView);
            viewHolder.cartGoodCountTextView = (TextView) convertView.findViewById(R.id.cartProductCountTextView);
            viewHolder.cartGoodPhotosGridLayout = (GridLayout) convertView.findViewById(R.id.cartPhotoGridLayout);
            viewHolder.editBarLayout = (LinearLayout) convertView.findViewById(R.id.cartEditBar);
//            viewHolder.cartLineImageView = (DashedLineView) convertView.findViewById(R.id.cartLine1);
            viewHolder.hideImageView = (ImageView) convertView.findViewById(R.id.hideView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //初始化数据
        if (goodArrayList.get(position).getIsSelect()) {
            viewHolder.selectedImageView.setImageResource(R.drawable.cart_select);
        } else {
            viewHolder.selectedImageView.setImageResource(R.drawable.cart_not_select);
        }
        gridviewlist = goodArrayList.get(position).getEmbedPhotos();
        gridlayoutList = new ArrayList<>();
        String pictureUrl;
        String[] pictureUrlArray = goodArrayList.get(position).getPictures();
        if (pictureUrlArray != null && pictureUrlArray.length > 0) {
            pictureUrl = pictureUrlArray[0];
        } else {
            pictureUrl = "";
        }
        PictureAirLog.v(TAG, "pictureUrl" + pictureUrl);
        //设置商品图片
//        if (Common.GOOD_NAME_SINGLE_DIGITAL.equals(goodArrayList.get(position).getProductName())) {//照片商品
//            if (!pictureUrl.contains("http")) {
//                pictureUrl = Common.PHOTO_URL + pictureUrl;
//            }
//            if (pictureUrl.contains("productImage/gift-singleDigital.jpg")) {
//                pictureUrl = pictureUrl.replace("4000", "3001");
//            }
//            imageLoader.displayImage(pictureUrl, viewHolder.cartGoodImageView);
//            viewHolder.cartGoodPhotosGridLayout.setVisibility(View.GONE);
//            viewHolder.cartLineImageView.setVisibility(View.GONE);
//            viewHolder.hideImageView.setVisibility(View.GONE);
//
//        } else
        if (Common.ppp.equals(goodArrayList.get(position).getProductName())) {//ppp商品
            imageLoader.displayImage(Common.PHOTO_URL + pictureUrl, viewHolder.cartGoodImageView);
            viewHolder.cartGoodPhotosGridLayout.setVisibility(View.GONE);
//            viewHolder.cartLineImageView.setVisibility(View.GONE);
            viewHolder.hideImageView.setVisibility(View.GONE);
        } else {//其他商品
            PictureAirLog.v(TAG, "other product");
            imageLoader.displayImage(Common.PHOTO_URL + pictureUrl, viewHolder.cartGoodImageView);
//            viewHolder.cartLineImageView.setVisibility(View.VISIBLE);
            viewHolder.cartGoodPhotosGridLayout.setVisibility(View.VISIBLE);
            viewHolder.hideImageView.setVisibility(View.INVISIBLE);
            viewHolder.cartGoodPhotosGridLayout.removeAllViews();
            if (0 == gridviewlist.size()) {//如果照片数量为0
                PictureAirLog.v(TAG, "0 == gridviewlist.size()");
                ImageView imageView = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 25)) / 4;
                params.height = params.width;
                imageView.setLayoutParams(params);
                imageView.setImageResource(R.drawable.empty);
                imageView.setScaleType(ScaleType.CENTER_CROP);
                imageView.setId(position * 10);//给添加的imageview添加id
                gridlayoutList.add(imageView);
                //imageview设置监听
                imageView.setOnClickListener(new PhotoOnClickListener());
                viewHolder.cartGoodPhotosGridLayout.addView(imageView, params);
                TextView textView = new TextView(context);
                GridLayout.LayoutParams params2 = new GridLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                textView.setLayoutParams(params2);
                textView.setText(R.string.add_photo);
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundColor(context.getResources().getColor(R.color.orange));
                viewHolder.cartGoodPhotosGridLayout.addView(textView, params2);

            } else {//有照片数量
                for (int i = 0; i < gridviewlist.size(); i++) {
                    ImageView imageView = new ImageView(context);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 25)) / 4;
                    params.height = params.width;
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ScaleType.CENTER_CROP);
                    imageView.setId(position * 10 + i);//给添加的imageview添加id
                    gridlayoutList.add(imageView);
                    //imageview设置监听
                    imageView.setOnClickListener(new PhotoOnClickListener());
                    viewHolder.cartGoodPhotosGridLayout.addView(imageView, params);
                    if (gridviewlist.get(i).getPhotoUrl() == null || gridviewlist.get(i).getPhotoUrl().equals("")) {
                        PictureAirLog.v(TAG, "getPhotoUrl() == null");
                        imageView.setImageResource(R.drawable.empty);
                        TextView textView = new TextView(context);
                        GridLayout.LayoutParams params2 = new GridLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                        textView.setLayoutParams(params2);
                        textView.setText(R.string.add_photo);
                        textView.setTextColor(Color.WHITE);
                        textView.setBackgroundColor(context.getResources().getColor(R.color.orange));
                        viewHolder.cartGoodPhotosGridLayout.addView(textView, params2);
                    } else {
                        String photoUrl;
                        if (gridviewlist.get(i).getPhotoUrl().contains("http")) {
                            photoUrl = gridviewlist.get(i).getPhotoUrl();
                        } else {
                            photoUrl = Common.PHOTO_URL + gridviewlist.get(i).getPhotoUrl();
                        }
                        PictureAirLog.v(TAG, "getPhotoUrl() != null" + photoUrl);
                        imageLoader.displayImage(photoUrl, imageView);
                    }
                }
            }
        }
        gridLayoutLists.add(gridlayoutList);
        viewHolder.cartGoodCountTextView.setText(goodArrayList.get(position).getQty() + "");
        viewHolder.cartCurrencyTextView.setText(currency);
        viewHolder.cartGoodProductQuentityTextView.setText("x" + goodArrayList.get(position).getQty() + "");
        viewHolder.cartGoodPriceTextView.setText(goodArrayList.get(position).getUnitPrice() + "");
        viewHolder.cartGoodNameTextView.setText(goodArrayList.get(position).getProductName());
        viewHolder.cartAddImageView.setOnClickListener(new ChangeCountOnclick(viewHolder, gridviewlist, goodArrayList.get(position), position));
        viewHolder.cartReduceImageView.setOnClickListener(new ChangeCountOnclick(viewHolder, gridviewlist, goodArrayList.get(position), position));
        if (goodArrayList.get(position).getShowEdit() == 1) {
            viewHolder.editBarLayout.setVisibility(View.VISIBLE);
            viewHolder.cartGoodNameTextView.setVisibility(View.GONE);
        } else {
            viewHolder.editBarLayout.setVisibility(View.GONE);
            viewHolder.cartGoodNameTextView.setVisibility(View.VISIBLE);
        }
        if (goodArrayList.get(position).getQty() > 1) {
            viewHolder.cartReduceImageView.setEnabled(true);//当数量>1时 可点击
        } else {
            viewHolder.cartReduceImageView.setEnabled(false);//当数量为1时,不可点击
        }
        viewHolder.selectedImageView.setOnClickListener(new SelectOnClick(viewHolder, goodArrayList.get(position)));
        return convertView;
    }

    private class ViewHolder {
        ImageView selectedImageView;//商品选择按钮
        ImageView cartGoodImageView;//商品预览图
        TextView cartGoodNameTextView;//商品名称
        TextView cartCurrencyTextView;//币种
        TextView cartGoodPriceTextView;//商品单价
        TextView cartGoodProductQuentityTextView;//商品数量
        ListViewImageView cartReduceImageView;//减少数量
        ListViewImageView cartAddImageView;//增加数量
        TextView cartGoodCountTextView;//商品数量（编辑数量的时候的数量）
        GridLayout cartGoodPhotosGridLayout;//商品携带图片的控件
        LinearLayout editBarLayout;//编辑数量
        //        DashedLineView cartLineImageView;//线
        ImageView hideImageView;//占位置的一个View
    }

    private class PhotoOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            PictureAirLog.v(TAG, "photo click " + v.getId() / 10 + "_" + v.getId() % 10);
            Message message = handler.obtainMessage();
            message.what = CHANGE_PHOTO;
            message.arg1 = v.getId();
            handler.sendMessage(message);
        }
    }

    //选择按键监听， 取消选中的时候，需要将对应列的价格减去，同时更新总的订单信息
    private class SelectOnClick implements OnClickListener {
        private CartItemInfo1 cartItemInfo;
        private ViewHolder viewHolder;

        public SelectOnClick(ViewHolder holderView, CartItemInfo1 cartItemInfo) {
            this.viewHolder = holderView;
            this.cartItemInfo = cartItemInfo;
        }

        @Override
        public void onClick(View v) {
            Message message = handler.obtainMessage();
            if (cartItemInfo.getIsSelect()) {//取消选中
                viewHolder.selectedImageView.setImageResource(R.drawable.cart_not_select);
                cartItemInfo.setIsSelect(false);
                message.what = NOSELECTED;
            } else {
                viewHolder.selectedImageView.setImageResource(R.drawable.cart_select);
                cartItemInfo.setIsSelect(true);
                message.what = SELECTED;
            }
            message.obj = cartItemInfo.getUnitPrice() * cartItemInfo.getQty();
            handler.sendMessage(message);
        }
    }

    /**
     * 修改购物车数量的监听
     */
    private class ChangeCountOnclick implements OnClickListener {
        private ViewHolder holderView;
        private List<CartPhotosInfo1> arraylist;
        private boolean ishandle = false;
        private CartItemInfo1 cartItemInfo;
        private int position;

        public ChangeCountOnclick(ViewHolder holderView, List<CartPhotosInfo1> arraylist, CartItemInfo1 cartItemInfo, int position) {
            // TODO Auto-generated constructor stub
            this.holderView = holderView;
            this.arraylist = arraylist;
            this.cartItemInfo = cartItemInfo;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            //检查网络
            if(AppUtil.getNetWorkType(MyApplication.getInstance()) == AppUtil.NETWORKTYPE_INVALID){
                myToast.setTextAndShow(R.string.no_network,Common.TOAST_SHORT_TIME);
                return;
            }
            if (!ishandle) {//如果已经在处理中，则忽略响应，反之，进行处理
                if (cartItemInfo.getCartProductType() == 2) {//如果是pp不允许添加或者减少数量
                    myToast.setTextAndShow(R.string.cannot_change_count, Common.TOAST_SHORT_TIME);
                    return;
                }
                ishandle = true;
                int count = Integer.parseInt(holderView.cartGoodCountTextView.getText().toString());
                boolean addcount = false;//true 代表添加操作，false代表减少操作
                if (v.getId() == holderView.cartAddImageView.getId()) {
                    //添加按钮
                    PictureAirLog.v(TAG, "add item count");
                    count++;
                    addcount = true;
                    holderView.cartReduceImageView.setEnabled(true);//激活减少按钮
                } else if (v.getId() == holderView.cartReduceImageView.getId()) {
                    //减少按钮
                    PictureAirLog.v(TAG, "remove");
                    if (count > 1) {// 判断数量是否小于1件，如果小于1，则不让更改
                        count--;
                        if (count <= 1) {
                            //减完后=1，也不可点击
                            holderView.cartReduceImageView.setEnabled(false);//当数量为1时,不可点击
                        }
                        addcount = false;
                    } else {
                        holderView.cartReduceImageView.setEnabled(false);//当数量为1时  不可点击
                        ishandle = false;
                        return;
                    }
                }
                modifyCart(addcount, count, cartItemInfo, handler);

            } else {
                PictureAirLog.v(TAG, "is still change count");
            }
        }

        /**
         * 修改购物车
         *
         * @param addOrminus   addOrminus
         * @param cartItemInfo 购物车项id参数(可选,不填时为移除全部)
         * @param handler      handler
         */
        public void modifyCart(final boolean addOrminus, final int count, final CartItemInfo1 cartItemInfo, final Handler handler) {
            PictureAirLog.v(TAG, "modifyCart");
            RequestParams params = new RequestParams();
            params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
            params.put(Common.GOODS_KEY, cartItemInfo.getGoodsKey());
            params.put(Common.QTY, count);
            String url = Common.BASE_URL_TEST + Common.ADD_TO_CART + "/" + cartItemInfo.getCartId();
            HttpUtil1.asyncPut(url, params, new HttpCallback() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    super.onSuccess(jsonObject);
                    PictureAirLog.v(TAG, "modifyCart onSuccess");
                    ishandle = false;
                    holderView.cartGoodCountTextView.setText(String.valueOf(count));
                    holderView.cartGoodProductQuentityTextView.setText("x" + String.valueOf(count));
                    for (int i = 0; i < arraylist.size(); i++) {
                        CartPhotosInfo1 map = arraylist.get(i);
                        map.setCartPhotoCount(String.valueOf(count));
                        arraylist.set(i, map);
                    }
                    cartItemInfo.setEmbedPhotos(arraylist);
                    cartItemInfo.setQty(count);
                    Message message = handler.obtainMessage();
                    if (addOrminus) {
                        message.what = ADDCOUNT;
                    } else {
                        message.what = MINUSCOUNT;
                    }
                    if (cartItemInfo.getIsSelect()) {
                        message.arg1 = 1;
                    } else {
                        message.arg1 = 0;
                    }
                    message.arg2 = position;
                    message.obj = cartItemInfo.getUnitPrice();
                    handler.sendMessage(message);
                }

                @Override
                public void onFailure(int status) {
                    super.onFailure(status);
                    ishandle = false;
                }
            });
        }

    }

}
