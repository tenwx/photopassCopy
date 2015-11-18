package com.pictureAir.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureAir.EditStoryAlbumActivity;
import com.pictureAir.MyPPActivity;
import com.pictureAir.R;
import com.pictureAir.entity.PPCodeInfo1;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.entity.PhotoItemInfo;
import com.pictureAir.util.ScreenUtil;
import com.pictureAir.util.UniversalImageLoadTool;

/** pp数据的适配器 */
public class ListOfPPAdapter1 extends BaseAdapter implements OnClickListener {
	/** 设置一个最初对比时间，防止空指针异常 */
	private ArrayList<PPCodeInfo1> arrayList;
	private Context mContext;
	private int screenWidth = 0;// 屏幕宽度
	private ViewHolder holder;
	private int curInedx;
    private doShowPhotoListener listener;
    private doDeletePhotoListener deleteListner;
    private LinearLayout.LayoutParams params;
    private RelativeLayout.LayoutParams params2;

	public ListOfPPAdapter1(ArrayList<PPCodeInfo1> list, Context mContext,final doShowPhotoListener listener,final doDeletePhotoListener deleteListner) {
		this.arrayList = list;
		this.mContext = mContext;
        this.listener = listener;
        this.deleteListner = deleteListner;
        screenWidth = ScreenUtil.getScreenWidth(mContext);// 获取屏幕宽度
         params = new LinearLayout.LayoutParams(
				(screenWidth - 60) / 6, (screenWidth - 60) / 6);
		params.setMargins(5, 0, 5, 0);
		params2 = new RelativeLayout.LayoutParams(
				(screenWidth - 60) / 6, (screenWidth - 60) / 6);
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
		curInedx = position;
		// 初始化view
		holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.my_pp_list_1, null);
			holder.ppCode = (TextView) convertView.findViewById(R.id.pp_code);
			holder.deleteMyPP = (ImageView) convertView
					.findViewById(R.id.delete_my_pp);
			holder.image1 = (ImageView) convertView.findViewById(R.id.pp_img1);
			holder.image2 = (ImageView) convertView.findViewById(R.id.pp_img2);
			holder.image3 = (ImageView) convertView.findViewById(R.id.pp_img3);
			holder.image4 = (ImageView) convertView.findViewById(R.id.pp_img4);
			holder.image5 = (ImageView) convertView.findViewById(R.id.pp_img5);
			holder.image6 = (ImageView) convertView.findViewById(R.id.pp_img6);
			holder.image7 = (ImageView) convertView.findViewById(R.id.pp_img7);
			holder.image8 = (ImageView) convertView.findViewById(R.id.pp_img8);
			holder.image9 = (ImageView) convertView.findViewById(R.id.pp_img9);
			holder.image10 = (ImageView) convertView.findViewById(R.id.pp_img10);
			holder.image11 = (ImageView) convertView.findViewById(R.id.pp_img11);
			holder.image12 = (ImageView) convertView.findViewById(R.id.pp_img12);
			
			holder.ppImageLayout1 = (LinearLayout) convertView
					.findViewById(R.id.pp_image_layout1);
			holder.ppImageLayout2 = (LinearLayout) convertView
					.findViewById(R.id.pp_image_layout2);
			holder.showCconutLayout = (RelativeLayout) convertView
					.findViewById(R.id.show_conut_layout);
			holder.photoCount = (TextView) convertView
					.findViewById(R.id.photo_count);
			holder.conerImageView = (ImageView) convertView.findViewById(R.id.my_pp_miqi);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		
		//判断是否显示删除按钮
		if (MyPPActivity.isDeletePhoto) {
			holder.deleteMyPP.setVisibility(View.VISIBLE);
		}else {
			holder.deleteMyPP.setVisibility(View.GONE);
		}
		//进图列表
		holder.ppCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (arrayList.get(position).getUrlList().size() > 12) {
					//进入相册
					
				}
			}
		});
		
		//删除图片
		holder.deleteMyPP.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.v("=========", "doDeletePhotoListener onClick: " + position);
				deleteListner.doDeletePhotoListener(position);
			}
		});

		
		holder.image1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 0)) {
					listener.doShowPhotoListener(position, 0);
				}
			}
		});
		holder.image2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 1)) {
					listener.doShowPhotoListener(position, 1);
				}
			}
		});
		holder.image3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 2)) {
					listener.doShowPhotoListener(position, 2);
				}
			}
		});
		holder.image4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 3)) {
					listener.doShowPhotoListener(position, 3);
				}
			}
		});
		holder.image5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 4)) {
					listener.doShowPhotoListener(position, 4);
				}
			}
		});
		holder.image6.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 5)) {
					listener.doShowPhotoListener(position, 5);
				}
			}
		});
		holder.image7.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 6)) {
					listener.doShowPhotoListener(position, 6);
				}
			}
		});
		holder.image8.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 7)) {
					listener.doShowPhotoListener(position, 7);
				}
			}
		});
		holder.image9.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 8)) {
					listener.doShowPhotoListener(position, 8);
				}
			}
		});
		holder.image10.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 9)) {
					listener.doShowPhotoListener(position, 9);
				}
			}
		});
		holder.image11.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 10)) {
					listener.doShowPhotoListener(position, 10);
				}
			}
		});
		
        holder.image12.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (checkUrl(position, 11)) {
					if (arrayList.get(position).getUrlList().size() > 12) {
						//进入相册
//						ArrayList<PhotoItemInfo> allPhotoItemInfos = new ArrayList<PhotoItemInfo>();
//						PhotoItemInfo photoItemInfo = new PhotoItemInfo();
//						photoItemInfo.list = arrayList.get(position).getSelectPhotoItemInfos();
////						public String shootTime;// 拍摄时间 年月日,同一个pp，有多个不同的时间，需要分类处理
////						public String place;// 拍摄地点，需要数据库中添加这个字段
//						Intent i = new Intent(mContext, EditStoryAlbumActivity.class);
//						Bundle b = new Bundle();
//						b.putParcelableArrayList("photos", allPhotoItemInfos);
//						i.putExtra("photos", b);
//						mContext.startActivity(i);
					}else {
						listener.doShowPhotoListener(position, 11);
					}
				}
			}
		});

		if (arrayList == null || arrayList.size() <= 0) {
			return convertView;
		}

		
		// 初始化数据
		// 初始化pp码
		PPCodeInfo1 ppInfo1 = new PPCodeInfo1();
		ppInfo1 = arrayList.get(position);
		ArrayList<String> urlList = new ArrayList<String>();
		holder.ppCode.setText(ppInfo1.getPpCode());
		if (ppInfo1.getUrlList() == null || ppInfo1.getUrlList().size() <= 0) {
			ppInfo1.setUrlList(new ArrayList<String>());
		}
		// 图片显示
		int photoCount = ppInfo1.getUrlList().size();
		Log.v("===================================开始=====================================",
				"");
		Log.v("===========", "PP卡: PPCode:" + ppInfo1.getPpCode());
		Log.v("===========", "图片显示 photoCount:" + photoCount);
		urlList.removeAll(urlList);
		urlList.addAll(ppInfo1.getUrlList());
		Log.v("============", "urlList.size()" + urlList.size());
		if (photoCount == 0) {
			holder.conerImageView.setImageResource(R.drawable.my_pp_miqi_no_photo);
		}else {
			holder.conerImageView.setImageResource(R.drawable.my_pp_miqi);
		}
		if (photoCount <= 6) {
			for (int i = 0; i < 6; i++) {
				int num = 6 - photoCount;
				if (num > 0 && i > photoCount - 1) {
					// 显示默认图片
					String defaultUrl = "";
					urlList.add(i, defaultUrl);
					Log.v("===========", "显示默认图片" + urlList.get(i));
				}
				Log.v("===========", "所有图片路径:" + urlList.get(i));
			}
			Log.v("============", "urlList.size()" + urlList.size());
			holder.ppImageLayout2.setVisibility(View.GONE);
			holder.photoCount.setVisibility(View.GONE);
			holder.image1.setLayoutParams(params);
			holder.image2.setLayoutParams(params);
			holder.image3.setLayoutParams(params);
			holder.image4.setLayoutParams(params);
			holder.image5.setLayoutParams(params);
			holder.image6.setLayoutParams(params);

			UniversalImageLoadTool.loadImage1(urlList.get(0), holder.image1);
			UniversalImageLoadTool.loadImage1(urlList.get(1), holder.image2);
			UniversalImageLoadTool.loadImage1(urlList.get(2), holder.image3);
			UniversalImageLoadTool.loadImage1(urlList.get(3), holder.image4);
			UniversalImageLoadTool.loadImage1(urlList.get(4), holder.image5);
			UniversalImageLoadTool.loadImage1(urlList.get(5), holder.image6);

		} else if (photoCount > 6 && photoCount <= 12) {
			for (int i = 0; i < 12; i++) {
				int num = 12 - photoCount;
				if (num > 0 && i > photoCount - 1) {
					// 显示默认图片
					String defaultUrl = "";
					urlList.add(i, defaultUrl);
				}
			}
			Log.v("============", "urlList.size()" + urlList.size());
			holder.ppImageLayout2.setVisibility(View.VISIBLE);
			holder.photoCount.setVisibility(View.GONE);

			holder.image1.setLayoutParams(params);
			holder.image2.setLayoutParams(params);
			holder.image3.setLayoutParams(params);
			holder.image4.setLayoutParams(params);
			holder.image5.setLayoutParams(params);
			holder.image6.setLayoutParams(params);
			holder.image7.setLayoutParams(params);
			holder.image8.setLayoutParams(params);
			holder.image9.setLayoutParams(params);
			holder.image10.setLayoutParams(params);
			holder.image11.setLayoutParams(params);
			holder.showCconutLayout.setLayoutParams(params);
			holder.image12.setLayoutParams(params2);

			UniversalImageLoadTool.loadImage1(urlList.get(0), holder.image1);
			UniversalImageLoadTool.loadImage1(urlList.get(1), holder.image2);
			UniversalImageLoadTool.loadImage1(urlList.get(2), holder.image3);
			UniversalImageLoadTool.loadImage1(urlList.get(3), holder.image4);
			UniversalImageLoadTool.loadImage1(urlList.get(4), holder.image5);
			UniversalImageLoadTool.loadImage1(urlList.get(5), holder.image6);
			UniversalImageLoadTool.loadImage1(urlList.get(6), holder.image7);
			UniversalImageLoadTool.loadImage1(urlList.get(7), holder.image8);
			UniversalImageLoadTool.loadImage1(urlList.get(8), holder.image9);
			UniversalImageLoadTool.loadImage1(urlList.get(9), holder.image10);
			UniversalImageLoadTool.loadImage1(urlList.get(10), holder.image11);
			UniversalImageLoadTool.loadImage1(urlList.get(11), holder.image12);

		} else if (photoCount > 12) {
			holder.ppImageLayout2.setVisibility(View.VISIBLE);
			holder.photoCount.setVisibility(View.VISIBLE);

			holder.image1.setLayoutParams(params);
			holder.image2.setLayoutParams(params);
			holder.image3.setLayoutParams(params);
			holder.image4.setLayoutParams(params);
			holder.image5.setLayoutParams(params);
			holder.image6.setLayoutParams(params);
			holder.image7.setLayoutParams(params);
			holder.image8.setLayoutParams(params);
			holder.image9.setLayoutParams(params);
			holder.image10.setLayoutParams(params);
			holder.image11.setLayoutParams(params);
			holder.showCconutLayout.setLayoutParams(params);
			holder.image12.setLayoutParams(params2);

			holder.photoCount.setText(urlList.size() + "");
			Log.v("============", "urlList.size()" + urlList.size());
			UniversalImageLoadTool.loadImage1(urlList.get(0), holder.image1);
			UniversalImageLoadTool.loadImage1(urlList.get(1), holder.image2);
			UniversalImageLoadTool.loadImage1(urlList.get(2), holder.image3);
			UniversalImageLoadTool.loadImage1(urlList.get(3), holder.image4);
			UniversalImageLoadTool.loadImage1(urlList.get(4), holder.image5);
			UniversalImageLoadTool.loadImage1(urlList.get(5), holder.image6);
			UniversalImageLoadTool.loadImage1(urlList.get(6), holder.image7);
			UniversalImageLoadTool.loadImage1(urlList.get(7), holder.image8);
			UniversalImageLoadTool.loadImage1(urlList.get(8), holder.image9);
			UniversalImageLoadTool.loadImage1(urlList.get(9), holder.image10);
			UniversalImageLoadTool.loadImage1(urlList.get(10), holder.image11);
			UniversalImageLoadTool.loadImage1(urlList.get(11), holder.image12);

		}

		return convertView;
	}

	private class ViewHolder {
		TextView ppCode;// pp的时间，pp码，pp对应的照片的数量,
		ImageView deleteMyPP;//删除PP
		ImageView image1, image2, image3, image4, image5, image6, image7,
				image8, image9, image10, image11, image12;
		LinearLayout ppImageLayout1, ppImageLayout2;
		RelativeLayout showCconutLayout;
		TextView photoCount;
		ImageView conerImageView;
	}
	
	/**
	 * 检测是否包含图片
	 * @param position
	 * @param n
	 * @return
	 */
	public boolean checkUrl(int position ,int n){
		if (arrayList == null || arrayList.size() <= 0 || arrayList.get(position).getUrlList() == null || arrayList.get(position).getUrlList().size() <= 0) {
			return false;
		}
		if (arrayList.get(position).getUrlList() == null || arrayList.get(position).getUrlList().size() < n+1 || arrayList.get(position).getUrlList().get(n).equals("")) {
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.pp_code:

			break;
		case R.id.delete_my_pp:

			break;
		default:
			break;
		}
	}

	/**
	 * 刷新界面
	 * 
	 * @param ppInfo1s
	 *            数据
	 */
	public void refresh(ArrayList<PPCodeInfo1> ppInfo1s) {
		this.arrayList = ppInfo1s;
		this.notifyDataSetChanged();
	}
	
	//删除图片监听
	public interface doDeletePhotoListener{
		public void doDeletePhotoListener(int position);
	}
	
	//显示图片监听
	public interface doShowPhotoListener{
		public void doShowPhotoListener(int position, int tag);
	}

}
