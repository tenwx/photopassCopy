package com.pictureair.hkdlphotopass.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.adapter.BannerViewAdapter;
import com.pictureair.hkdlphotopass.util.GlideUtil;
import com.pictureair.hkdlphotopass.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class BannerView extends LinearLayout {
    private ViewPager adViewPager;
    private ViewGroup pointGroup;
    private List<ImageView> bannerViewList = new ArrayList<>();
    private List<ImageView> pointViewList = new ArrayList<>();
    private BannerViewAdapter adapter;
    private ImageView imageView;
    private LinearLayout viewpagerGroup;
    private Subscription subscription;
    private int index = 0;
    private boolean isSliding = false;
    private boolean isPlaying = false;
    private Context context;

    public BannerView(Context context) {
        super(context);
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.banner_ppp_introduce, this);
        viewpagerGroup = (LinearLayout) findViewById(R.id.view_pager_content);
        pointGroup = (ViewGroup) findViewById(R.id.iv_image);
        adViewPager = new ViewPager(context);
        //根据屏幕信息设置ViewPager广告容器的宽高
        adViewPager.setLayoutParams(new LayoutParams(ScreenUtil.getScreenWidth(context), ScreenUtil.getScreenWidth(context) / 2));
        //将ViewPager容器设置到布局文件父容器中
        viewpagerGroup.addView(adViewPager);

        adapter = new BannerViewAdapter(getContext(), bannerViewList);
        //添加适配器
        adViewPager.setAdapter(adapter);
        adViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
                isSliding = arg0 != 0;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageSelected(int arg0) {
                //当viewpager换页时 改掉下面对应的小点
                index = arg0;
                for (int i = 0; i < pointViewList.size(); i++) {
                    //设置当前的对应的小点为选中状态
                    pointViewList.get(arg0).setImageResource(R.drawable.ppp_page_selected);
                    if (arg0 != i) {
                        //设置为非选中状态
                        pointViewList.get(i).setImageResource(R.drawable.ppp_page_not_select);
                    }
                }
            }

        });
    }

    /**
     * 设置图片
     * @param list
     */
    public void setPhotos(ArrayList<String> list) {
        bannerViewList.clear();
        pointViewList.clear();
        if (pointGroup != null) {
            pointGroup.removeAllViews();
        }

        for (int i = 0; i < list.size(); i++) {//加载图片
            imageView = new ImageView(getContext());//新建一个新的imageview
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            GlideUtil.load(context, list.get(i), imageView);
            bannerViewList.add(imageView);
        }
        adapter.notifyDataSetChanged();//图片改了之后，要通知适配器改变数据

        if (list.size() != 1) {//如果数量为1的话，点点就不要了
            for (int i = 0; i < list.size(); i++) {//加载对应的小点
                LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(15, 15);
                // 设置每个小圆点距离左边的间距
                margin.setMargins(20, 0, 0, 0);
                imageView = new ImageView(getContext());
                // 设置每个小圆点的宽高
                imageView.setLayoutParams(new LayoutParams(15, 15));
                pointViewList.add(imageView);
                if (i == 0) {
                    // 默认选中第一张图片
                    imageView.setImageResource(R.drawable.ppp_page_selected);
                } else {
                    // 其他图片都设置未选中状态
                    imageView.setImageResource(R.drawable.ppp_page_not_select);
                }
                pointGroup.addView(imageView, margin);
            }
        }

    }

    /**
     * 开始自动轮播
     */
    public void bannerStartPlay() {
        if (bannerViewList.size() == 0) {
            return;
        }
        if (isPlaying) {
            return;
        } else {
            isPlaying = true;
        }
        subscription = Observable.interval(3, 3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (!isSliding) {//如果在手动滑动中，则不处理
                            index ++;
                            adViewPager.setCurrentItem(index % bannerViewList.size());
                        }
                    }
                });

    }

    /**
     * 结束自动轮播
     */
    public void bannerStopPlay() {
        isPlaying = false;
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

}
