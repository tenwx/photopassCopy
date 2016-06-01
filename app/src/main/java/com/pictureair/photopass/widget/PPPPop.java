package com.pictureair.photopass.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pictureair.photopass.R;

public class PPPPop extends PopupWindow implements OnClickListener {
	private LayoutInflater inflater;
	private Context context;
	private View pppView;
	private TextView llBuy, llAuto, llInput;
	private Handler handler;
	private ImageView line;
	private boolean isStory;
	public static final int POP_BUY = 9999;
	public static final int POP_SCAN = 8888;
	public static final int POP_INPUT = 7777;

	public PPPPop(Context context, Handler handler, boolean isStroy) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.isStory = isStroy;
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
		llInput = (TextView) pppView.findViewById(R.id.input_tv);
		line = (ImageView) pppView.findViewById(R.id.line);

		llBuy.setOnClickListener(this);
		llAuto.setOnClickListener(this);
		llInput.setOnClickListener(this);

		if (isStory) {
			line.setVisibility(View.GONE);
			llBuy.setVisibility(View.GONE);
		}

		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent)));
		setFocusable(true);
		setOutsideTouchable(true);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.buyTextView:
				//handler 传到MyPPP页面
				handler.sendEmptyMessage(POP_BUY);
				break;

			case R.id.scanTextView:
				handler.sendEmptyMessage(POP_SCAN);
				break;

			case R.id.input_tv:
				handler.sendEmptyMessage(POP_INPUT);
				break;

			default:
				break;
		}
	}
}
