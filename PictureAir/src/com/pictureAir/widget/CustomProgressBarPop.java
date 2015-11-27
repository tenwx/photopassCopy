package com.pictureAir.widget;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pictureAir.R;
import com.pictureAir.util.ScreenUtil;

public class CustomProgressBarPop{
	private Context context;
	private ImageView imageView;
	private TextView textView;
	private LayoutInflater inflater;
	private View defaultView;
	private PopupWindow popupWindow;
	private View parentView;
	private boolean isDelete;//删除文件的总文件数量，仅针对删除文件操作
	/**
	 * 初始化
	 * @param context
	 * @param parentView 父控件
	 * @param isDelete 删除文件操作
	 */
	public CustomProgressBarPop(Context context, View parentView, boolean isDelete) {
		this.context = context;
		this.parentView = parentView;
		this.isDelete = isDelete;
		initPopupWindow();
	}
	
	private void initPopupWindow() {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		defaultView = inflater.inflate(R.layout.customprogressdialog, null);
		popupWindow = new PopupWindow(defaultView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(true);
		
		
		
		imageView = (ImageView) defaultView.findViewById(R.id.loadingImageView);
		textView = (TextView) defaultView.findViewById(R.id.id_tv_loadingmsg);
		LayoutParams params = imageView.getLayoutParams();
		params.width = ScreenUtil.getScreenWidth(context) / 5;
		params.height = params.width;
		imageView.setBackgroundColor(Color.TRANSPARENT);
//		setWidth(LayoutParams.WRAP_CONTENT);
//		setHeight(LayoutParams.WRAP_CONTENT);
//		setFocusable(false);
//		setOutsideTouchable(true);
		
//		defaultView.setFocusable(true);
//		defaultView.setFocusableInTouchMode(true);
//		defaultView.setOnKeyListener(new OnKeyListener() {
//			
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				// TODO Auto-generated method stub
//				System.out.println("---> back on click");
//				return false;
//			}
//		});
	}
	
	/**
	 * 显示对话框
	 * @param totalCount 删除文件的文件数量，如果是上传下载图片操作，可以传0
	 */
	public void show(int totalCount){
		popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
		imageView.setImageResource(R.drawable.loading_0);
		if (isDelete) {//删除文件操作
			textView.setText(String.format(context.getString(R.string.delete_percent), 0, totalCount));
		}else {//上传下载操作
			textView.setText(String.format(context.getString(R.string.loading_percent), 0) + "%");
			
		}
	}
	
	public void dismiss(){
		if (popupWindow != null) {
			popupWindow.dismiss();
		}
	}
	
	public boolean isShowing(){
		return popupWindow.isShowing();
	}
	
	/**
	 * 更新进度条
	 * @param progress
	 */
	public void setProgress(int progress, int total){
//		System.out.println("--------->"+ progress);
		int currentProgress = progress * 100 / total;
		int result = R.drawable.loading_0;
		if (currentProgress >=0 && currentProgress <= 8) {
			result = R.drawable.loading_0;
		}else if (currentProgress >8 && currentProgress <= 16) {
			result = R.drawable.loading_1;
		}else if (currentProgress >16 && currentProgress <= 25) {
			result = R.drawable.loading_2;
		}else if (currentProgress >25 && currentProgress <= 33) {
			result = R.drawable.loading_3;
		}else if (currentProgress >33 && currentProgress <= 41) {
			result = R.drawable.loading_4;
		}else if (currentProgress >41 && currentProgress <= 50) {
			result = R.drawable.loading_5;
		}else if (currentProgress >50 && currentProgress <= 58) {
			result = R.drawable.loading_6;
		}else if (currentProgress >58 && currentProgress <= 66) {
			result = R.drawable.loading_7;
		}else if (currentProgress >66 && currentProgress <= 75) {
			result = R.drawable.loading_8;
		}else if (currentProgress >75 && currentProgress <= 83) {
			result = R.drawable.loading_9;
		}else if (currentProgress >83 && currentProgress <= 91) {
			result = R.drawable.loading_10;
		}else if (currentProgress >91 && currentProgress < 100) {
			result = R.drawable.loading_11;
		}else {
			result = R.drawable.loading_12;
		}
		imageView.setImageResource(result);
		if (isDelete) {//删除文件操作
			textView.setText(String.format(context.getString(R.string.delete_percent), progress, total));
		}else {//上传下载操作
			textView.setText(String.format(context.getString(R.string.loading_percent), progress * 100 / total) + "%");
		}
	}
	
}
