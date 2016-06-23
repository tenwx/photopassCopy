package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.SelectPhotoActivity;
import com.pictureair.photopass.animation.Rotate3dAnimation;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.pullloadlayout.MYTouchListener;

import java.util.ArrayList;
import java.util.HashMap;

/**pp+数据的适配器*/
public class ListOfPPPAdapter extends BaseAdapter {
	private static final String TAG = "MyPPPActivity";
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
	private PWToast myToast;
	private boolean[] mInFace;
	public ListOfPPPAdapter(ArrayList<?> arrayList, boolean isUseHavedPPP, Handler handler, Context mContext) {
		this.arrayList = arrayList;
		this.mContext = mContext;
		this.isUseHavedPPP = isUseHavedPPP;
		this.handler = handler;
		myToast = new PWToast(mContext);
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
		initFaceOrNot();
		if (isUseHavedPPP){
			map = new HashMap<Integer, Boolean>();
		}
	}
	public void initFaceOrNot() {
		if (arrayList != null && arrayList.size() >0) {
			if (mInFace == null) {
				mInFace = new boolean[arrayList.size()];
			}
			boolean[] tmp = new boolean[arrayList.size()];
			for (int i = 0; i < arrayList.size(); i++) {
				if (mInFace.length > i) {
					tmp[i] = mInFace[i];
				}else {
					tmp[i] = false;
				}
			}
			mInFace = tmp;
		}
	}

	public ArrayList<?> getArrayList() {
		return arrayList;
	}

	public void setArrayList(ArrayList<?> arrayList) {
		this.arrayList = arrayList;
		initFaceOrNot();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
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
//			holder.tvState =(TextView)convertView.findViewById(R.id.tv_state);
			holder.tv_cardStatus = (TextView) convertView.findViewById(R.id.tv_ppp_state);
			holder.rl_ppp_status = (RelativeLayout) convertView.findViewById(R.id.rl_ppp_status);
			holder.ppp_cardHeader = (RelativeLayout) convertView.findViewById(R.id.ppp_card_head);
			holder.tvExpired = (TextView)convertView.findViewById(R.id.tv_expired);
			holder.ppp_imageView = (ImageView) convertView.findViewById(R.id.ppp_imageView);
//			holder.pppName = (TextView) convertView.findViewById(R.id.ppp_card_name);
			holder.pppCardCenterCover = convertView.findViewById(R.id.card_center);
			holder.ppp_content = (RelativeLayout) convertView.findViewById(R.id.rl_ppp_content);
			holder.tv_pp_num1 = (TextView) convertView.findViewById(R.id.pp_detail_num1);
			holder.tv_pp_date1 = (TextView) convertView.findViewById(R.id.pp_detail_date1);
			holder.tv_pp_num2 = (TextView) convertView.findViewById(R.id.pp_detail_num2);
			holder.tv_pp_date2 = (TextView) convertView.findViewById(R.id.pp_detail_date2);
			holder.tv_pp_num3 = (TextView) convertView.findViewById(R.id.pp_detail_num3);
			holder.tv_pp_date3 = (TextView) convertView.findViewById(R.id.pp_detail_date3);
			holder.tv_ppp_no_pp = (TextView) convertView.findViewById(R.id.ppp_detail_no_pp);
			holder.ppp_detail_pp1 = (RelativeLayout) convertView.findViewById(R.id.rl_detail_pp1);
			holder.ppp_detail_pp2 = (RelativeLayout) convertView.findViewById(R.id.rl_detail_pp2);
			holder.ppp_detail_pp3 = (RelativeLayout) convertView.findViewById(R.id.rl_detail_pp3);
			holder.ll_ppp_with_pp = (LinearLayout) convertView.findViewById(R.id.ppp_detail_with_pp);
			holder.ppp_detail = (RelativeLayout) convertView.findViewById(R.id.rl_ppp_detail);
			holder.ppp_face = (RelativeLayout) convertView.findViewById(R.id.rl_ppp_face);
			ViewGroup.LayoutParams params = holder.ppp_imageView.getLayoutParams();
			params.width = ScreenUtil.getScreenWidth(mContext);
			params.height = params.width * 3 / 5;
			holder.ppp_imageView.setLayoutParams(params);
			ViewGroup.LayoutParams params1 = holder.ppp_detail.getLayoutParams();
			params1.width = params.width;
			params1.height = params.height;
			holder.ppp_detail.setLayoutParams(params);
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
		}else{
			childClickListener = new OnItemChildClickListener(position);
			holder.ppp_cardHeader.setOnClickListener(childClickListener);
			holder.ppp_content.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Rotate3dAnimation animation = null;
					if (!mInFace[position]) {
						animation = new Rotate3dAnimation(0, -90, holder.ppp_face.getWidth() / 2, holder.ppp_face.getHeight() / 2, 300f,0f,true);
					}else{
						animation = new Rotate3dAnimation(0,90, holder.ppp_face.getWidth() / 2, holder.ppp_face.getHeight() / 2, 300f, 0f,true);
					}
					animation.setDuration(500);
					animation.setInterpolator(new LinearInterpolator());
					animation.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {}
						@Override
						public void onAnimationEnd(
								Animation animation) {
							holder.ppp_content.clearAnimation();
							if (!mInFace[position]) {
								hideFace(holder);
							}else{
								hideOppsite(holder);
							}
							Rotate3dAnimation animation1 = null;
							if (!mInFace[position]) {

								mInFace[position] = true;
							} else {
								animation1 = new Rotate3dAnimation(270, 360, holder.ppp_face.getWidth() / 2, holder.ppp_face.getHeight() / 2, 300f, 0f,false);
								mInFace[position] = false;
							}
							animation1.setDuration(500);
							animation1.setInterpolator(new LinearInterpolator());
							animation1.setAnimationListener(new Animation.AnimationListener() {
								@Override
								public void onAnimationStart(Animation animation) {}

								@Override
								public void onAnimationEnd(Animation animation) {holder.ppp_content.setEnabled(true);}

								@Override
								public void onAnimationRepeat(Animation animation) {}
							});
							holder.ppp_content.startAnimation(animation1);
						}

						@Override
						public void onAnimationRepeat(Animation animation) {}
					});
					holder.ppp_content.clearAnimation();
					holder.ppp_content.startAnimation(animation);
					holder.ppp_content.setEnabled(false);
				}
			});
		}

		dpp = (PPPinfo) arrayList.get(position);
		setView(holder, dpp, position);
		return convertView;
	}

//	class MyAnimationListener implements Animation.AnimationListener{
//		ViewHolder viewHolder;
//		public MyAnimationListener(ViewHolder holder) {
//			this.viewHolder = holder;
//		}
//
//		@Override
//		public void onAnimationStart(Animation animation) {
//
//		}
//
//		@Override
//		public void onAnimationEnd(Animation animation) {
//
////				}
////			});
//		}

//		@Override
//		public void onAnimationRepeat(Animation animation) {
//
//		}
//	}

	private void hideFace(ViewHolder holder) {
		holder.ppp_face.setVisibility(View.GONE);
		holder.ppp_detail.setVisibility(View.VISIBLE);
	}

	private void hideOppsite(ViewHolder holder) {
		holder.ppp_face.setVisibility(View.VISIBLE);
		holder.ppp_detail.setVisibility(View.GONE);
	}

	private class ViewHolder {
		TextView time;//绑定的时间
		TextView pppNumber;//ppp卡的序列号
		ImageView pp1_img, pp2_img, pp3_img;//三个ppp的格子
//		TextView tvState; //状态
		TextView tvExpired; //日期
		ImageView ppp_imageView;//背景图
		View pppCardCenterCover;
//		TextView pppName;
		ImageView img_no_check; // 左边的选择框
		LinearLayout itemLayout;
		TextView tv_cardStatus;//PPP卡状态
		RelativeLayout ppp_cardHeader;//三个米奇头的外部容器
		RelativeLayout ppp_content;
		RelativeLayout ppp_detail;
		RelativeLayout ppp_detail_pp1;
		RelativeLayout ppp_detail_pp2;
		RelativeLayout ppp_detail_pp3;
		LinearLayout ll_ppp_with_pp;
		RelativeLayout ppp_face;
		RelativeLayout rl_ppp_status;
		TextView tv_pp_num1;
		TextView tv_pp_num2;
		TextView tv_pp_num3;
		TextView tv_pp_date1;
		TextView tv_pp_date2;
		TextView tv_pp_date3;
		TextView tv_ppp_no_pp;
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
			holder.tvExpired.setText(mContext.getResources().getString(R.string.expired_on) +"\n"+ AppUtil.GTMToLocal(dpp.expiredOn).substring(0, 10));
		}

		//设置过期/已用完的透明层
		if (dpp.expired == 1 || dpp.bindInfo.size() == dpp.capacity) {
			holder.pppCardCenterCover.setVisibility(View.VISIBLE);
		} else {
			holder.pppCardCenterCover.setVisibility(View.GONE);
		}

		//初始化PPP使用情况
		if (dpp.expericePPP == 1) {//体验卡
//			holder.pppName.setText(R.string.experiencephotopassplus);
			holder.pp1_img.setVisibility(View.INVISIBLE);
			holder.pp2_img.setVisibility(View.INVISIBLE);
			holder.pp3_img.setVisibility(View.INVISIBLE);
			if (dpp.bindInfo.size() == 0) {//未使用
				holder.tv_cardStatus.setText(R.string.no_activated);
			} else {//已使用
				holder.tv_cardStatus.setText(R.string.ppp_has_used);
			}
		} else {//普通ppp
//			holder.pppName.setText(R.string.mypage_ppp);
			holder.pp1_img.setVisibility(View.VISIBLE);
			holder.pp2_img.setVisibility(View.VISIBLE);
			holder.pp3_img.setVisibility(View.VISIBLE);
			switch (dpp.bindInfo.size()) {
				case 0://全新的
					holder.tv_cardStatus.setText(R.string.no_activated);
					holder.pp1_img.setImageResource(R.drawable.no_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.no_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
					holder.tv_ppp_no_pp.setVisibility(View.VISIBLE);
					holder.ll_ppp_with_pp.setVisibility(View.GONE);
					break;

				case 1://用过一张
					holder.tv_cardStatus.setText(R.string.activated);
					holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.no_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
					holder.ppp_detail_pp1.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp2.setVisibility(View.INVISIBLE);
					holder.ppp_detail_pp3.setVisibility(View.INVISIBLE);
					holder.tv_pp_num1.setText(AppUtil.getFormatPPCode(dpp.bindInfo.get(0).customerId));
					holder.tv_pp_date1.setText(dpp.bindInfo.get(0).bindDate);
					break;
				case 2://用过两张
					holder.tv_cardStatus.setText(R.string.activated);
					holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
					holder.ppp_detail_pp1.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp2.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp3.setVisibility(View.INVISIBLE);
					holder.tv_pp_num1.setText(AppUtil.getFormatPPCode(dpp.bindInfo.get(0).customerId));
					holder.tv_pp_date1.setText(dpp.bindInfo.get(0).bindDate);
					holder.tv_pp_num2.setText(AppUtil.getFormatPPCode(dpp.bindInfo.get(1).customerId));
					holder.tv_pp_date2.setText(dpp.bindInfo.get(1).bindDate);
					break;

				case 3://全部用过
					holder.tv_cardStatus.setText(R.string.ppp_has_used);
					holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.has_ppp_icon);
					holder.ppp_detail_pp1.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp2.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp3.setVisibility(View.VISIBLE);
					holder.tv_pp_num1.setText(AppUtil.getFormatPPCode(dpp.bindInfo.get(0).customerId));
					holder.tv_pp_date1.setText(dpp.bindInfo.get(0).bindDate);
					holder.tv_pp_num2.setText(AppUtil.getFormatPPCode(dpp.bindInfo.get(1).customerId));
					holder.tv_pp_date2.setText(dpp.bindInfo.get(1).bindDate);
					holder.tv_pp_num3.setText(AppUtil.getFormatPPCode(dpp.bindInfo.get(2).customerId));
					holder.tv_pp_date3.setText(dpp.bindInfo.get(2).bindDate);
					break;

				default:
					break;
			}

			if (dpp.expired == 1) {//设置是否过期
				if (dpp.expericePPP == 1) {//体验卡
					if (dpp.bindInfo.size() == 1) {//已用完

					} else {//过期
						holder.tv_cardStatus.setText(R.string.ppp_has_expired);
					}
				} else {//正常ppp卡
					if (dpp.bindInfo.size() == 3) {//已用完

					} else {//过期
						holder.tv_cardStatus.setText(R.string.ppp_has_expired);
					}
				}
			}

			if (!mInFace[position]) {
				hideOppsite(holder);
			}else{
				hideFace(holder);
			}

			//如果是选择 pp＋的状态
			if (isUseHavedPPP) {
				//显示单选框，隐藏状态
				holder.img_no_check.setVisibility(View.VISIBLE);
				holder.rl_ppp_status.setVisibility(View.GONE);

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
					case R.id.ppp_card_head:
						PPPinfo info = (PPPinfo)arrayList.get(position);
						if (info.bindInfo.size() < info.capacity && info.expired == 0) {
							if (info.expericePPP == 1) {//体验卡
								Intent intent = new Intent(mContext, SelectPhotoActivity.class);
								intent.putExtra("activity", "mypppactivity");
								intent.putExtra("pppCode", info.PPPCode);
								intent.putExtra("photoCount", 1);
								mContext.startActivity(intent);
							} else {
								PictureAirLog.v(TAG, "pppSize :" + info.PPPCode);
//								ppp = list1.get(position);
								API1.getPPsByPPPAndDate(info.PPPCode, handler);
							}
						}
						break;
//					case R.id.rl_ppp_content:
//						Rotate3dAnimation rotate1 = new Rotate3dAnimation(0,180,,0.5f,10,false);
//						rotate1.in

//						break;
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
