package com.pictureAir.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.pictureAir.R;
import com.pictureAir.entity.PhotoItemInfo;

/**
 *此控件负责photo-album页面中menu下拉菜单的内容 
 */
public class DefaultPop2 extends PopupWindow implements OnClickListener{
	public static final int ADDPHOTOS = 201;
	public static final int MAKEFIGT = 202;
	private Context context;
	private LayoutInflater inflater;
	private View defaultView;
//	private PhotoItemInfo info;
//	private Handler mHandler;
	private ImageView addphotoImageView, makegiftImageView;
//	private RelativeLayout download,buy,location,favourite;
	private LinearLayout addphoto, makegift;
	public DefaultPop2(Context context) {
		super(context);
		this.context = context;
		initPopupWindow();
	}

	public void initPopupWindow() {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		defaultView = inflater.inflate(R.layout.pop2, null);
		defaultView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		setContentView(defaultView);
		addphoto = (LinearLayout) defaultView.findViewById(R.id.addphoto);
		makegift = (LinearLayout) defaultView.findViewById(R.id.makegift);
		
		addphotoImageView = (ImageView)defaultView.findViewById(R.id.imageviewaddphoto);
		makegiftImageView = (ImageView)defaultView.findViewById(R.id.imageviewmakegift);
		addphotoImageView.setOnClickListener(null);
		makegiftImageView.setOnClickListener(null);
		
		addphoto.setOnClickListener(this);
		makegift.setOnClickListener(this);
		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
		setFocusable(true);
		setOutsideTouchable(true);

	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.addphoto:
			System.out.println("addphoto");
			addphotoImageView.performClick();
			break;
		case R.id.makegift:
			System.out.println("makegift");
			makegiftImageView.performClick();
			break;
		default:
			break;
		}
		if(isShowing()){
			dismiss();
		}
		
	}
}
