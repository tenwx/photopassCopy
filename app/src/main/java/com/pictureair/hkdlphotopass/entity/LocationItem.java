package com.pictureair.photopass.entity;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LocationItem {
	public TextView locationNameTextView;//地点名称
	public ImageView locationPhotoImageView;//地点默认背景图片
	public ImageView favoriteImageView;//喜爱按钮
	public ImageView locationLeadImageView;//地点导航
	public TextView distanceTextView;//距离
	public ImageView showDetailImageView;//显示详情按钮
	public RelativeLayout locationDetailLayout;//详情layout
//	public TextView locationDetailNameTextView;//详情的地点名称
	public TextView locationDetailInfoTextView;//地点的详情介绍
	public ImageView locationBlurPhotoImageView;//高斯模糊背景图
	public RelativeLayout photoFrameLayout;
}
