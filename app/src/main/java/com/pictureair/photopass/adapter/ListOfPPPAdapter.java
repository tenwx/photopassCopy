package com.pictureair.photopass.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ScreenUtil;

import java.util.ArrayList;

/**pp+数据的适配器*/
public class ListOfPPPAdapter extends BaseAdapter {
	private ArrayList<?> arrayList = null;
	private Context mContext;
	private String pppCode = null;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	public ListOfPPPAdapter(ArrayList<?> arrayList, Context mContext) {
		this.arrayList = arrayList;
		this.mContext = mContext;
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_discover_loading)
				.showImageOnFail(R.drawable.ic_discover_failed)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.imageScaleType(ImageScaleType.NONE)
				.bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
				.delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
				.build();
		imageLoader = ImageLoader.getInstance();
	}

	public ArrayList<?> getArrayList() {
		return arrayList;
	}

	public void setArrayList(ArrayList<?> arrayList) {
		this.arrayList = arrayList;
	}

	@Override
	public int getCount() {
		return arrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		PPPinfo dpp;
		//初始化view
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.my_ppp_list, null);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.pppNumber = (TextView) convertView.findViewById(R.id.ppp_number);
			holder.pp1_img = (ImageView)convertView.findViewById(R.id.ppp_imageView1);
			holder.pp2_img = (ImageView)convertView.findViewById(R.id.ppp_imageView2);
			holder.pp3_img = (ImageView)convertView.findViewById(R.id.ppp_imageView3);
			holder.tvState =(TextView)convertView.findViewById(R.id.tv_state);
			holder.tvExpired = (TextView)convertView.findViewById(R.id.tv_expired);
			holder.ppp_imageView = (ImageView) convertView.findViewById(R.id.ppp_imageView);
			holder.pppName = (TextView) convertView.findViewById(R.id.ppp_card_name);
			holder.pppCardCenterCover = convertView.findViewById(R.id.card_center);
			ViewGroup.LayoutParams params = holder.ppp_imageView.getLayoutParams();
			params.width = ScreenUtil.getScreenWidth(mContext);
			params.height = params.width * 3 / 5;
			holder.ppp_imageView.setLayoutParams(params);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//初始化数据
		dpp = (PPPinfo) arrayList.get(position);

		//初始化时间
		if (null==dpp.ownOn||"".equals(dpp.ownOn)) {
			holder.time.setText("");
		}else {
			holder.time.setText(dpp.ownOn.replace("-", "."));
		}

		//初始化ppp号码
		pppCode = dpp.PPPCode.substring(0, 4);
		for (int i = 0; i < 3; i++) {//4-7，8-11，12-15
			pppCode += "-"+ dpp.PPPCode.substring(4 * i + 4, 4 * i + 8);
		}
		holder.pppNumber.setText(pppCode);

		//初始化背景图片
		ImageAware imageAware = new ImageViewAware(holder.ppp_imageView, false);
		if (holder.ppp_imageView.getTag() == null || !holder.ppp_imageView.getTag().equals(dpp.pppCardBg)) {
			imageLoader.displayImage(Common.PHOTO_URL + dpp.pppCardBg, imageAware, options);
			holder.ppp_imageView.setTag(dpp.pppCardBg);
		}

		//设置过期时间
		if (dpp.expiredOn.equals("") || dpp.expericePPP == 1) { // 如果有效期为空。就不显示，如果不为空，就显示。
			holder.tvExpired.setText("");
		} else {
			holder.tvExpired.setText(mContext.getResources().getString(R.string.expired_on) + AppUtil.GTMToLocal(dpp.expiredOn).substring(0, 10));
		}

		//设置过期/已用完的透明层
		if (dpp.expired == 1 || dpp.bindInfo.size() == dpp.capacity) {
			holder.pppCardCenterCover.setVisibility(View.VISIBLE);
		} else {
			holder.pppCardCenterCover.setVisibility(View.GONE);
		}

		//初始化PPP使用情况
		if (dpp.expericePPP == 1) {//体验卡
			holder.pppName.setText(R.string.experiencephotopassplus);
			holder.pp1_img.setVisibility(View.INVISIBLE);
			holder.pp2_img.setVisibility(View.INVISIBLE);
			holder.pp3_img.setVisibility(View.INVISIBLE);
			if (dpp.bindInfo.size() == 0) {//未使用
				holder.tvState.setText(R.string.no_activated);
			} else {//已使用
				holder.tvState.setText(R.string.ppp_has_used);
			}
		} else {//普通ppp
			holder.pppName.setText(R.string.photopassplus);
			holder.pp1_img.setVisibility(View.VISIBLE);
			holder.pp2_img.setVisibility(View.VISIBLE);
			holder.pp3_img.setVisibility(View.VISIBLE);
			switch (dpp.bindInfo.size()) {
				case 0://全新的
					holder.tvState.setText(R.string.no_activated);
					holder.pp1_img.setImageResource(R.drawable.no_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.no_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
					break;

				case 1://用过一张
					holder.tvState.setText(R.string.activated);
					holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.no_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
					break;

				case 2://用过两张
					holder.tvState.setText(R.string.activated);
					holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
					break;

				case 3://全部用过
					holder.tvState.setText(R.string.ppp_has_used);
					holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.has_ppp_icon);
					break;

				default:
					break;
			}

			if (dpp.expired == 1) {
				holder.tvState.setText(R.string.ppp_has_expired);
			}

		}

		return convertView;
	}

	private class ViewHolder {
		TextView time;//绑定的时间
		TextView pppNumber;//ppp卡的序列号
		ImageView pp1_img, pp2_img, pp3_img;//三个ppp的格子
		TextView tvState; //状态
		TextView tvExpired; //日期
		ImageView ppp_imageView;//背景图
		View pppCardCenterCover;
		TextView pppName;
	}

}
