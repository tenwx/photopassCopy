package com.pictureair.photopassCopy.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pictureair.photopassCopy.R;

public class PPPPop extends PopupWindow implements OnClickListener {
	private LayoutInflater inflater;
	private Context context;
	private View pppView;
	private TextView llAuto, llInput, onedayPassTV;
	private Handler handler;
	private int type;
	public static final int POP_BUY = 9999;
	public static final int POP_SCAN = 8888;
	public static final int POP_INPUT = 7777;
	public static final int POP_INPUT_ONE_DAY_PASS = 6666;

	public static final int MENU_TYPE_STORY = 111;
	public static final int MENU_TYPE_PP = 222;

	public PPPPop(Context context, Handler handler, int type) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.type = type;
		initPopupWindow();
	}

	public void initPopupWindow() {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		pppView = inflater.inflate(R.layout.ppppop, null);
		setContentView(pppView);
		llAuto = (TextView) pppView.findViewById(R.id.scanTextView);
		llInput = (TextView) pppView.findViewById(R.id.input_tv);
		onedayPassTV = (TextView) pppView.findViewById(R.id.input_oneday_tv);

		llAuto.setOnClickListener(this);
		llInput.setOnClickListener(this);
		onedayPassTV.setOnClickListener(this);

		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)));
		setFocusable(true);
		setOutsideTouchable(true);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.scanTextView:
				handler.sendEmptyMessage(POP_SCAN);
				break;

			case R.id.input_tv:
				handler.sendEmptyMessage(POP_INPUT);
				break;

			case R.id.input_oneday_tv:
				handler.sendEmptyMessage(POP_INPUT_ONE_DAY_PASS);
				break;

			default:
				break;
		}
	}
}
