package com.pictureair.hkdlphotopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.PictureAirLog;
import com.pictureair.hkdlphotopass.util.SPUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 第一次进入app的引导页，引导页结束，要么继续左滑，要么点击立即体验按钮进入登录页面
 * @author bauer_bao
 *
 */
public class WelcomeActivity extends BaseActivity implements OnPageChangeListener, OnTouchListener {
	private ViewPager mViewPager;
	private ImageView view2, view3, view4;
	private List<View> list;
	private LinearLayout pointLLayout;
	private ImageView[] imgs;
	private int count;
	private int currentItem;
	private int lastX = 0;// 获得当前X坐标
	private int lastY = 0;
	private static final String TAG = "WelcomeActivity";
	private String currentLanguage;// en表示英语，zh表示简体中文。
	private static final int START_JUMP = 1001;
	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case START_JUMP:
					startJump();
					break;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		pointLLayout = (LinearLayout) findViewById(R.id.llayout);
		count = pointLLayout.getChildCount();
		imgs = new ImageView[count];
		for (int i = 0; i < count; i++) {
			imgs[i] = (ImageView) pointLLayout.getChildAt(i);
			imgs[i].setEnabled(true);
			imgs[i].setTag(i);
		}
		currentItem = 0;
		imgs[currentItem].setEnabled(false);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.addOnPageChangeListener(this);
		mViewPager.setOnTouchListener(this);
		LayoutInflater inflater = LayoutInflater.from(WelcomeActivity.this);
		list = new ArrayList<>();
		currentLanguage = SPUtils.getString(this, Common.SHARED_PREFERENCE_APP, Common.LANGUAGE_TYPE, Common.ENGLISH);
		initpage(inflater);

	}

	//初始化viewpager
	public void initpage(LayoutInflater flater) {
		view2 = (ImageView) flater.inflate(R.layout.loading_start, null);
		view3 = (ImageView) flater.inflate(R.layout.loading_start, null);
		view4 = (ImageView) flater.inflate(R.layout.loading_start, null);

		if (currentLanguage.equals(Common.SIMPLE_CHINESE)){
			//中文
			view2.setImageResource(R.drawable.loading_zh_1);
			view3.setImageResource(R.drawable.loading_zh_2);
			view4.setImageResource(R.drawable.loading_zh_3);
		}else if (currentLanguage.equals(Common.ENGLISH)){
			//英文
			view2.setImageResource(R.drawable.loading_en_1);
			view3.setImageResource(R.drawable.loading_en_2);
			view4.setImageResource(R.drawable.loading_en_3);
		} else {
			view2.setImageResource(R.drawable.loading_tw_1);
			view3.setImageResource(R.drawable.loading_tw_2);
			view4.setImageResource(R.drawable.loading_tw_3);
		}

//		startNow = (TextView)view4.findViewById(R.id.startNow);
//		startNow.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				PictureAirLog.d(TAG, "start now-------->");
//				startJump();
//			}
//		});
		list.add(view2);
		list.add(view3);
		list.add(view4);
		mViewPager.setAdapter(pager);
	}

	private void setcurrentPoint(int position) {
		if (position < 0 || position > count - 1 || currentItem == position) {
			return;
		}
		imgs[currentItem].setEnabled(true);
		imgs[position].setEnabled(false);
		currentItem = position;
	}


	PagerAdapter pager = new PagerAdapter() {

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(list.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(list.get(position));
			return list.get(position);
		}
	};

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		setcurrentPoint(arg0);

	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		switch (arg1.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastX = (int) arg1.getX();
			lastY = (int) arg1.getY();
			break;

		case MotionEvent.ACTION_UP:
			if ((lastX - arg1.getX() > 100) && (mViewPager.getCurrentItem() == mViewPager.getAdapter().getCount() - 1)) {// 从最后一页向右滑动
				handler.sendEmptyMessage(START_JUMP);
			}
		}
		return false;
	}
	
	/**
	 * 跳转到登录页面
	 */
	private void startJump(){
		PictureAirLog.out("start jump---->");
		Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

}
