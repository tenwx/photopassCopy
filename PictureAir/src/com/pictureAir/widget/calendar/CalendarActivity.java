package com.pictureAir.widget.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.pictureAir.SelectPPActivity;
import com.pictureAir.R;
import com.pictureAir.R.color;
import com.pictureAir.entity.DayOfPP;
import com.pictureAir.entity.DayOfPPP;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

/**
 * 日历显示activity
 * 
 * @author bass
 * 
 */
public class CalendarActivity extends Activity implements View.OnClickListener {

	private GestureDetector gestureDetector = null;
	private CalendarAdapter calV = null;
	private ViewFlipper flipper = null;
	private GridView gridView = null;
	private static int jumpMonth = 0; // 每次滑动，增加或减去一个月,默认为0（即显示当前月）
	private static int jumpYear = 0; // 滑动跨越一年，则增加或者减去一年,默认为0(即当前年)
	private int year_c = 0;
	private int month_c = 0;
	private int day_c = 0;
	private String currentDate = "";
	/** 每次添加gridview到viewflipper中时给的标记 */
	private int gvFlag = 0;
	/** 当前的年月，现在日历顶端 */
	private TextView currentMonth;
	/** 上个月 */
	private ImageView prevMonth;
	/** 下个月 */
	private ImageView nextMonth;
	/** 上个年 */
	private ImageView prevyYear;
	/** 下个年 */
	private ImageView nextYear;
	/* 提交按钮 * */
	private Button btn_submit_r;
	private ImageView login_back;

	private String result = ""; // 得到点击的日期 ，如：1993-10-11
	private DayOfPPP ppp;
	
	private int width;
	
	/**
	 *  用于标记当天
	 */
	public static int currentFlag = -11; 
	
	SharedPreferences sharedPreferences;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case API.GET_PP_SUCCESS:
				Log.e("接受", "长度:"+API.PPlist.size());
				//跳转到选择PP的界面。
				Intent intent = new Intent(CalendarActivity.this,SelectPPActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("pp", API.PPlist);
				bundle.putParcelable("ppp", ppp);
				intent.putExtras(bundle);
				startActivity(intent);
				break;

			default:
				break;
			}
			
		}};

	@SuppressLint("SimpleDateFormat")
	public CalendarActivity() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
		currentDate = sdf.format(date); // 当期日期
		year_c = Integer.parseInt(currentDate.split("-")[0]);
		month_c = Integer.parseInt(currentDate.split("-")[1]);
		day_c = Integer.parseInt(currentDate.split("-")[2]);

//		result = year_c + "-" + month_c + "-" + day_c;
		String mm= month_c > 9? month_c+"":"0"+month_c;
		String day = day_c >9? day_c+"":"0"+day_c ;
		result = year_c + "-" + mm + "-" + day;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar);
		AppManager.getInstance().addActivity(this);// 添加到activity管理器
		
		ppp = getIntent().getParcelableExtra("ppp");
		Log.e("ppp", "ppp:"+ppp);
		width = ScreenUtil.getScreenWidth(this) - ScreenUtil.dip2px(this, 12);
		
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		login_back = (ImageView) findViewById(R.id.login_back);
		currentMonth = (TextView) findViewById(R.id.currentMonth);
		prevMonth = (ImageView) findViewById(R.id.prevMonth);// 后退一个月
		nextMonth = (ImageView) findViewById(R.id.nextMonth);// 添加一个月
		prevyYear = (ImageView) findViewById(R.id.prevYear);// 后退一年
		nextYear = (ImageView) findViewById(R.id.nextYear);// 前进一年

		btn_submit_r = (Button) findViewById(R.id.btn_submit_r);// 提交按钮

		login_back.setOnClickListener(this);
		nextYear.setOnClickListener(this);
		prevyYear.setOnClickListener(this);
		prevMonth.setOnClickListener(this);
		nextMonth.setOnClickListener(this);
		btn_submit_r.setOnClickListener(this);

		gestureDetector = new GestureDetector(this, new MyGestureListener());
		flipper = (ViewFlipper) findViewById(R.id.flipper);
		flipper.removeAllViews();
		calV = new CalendarAdapter(this, getResources(), jumpMonth, jumpYear,
				year_c, month_c, day_c);
		addGridView();
		gridView.setAdapter(calV);
		flipper.addView(gridView, 0);
		addTextToTopTextView(currentMonth);
	}

	private class MyGestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			int gvFlag = 0; // 每次添加gridview到viewflipper中时给的标记
			if (e1.getX() - e2.getX() > 120) {
				// 像左滑动
				enterNextMonth(gvFlag);
				return true;
			} else if (e1.getX() - e2.getX() < -120) {
				// 向右滑动
				enterPrevMonth(gvFlag);
				return true;
			}
			return false;
		}
	}

	/** 下一年年份 */
	private void moveYear(int gvFlag) {
		addGridView(); // 添加一个gridView
		jumpMonth = jumpMonth + 12; // 下年

		calV = new CalendarAdapter(this, this.getResources(), jumpMonth,
				jumpYear, year_c, month_c, day_c);
		gridView.setAdapter(calV);
		addTextToTopTextView(currentMonth); // 移动到下一月后，将当月显示在头标题中
		gvFlag++;
		flipper.addView(gridView, gvFlag);
		flipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_left_in));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_left_out));
		flipper.showNext();
		flipper.removeViewAt(0);
	}

	/** 上一年份 */
	private void nextYear(int gvFlag) {
		addGridView(); // 添加一个gridView
		jumpMonth = jumpMonth - 12; // 下年
		calV = new CalendarAdapter(this, this.getResources(), jumpMonth,
				jumpYear, year_c, month_c, day_c);
		gridView.setAdapter(calV);
		addTextToTopTextView(currentMonth); // 移动到下一月后，将当月显示在头标题中
		gvFlag++;
		flipper.addView(gridView, gvFlag);
		flipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_left_in));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_left_out));
		flipper.showNext();
		flipper.removeViewAt(0);
	}

	/**
	 * 移动到下一个月
	 * 
	 * @param gvFlag
	 */
	private void enterNextMonth(int gvFlag) {
		addGridView(); // 添加一个gridView
		jumpMonth++; // 下一个月

		calV = new CalendarAdapter(this, this.getResources(), jumpMonth,
				jumpYear, year_c, month_c, day_c);
		gridView.setAdapter(calV);
		addTextToTopTextView(currentMonth); // 移动到下一月后，将当月显示在头标题中
		gvFlag++;
		flipper.addView(gridView, gvFlag);
		flipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_left_in));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_left_out));
		flipper.showNext();
		flipper.removeViewAt(0);
	}

	/**
	 * 移动到上一个月
	 * 
	 * @param gvFlag
	 */
	private void enterPrevMonth(int gvFlag) {
		addGridView(); // 添加一个gridView
		jumpMonth--; // 上一个月

		calV = new CalendarAdapter(this, this.getResources(), jumpMonth,
				jumpYear, year_c, month_c, day_c);
		gridView.setAdapter(calV);
		gvFlag++;
		addTextToTopTextView(currentMonth); // 移动到上一月后，将当月显示在头标题中
		flipper.addView(gridView, gvFlag);

		flipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_right_in));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_right_out));
		flipper.showPrevious();
		flipper.removeViewAt(0);
	}

	/**
	 * 添加头部的年份 闰哪月等信息
	 * 
	 * @param view
	 */
	public void addTextToTopTextView(TextView view) {
		StringBuffer textDate = new StringBuffer();
//		if (getResources().getConfiguration().locale.getCountry().equals("CN")) {
			textDate.append(calV.getShowYear()).append(getString(R.string.calendar_year))
					.append(calV.getShowMonth()).append(getString(R.string.calendar_month)).append("\t");
//		} else {
//			textDate.append(calV.getShowMonth()).append("-")
//					.append(calV.getShowYear()).append("\t");
//		}

		view.setText(textDate);
	}

	@SuppressLint("NewApi")
	private void addGridView() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
		// 取得屏幕的宽度和高度
//		WindowManager windowManager = getWindowManager();
//		Display display = windowManager.getDefaultDisplay();
//		int Width = display.getWidth();
//		int Height = display.getHeight();
		
//		params.width = width;
//		params.height = width;
		
		gridView = new GridView(this);
		gridView.setNumColumns(7);
		gridView.setColumnWidth(width / 7);
		gridView.setMinimumHeight(width);
		System.out.println("gridView width ------->" + width / 7);
//		if (Width == 720 && Height == 1280) {
//			gridView.setColumnWidth(40);
//		}
		gridView.setScrollBarStyle(View.GONE);
		gridView.setHovered(true);
		gridView.setGravity(Gravity.CENTER_VERTICAL);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setBackgroundResource(R.drawable.calendar_bg);
		
		// 去除gridView边框
//		gridView.setVerticalSpacing(1);
//		gridView.setHorizontalSpacing(1);
		gridView.setOnTouchListener(new OnTouchListener() {
			// 将gridview中的触摸事件回传给gestureDetector

			public boolean onTouch(View v, MotionEvent event) {
				return CalendarActivity.this.gestureDetector
						.onTouchEvent(event);
			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@SuppressLint("ShowToast")
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long arg3) {
				// 点击任何一个item，得到这个item的日期(排除点击的是周日到周六(点击不响应))
				int startPosition = calV.getStartPositon();
				int endPosition = calV.getEndPosition();
				String scheduleDay;
				String scheduleYear;
				String scheduleMonth;
				if (startPosition <= position + 7
						&& position <= endPosition - 7) {
					scheduleDay = calV.getDateByClickItem(position)
							.split("\\.")[0]; // 这一天的阳历

					scheduleYear = calV.getShowYear();
					scheduleMonth = calV.getShowMonth();
					
					int mm = Integer.valueOf(scheduleMonth);
					int day = Integer.valueOf(scheduleDay);
					
					String sm = mm>9? mm+"":"0"+mm;
					String sd = day > 9?day+"":"0"+day;
					
					result = scheduleYear + "-" + sm + "-"
							+ sd;
					// ---------------------------------------------------------------------------------
					dayC = scheduleDay;
					positionC = position;

					calV.setSeclection(position);
					calV.notifyDataSetChanged();
				}

			}
		});
		gridView.setLayoutParams(params);
	}

	/**
	 * 保存变量，1.在calendarAdapter中，每次换一个月，变量都会刷为初始化，所以将变量保存到此类。
	 */
	public static String dayC = "0";
	public static int positionC = -1;

	public CalendarActivity(int i) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nextMonth: // 下一个月
			enterNextMonth(gvFlag);
			break;
		case R.id.prevMonth: // 上一个月
			enterPrevMonth(gvFlag);
			break;
		case R.id.prevYear://
			nextYear(gvFlag);
			break;
		case R.id.nextYear:
			moveYear(gvFlag);
			break;
		case R.id.btn_submit_r:
			Toast.makeText(CalendarActivity.this, result, 2000).show();
			//跳转到 选择PP的页面。
			API.getPPByDate(sharedPreferences.getString(Common.USERINFO_TOKENID, null), result, mHandler);
			break;
		case R.id.login_back:
			finish();
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		
		dayC = "0";
		positionC = -1;
		currentFlag = -11; 
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}

}