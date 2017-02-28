package com.pictureair.photopass.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.CompositeImageProductAdapter;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 预览合成图的banner
 *
 * @author bauer_bao
 */
@SuppressLint("NewApi")
public class BannerView_PreviewCompositeProduct extends LinearLayout {
    private static final String TAG = "";
    private ViewPager adViewPager;
    private ViewGroup group;
    private List<CompositeImageProductView> bannerViewList = new ArrayList<>();
    private CompositeImageProductView compositeImageProductView;
    private ImageView imageView;
    private CompositeImageProductAdapter adapter;
    private LinearLayout viewpagerGroup;

    public BannerView_PreviewCompositeProduct(Context context) {
        super(context);

    }

    public BannerView_PreviewCompositeProduct(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.banner_compositeimageview, this);
        adViewPager = new ViewPager(context);
        adViewPager.setLayoutParams(new LayoutParams(ScreenUtil.getScreenWidth(context), ScreenUtil.getScreenWidth(context) * 3 / 4));
        group = (ViewGroup) findViewById(R.id.iv_image_compositeimage);

        viewpagerGroup = (LinearLayout) findViewById(R.id.relativelayout_compositeimage);
        viewpagerGroup.addView(adViewPager);
    }


    //改变图片
    public void changeimagepath(ArrayList<PhotoInfo> list, String goodsUrl, int viewWidth, int viewHeight,
                                int goodWidth, int goodHeight, int marginLeft, int marginTop, int photoWidth, int photoHeight, float degree, int maskBottom, int maskTop, String goodName) {
        PictureAirLog.v(TAG, "changeimage");
        bannerViewList.clear();
        group.removeAllViews();
        initImageList(list, goodsUrl, viewWidth, viewHeight, goodWidth, goodHeight, marginLeft, marginTop, photoWidth, photoHeight, degree, maskBottom, maskTop, goodName);
    }

    //初始化图片信息list
    public void initImageList(ArrayList<PhotoInfo> list, String goodsUrl, int viewWidth, int viewHeight,
                              int goodWidth, int goodHeight, int marginLeft, int marginTop, int photoWidth, int photoHeight, float degree, int maskBottom, int maskTop, String goodName) {
        int count = list.size();
        adapter = new CompositeImageProductAdapter(getContext(), bannerViewList);
        String photopathString;
        for (int i = 0; i < count; i++) {//加载图片
            //需要判断是否已经上传过了，如果图片上传过了，直接拿网络上的照片
            if (list.get(i).getIsUploaded() == 1) {//说明是已经上传了得照片，直接取网络图片
                photopathString = Common.PHOTO_URL + list.get(i).getPhotoOriginalURL();

            } else {//没有上传过，要判断是否是photopass的相册
                //获取对应的图片路径
                if (list.get(i).getIsOnLine() == 1) {
                    if (list.get(i).getIsPaid() == 1) {
                        photopathString = Common.PHOTO_URL + list.get(i).getPhotoThumbnail_512();
                        PictureAirLog.out("payed url----->" + photopathString);
                    } else {
                        photopathString = list.get(i).getPhotoThumbnail_128();
                        PictureAirLog.out("unpayed url----->" + photopathString);
                    }
                } else {
                    photopathString = "file://" + list.get(i).getPhotoOriginalURL();
                }
            }

            boolean isEncrypted = AppUtil.isEncrypted(list.get(i).getIsEnImage());

            PictureAirLog.v(TAG, "good name--------->" + goodName);
            PictureAirLog.v(TAG, "photo path--------->" + photopathString);
            //新建view
            compositeImageProductView = new CompositeImageProductView(getContext(), goodsUrl, viewWidth, viewHeight, photopathString,
                    goodWidth, goodHeight, marginLeft, marginTop, photoWidth, photoHeight, degree, maskBottom, maskTop, goodName, isEncrypted);
            bannerViewList.add(compositeImageProductView);
            adapter.notifyDataSetChanged();//图片改了之后，要通知适配器改变数据
        }
        final ImageView[] imageViews = new ImageView[count];
        if (count != 1) {//如果数量为1的话，点点就不要了
            for (int i = 0; i < count; i++) {//加载对应的小点
                LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                // 设置每个小圆点距离左边的间距
                margin.setMargins(10, 0, 0, 0);
                imageView = new ImageView(getContext());
                // 设置每个小圆点的宽高
                imageView.setLayoutParams(new LayoutParams(15, 15));
                imageViews[i] = imageView;
                if (i == 0) {
                    // 默认选中第一张图片
                    imageViews[i].setBackgroundResource(R.drawable.page_select);
                } else {
                    // 其他图片都设置未选中状态
                    imageViews[i].setBackgroundResource(R.drawable.page_not_select);
                }
                group.addView(imageViews[i], margin);
            }
        } else {

        }

        //添加适配器
        adViewPager.setAdapter(adapter);
        adViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageSelected(int arg0) {
                //当viewpager换页时 改掉下面对应的小点
                for (int i = 0; i < imageViews.length; i++) {
                    //设置当前的对应的小点为选中状态
                    imageViews[arg0].setBackgroundResource(R.drawable.page_select);
                    if (arg0 != i) {
                        //设置为非选中状态
                        imageViews[i].setBackgroundResource(R.drawable.page_not_select);
                    }
                }
            }

        });
    }
}
