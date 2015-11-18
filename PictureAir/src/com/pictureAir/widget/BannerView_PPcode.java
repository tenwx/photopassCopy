package com.pictureAir.widget;

import java.util.ArrayList;
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

import com.pictureAir.R;
import com.pictureAir.adapter.PPCodeAdapter;
import com.pictureAir.entity.PPCodeInfo;
import com.pictureAir.util.Common;
/**
 * pp二维码显示的banner
 * 需要的参数是pp码和pp码对应的第一张图（如果没有图片，显示默认背景图片）
 * 
 * 每次切换的时候，首先要更改生成的ppcode的二维码，然后要更改背景图片
 * @author bauer_bao
 *
 */
@SuppressLint("NewApi") public class BannerView_PPcode extends LinearLayout {
	private ViewPager adViewPager;
	private ViewGroup group;
	private List<CompositePPCodeView> bannerViewList = new ArrayList<CompositePPCodeView>();
//	private CompositePPCodeView myCodeView;
	private ImageView imageView;
	private PPCodeAdapter adapter;
	private int currentPosition = 0;
	public BannerView_PPcode(Context context) {
		super(context);

	}

	public BannerView_PPcode(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.banner_ppcode, this);
		initView(context);
	}

	private void initView(final Context context) {
		adViewPager = (ViewPager) this.findViewById(R.id.viewPager_ppcode);
		group = (ViewGroup) findViewById(R.id.iv_image_ppcode);
		
	}
	
	//初始化图片信息list
	public void initImageList(final ArrayList<PPCodeInfo> PPlist) {
		// TODO Auto-generated method stub
		int count = PPlist.size();
		adapter = new PPCodeAdapter(getContext(), bannerViewList);

		for (int i = 0; i < count; i++) {//加载图片
			//数据是显示pp二维码的背景图的坐标
			CompositePPCodeView myCodeView = new CompositePPCodeView(getContext(), R.drawable.ppcode_mikey_bg, 
					Common.BARCODEURL + PPlist.get(i).ppCode, 317, 281, 66, 64, 185, 185, i);
			System.out.println("addview");
			bannerViewList.add(myCodeView);
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
				switch (arg0) {
				case 0://滑动完成
					//最好在滑动显示的时候，设置ppcode码
					System.out.println("当前显示第"+currentPosition+"个");
					bannerViewList.get(currentPosition).setPPCodeImage(Common.BARCODEURL + PPlist.get(currentPosition).ppCode);
					break;

				default:
					break;
				}
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				currentPosition = arg0;
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
