package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.util.AppUtil;

import java.util.ArrayList;

/**pp+数据的适配器*/
public class ListOfPPPAdapter extends BaseAdapter {
	private ArrayList<?> arrayList = null;
	private Context mContext;
	private String pppCode = null;
	
	public ListOfPPPAdapter(ArrayList<?> arrayList, Context mContext) {
		this.arrayList = arrayList;
		this.mContext = mContext;
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
		ViewHolder holder = null;
		PPPinfo dpp = null;
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
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//初始化数据
		dpp = (PPPinfo) arrayList.get(position);

		//初始化时间
		if (null==dpp.ownOn||"".equals(dpp.ownOn)) {
//			if (dpp.usedNumber == 0) {
//				
//				holder.time.setText(mContext.getString(R.string.empty_ppp));
//			}else {
//				holder.time.setText(mContext.getString(R.string.no_photo));
//				
//			}
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
		if (dpp.expiredOn.equals("")){ // 如果有效期为空。就不显示，如果不为空，就显示。
			holder.tvExpired.setText("");
		}else{
			holder.tvExpired.setText(mContext.getResources().getString(R.string.expired_on) + AppUtil.GTMToLocal(dpp.expiredOn).substring(0,10));

		}

		//初始化ppp三个格子
		switch (dpp.bindInfo.size()) {
		case 0://全新的
			holder.tvState.setText(R.string.no_activated);
			holder.pp1_img.setImageResource(R.drawable.no_ppp_icon);
			holder.pp2_img.setImageResource(R.drawable.no_ppp_icon);
			holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
			holder.ppp_imageView.setImageResource(R.drawable.ppp_bottom1);
			break;

		case 1://用过一张
			holder.tvState.setText(R.string.activated);
			holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
			holder.pp2_img.setImageResource(R.drawable.no_ppp_icon);
			holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
			holder.ppp_imageView.setImageResource(R.drawable.ppp_bottom1);
			break;

		case 2://用过两张
			holder.tvState.setText(R.string.activated);
			holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
			holder.pp2_img.setImageResource(R.drawable.has_ppp_icon);
			holder.pp3_img.setImageResource(R.drawable.no_ppp_icon);
			holder.ppp_imageView.setImageResource(R.drawable.ppp_bottom1);
			break;

		case 3://全部用过
			holder.tvState.setText(R.string.activated);
			holder.pp1_img.setImageResource(R.drawable.has_ppp_icon);
			holder.pp2_img.setImageResource(R.drawable.has_ppp_icon);
			holder.pp3_img.setImageResource(R.drawable.has_ppp_icon);
			holder.ppp_imageView.setImageResource(R.drawable.ppp_bottom1_used);
			break;

		default:
			break;
		} 
		
		//初始化背景图片
//		System.out.println("load bg url------------>"+dpp.pppBgUrl);
//		LayoutParams params = (LayoutParams) holder.ppp_imageView.getLayoutParams();
//		params.width = ScreenUtil.getScreenWidth(mContext);
//		params.height = ScreenUtil.getScreenHeight(mContext) * 2 / 5;
//		holder.ppp_imageView.setLayoutParams(params);
//		if (dpp.pppBgUrl != null && !"".equals(dpp.pppBgUrl)) {//如果不为空，显示照片
//			UniversalImageLoadTool.loadImage(dpp.pppBgUrl, holder.ppp_imageView);
////			imageLoader.displayImage(dpp.pppBgUrl, holder.ppp_imageView);
//		}else {//显示内置图片
////			if (position/2 ==0) {
//			holder.ppp_imageView.setImageResource(R.drawable.ppp_bottom1);
////			}else if (position/2 ==1) {
////				holder.ppp_imageView.setImageResource(R.drawable.ppp_bottom2);
////			}else {
////				holder.ppp_imageView.setImageResource(R.drawable.ppp_bottom2);
////			}
//			holder.ppp_imageView.setTag("null");
//		}
		return convertView;
	}

	private class ViewHolder {
		TextView time;//绑定的时间
		TextView pppNumber;//ppp卡的序列号
		ImageView pp1_img, pp2_img, pp3_img;//三个ppp的格子
		TextView tvState; //状态
		TextView tvExpired; //日期
		ImageView ppp_imageView;//背景图
	}

}
