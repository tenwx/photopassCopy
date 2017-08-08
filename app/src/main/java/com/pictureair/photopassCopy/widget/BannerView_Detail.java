package com.pictureair.photopassCopy.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.adapter.BannerViewAdapter;
import com.pictureair.photopassCopy.entity.GoodInfoPictures;
import com.pictureair.photopassCopy.util.Common;
import com.pictureair.photopassCopy.util.GlideUtil;
import com.pictureair.photopassCopy.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品详情的图片banner
 *
 * @author bauer_bao
 */
public class BannerView_Detail extends LinearLayout {
    private ViewPager adViewPager;
    private ViewGroup group;
    private List<ImageView> bannerViewList = new ArrayList<>();
    private BannerViewAdapter adapter;
    private ImageView imageView;
    private LinearLayout viewpagerGroup;

    public BannerView_Detail(Context context) {
        super(context);
    }

    public BannerView_Detail(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.banner_ppp_introduce, this);
        viewpagerGroup = (LinearLayout) findViewById(R.id.view_pager_content);
        adViewPager = new ViewPager(context);
        group = (ViewGroup) findViewById(R.id.iv_image);
        adViewPager.setLayoutParams(new LayoutParams(ScreenUtil.getScreenWidth(context), ScreenUtil.getScreenWidth(context) * 3 / 4));
        viewpagerGroup.addView(adViewPager);
    }

    public void findimagepath(List<GoodInfoPictures> list) {
        // TODO Auto-generated method stub
        adapter = new BannerViewAdapter(getContext(), bannerViewList);
        if (list == null || list.size() <= 0) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            imageView = new ImageView(getContext());//新建一个新的imageview
            android.view.ViewGroup.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ScaleType.CENTER_CROP);
            GlideUtil.load(getContext(), Common.PHOTO_URL + list.get(i).getUrl(), imageView);
            bannerViewList.add(imageView);
            adapter.notifyDataSetChanged();//图片改了之后，要通知适配器改变数据

        }

        final ImageView[] imageViews = new ImageView[list.size()];
        if (list.size() > 1) {//如果数量为1的话，点点就不要了
            for (int i = 0; i < list.size(); i++) {//加载对应的小点
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
                    imageViews[i]
                            .setBackgroundResource(R.drawable.page_select);
                } else {
                    // 其他图片都设置未选中状态
                    imageViews[i]
                            .setBackgroundResource(R.drawable.page_not_select);
                }
                group.addView(imageViews[i], margin);
            }
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
                    imageViews[arg0]
                            .setBackgroundResource(R.drawable.page_select);
                    if (arg0 != i) {
                        //设置为非选中状态
                        imageViews[i]
                                .setBackgroundResource(R.drawable.page_not_select);
                    }
                }
            }

        });
    }
}
