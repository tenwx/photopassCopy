package com.pictureAir.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureAir.R;
import com.pictureAir.entity.DayOfPP;
import com.pictureAir.entity.DayOfPPP;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;
import com.pictureAir.util.UniversalImageLoadTool;
import com.pictureAir.widget.MyToast;

/**pp数据的适配器*/
public class ListOfPPAdapter extends BaseAdapter {
	/**设置一个最初对比时间，防止空指针异常*/
	private ArrayList<?> arrayList = null;
	private Context mContext;
	private HashMap<Integer, Boolean> map;//统计被勾选的子项
	private OnItemChildClickListener childClickListener;
	private Handler mHandler;
	private DayOfPPP dpp;
	private int useNumber = 0;//已经使用的个数
	private int choice = 0;//选中的个数

	private MyToast newToast;
	private ImageLoader imageLoader;

	int requestWidth = 0;

	public ListOfPPAdapter(ArrayList<?> arrayList, Context mContext) {
		this.arrayList = arrayList;
		this.mContext = mContext;
		map = new HashMap<Integer, Boolean>();
		newToast = new MyToast(mContext);
		imageLoader = ImageLoader.getInstance();
	}

	public ListOfPPAdapter(ArrayList<?> arrayList, Context mContext, Handler mHandler, DayOfPPP dpp) {
		this.arrayList = arrayList;
		this.mContext = mContext;
		this.mHandler = mHandler;
		this.dpp = dpp;
		map = new HashMap<Integer, Boolean>();
		useNumber = dpp.usedNumber;
		newToast = new MyToast(mContext);
		imageLoader = ImageLoader.getInstance();
		requestWidth = (ScreenUtil.getScreenWidth(mContext) - ScreenUtil.dip2px(mContext, 30)) / 6;
	}

	public ArrayList<?> getArrayList() {
		return arrayList;
	}

	public void setArrayList(ArrayList<?> arrayList) {
		this.arrayList = arrayList;
	}

	public HashMap<Integer, Boolean> getMap() {
		return map;
	}

	public void setMap(HashMap<Integer, Boolean> map) {
		this.map = map;
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
		DayOfPP dp = null;
		childClickListener = new OnItemChildClickListener(position);
		//		if (position == 0) {
		//			convertView = null;
		//		}
		//初始化view
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.my_pp_list, null);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.img1 = (ImageView) convertView.findViewById(R.id.pp_img1);
			holder.img2 = (ImageView) convertView.findViewById(R.id.pp_img2);
			holder.img3 = (ImageView) convertView.findViewById(R.id.pp_img3);
			holder.img4 = (ImageView) convertView.findViewById(R.id.pp_img4);
			holder.img5 = (ImageView) convertView.findViewById(R.id.pp_img5);
			holder.img6 = (ImageView) convertView.findViewById(R.id.pp_img6);
			holder.img7 = (ImageView) convertView.findViewById(R.id.pp_img7);
			holder.img8 = (ImageView) convertView.findViewById(R.id.pp_img8);
			holder.img9 = (ImageView) convertView.findViewById(R.id.pp_img9);
			holder.img10 = (ImageView) convertView.findViewById(R.id.pp_img10);
			holder.img11 = (ImageView) convertView.findViewById(R.id.pp_img11);

			holder.img12 = (ImageView) convertView.findViewById(R.id.pp_img12);
			holder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.rl);
			holder.number = (TextView) convertView.findViewById(R.id.tv_number);

			holder.img_no_check = (ImageView) convertView.findViewById(R.id.img);
			holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.pp_item);
			holder.pp_image_layout2 = (LinearLayout) convertView.findViewById(R.id.pp_image_layout2);
			holder.img_miqi = (ImageView) convertView.findViewById(R.id.lefttop);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		LinearLayout.LayoutParams paramTest = (LinearLayout.LayoutParams)holder.img1.getLayoutParams();  
		paramTest.setMargins(2, 2, 2, 2);
		paramTest.height = requestWidth;
		//初始化照片控件大小
		//		LayoutParams params1 = holder.img1.getLayoutParams();
		//		params1.height = requestWidth;
		holder.img1.setLayoutParams(paramTest);
		holder.img1.setScaleType(ScaleType.CENTER_CROP);
		holder.img2.setLayoutParams(paramTest);
		holder.img2.setScaleType(ScaleType.CENTER_CROP);
		holder.img3.setLayoutParams(paramTest);
		holder.img3.setScaleType(ScaleType.CENTER_CROP);
		holder.img4.setLayoutParams(paramTest);
		holder.img4.setScaleType(ScaleType.CENTER_CROP);
		holder.img5.setLayoutParams(paramTest);
		holder.img5.setScaleType(ScaleType.CENTER_CROP);
		holder.img6.setLayoutParams(paramTest);
		holder.img6.setScaleType(ScaleType.CENTER_CROP);
		holder.img7.setLayoutParams(paramTest);
		holder.img7.setScaleType(ScaleType.CENTER_CROP);
		holder.img8.setLayoutParams(paramTest);
		holder.img8.setScaleType(ScaleType.CENTER_CROP);
		holder.img9.setLayoutParams(paramTest);
		holder.img9.setScaleType(ScaleType.CENTER_CROP);
		holder.img10.setLayoutParams(paramTest);
		holder.img10.setScaleType(ScaleType.CENTER_CROP);
		holder.img11.setLayoutParams(paramTest);
		holder.img11.setScaleType(ScaleType.CENTER_CROP);
		//		holder.img12.setLayoutParams(paramTest);
		holder.relativeLayout.setLayoutParams(paramTest);



		dp = (DayOfPP) arrayList.get(position);
		if (dp.number == 0) {
			holder.img_miqi.setImageResource(R.drawable.my_pp_miqi_no_photo);
		}else {
			holder.img_miqi.setImageResource(R.drawable.my_pp_miqi);
		}
		//		db.number  数量
		holder.time.setText(dp.ppId);

		if (dp.number<=6) {
			holder.pp_image_layout2.setVisibility(View.GONE);
			if (dp.pp_url1 == null || "".equals(dp.pp_url1)) {
				holder.img1.setImageResource(R.drawable.default_pp);
				holder.img1.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img1.setTag("null");//要全部设置tag，不然获取tag的时候，会数据混乱
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url1, holder.img1);
			}
			if (dp.pp_url2 == null || "".equals(dp.pp_url2)) {
				holder.img2.setImageResource(R.drawable.default_pp);
				holder.img2.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img2.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url2, holder.img2);
			}
			if (dp.pp_url3 == null || "".equals(dp.pp_url3)) {
				holder.img3.setImageResource(R.drawable.default_pp);
				holder.img3.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img3.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url3, holder.img3);
			}

			if (dp.pp_url4 == null || "".equals(dp.pp_url4)) {
				holder.img4.setImageResource(R.drawable.default_pp);
				holder.img4.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img4.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url4, holder.img4);
			}


			if (dp.pp_url5 == null || "".equals(dp.pp_url5)) {
				holder.img5.setImageResource(R.drawable.default_pp);
				holder.img5.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img5.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url5, holder.img5);
			}

			if (dp.pp_url6 == null || "".equals(dp.pp_url6)) {
				holder.img6.setImageResource(R.drawable.default_pp);
				holder.img6.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img6.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url6, holder.img6);
			}

		}else if(dp.number>6 && dp.number <= 12){
			holder.number.setVisibility(View.GONE);
			holder.pp_image_layout2.setVisibility(View.VISIBLE);
			if (dp.pp_url1 == null || "".equals(dp.pp_url1)) {
				holder.img1.setImageResource(R.drawable.default_pp);
				holder.img1.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img1.setTag("null");//要全部设置tag，不然获取tag的时候，会数据混乱
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url1, holder.img1);
			}
			if (dp.pp_url2 == null || "".equals(dp.pp_url2)) {
				holder.img2.setImageResource(R.drawable.default_pp);
				holder.img2.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img2.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url2, holder.img2);
			}
			if (dp.pp_url3 == null || "".equals(dp.pp_url3)) {
				holder.img3.setImageResource(R.drawable.default_pp);
				holder.img3.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img3.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url3, holder.img3);
			}

			if (dp.pp_url4 == null || "".equals(dp.pp_url4)) {
				holder.img4.setImageResource(R.drawable.default_pp);
				holder.img4.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img4.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url4, holder.img4);
			}


			if (dp.pp_url5 == null || "".equals(dp.pp_url5)) {
				holder.img5.setImageResource(R.drawable.default_pp);
				holder.img5.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img5.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url5, holder.img5);
			}

			if (dp.pp_url6 == null || "".equals(dp.pp_url6)) {
				holder.img6.setImageResource(R.drawable.default_pp);
				holder.img6.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img6.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url6, holder.img6);
			}

			if (dp.pp_url7 == null || "".equals(dp.pp_url7)) {
				holder.img7.setImageResource(R.drawable.default_pp);
				holder.img7.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img7.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url7, holder.img7);
			}

			if (dp.pp_url8 == null || "".equals(dp.pp_url8)) {
				holder.img8.setImageResource(R.drawable.default_pp);
				holder.img8.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img8.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url8, holder.img8);
			}

			if (dp.pp_url9 == null || "".equals(dp.pp_url9)) {
				holder.img9.setImageResource(R.drawable.default_pp);
				holder.img9.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img9.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url9, holder.img9);
			}

			if (dp.pp_url10 == null || "".equals(dp.pp_url10)) {
				holder.img10.setImageResource(R.drawable.default_pp);
				holder.img10.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img10.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url10, holder.img10);
			}

			if (dp.pp_url11 == null || "".equals(dp.pp_url11)) {
				holder.img11.setImageResource(R.drawable.default_pp);
				holder.img11.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img11.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url11, holder.img11);
			}

			if (dp.pp_url12 == null || "".equals(dp.pp_url12)) {
				holder.img12.setImageResource(R.drawable.default_pp);
				holder.img12.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img12.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url12, holder.img12);
			}
		}else if(dp.number > 12){
			holder.pp_image_layout2.setVisibility(View.VISIBLE);

			Log.e("dp.number", "dp number :"+dp.number);
			if (dp.pp_url1 == null || "".equals(dp.pp_url1)) {
				holder.img1.setImageResource(R.drawable.default_pp);
				holder.img1.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img1.setTag("null");//要全部设置tag，不然获取tag的时候，会数据混乱
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url1, holder.img1);
			}
			if (dp.pp_url2 == null || "".equals(dp.pp_url2)) {
				holder.img2.setImageResource(R.drawable.default_pp);
				holder.img2.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img2.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url2, holder.img2);
			}
			if (dp.pp_url3 == null || "".equals(dp.pp_url3)) {
				holder.img3.setImageResource(R.drawable.default_pp);
				holder.img3.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img3.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url3, holder.img3);
			}

			if (dp.pp_url4 == null || "".equals(dp.pp_url4)) {
				holder.img4.setImageResource(R.drawable.default_pp);
				holder.img4.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img4.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url4, holder.img4);
			}


			if (dp.pp_url5 == null || "".equals(dp.pp_url5)) {
				holder.img5.setImageResource(R.drawable.default_pp);
				holder.img5.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img5.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url5, holder.img5);
			}

			if (dp.pp_url6 == null || "".equals(dp.pp_url6)) {
				holder.img6.setImageResource(R.drawable.default_pp);
				holder.img6.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img6.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url6, holder.img6);
			}

			if (dp.pp_url7 == null || "".equals(dp.pp_url7)) {
				holder.img7.setImageResource(R.drawable.default_pp);
				holder.img7.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img7.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url7, holder.img7);
			}

			if (dp.pp_url8 == null || "".equals(dp.pp_url8)) {
				holder.img8.setImageResource(R.drawable.default_pp);
				holder.img8.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img8.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url8, holder.img8);
			}

			if (dp.pp_url9 == null || "".equals(dp.pp_url9)) {
				holder.img9.setImageResource(R.drawable.default_pp);
				holder.img9.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img9.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url9, holder.img9);
			}

			if (dp.pp_url10 == null || "".equals(dp.pp_url10)) {
				holder.img10.setImageResource(R.drawable.default_pp);
				holder.img10.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img10.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url10, holder.img10);
			}

			if (dp.pp_url11 == null || "".equals(dp.pp_url11)) {
				holder.img11.setImageResource(R.drawable.default_pp);
				holder.img11.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img11.setTag("null");
			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url11, holder.img11);
			}

			if (dp.pp_url12 == null || "".equals(dp.pp_url12)) {
				holder.img12.setImageResource(R.drawable.default_pp);
				holder.img12.setScaleType(ScaleType.CENTER_INSIDE);
				holder.img12.setTag("null");

			}else {
				UniversalImageLoadTool.loadImage(dp.pp_url12, holder.img12);
				holder.number.setVisibility(View.VISIBLE);
				holder.number.setText(""+dp.number);
			}

		}



		//初始化选中与否
		if (dp.isSelected == 0){//未选中
			holder.img_no_check.setImageResource(R.drawable.nosele);
		}else if (dp.isSelected == 1) {//选中
			holder.img_no_check.setImageResource(R.drawable.sele);
		}else {//不可选
			holder.img_no_check.setImageResource(R.drawable.del1);
		}
		holder.itemLayout.setOnClickListener(childClickListener);

		return convertView;
	}

	private class ViewHolder {
		TextView time, ppCode,number;//pp的时间，pp码，pp对应的照片的数量
		ImageView img1, img2, img3,img4,img5,img6,img7,img8,img9,img10,img11,img12, img_no_check, img_miqi;//pp显示的三个imageview，一个不可选择的imageview
		LinearLayout itemLayout,pp_image_layout2;//每一项
		RelativeLayout relativeLayout; //最后一个图片的布局

	}


	private class OnItemChildClickListener implements OnClickListener {
		private int position;

		public OnItemChildClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			if (arrayList.get(position) instanceof DayOfPP) {
				DayOfPP dp = (DayOfPP) arrayList.get(position);
				switch (v.getId()) {
				case R.id.pp_item:
					System.out.println("adapter---size="+map.size());
					if (null != mHandler) {
						Message msg = mHandler.obtainMessage();
						if (dp.isSelected == 0) {
							if (useNumber >= dpp.amount) {
								newToast.setTextAndShow(R.string.outofrange, Common.TOAST_SHORT_TIME);
								break;
							} else {
								++choice;
								for (int j = 0; j < arrayList.size(); j++) {
									DayOfPP pp = (DayOfPP) arrayList.get(j);
									if (!pp.time.equals(dp.time)) {// && !pp.time.equals("")
										pp.isSelected = 2;
									}
								}
								dp.isSelected = 1;
								++useNumber;
							}
						} else if (dp.isSelected == 2) {

						}else {
							--choice;
							if (choice == 0) {
								for (int j = 0; j < arrayList.size(); j++) {
									DayOfPP pp = (DayOfPP) arrayList.get(j);
									if (pp.isSelected == 2) {
										pp.isSelected = 0;
									}
								}
							}
							dp.isSelected = 0;
							--useNumber;
						}
						notifyDataSetChanged();
						msg.arg1 = useNumber;
						msg.what = 2;
						mHandler.sendMessage(msg);
					}
					map.put(position, dp.isSelected == 1);
					break;

				default:
					break;
				}
			} 
		}
	}

}
