package com.pictureair.photopassCopy.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
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

import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.animation.Rotate3dAnimation;
import com.pictureair.photopassCopy.entity.PPPinfo;
import com.pictureair.photopassCopy.util.AppUtil;
import com.pictureair.photopassCopy.util.Common;
import com.pictureair.photopassCopy.util.GlideUtil;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.pictureair.photopassCopy.util.ScreenUtil;
import com.pictureair.photopassCopy.widget.PWToast;

import java.util.ArrayList;

/**
 * pp+数据的适配器
 */
public class ListOfPPPAdapter extends BaseAdapter {
	private static final String TAG = "MyPPPActivity";
	private ArrayList<?> arrayList = null;
	private Context mContext;
	private String pppCode = null;
	private boolean isUseHavedPPP;
	private OnItemChildClickListener childClickListener;
	private SparseBooleanArray map;//统计被勾选的子项 只能选一张PP+.
	private int onclickPosition;
	private Handler handler;
	private PWToast myToast;
	private boolean[] mInFace;
	public static final int HEAD_CLICK = 555;
	private static final int STATUS_UNUSED = 1;//1:未使用2:已使用3:已用完4:已过期
	private static final int STATUS_USED = 2;
	private static final int STATUS_ALLUSED = 3;
	private static final int STATUS_EXPIRED = 4;

	public ListOfPPPAdapter(ArrayList<?> arrayList, boolean isUseHavedPPP, Handler handler, Context mContext) {
		this.arrayList = arrayList;
		this.mContext = mContext;
		this.isUseHavedPPP = isUseHavedPPP;
		this.handler = handler;
		myToast = new PWToast(mContext);
		initFaceOrNot();
		if (isUseHavedPPP){
			map = new SparseBooleanArray();
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.my_ppp_list, parent, false);
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
			holder.pppLine = (RelativeLayout) convertView.findViewById(R.id.card_bottom);
			if (isUseHavedPPP) {
				holder.img_no_check = (ImageView) convertView.findViewById(R.id.iv_select);
				holder.ppp_empty_view = (ImageView) convertView.findViewById(R.id.ppp_empty_view);
			}
			adjustLayout(holder);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (isUseHavedPPP){ // 如果是选择，注册监听事件
			childClickListener = new OnItemChildClickListener(position);
			//显示单选框，隐藏状态
			holder.img_no_check.setVisibility(View.VISIBLE);
			holder.ppp_empty_view.setVisibility(View.VISIBLE);
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
					final Rotate3dAnimation animation1;
					if (!mInFace[position]) {
						animation1 = new Rotate3dAnimation(90, 0, holder.ppp_content.getWidth() / 2, holder.ppp_content.getHeight() / 2, 300f, 0f,false);
					} else {
						animation1 = new Rotate3dAnimation(270, 360, holder.ppp_content.getWidth() / 2, holder.ppp_content.getHeight() / 2, 300f, 0f,false);
					}
					animation1.setDuration(250);
					animation1.setInterpolator(new LinearInterpolator());
					animation1.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {}

						@Override
						public void onAnimationEnd(Animation animation) {
							holder.ppp_content.setEnabled(true);
							holder.ppp_content.clearAnimation();
						}

						@Override
						public void onAnimationRepeat(Animation animation) {}
					});

					if (!mInFace[position]) {
						animation = new Rotate3dAnimation(0, -90, holder.ppp_content.getWidth() / 2, holder.ppp_content.getHeight() / 2, 300f,0f,true);
					}else{
						animation = new Rotate3dAnimation(0,90, holder.ppp_content.getWidth() / 2, holder.ppp_content.getHeight() / 2, 300f, 0f,true);
					}
					animation.setDuration(250);
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
								mInFace[position] = true;
							}else{
								hideOppsite(holder);
								mInFace[position] = false;
							}
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

	private void adjustLayout(ViewHolder holder){
		ViewGroup.LayoutParams params = holder.ppp_content.getLayoutParams();
		params.width = ScreenUtil.getScreenWidth(mContext);
		params.height = params.width * 3 / 5;
		holder.ppp_content.setLayoutParams(params);

		ViewGroup.LayoutParams params2 = holder.ppp_imageView.getLayoutParams();
		if (isUseHavedPPP) {//588，372 ppp卡面图片的大小
			params2.width = params.width - ScreenUtil.dip2px(mContext, 35);
			params2.height = params2.width * 372 / 588;

		} else {
			params2.height = params.height - ScreenUtil.dip2px(mContext, 5);
			params2.width = params2.height * 588 / 372;

		}

//		if (isUseHavedPPP) {
//			RelativeLayout.LayoutParams checkParams = (RelativeLayout.LayoutParams) holder.img_no_check.getLayoutParams();
//			checkParams.leftMargin = (params.width - params2.width)/2 + ScreenUtil.dip2px(mContext, 10);
//			checkParams.topMargin = (params.height - params2.height)/2 + ScreenUtil.dip2px(mContext, 10);
//			holder.img_no_check.setLayoutParams(checkParams);
//		}

		holder.ppp_imageView.setLayoutParams(params2);

		ViewGroup.LayoutParams params3 = holder.rl_ppp_status.getLayoutParams();
		params3.width = params2.height * 125 / 372;//右上角三角状态图标大小148，150
		params3.height = params2.height * 125 / 372;
		holder.rl_ppp_status.setLayoutParams(params3);

		ViewGroup.LayoutParams params1 = holder.pppCardCenterCover.getLayoutParams();
		params1.width = params.width;
		params1.height = params.height;
		holder.pppCardCenterCover.setLayoutParams(params1);
	}

	private void hideFace(ViewHolder holder) {
		holder.ppp_face.setVisibility(View.GONE);
		holder.ppp_detail.setVisibility(View.VISIBLE);
	}

	private void hideOppsite(ViewHolder holder) {
		holder.ppp_face.setVisibility(View.VISIBLE);
		holder.ppp_detail.setVisibility(View.GONE);
	}

	public class ViewHolder {
		public TextView time;//绑定的时间
		TextView pppNumber;//ppp卡的序列号
		public ImageView pp1_img, pp2_img, pp3_img;//三个ppp的格子
		TextView tvExpired; //日期
		ImageView ppp_imageView;//背景图
		View pppCardCenterCover;
		ImageView img_no_check; // 左边的选择框
		LinearLayout itemLayout;
		TextView tv_cardStatus;//PPP卡状态
		RelativeLayout ppp_cardHeader;//三个米奇头的外部容器
		RelativeLayout ppp_content; //3个米奇头下面的内容
		RelativeLayout ppp_detail;//卡反面内容
		RelativeLayout ppp_detail_pp1;//卡反面对应的每一条详情
		RelativeLayout ppp_detail_pp2;
		RelativeLayout ppp_detail_pp3;
		LinearLayout ll_ppp_with_pp;
		RelativeLayout ppp_face;//卡正面
		RelativeLayout rl_ppp_status;//卡的状态
		TextView tv_pp_num1;//卡反面对应的每一条数据的信息
		TextView tv_pp_num2;
		TextView tv_pp_num3;
		TextView tv_pp_date1;
		TextView tv_pp_date2;
		TextView tv_pp_date3;
		TextView tv_ppp_no_pp;//卡反面没有信息时的显示
		ImageView ppp_empty_view;
		RelativeLayout pppLine;
	}


	private void setView(ViewHolder holder, PPPinfo dpp, int position){
		int status = 0;
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
		if (holder.ppp_imageView.getTag(R.id.glide_image_tag) == null || !holder.ppp_imageView.getTag(R.id.glide_image_tag).equals(dpp.pppCardBg)) {
			PictureAirLog.out("ppp url---->" + Common.PHOTO_URL + dpp.pppCardBg);
			GlideUtil.load(mContext, Common.PHOTO_URL + dpp.pppCardBg, R.drawable.ic_discover_loading, R.drawable.ic_discover_failed, holder.ppp_imageView);
			holder.ppp_imageView.setTag(R.id.glide_image_tag, dpp.pppCardBg);
		}
		if (dpp.pppCardBg != null) {
			if (!dpp.pppCardBg.contains("ppp.")) {
				holder.pppLine.setBackgroundResource(R.drawable.ppp_white_line);
			} else {
				holder.pppLine.setBackgroundColor(Color.TRANSPARENT);
			}
		}

		//设置过期时间
		if (dpp.expiredOn.equals("") || dpp.expericePPP == 1) { // 如果有效期为空。就不显示，如果不为空，就显示。
			holder.tvExpired.setText("");
		} else {
			holder.tvExpired.setText(mContext.getResources().getString(R.string.expired_on) +"\n"+ AppUtil.GTMToLocal(dpp.expiredOn).substring(0, 10).replace("-", "."));
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
				status = STATUS_UNUSED;
			} else {//已使用
				holder.tv_cardStatus.setText(R.string.ppp_has_used);
				status = STATUS_ALLUSED;
			}
		} else {//普通ppp
//			holder.pppName.setText(R.string.mypage_ppp);
			holder.pp1_img.setVisibility(dpp.capacity >= 1 ? View.VISIBLE : View.GONE );
			holder.pp2_img.setVisibility(dpp.capacity >= 2 ? View.VISIBLE : View.GONE);
			holder.pp3_img.setVisibility(dpp.capacity == 3 ? View.VISIBLE : View.GONE);
			holder.tv_ppp_no_pp.setVisibility(View.INVISIBLE);
			holder.ll_ppp_with_pp.setVisibility(View.INVISIBLE);
			switch (dpp.bindInfo.size()) {
				case 0://全新的
					status = STATUS_UNUSED;
					holder.tv_cardStatus.setText(R.string.no_activated);
					holder.pp1_img.setImageResource(R.drawable.no_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.no_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
					holder.tv_ppp_no_pp.setVisibility(View.VISIBLE);
					holder.ll_ppp_with_pp.setVisibility(View.GONE);
					break;

				case 1://用过一张
					if (dpp.capacity == 1) {//一日通
						status = STATUS_ALLUSED;
						holder.tv_cardStatus.setText(R.string.ppp_has_used);
					} else {//一卡通
						status = STATUS_USED;
						holder.tv_cardStatus.setText(R.string.activated);
					}
					holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.no_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
					holder.ll_ppp_with_pp.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp1.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp2.setVisibility(View.INVISIBLE);
					holder.ppp_detail_pp3.setVisibility(View.INVISIBLE);
					holder.tv_pp_num1.setText(dpp.bindInfo.get(0).customerId);
					holder.tv_pp_date1.setText(replace(dpp.bindInfo.get(0).bindDate));
					break;
				case 2://用过两张
					status = STATUS_USED;
					holder.tv_cardStatus.setText(R.string.activated);
					holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
					holder.ll_ppp_with_pp.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp1.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp2.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp3.setVisibility(View.INVISIBLE);
					holder.tv_pp_date1.setText(replace(dpp.bindInfo.get(0).bindDate));
					holder.tv_pp_date2.setText(replace(dpp.bindInfo.get(1).bindDate));
					holder.tv_pp_num1.setText(dpp.bindInfo.get(0).customerId);
					holder.tv_pp_num2.setText(dpp.bindInfo.get(1).customerId);
					break;

				case 3://全部用过
					status = STATUS_ALLUSED;
					holder.tv_cardStatus.setText(R.string.ppp_has_used);
					holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp2_img.setImageResource(R.drawable.has_ppp_icon);
					holder.pp3_img.setImageResource(R.drawable.has_ppp_icon);
					holder.ll_ppp_with_pp.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp1.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp2.setVisibility(View.VISIBLE);
					holder.ppp_detail_pp3.setVisibility(View.VISIBLE);
					holder.tv_pp_date1.setText(replace(dpp.bindInfo.get(0).bindDate));
					holder.tv_pp_date2.setText(replace(dpp.bindInfo.get(1).bindDate));
					holder.tv_pp_date3.setText(replace(dpp.bindInfo.get(2).bindDate));
					holder.tv_pp_num1.setText(dpp.bindInfo.get(0).customerId);
					holder.tv_pp_num2.setText(dpp.bindInfo.get(1).customerId);
					holder.tv_pp_num3.setText(dpp.bindInfo.get(2).customerId);
					break;

				default:
					break;
			}

			if (dpp.expired == 1) {//设置是否过期
				if (dpp.expericePPP == 1) {//体验卡
					if (dpp.bindInfo.size() == 1) {//已用完

					} else {//过期
						status = STATUS_EXPIRED;
						holder.tv_cardStatus.setText(R.string.ppp_has_expired);
					}
				} else {//正常ppp卡
					if (dpp.bindInfo.size() == 3) {//已用完

					} else {//过期
						status = STATUS_EXPIRED;
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
				//判断 选择框的选中 和 非选中状态。
				if (map.size() == 1) {
					if (map.get(position, false)) {
							holder.img_no_check.setImageResource(R.drawable.sele);
					}else{
						holder.img_no_check.setImageResource(R.drawable.nosele);
					}
				} else {
					holder.img_no_check.setImageResource(R.drawable.nosele);
				}
			}
		}
		switch (status) {
			case STATUS_USED://已使用
				holder.rl_ppp_status.setBackgroundResource(R.drawable.ppp_status_sele);
				holder.tv_cardStatus.setTextColor(ContextCompat.getColor(mContext, R.color.white));
				break;

			case STATUS_UNUSED://未使用
			case STATUS_ALLUSED://已用完
			case STATUS_EXPIRED://已过期
			default:
				holder.rl_ppp_status.setBackgroundResource(R.drawable.ppp_status_none);
				holder.tv_cardStatus.setTextColor(ContextCompat.getColor(mContext, R.color.pp_dark_blue));
				break;
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
							if (map.get(position, false)){
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
						handler.obtainMessage(HEAD_CLICK,position).sendToTarget();
						break;
					default:
						break;
				}
		}
	}

	private String replace(String oldStr){
		if (TextUtils.isEmpty(oldStr)) {
			return "";
		} else {
			return oldStr.replace("-", ".");
		}
	}

	public SparseBooleanArray getMap() {
		PictureAirLog.d("map size--> " + map.size());
		return map;
	}

	public void setMap(SparseBooleanArray map) {
		this.map = map;
	}

	public int getOnclickPosition() {
		return onclickPosition;
	}

	public void setOnclickPosition(int onclickPosition) {
		this.onclickPosition = onclickPosition;
	}
}
