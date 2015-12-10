package com.pictureair.photopass.widget.wheelview;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.wheelview.adapter.NumericWheelAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期选择器
 * @author bass
 * 
 */
public class SelectDateWeidget extends PopupWindow implements OnClickListener{
	private View conentView, parent;
	private Context context;
	private WheelView year;
	private WheelView month;
	private WheelView day;
	/** 初始化读取日期 */
	private int mYear = 1996;// 初始化
	private int mMonth = 0;
	private int mDay = 1;
	private String mYear_Str = "1996";// 初始化
	private String mMonth_Str = "01";
	private String mDay_Str = "01";
	private Button btnCancel, btnSubmit;
	private Handler handler;
	
	/**
	 * 时间选择器中最小的时间
	 */
	private final static int START_YEAR = 1900;
	
	/**
	 * 可见的数目
	 */
	private final static int VISIBILE_ITEM = 4;
	
	public final static int SUBMIT_SELECT_DATE = 11;
	
	/**
	 * 构造器
	 * @param context 
	 * @param parent popwindow显示的父控件
	 * @param handler 消息传递
	 */
	public SelectDateWeidget(Context context, View parent, Handler handler) {
		this.context = context;
		this.parent = parent;
		this.handler = handler;
		getDateYMD();
		Calendar c = Calendar.getInstance();
		int norYear = c.get(Calendar.YEAR);
		conentView = (View) ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.wheel_date_picker, null);

		setContentView(conentView);
		setWidth(ScreenUtil.getScreenWidth(context));
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setOutsideTouchable(true);
		update();
		ColorDrawable dw = new ColorDrawable(0000000000);
		setBackgroundDrawable(dw);
		setAnimationStyle(R.style.AnimationPreview);

		// 得到按钮
		btnCancel = (Button) conentView.findViewById(R.id.btn_c_date);
		btnSubmit = (Button) conentView.findViewById(R.id.btn_s_date);
		btnCancel.setOnClickListener(this);
		btnSubmit.setOnClickListener(this);

		year = (WheelView) conentView.findViewById(R.id.year);
		NumericWheelAdapter numericWheelAdapter1 = new NumericWheelAdapter(context, START_YEAR, norYear); // 最小
		numericWheelAdapter1.setLabel("");// 这里改变
		year.setViewAdapter(numericWheelAdapter1);
		year.setCyclic(true);// 是否可循环滑动
		year.addScrollingListener(scrollListener);

		month = (WheelView) conentView.findViewById(R.id.month);
		NumericWheelAdapter numericWheelAdapter2 = new NumericWheelAdapter(context, 1, 12, "%02d");
		numericWheelAdapter2.setLabel("");
		month.setViewAdapter(numericWheelAdapter2);
		month.setCyclic(true);
		month.addScrollingListener(scrollListener);

		day = (WheelView) conentView.findViewById(R.id.day);
		initDay(mYear, mMonth);
		day.setCyclic(true);
		day.addScrollingListener(scrollListener);

		year.setVisibleItems(VISIBILE_ITEM);// 设置显示行数
		month.setVisibleItems(VISIBILE_ITEM);
		day.setVisibleItems(VISIBILE_ITEM);

		year.setCurrentItem(mYear - START_YEAR);
		month.setCurrentItem(mMonth - 1);
		day.setCurrentItem(mDay - 1);
		
	}
	
	@Override
	public void onClick(View v) {
		Message message = handler.obtainMessage();
		switch (v.getId()) {
		case R.id.btn_c_date:
			break;
			
		case R.id.btn_s_date:
			message.what = SUBMIT_SELECT_DATE;
			Bundle bundle = new Bundle();
			bundle.putString("year", mYear_Str);
			bundle.putString("month", mMonth_Str);
			bundle.putString("day", mDay_Str);
			message.obj = bundle;
			handler.sendMessage(message);
			break;

		default:
			break;
		}
		dismiss();
	}

	/**
	 * 显示popupWindow
	 */
	public void showPopupWindow() {
		if (!isShowing()) {
			// 以下拉方式显示popupwindow
			showAtLocation(parent, Gravity.BOTTOM, 0, 0);
		}
	}

	private OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
		@Override
		public void onScrollingStarted(WheelView wheel) {
		}

		@Override
		public void onScrollingFinished(WheelView wheel) {
			int n_year = year.getCurrentItem() + START_YEAR;// 年
			int n_month = month.getCurrentItem() + 1;// 月

			initDay(n_year, n_month);

			mYear_Str = String.valueOf(year.getCurrentItem() + START_YEAR);
			mMonth_Str = String.valueOf((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1));
			mDay_Str = String.valueOf(((day.getCurrentItem() + 1) < 10) ? "0" + (day.getCurrentItem() + 1) : (day.getCurrentItem() + 1));
		}
	};

	/**
	 * 初始化日这个控件
	 * @param arg1 年
	 * @param arg2 月
	 */
	private void initDay(int arg1, int arg2) {
		NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(context, 1, AppUtil.getDay(arg1, arg2), "%02d");
		numericWheelAdapter.setLabel("");
		day.setViewAdapter(numericWheelAdapter);
	}

	/**
	 * 获取年月日
	 */
	private void getDateYMD() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = sdf.format(date); // 当期日期
		mYear = Integer.parseInt(currentDate.split("-")[0]);
		mMonth = Integer.parseInt(currentDate.split("-")[1]);
		mDay = Integer.parseInt(currentDate.split("-")[2]);

		mYear_Str = String.valueOf(mYear);
		mMonth_Str = String.valueOf(mMonth < 10 ? "0" + mMonth : mMonth);
		mDay_Str = String.valueOf(mDay < 10 ? "0" + mDay : mDay);
	}
}
