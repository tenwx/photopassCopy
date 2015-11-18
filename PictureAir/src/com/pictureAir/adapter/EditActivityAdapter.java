package com.pictureAir.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureAir.R;
import com.pictureAir.util.ScreenUtil;

public class EditActivityAdapter extends BaseAdapter {
	private Context mContext ;
	private int[] mImageUrl;
	private int editType = 0;
	private String[] rotateText = {"Left 90°","Right 90°"};
	private String[] filterText = {"Original","Lomo","Earlybird","Natural","HDR","whitening","Vintage"};
	
	//图片
	private Bitmap bitmap;
	public EditActivityAdapter(Context context ,Bitmap bitmap,int[] imageUrl, int editType){
		this.mContext = context;
		this.mImageUrl = imageUrl;
		this.editType = editType;
		this.bitmap = bitmap;
		
	}
	
	@Override
	public int getCount() {
		return mImageUrl.length;
	}

	public int[] getmList() {
		return mImageUrl;
	}

	public void setmList(int[] mList) {
		this.mImageUrl = mList;
	}

	@Override
	public Object getItem(int position) {
		return mImageUrl[position];   
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HolderView holderView = null;
		// 布局混乱：如果position等于0的时候，重新加载布局。
		if (position == 0) {
			convertView = null;
		}
		if(convertView == null ){
			holderView = new HolderView();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.edit_item, parent, false);
			holderView.editImageview = (ImageView) convertView.findViewById(R.id.editImageview);
			holderView.editText = (TextView) convertView.findViewById(R.id.editTextview);
			holderView.editText.setVisibility(View.GONE);
			convertView.setTag(holderView);
		}else{
			holderView = (HolderView) convertView.getTag();
		}
		
		holderView.editImageview.setImageResource(mImageUrl[position]);
		
		if(editType == 2){
			holderView.editText.setText(filterText[position]);
			holderView.editText.setVisibility(View.VISIBLE);
		}else if(editType == 3){
			holderView.editImageview.setBackgroundResource(R.drawable.decoration_bg);
		}else if(editType == 1){
			convertView.measure(0,0);
			LayoutParams layoutParams = holderView.editImageview.getLayoutParams();
			layoutParams.height = holderView.editImageview.getMeasuredHeight();
			layoutParams.width = holderView.editImageview.getMeasuredWidth();
			holderView.editImageview.setLayoutParams(layoutParams);
			holderView.editImageview.setBackgroundDrawable(new BitmapDrawable(bitmap));
			holderView.editImageview.setScaleType(ImageView.ScaleType.FIT_XY);
		}
		
		return convertView;
	}
	
	class HolderView{
		ImageView editImageview;
		TextView editText;
	}
	
}
