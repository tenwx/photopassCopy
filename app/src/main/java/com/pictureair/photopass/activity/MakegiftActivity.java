package com.pictureair.photopass.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.MakegiftGoodsAdapter;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.BannerView_PreviewCompositeProduct;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 制作礼物页面，按照目前来讲，其实就是商店的一个变种。初始目的，图片和商品合成的预览图
 */
public class MakegiftActivity extends BaseActivity implements OnClickListener {
    private ListView goodsGridView;
    private MakegiftGoodsAdapter mAdapter;
    private List<GoodsInfo> allList;
    private List<GoodsInfo> originalGoodsList;
    private GoodsInfo goodsInfo;

    private ImageView returnLayout;
    private ImageView cartButton;
    private Button buyButton;
    private Button addtocartButton;
    private TextView cartcountTextView, currencytextview;
    private ImageView buttonSelectproduct;

    private ViewGroup anim_mask_layout;//动画层
    private ImageView buyImg;// 这是在界面上跑的小图片
    private boolean isbuynow = false;
    private BannerView_PreviewCompositeProduct bannerView_Makegift;

    private ArrayList<PhotoInfo> photoList;
    private ArrayList<CartPhotosInfo> photoListAfterUpload;//图片上传之后的list
    //选择商品popupwindow
    private PopupWindow selproductPopupWindow;
    private View selproductView_popwindow;

    private TextView priceTextView;
    private TextView introduceTextView;
    private TextView addressTextView;
    private int recordcount;

    private PWToast newToast;

    private int previewViewWidth;
    private int previewViewHeight;

    private final static int WAIT_DRAW_FINISH = 111;
    private LinearLayout productNameLl;

    private final static String TAG = "MakegiftAct";

    private final Handler makeGiftHandler = new MakeGiftHandler(this);


    private static class MakeGiftHandler extends Handler{
        private final WeakReference<MakegiftActivity> mActivity;

        public MakeGiftHandler(MakegiftActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    /**
     * 处理Message
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case WAIT_DRAW_FINISH:
                //此处，如果数据已经返回，但是控件还没有画好的话，会显示不出来。需要做判断
                if (previewViewWidth != 0 && previewViewHeight != 0) {//onCreate已经执行完，显示图片
                    PictureAirLog.out("--------->ok");
                    setProductImage(goodsInfo.getName(), (goodsInfo.getPictures().size() > 0) ? goodsInfo.getPictures().get(0).getUrl() : "");
                } else {//onCreate还没执行完，需要等待
                    PictureAirLog.out("---------->not ok, waiting.....");
                    makeGiftHandler.sendEmptyMessageDelayed(WAIT_DRAW_FINISH, 500);
                }
                break;

            default:
                break;

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_makegift);
        init();
    }

    private void init() {
        newToast = new PWToast(this);
        currencytextview = (TextView) findViewById(R.id.textView2);
        currencytextview.setText( Common.DEFAULT_CURRENCY);//SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CURRENCY, Common.DEFAULT_CURRENCY)
        priceTextView = (TextView) findViewById(R.id.textview_productprice);
        introduceTextView = (TextView) findViewById(R.id.product_detail);
        addressTextView = (TextView) findViewById(R.id.detail_receive_address);
        returnLayout = (ImageView) findViewById(R.id.rt);
        returnLayout.setOnClickListener(this);
        cartButton = (ImageView) findViewById(R.id.button_cart);
        productNameLl = (LinearLayout) findViewById(R.id.product_name_ll);
        buyButton = (Button) findViewById(R.id.button_buy);
        addtocartButton = (Button) findViewById(R.id.button_addtocart);
        buyButton.setTypeface(MyApplication.getInstance().getFontBold());
        addtocartButton.setTypeface(MyApplication.getInstance().getFontBold());
        currencytextview.setTypeface(MyApplication.getInstance().getFontBold());
        priceTextView.setTypeface(MyApplication.getInstance().getFontBold());
        buttonSelectproduct = (ImageView) findViewById(R.id.button_selectproduct);
        cartButton.setOnClickListener(this);

        productNameLl.setOnClickListener(this);
        buyButton.setOnClickListener(this);
        addtocartButton.setOnClickListener(this);
        cartcountTextView = (TextView) findViewById(R.id.textview_cart_count);
        cartcountTextView.setOnClickListener(this);
        showPWProgressDialog();
        allList = new ArrayList<>();
        originalGoodsList = new ArrayList<>();
        getGoods();
        recordcount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
        if (recordcount <= 0) {
            cartcountTextView.setVisibility(View.INVISIBLE);
        } else {
            cartcountTextView.setVisibility(View.VISIBLE);
            cartcountTextView.setText(recordcount + "");
        }
        photoList = new ArrayList<>();
        photoListAfterUpload = new ArrayList<>();
        PhotoInfo itemInfo = getIntent().getParcelableExtra("selectPhoto");
        photoList.add(itemInfo);
        bannerView_Makegift = (BannerView_PreviewCompositeProduct) findViewById(R.id.bannerview_makegift_detail);
        ViewTreeObserver viewTreeObserver = bannerView_Makegift.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                bannerView_Makegift.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                previewViewWidth = bannerView_Makegift.getWidth();
                previewViewHeight = bannerView_Makegift.getHeight();
            }
        });
        //选择商品的popupwindow
        selproductView_popwindow = getLayoutInflater().inflate(R.layout.popupwindow_selectproduct, null);
        selproductPopupWindow = new PopupWindow(selproductView_popwindow, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        selproductPopupWindow.setFocusable(false);//设置能够获得焦点
        selproductPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//此代码和上一条代码两者结合，实现能够点击popupwindow外面将popupwindow关闭
        goodsGridView = (ListView) selproductView_popwindow.findViewById(R.id.id_horizontalScrollView);
        mAdapter = new MakegiftGoodsAdapter(MakegiftActivity.this, allList);

        goodsGridView.setAdapter(mAdapter);
        goodsGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                goodsInfo = allList.get(position);
                priceTextView.setText(goodsInfo.getPrice() + "");
//                selectButton.setText(goodsInfo.getNameAlias());
                introduceTextView.setText(goodsInfo.getDescription());
                setProductImage(goodsInfo.getName(), (goodsInfo.getPictures().size() > 0) ? goodsInfo.getPictures().get(0).getUrl() : "");
                gonePopupwindow();
            }
        });

        selproductView_popwindow.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                gonePopupwindow();
                return false;
            }
        });
    }

    /**
     * popwindow消失
     */
    private void gonePopupwindow() {
        if (selproductPopupWindow.isShowing()) {
            selproductPopupWindow.dismiss();
        }
    }

    /**
     * 设置makeGift的背景预览图
     *
     * @param productName 商品名字
     * @param productURL  商品预览图的背景图片URL
     */
    private void setProductImage(String productName, String productURL) {
        PictureAirLog.out("------->" + productURL);
        if (productName.equals("canvas")) {
            //1.画布，商品宽 355
            //		 商品高 258
            //       左边留白 20
            //		 上边留白 12
            //		 预览图片宽 355-20-19 = 316
            //		 预览图片高 258-12-19 = 227
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 355, 258, 20, 12, 316, 227, 0, 0, 0, "canvas");//设置bannerview的图片
//		}else if (productName.equals("iphone5Case")) {
//			//2.手机后盖，商品宽 480
//			//		 商品高 946
//			//       左边留白 0
//			//		 上边留白 0
//			//		 预览图片宽 480
//			//		 预览图片高 946
//			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 480, 946, 0, 0, 480, 946, 0, R.drawable.iphone_case_mask_bottom, R.drawable.iphone_case_mask_top, "iphone5Case");//设置bannerview的图片
        } else if (productName.equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {
            //3.数码商品，商品宽 300
            //		 商品高 217
            //       左边留白 22
            //		 上边留白 26
            //		 预览图片宽 300-22-21 = 257
            //		 预览图片高 217-26-25 = 166
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 300, 217, 22, 26, 257, 166, 0, 0, 0, Common.GOOD_NAME_SINGLE_DIGITAL);//设置bannerview的图片
        } else if (productName.equals("4R Print")) {
            //4.4r相框，商品宽 180
            //		 商品高 120
            //       左边留白 7
            //		 上边留白 7
            //		 预览图片宽 180-7-7 = 166
            //		 预览图片高 120-7-7 = 106
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 180, 120, 7, 7, 166, 106, 0, 0, 0, "4R Print");//设置bannerview的图片
        } else if (productName.equals(Common.GOOD_NAME_6R) || productName.equals(Common.GOOD_NAME_COOK)) {
            //5.6r相框，商品宽 240
            //		 商品高 180
            //       左边留白 10
            //		 上边留白 14
            //		 预览图片宽 240-10-10 = 220
            //		 预览图片高 180-14-14 = 152
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 240, 180, 10, 14, 220, 152, 0, 0, 0, productName);//设置bannerview的图片
        } else if (productName.equals("keyChain")) {
            //6.钥匙圈，商品宽 205
            //		 商品高 89
            //       左边留白 88
            //		 上边留白 18
            //		 预览图片宽 205-88-21 = 205 - 109 = 96
            //		 预览图片高 89-18-16 = 55
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 205, 89, 88, 18, 96, 55, 0.15f, 0, 0, "keyChain");//设置bannerview的图片
//		}else if (productName.equals("mug")) {
//			//7.杯子，商品宽 185
//			//		 商品高 160
//			//       左边留白 10
//			//		 上边留白 12
//			//		 预览图片宽 185-10-61 = 114
//			//		 预览图片高 160-12-34 = 114
//			bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 185, 160, 10, 12, 114, 114, 0, 0, R.drawable.mug_mask_top, "mug");//设置bannerview的图片
        } else {
            //3.数码商品，商品宽 300
            //		 商品高 217
            //       左边留白 22
            //		 上边留白 26
            //		 预览图片宽 300-22-21 = 257
            //		 预览图片高 217-26-25 = 166
            bannerView_Makegift.changeimagepath(photoList, productURL, previewViewWidth, previewViewHeight, 300, 217, 22, 26, 257, 166, 0, 0, 0, Common.GOOD_NAME_SINGLE_DIGITAL);//设置bannerview的图片
        }
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        recordcount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
        if (recordcount <= 0) {
            cartcountTextView.setVisibility(View.INVISIBLE);
        } else {
            cartcountTextView.setVisibility(View.VISIBLE);
            cartcountTextView.setText(recordcount + "");
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent;
        switch (v.getId()) {
            case R.id.rt://返回按钮
                finish();
                break;

            case R.id.textview_cart_count:
            case R.id.button_cart:
                intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                break;

            case R.id.product_name_ll:
                PictureAirLog.out("选择商品");
                if (allList.size() == 0) {
                    newToast.setTextAndShow(R.string.http_error_code_5005, Toast.LENGTH_SHORT);
                    return;
                }
                if (selproductPopupWindow.isShowing()) {
                    selproductPopupWindow.dismiss();
                } else {
                    mAdapter.notifyDataSetChanged();
                    selproductPopupWindow.showAsDropDown(findViewById(R.id.product_name_tv));
                }
                break;

            case R.id.button_buy:
                isbuynow = true;//buy now
                addCartAndBuy();
                break;

            case R.id.button_addtocart:
                isbuynow = false;//add to cart
                addCartAndBuy();
                break;

            default:
                break;

        }
    }

    /**
     * 获取商品数据
     */
    private void getGoods() {
        //从缓层中获取数据
        String goodsByACache = ACache.get(this).getAsString(Common.ALL_GOODS);
        Observable.just(goodsByACache)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(String s) {
                        if (TextUtils.isEmpty(s)) {
                            return API2.getGoods()
                                    .map(new Func1<JSONObject, JSONObject>() {
                                        @Override
                                        public JSONObject call(JSONObject jsonObject) {
                                            ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, jsonObject.toString(), ACache.TIME_DAY);
                                            return jsonObject;
                                        }
                                    });
                        } else {
                            return Observable.just(JSONObject.parseObject(s));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.v(TAG, "GET_GOODS_SUCCESS---->" + jsonObject.toString());
                        GoodsInfoJson goodsInfoJson = JsonTools.parseObject(jsonObject, GoodsInfoJson.class);//GoodsInfoJson.getString()

                        allList.clear();
                        if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
                            originalGoodsList = goodsInfoJson.getGoods();
                            PictureAirLog.v(TAG, "goods size: " + allList.size());
                        }

                        for (int i = 0; i < originalGoodsList.size(); i++) {
                            if (!Common.GOOD_NAME_PPP.equals(originalGoodsList.get(i).getName()) &&
                                    !Common.GOOD_NAME_SINGLE_DIGITAL.equals(originalGoodsList.get(i).getName()) &&
                                    originalGoodsList.get(i).getEmbedPhotosCount() == 1) {
                                allList.add(originalGoodsList.get(i));
                            }
                        }

                        if (allList.size() > 0) {
                            goodsInfo = allList.get(0);
                            priceTextView.setText(allList.get(0).getPrice() + "");
                            introduceTextView.setText(allList.get(0).getDescription());
                            makeGiftHandler.sendEmptyMessageDelayed(WAIT_DRAW_FINISH, 500);
                        } else {
                            currencytextview.setVisibility(View.GONE);
                            addressTextView.setVisibility(View.GONE);
                        }

                        dismissPWProgressDialog();
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 加入购物车或者购买
     */
    private void addCartAndBuy() {
        if (goodsInfo == null) {
            newToast.setTextAndShow(R.string.http_error_code_5005, Toast.LENGTH_SHORT);
            return;
        }
        showPWProgressDialog();

        Observable.just(photoList)
                .subscribeOn(Schedulers.io())
                .map(new Func1<ArrayList<PhotoInfo>, ArrayList<PhotoInfo>>() {
                    @Override
                    public ArrayList<PhotoInfo> call(ArrayList<PhotoInfo> arrayList) {
                        for (int i = 0; i < arrayList.size(); i++) {
                            //服务器上获取的图片，只需要将photoid获取就行
                            PhotoInfo info = arrayList.get(i);
                            info.setPhotoOriginalURL(info.getPhotoThumbnail_512());
                        }
                        return arrayList;
                    }
                })
                .flatMap(new Func1<ArrayList<PhotoInfo>, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(ArrayList<PhotoInfo> photoInfos) {
                        //编辑传入照片的信息
                        JSONArray embedPhotos = new JSONArray();//放入图片的图片id数组
                        for (int i = 0; i < photoInfos.size(); i++) {
                            JSONObject photoid = new JSONObject();
                            photoid.put("photoId", photoInfos.get(i).getPhotoId());
                            embedPhotos.add(photoid);
                        }
                        PictureAirLog.v(TAG, embedPhotos.toString());
                        //开始加入购物车
                        return API2.addToCart(goodsInfo.getGoodsKey(), 1, isbuynow, embedPhotos);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.d("add to cart ---> " + jsonObject);
                        dismissPWProgressDialog();
                        int currentCartCount = SPUtils.getInt(MakegiftActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        SPUtils.put(MakegiftActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + 1);
                        String itemidString = jsonObject.getString("cartId");
                        if (isbuynow) {//获取订单信息，传送到下一界面
                            Intent intent = new Intent(MakegiftActivity.this, SubmitOrderActivity.class);
                            ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();
                            CartItemInfo cartItemInfo = new CartItemInfo();
                            cartItemInfo.setProductName(goodsInfo.getName());
                            cartItemInfo.setProductNameAlias(goodsInfo.getNameAlias());
                            cartItemInfo.setPrice(goodsInfo.getPrice() * 1);
                            cartItemInfo.setUnitPrice(goodsInfo.getPrice());
                            cartItemInfo.setEntityType(goodsInfo.getEntityType());
                            cartItemInfo.setCartProductType(1);

                            photoListAfterUpload.clear();
                            for (int i = 0; i < photoList.size(); i++) {
                                CartPhotosInfo cartPhotosInfo = new CartPhotosInfo();
                                cartPhotosInfo.setPhotoUrl(photoList.get(i).getPhotoOriginalURL());
                                cartPhotosInfo.setPhotoId(photoList.get(i).getPhotoId());
                                cartPhotosInfo.setIsEncrypted(photoList.get(i).getIsEnImage());
                                photoListAfterUpload.add(cartPhotosInfo);
                            }

                            cartItemInfo.setEmbedPhotos(photoListAfterUpload);
                            cartItemInfo.setDescription(goodsInfo.getDescription());
                            cartItemInfo.setQty(1);
                            cartItemInfo.setCartId(itemidString);//会返回此数据
                            cartItemInfo.setStoreId(goodsInfo.getStoreId());
                            if (goodsInfo.getPictures() != null && goodsInfo.getPictures().size() > 0) {
                                String[] cartProductImageUrl = new String[goodsInfo.getPictures().size()];
                                for (int i = 0; i < goodsInfo.getPictures().size(); i++) {
                                    cartProductImageUrl[i] = goodsInfo.getPictures().get(i).getUrl();
                                }
                                cartItemInfo.setPictures(cartProductImageUrl);
                            }
                            cartItemInfo.setGoodsKey(goodsInfo.getGoodsKey());
                            orderinfoArrayList.add(cartItemInfo);
                            intent.putExtra("orderinfo", orderinfoArrayList);
                            intent.putExtra("activity", "previewproduct");
                            startActivity(intent);

                        } else {
                            buyImg = new ImageView(MakegiftActivity.this);// buyImg是动画的图片
                            buyImg.setImageResource(R.drawable.addtocart);// 设置buyImg的图片
                            setAnim(buyImg);// 开始执行动画
                        }
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /*
     * 设置动画
     */
    private void setAnim(final View v) {
        anim_mask_layout = null;
        anim_mask_layout = createAnimLayout();
        int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
        start_location[0] = ScreenUtil.getScreenWidth(this) / 2 - Common.CART_WIDTH;//减去的值和图片大小有关系
        start_location[1] = ScreenUtil.getScreenHeight(this) / 2 - Common.CART_HEIGHT;
        // 将组件添加到我们的动画层上
        final View view = addViewToAnimLayout(anim_mask_layout, v, start_location);
        int[] end_location = new int[2];
        cartcountTextView.getLocationInWindow(end_location);
        // 计算位移
        final int endX = end_location[0] - start_location[0];
        final int endY = end_location[1] - start_location[1];

        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setInterpolator(new LinearInterpolator());//匀速
        scaleAnimation.setRepeatCount(0);//不重复
        scaleAnimation.setFillAfter(true);//停在最后动画
        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(false);
        set.addAnimation(scaleAnimation);
        set.setDuration(500);//动画整个时间
        view.startAnimation(set);//开始动画
        set.setAnimationListener(new AnimationListener() {
            // 动画的开始
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            // 动画的结束
            @Override
            public void onAnimationEnd(Animation animation) {
                //x轴的路径动画，匀速
                TranslateAnimation translateAnimationX = new TranslateAnimation(0,
                        endX, 0, 0);
                translateAnimationX.setInterpolator(new LinearInterpolator());
                translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
                //y轴的路径动画，加速
                TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0,
                        0, endY);
                translateAnimationY.setInterpolator(new AccelerateInterpolator());
                translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
                ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                AnimationSet set2 = new AnimationSet(false);
                //要先添加形状的，后添加位移的，不然动画效果不能达到要求
                set2.addAnimation(scaleAnimation);
                set2.addAnimation(translateAnimationY);
                set2.addAnimation(translateAnimationX);

                set2.setFillAfter(false);
                set2.setStartOffset(200);//等待时间
                set2.setDuration(800);// 动画的执行时间
                view.startAnimation(set2);
                set2.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // TODO Auto-generated method stub
                        v.setVisibility(View.GONE);//控件消失
                        int i = SPUtils.getInt(MakegiftActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        if (i <= 0) {
                            cartcountTextView.setVisibility(View.INVISIBLE);
                        } else {
                            cartcountTextView.setVisibility(View.VISIBLE);
                            cartcountTextView.setText(i + "");
                        }
                    }
                });
            }
        });
    }

    private ViewGroup createAnimLayout() {
        ViewGroup rootView = (ViewGroup) this.getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    private View addViewToAnimLayout(ViewGroup vg, final View view,
                                     int[] location) {
        int x = location[0];
        int y = location[1];
        vg.addView(view);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }

    @Override
    protected void onPause() {
        super.onPause();
        gonePopupwindow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        makeGiftHandler.removeCallbacksAndMessages(null);
    }
}
