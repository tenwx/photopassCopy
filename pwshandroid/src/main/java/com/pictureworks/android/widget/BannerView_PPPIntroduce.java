package com.pictureworks.android.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pictureworks.android.R;
import com.pictureworks.android.adapter.BannerViewAdapter;
import com.pictureworks.android.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;
//ppp介绍页的banner
public class BannerView_PPPIntroduce extends LinearLayout {
	private ViewPager adViewPager;
	private ViewGroup group;
	private List<ImageView> bannerViewList = new ArrayList<ImageView>();
	private BannerViewAdapter adapter;
	private ImageView imageView;
	private LinearLayout viewpagerGroup;
	private int count = 4;
	public BannerView_PPPIntroduce(Context context) {
		super(context);
	}
	
	public BannerView_PPPIntroduce(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.banner_ppp_introduce, this);
		viewpagerGroup = (LinearLayout) findViewById(R.id.view_pager_content);
		group = (ViewGroup) findViewById(R.id.iv_image);
		adViewPager = new ViewPager(context);
		 //根据屏幕信息设置ViewPager广告容器的宽高
        adViewPager.setLayoutParams(new LayoutParams(ScreenUtil.getScreenWidth(context), ScreenUtil.getScreenHeight(context) / 2));
        //将ViewPager容器设置到布局文件父容器中
    	viewpagerGroup.addView(adViewPager);
    	initImageView();
	}

	//添加图片
	private void initImageView() {
		
		adapter = new BannerViewAdapter(getContext(), bannerViewList);

		for (int i = 0; i < count; i++) {//加载图片
			imageView = new ImageView(getContext());//新建一个新的imageview
			imageView.setImageResource(R.drawable.ppp_introduce1);
			bannerViewList.add(imageView);
			adapter.notifyDataSetChanged();//图片改了之后，要通知适配器改变数据
		}
		final ImageView[] imageViews = new ImageView[count];
		if (count!=1) {//如果数量为1的话，点点就不要了
			for (int i = 0; i < count; i++) {//加载对应的小点
				LayoutParams margin = new LayoutParams(
						LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
				// 设置每个小圆点距离左边的间距
				margin.setMargins(10, 0, 0, 0);
				imageView = new ImageView(getContext());
				// 设置每个小圆点的宽高
				imageView.setLayoutParams(new LayoutParams(15, 15));
				imageViews[i] = imageView;
				if (i == 0) {
					// 默认选中第一张图片
					imageViews[i]
							.setBackgroundResource(R.drawable.ppp_page_selected);
				} else {
					// 其他图片都设置未选中状态
					imageViews[i]
							.setBackgroundResource(R.drawable.ppp_page_not_select);
				}
				group.addView(imageViews[i], margin);
			}
		}
		//添加适配器
		adViewPager.setAdapter(adapter);
		adViewPager.setOnPageChangeListener(new OnPageChangeListener() {
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
							.setBackgroundResource(R.drawable.ppp_page_selected);
					if (arg0 != i) {
						//设置为非选中状态
						imageViews[i]
								.setBackgroundResource(R.drawable.ppp_page_not_select);
					}
				}
			}

		});
	}
	
}
