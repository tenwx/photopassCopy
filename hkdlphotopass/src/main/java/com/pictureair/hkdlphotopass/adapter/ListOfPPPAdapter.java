package com.pictureair.hkdlphotopass.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.pictureair.hkdlphotopass.R;
import com.pictureworks.android.entity.PPPinfo;
import com.pictureworks.android.util.AppUtil;
import com.pictureworks.android.util.Common;
import com.pictureworks.android.util.ScreenUtil;
import com.pictureworks.android.widget.MyToast;

import java.util.ArrayList;
import java.util.HashMap;

/**pp+数据的适配器*/
public class ListOfPPPAdapter extends BaseAdapter {
	private ArrayList<?> arrayList = null;
	private Context mContext;
	private String pppCode = null;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private boolean isUseHavedPPP;
	private OnItemChildClickListener childClickListener;
	private HashMap<Integer, Boolean> map;//统计被勾选的子项 只能选一张PP+.
	private int onclickPosition;
	private Handler handler;
	private MyToast myToast;
	
	public ListOfPPPAdapter(ArrayList<?> arrayList, boolean isUseHavedPPP, Handler handler, Context mContext) {
		this.arrayList = arrayList;
		this.mContext = mContext;
		this.isUseHavedPPP = isUseHavedPPP;
		this.handler = handler;
		myToast = new MyToast(mContext);
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

		if (isUseHavedPPP){
			map = new HashMap<Integer, Boolean>();
		}
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

		if (isUseHavedPPP){ // 如果是选择，注册监听事件
			childClickListener = new OnItemChildClickListener(position);
			holder.img_no_check = (ImageView) convertView.findViewById(R.id.iv_select);
			holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.ppp_item);
			holder.img_no_check.setOnClickListener(childClickListener);
			holder.itemLayout.setOnClickListener(childClickListener);
		}

		dpp = (PPPinfo) arrayList.get(position);
		setView(holder, dpp, position);


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
		ImageView img_no_check; // 左边的选择框
		LinearLayout itemLayout;
	}


	private void setView(ViewHolder holder, PPPinfo dpp, int position){
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
			holder.pppName.setText(R.string.mypage_ppp);
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

			if (dpp.expired == 1) {//设置是否过期
				if (dpp.expericePPP == 1) {//体验卡
					if (dpp.bindInfo.size() == 1) {//已用完

					} else {//过期
						holder.tvState.setText(R.string.ppp_has_expired);
					}
				} else {//正常ppp卡
					if (dpp.bindInfo.size() == 3) {//已用完

					} else {//过期
						holder.tvState.setText(R.string.ppp_has_expired);
					}
				}
			}


			//如果是选择 pp＋的状态
			if (isUseHavedPPP) {
				//显示单选框，隐藏状态
				holder.img_no_check.setVisibility(View.VISIBLE);
				holder.tvState.setVisibility(View.GONE);

				//判断 选择框的选中 和 非选中状态。
				if (map.size() == 1) {
					if (map.get(position) != null) {
						if (map.get(position)) {
							holder.img_no_check.setImageResource(R.drawable.sele);
						}
					}else{
						holder.img_no_check.setImageResource(R.drawable.nosele);
					}
				} else {
					holder.img_no_check.setImageResource(R.drawable.nosele);
				}
			}

		}
	}



	//点击一个item的事件

	private class OnItemChildClickListener implements View.OnClickListener {
		private int position;

		public OnItemChildClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
				switch (v.getId()) {
					case R.id.iv_select:
					case R.id.ppp_item:
						if (map.size() == 0){ //增加
							onclickPosition = position; //通过MyPPP获取它
							map.put(position,true);
						}else if (map.size() == 1){ // 超出范围
							if (map.containsKey(position)){
								map.clear();
//							    map.put(position,true);
							}else {
								myToast.setTextAndShow(R.string.outofrange, Common.TOAST_SHORT_TIME);
							}
//							map.clear();
//							map.put(position,true);
						}
						notifyDataSetChanged();
						handler.sendEmptyMessage(2);
						break;

					default:
						break;
				}
		}
	}


	public HashMap<Integer, Boolean> getMap() {
		return map;
	}

	public void setMap(HashMap<Integer, Boolean> map) {
		this.map = map;
	}

	public int getOnclickPosition() {
		return onclickPosition;
	}

	public void setOnclickPosition(int onclickPosition) {
		this.onclickPosition = onclickPosition;
	}
}
