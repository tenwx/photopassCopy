package com.pictureAir.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureAir.DetailProductActivity;
import com.pictureAir.PPPDetailProductActivity;
import com.pictureAir.R;
import com.pictureAir.adapter.BannerViewAdapter;
import com.pictureAir.util.Common;

public class BannerView extends LinearLayout {
	private ViewPager adViewPager;
	private ViewGroup group;
	private List<ImageView> bannerViewList = new ArrayList<ImageView>();
	private Timer bannerTimer;
	private Handler handler;
	private BannerTimerTask bannerTimerTask;
	private BannerViewAdapter adapter;
	private ImageLoader imageLoader;
	private ImageView imageView;
	public BannerView(Context context) {
		super(context);
	}

	public BannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.banner_view, this);
		initView(context);
		handler = new Handler() {
			public void handleMessage(Message msg) {
				adViewPager.setCurrentItem(msg.what);
				super.handleMessage(msg);
			}
		};
		bannerTimer = new Timer();
		imageLoader = ImageLoader.getInstance();
	}
	//添加图片
	public void findimagepath(final ArrayList<HashMap<String, String>> list) {
		// TODO Auto-generated method stub
		int count = list.size();
		String pathString = null;
		adapter = new BannerViewAdapter(getContext(), bannerViewList);

		for (int i = 0; i < count; i++) {//加载图片
			System.out.println(list.get(i).get("targetURL")+"+"+list.get(i).get("imageurl"));
			pathString = Common.BASE_URL+list.get(i).get("imageurl");
			imageView = new ImageView(getContext());//新建一个新的imageview
			imageLoader.displayImage(pathString, imageView);
			bannerViewList.add(imageView);
			adapter.notifyDataSetChanged();//图片改了之后，要通知适配器改变数据
			final int currentposition = i;
			bannerViewList.get(i).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					System.out.println("on click"+currentposition+"+"+list.get(currentposition).get("imageurl")+"+"+ "/"+list.get(currentposition).get("targetURL"));
					Intent intent = new Intent(getContext(), PPPDetailProductActivity.class);
					intent.putExtra("targetURL", "/" + list.get(currentposition).get("targetURL"));
					intent.putExtra("showComment", "Y");
					getContext().startActivity(intent);
				}
			});
		}
		final ImageView[] imageViews = new ImageView[count];
		if (count!=1) {//如果数量为1的话，点点就不要了
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
		adViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				//当页面开始滑动的时候，三种状态的变化顺序为（1，2，0）
				switch (arg0) {
				case 0:
//					System.out.println("nothing");
					bannerStartPlay();
					break;
				case 1:
//					System.out.println("scrolling");
					bannerStopPlay();
					break;
				case 2:
//					System.out.println("scroll done");
					break;
				default:
					break;
				}
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
	private void initView(final Context context) {
		RelativeLayout layout = (RelativeLayout) this
				.findViewById(R.id.view_pager_content);
		adViewPager = (ViewPager) this.findViewById(R.id.viewPager1);
		group = (ViewGroup) findViewById(R.id.iv_image);

		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
		.getMetrics(dm);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout
				.getLayoutParams();
		//根据图片高和宽的比例计算高度
		//		//测试图宽为694 高为323
		params.height = (int) (((double) dm.widthPixels / (double) 640 * (double) 301));
		layout.setLayoutParams(params);

	}

	//启动banner自动轮播
	public void bannerStartPlay(){
	
		if (bannerTimer != null) {
			if (bannerTimerTask != null)
				bannerTimerTask.cancel();
			bannerTimerTask = new BannerTimerTask();
			bannerTimer.schedule(bannerTimerTask, 2000, 2000);//2秒后执行，每隔2秒执行一次
//			System.out.println("banner111 start");
		}
	}
	//暂停banner自动轮播
	public void bannerStopPlay(){
		if (bannerTimerTask != null){
			bannerTimerTask.cancel();
//			System.out.println("banner stop");
		}
	}
	class BannerTimerTask extends TimerTask {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Message msg = new Message();
			if (bannerViewList.size() <= 1)
				return;
			int currentIndex = adViewPager.getCurrentItem();
			if (currentIndex == bannerViewList.size() - 1)
				msg.what = 0;
			else
				msg.what = currentIndex + 1;
			handler.sendMessage(msg);
		}

	}
}
