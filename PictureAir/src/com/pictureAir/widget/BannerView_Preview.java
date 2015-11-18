package com.pictureAir.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureAir.R;
import com.pictureAir.adapter.BannerViewAdapter;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;

@SuppressLint("NewApi") public class BannerView_Preview extends LinearLayout {
	private ViewPager adViewPager;
	private ViewGroup group;
	private List<ImageView> bannerViewList = new ArrayList<ImageView>();
	private ImageView imageView;
	private BannerViewAdapter adapter;
	private ImageLoader imageLoader;
	public BannerView_Preview(Context context) {
		super(context);

	}

	public BannerView_Preview(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.preview_detail, this);
		imageLoader = ImageLoader.getInstance();
		initView(context);
	}

	private void initView(final Context context) {
		LinearLayout layout = (LinearLayout) this
				.findViewById(R.id.view_pager_content2);
		adViewPager = (ViewPager) this.findViewById(R.id.viewPager_preview);
		group = (ViewGroup) findViewById(R.id.iv_image_preview);
		//		layout.setX(20);
		//		WindowManager wm = (WindowManager) getContext()
		//                .getSystemService(Context.WINDOW_SERVICE);
		int width = ScreenUtil.getScreenWidth(context);
		System.out.println(width+"kuandu"+width*290/452+"++"+width*16/452);
		layout.setLayoutParams(new LinearLayout.LayoutParams(width*290/452, LayoutParams.WRAP_CONTENT));
		//		layout.setLayoutParams(new LinearLayout.LayoutParams(30, 222));
		layout.setX(ScreenUtil.px2dip(context, width*12/452));
		//		layout.setX(20);
		//		adViewPager.setX(20);     图片款452   杯子315   杯子去除边缘290   左侧边缘16
		//		group.setX(30);
		//		DisplayMetrics dm = new DisplayMetrics();
		//		((Activity) context).getWindowManager().getDefaultDisplay()
		//				.getMetrics(dm);
		//		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout
		//				.getLayoutParams();
		//		//根据图片高和宽的比例计算高度
		////		//测试图宽为694 高为323
		////		params.height = (int) (((double) dm.widthPixels / (double) 694 * (double) 323));
		////		params.
		//		layout.setLayoutParams(params);
	}
	//添加图片
	public void findimagepath( ArrayList<PhotoInfo> list) {
		// TODO Auto-generated method stub
		int count = list.size();
		String pathString = null;
		adapter = new BannerViewAdapter(getContext(), bannerViewList);

		for (int i = 0; i < count; i++) {//加载图片
			imageView = new ImageView(getContext());//新建一个新的imageview
			if (list.get(i).onLine == 1) {
				pathString = Common.PHOTO_URL + list.get(i).photoThumbnail_512;
				System.out.println("pictureair=="+pathString+"_"+list.get(i).photoId);
				imageLoader.displayImage(pathString, imageView);
			}else {
				pathString = list.get(i).photoPathOrURL;
				imageLoader.displayImage("file://"+pathString, imageView);
				System.out.println("all------"+pathString);
			}
			bannerViewList.add(imageView);
			adapter.notifyDataSetChanged();//图片改了之后，要通知适配器改变数据
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
		}else {

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
