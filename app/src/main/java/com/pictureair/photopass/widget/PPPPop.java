package com.pictureair.photopass.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.ScreenUtil;

public class PPPPop extends PopupWindow implements OnClickListener {
	private LayoutInflater inflater;
	private Context context;
	private View pppView;
	private TextView llBuy, llAuto;
	private Handler handler;

	public PPPPop(Context context,Handler handler) {
		super(context);
		this.context = context;
		this.handler = handler;
		initPopupWindow();
	}

	public void initPopupWindow() {
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		pppView = inflater.inflate(R.layout.ppppop, null);
//		pppView.setLayoutParams(new LayoutParams(ScreenUtil.getScreenWidth(context) / 3,
//				LayoutParams.WRAP_CONTENT));
		setContentView(pppView);
		llBuy = (TextView) pppView.findViewById(R.id.buyTextView);
		llAuto = (TextView) pppView.findViewById(R.id.scanTextView);

		llBuy.setOnClickListener(this);
		llAuto.setOnClickListener(this);

		setWidth(ScreenUtil.getScreenWidth(context) * 2 / 5);
		setHeight(LayoutParams.WRAP_CONTENT);
		setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent)));
		setFocusable(true);
		setOutsideTouchable(true);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		Message msg;
		switch (v.getId()) {
		case R.id.buyTextView:
			//handler 传到MyPPP页面
			msg = handler.obtainMessage();
			msg.what = 999;
			handler.sendMessage(msg);
			break;

		case R.id.scanTextView:
			msg = handler.obtainMessage();
			msg.what = 888;
			handler.sendMessage(msg);
			break;
		default:
			break;
		}
	}
}
