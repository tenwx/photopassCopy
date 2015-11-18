package com.pictureAir.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;

import com.pictureAir.R;
import com.pictureAir.util.ScreenUtil;

/**
 *此控件负责album页面中切换相册的下拉菜单
 */
public class SelectAlbumPop extends PopupWindow implements OnClickListener{
	public static final int PHOTOPASS = 201;
	public static final int MAGIC = 202;
	public static final int DOWNLOAD = 203;
	public static final int OTHER = 204;
	private Context context;
	private LayoutInflater inflater;
	private Handler handler;
	private View defaultView, photopassLine, magicLine, downloadLine;
	private RelativeLayout photoPassLayout, magicLayout, downloadLayout, otherLayout;
	public SelectAlbumPop(Context context, Handler handler) {
		super(context);
		this.context = context;
		this.handler = handler;
		initPopupWindow();
	}

	public void initPopupWindow() {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		defaultView = inflater.inflate(R.layout.album_pop, null);
//		defaultView.setLayoutParams(new LayoutParams(ScreenUtil.getScreenWidth(context) / 2, ScreenUtil.getScreenHeight(context) / 3));
		setContentView(defaultView);
		photoPassLayout = (RelativeLayout) defaultView.findViewById(R.id.photopassLinearLayout);
		magicLayout = (RelativeLayout) defaultView.findViewById(R.id.magicLinearLayout);
		downloadLayout = (RelativeLayout) defaultView.findViewById(R.id.downloadLinearLayout);
		otherLayout = (RelativeLayout) defaultView.findViewById(R.id.otherLinearLayout);
		
		photopassLine = (View) defaultView.findViewById(R.id.photopass_line);
		magicLine = (View) defaultView.findViewById(R.id.magic_line);
		downloadLine = (View) defaultView.findViewById(R.id.download_line);
		
		photoPassLayout.setOnClickListener(this);
		magicLayout.setOnClickListener(this);
		downloadLayout.setOnClickListener(this);
		otherLayout.setOnClickListener(this);
		setWidth(ScreenUtil.getScreenWidth(context) * 2 / 3);
		setHeight(ScreenUtil.getScreenHeight(context) / 3);
		setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
		setFocusable(true);
		setOutsideTouchable(true);

	}
	
	public void setType(int type){
		photoPassLayout.setVisibility(View.VISIBLE);
		photopassLine.setVisibility(View.VISIBLE);
		magicLayout.setVisibility(View.VISIBLE);
		magicLine.setVisibility(View.VISIBLE);
		downloadLayout.setVisibility(View.VISIBLE);
		downloadLine.setVisibility(View.VISIBLE);
		otherLayout.setVisibility(View.VISIBLE);
		switch (type) {
		case PHOTOPASS://photopass被选中
			photoPassLayout.setVisibility(View.GONE);
			photopassLine.setVisibility(View.GONE);
			break;
			
		case MAGIC://magic被选中
			magicLayout.setVisibility(View.GONE);
			magicLine.setVisibility(View.GONE);
			break;
			
		case DOWNLOAD://download被选中
			downloadLayout.setVisibility(View.GONE);
			downloadLine.setVisibility(View.GONE);
			break;
			
		case OTHER://other被选中
			downloadLine.setVisibility(View.GONE);
			otherLayout.setVisibility(View.GONE);
			break;

		default:
			break;
		}
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.photopassLinearLayout:
			handler.sendEmptyMessage(PHOTOPASS);
			break;
			
		case R.id.magicLinearLayout:
			handler.sendEmptyMessage(MAGIC);
			break;
			
		case R.id.downloadLinearLayout:
			handler.sendEmptyMessage(DOWNLOAD);
			break;
			
		case R.id.otherLinearLayout:
			handler.sendEmptyMessage(OTHER);
			break;
			
		default:
			break;
		}
		if(isShowing()){
			dismiss();
		}
		
	}
}
