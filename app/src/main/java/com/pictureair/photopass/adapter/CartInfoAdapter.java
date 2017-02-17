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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.ListViewImageView;
import com.pictureair.photopass.widget.PWToast;
import com.trello.rxlifecycle.components.RxActivity;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

/**
 * 购物车页面的ExpandableListView的适配器
 *
 * @author bauer_bao
 */
public class CartInfoAdapter extends BaseAdapter {
    private List<CartPhotosInfo> gridviewlist;
    private LayoutInflater layoutInflater;
    private ArrayList<ImageView> gridlayoutList;
    private ArrayList<CartItemInfo> goodArrayList;
    private Context context;
    private Handler handler;
    private String currency;
    private String userId;
    private ArrayList<ArrayList<ImageView>> gridLayoutLists;
    private PWToast myToast;

    public static final int MINUSCOUNT = 0;// 减少数量
    public static final int ADDCOUNT = 1;// 增加数量
    public static final int SELECTED = 3;// 选中item
    public static final int NOSELECTED = 4;// 取消选中item
    public static final int CHANGE_PHOTO = 5;//更改照片
    private static final String TAG = "CartInfoAdapter";


    public CartInfoAdapter(Context context, String currency, ArrayList<CartItemInfo> goodArrayList, String userId, Handler handler) {
        this.context = context;
        this.goodArrayList = goodArrayList;
        this.handler = handler;
        this.currency = currency;
        this.userId = userId;
        layoutInflater = LayoutInflater.from(context);
        gridLayoutLists = new ArrayList<>();
        myToast = new PWToast(context);
    }

    /**
     * 更新list
     *
     * @param cartInfoList
     */
    public void refresh(ArrayList<CartItemInfo> cartInfoList) {
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
            viewHolder.dividerLine = convertView.findViewById(R.id.divider);
//            viewHolder.cartLineImageView = (DashedLineView) convertView.findViewById(R.id.cartLine1);
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
        if (goodArrayList.get(position).getEntityType() == 0 && Common.GOOD_NAME_PPP.equals(goodArrayList.get(position).getProductName())) {//ppp商品
            GlideUtil.load(context, Common.PHOTO_URL + pictureUrl, viewHolder.cartGoodImageView);
            viewHolder.cartGoodPhotosGridLayout.setVisibility(View.GONE);
        } else {//其他商品
            PictureAirLog.v(TAG, "other product");
            GlideUtil.load(context, Common.PHOTO_URL + pictureUrl, viewHolder.cartGoodImageView);
            viewHolder.cartGoodPhotosGridLayout.setVisibility(View.VISIBLE);
            viewHolder.cartGoodPhotosGridLayout.removeAllViews();
            for (int i = 0; i < gridviewlist.size(); i++) {
                ImageView imageView = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 100)) / 4;
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
                } else {
                    String photoUrl;
                    if (gridviewlist.get(i).getPhotoUrl().contains("http")) {
                        photoUrl = gridviewlist.get(i).getPhotoUrl();
                    } else {
                        photoUrl = Common.PHOTO_URL + gridviewlist.get(i).getPhotoUrl();
                    }
                    PictureAirLog.v(TAG, "getPhotoUrl() != null" + photoUrl);
                    GlideUtil.load(context, photoUrl, imageView);
                }
            }
        }
        gridLayoutLists.add(gridlayoutList);
        viewHolder.cartGoodCountTextView.setText(goodArrayList.get(position).getQty() + "");
        viewHolder.cartCurrencyTextView.setText(currency);
        viewHolder.cartGoodProductQuentityTextView.setText("x" + goodArrayList.get(position).getQty() + "");
        viewHolder.cartGoodPriceTextView.setText(goodArrayList.get(position).getUnitPrice() + "");
        viewHolder.cartGoodNameTextView.setText(goodArrayList.get(position).getProductNameAlias());
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

        if (position == goodArrayList.size() - 1) {
            viewHolder.dividerLine.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.dividerLine.setVisibility(View.VISIBLE);
        }
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
        View dividerLine;//分割线
        //        DashedLineView cartLineImageView;//线
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
        private CartItemInfo cartItemInfo;
        private ViewHolder viewHolder;

        public SelectOnClick(ViewHolder holderView, CartItemInfo cartItemInfo) {
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
        private List<CartPhotosInfo> arraylist;
        private boolean ishandle = false;
        private CartItemInfo cartItemInfo;
        private int position;

        public ChangeCountOnclick(ViewHolder holderView, List<CartPhotosInfo> arraylist, CartItemInfo cartItemInfo, int position) {
            // TODO Auto-generated constructor stub
            this.holderView = holderView;
            this.arraylist = arraylist;
            this.cartItemInfo = cartItemInfo;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            //检查网络
            if (AppUtil.getNetWorkType(MyApplication.getInstance()) == AppUtil.NETWORKTYPE_INVALID) {
                myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                return;
            }
            if (!ishandle) {//如果已经在处理中，则忽略响应，反之，进行处理
                if (cartItemInfo.getCartProductType() == 2 && v.getId() == holderView.cartAddImageView.getId()) {//如果是pp不允许添加或者减少数量
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
        public void modifyCart(final boolean addOrminus, final int count, final CartItemInfo cartItemInfo, final Handler handler) {
            PictureAirLog.v(TAG, "modifyCart");
            API2.modifyCart(cartItemInfo.getCartId(), cartItemInfo.getGoodsKey(), count, null)
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((RxActivity)context).<JSONObject>bindToLifecycle())
                    .subscribe(new RxSubscribe<JSONObject>() {
                        @Override
                        public void _onNext(JSONObject jsonObject) {
                            PictureAirLog.v(TAG, "modifyCart onSuccess");
                            ishandle = false;
                            holderView.cartGoodCountTextView.setText(String.valueOf(count));
                            holderView.cartGoodProductQuentityTextView.setText("x" + String.valueOf(count));
                            for (int i = 0; i < arraylist.size(); i++) {
                                arraylist.get(i).setCartPhotoCount(String.valueOf(count));
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
                        public void _onError(int status) {
                            ishandle = false;
                        }

                        @Override
                        public void onCompleted() {

                        }
                    });

        }

    }

}
