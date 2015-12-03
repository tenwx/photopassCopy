package com.pictureair.photopass.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.CalendarActivity;
import com.pictureair.photopass.util.SpecialCalendarUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日历gridview中的每一个item显示的textview
 * 
 * @author bass
 * 
 */
public class CalendarAdapter extends BaseAdapter {

	private boolean isLeapyear = false; // 是否为闰年
	private int daysOfMonth = 0; // 某月的天数
	private int dayOfWeek = 0; // 具体某一天是星期几
	private int lastDaysOfMonth = 0; // 上一个月的总天数
	private Context context;
	private String[] dayNumber = new String[42]; // 一个gridview中的日期存入此数组中
	private SpecialCalendarUtil sc = null;

	private String currentYear = "";
	private String currentMonth = "";

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");

	private String showYear = ""; // 用于在头部显示的年份
	private String showMonth = ""; // 用于在头部显示的月份
//	private String animalsYear = "";
	private String leapMonth = ""; // 闰哪一个月
//	private String cyclical = ""; // 天干地支
	// 系统当前时间
	private String sysDate = "";
	private String sys_year = "";
	private String sys_month = "";
	private String sys_day = "";
	
	private int clickTemp = -1;

	public CalendarAdapter() {
		Date date = new Date();
		sysDate = sdf.format(date); // 当期日期
		sys_year = sysDate.split("-")[0];
		sys_month = sysDate.split("-")[1];
		sys_day = sysDate.split("-")[2];

	}

	public CalendarAdapter(Context context, int jumpMonth, int jumpYear, int year_c, int month_c, int day_c) {
		this();
		this.context = context;
		sc = new SpecialCalendarUtil();
		int stepYear = year_c + jumpYear;
		int stepMonth = month_c + jumpMonth;
		if (stepMonth > 0) {
			// 往下一个月滑动
			if (stepMonth % 12 == 0) {
				stepYear = year_c + stepMonth / 12 - 1;
				stepMonth = 12;
			} else {
				stepYear = year_c + stepMonth / 12;
				stepMonth = stepMonth % 12;
			}
		} else {
			// 往上一个月滑动
			stepYear = year_c - 1 + stepMonth / 12;
			stepMonth = stepMonth % 12 + 12;
			if (stepMonth % 12 == 0) {

			}
		}

		currentYear = String.valueOf(stepYear); // 得到当前的年份
		currentMonth = String.valueOf(stepMonth); // 得到本月
													// （jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
		getCalendar(Integer.parseInt(currentYear), Integer.parseInt(currentMonth));
//		System.out.println("click item is----> "+ clickTemp);
	}

	@Override
	public int getCount() {
		return dayNumber.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
	/** 回收机制！这里需要清理垃圾
	 *  需要农历的时候在此处更改 */
	@SuppressWarnings("static-access")
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.calendar_item, null);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.tvtext);

		String d = dayNumber[position].trim();
		textView.setText(d);
		textView.setTextColor(context.getResources().getColor(R.color.gray2));

		if (position < daysOfMonth + dayOfWeek && position >= dayOfWeek) {
			// 当前月信息显示
			textView.setTextColor(context.getResources().getColor(R.color.gray));// 当月字体
		}

		// 点击改变选中listItem的背景色
		if (clickTemp == position) {
			
			textView.setBackgroundColor(0);
			textView.setTextColor(Color.WHITE);
		}

		if (d.equals(new CalendarActivity().dayC) && new CalendarActivity().positionC == position &&
				getShowYear().equals(CalendarActivity.yearC) && getShowMonth().equals(CalendarActivity.monthC)) {
 
			
			System.out.println("选中的日期进行保存");
			
			textView.setBackgroundColor(0);
			textView.setTextColor(Color.WHITE);
		}

		else{
			textView.setBackgroundColor(Color.WHITE);
		}
		
		// 设置当天的背景
		if (new CalendarActivity().currentFlag == position) {
			//如果clickTemp ！＝ －1.说明已经被点击过，被点击过，就要改变当天的值
			if (!(clickTemp == -1)) {
				new CalendarActivity().currentFlag = -111;
			}
			else if(d.equals(dc) ){
				// 设置当天的背景
				System.out.println("当天背景");
				textView.setBackgroundColor(0);
				textView.setTextColor(Color.WHITE); // 当天的字体设白色
			}
		}

		return convertView;
	}
	
	/**
	 * 记录当天的日
	 */
	private String dc="";
	
    //标识选择的Item
	public void setSeclection(int position) {
		clickTemp = position;
//		System.out.println("click item is----> "+ clickTemp);
	}

	// 得到某年的某月的天数且这月的第一天是星期几
	public void getCalendar(int year, int month) {
		isLeapyear = sc.isLeapYear(year); // 是否为闰年
		daysOfMonth = sc.getDaysOfMonth(isLeapyear, month); // 某月的总天数
		dayOfWeek = sc.getWeekdayOfMonth(year, month); // 某月第一天为星期几
		lastDaysOfMonth = sc.getDaysOfMonth(isLeapyear, month - 1); // 上一个月的总天数
		Log.d("DAY", isLeapyear + " ======  " + daysOfMonth + "  ============  " + dayOfWeek + "  =========   " + lastDaysOfMonth);
		getweek(year, month);
	}

	// 将一个月中的每一天的值添加入数组dayNuber中
	private void getweek(int year, int month) {
		int j = 1;
		// 得到当前月的所有日程日期(这些日期需要标记)

		for (int i = 0; i < dayNumber.length; i++) {
			if (i < dayOfWeek) { // 前一个月
				int temp = lastDaysOfMonth - dayOfWeek + 1;
				dayNumber[i] = (temp + i) + "";
				System.out.println("dayNumber is "+ dayNumber[i]);

			} else if (i < daysOfMonth + dayOfWeek) { // 本月
				String day = String.valueOf(i - dayOfWeek + 1); // 得到的日期
				dayNumber[i] = i - dayOfWeek + 1 + "";
				// 对于当前月才去标记当前日期
				if (sys_year.equals(String.valueOf(year))
						&& sys_month.equals(String.valueOf(month))
						&& sys_day.equals(day)) {
					// 标记当前日期

					dc = day;

					// 如果为 －11 ，说明
					// 第一次进来。。当点击了其他日期。currentFlag就会被改变，不会再给currentFlag赋当天值
					if (new CalendarActivity().currentFlag == -11) {
						new CalendarActivity().currentFlag = i;
						System.out.println("第一次进来－－－－－－－显示当天");
					}
				}
				setShowYear(String.valueOf(year));
				setShowMonth(String.valueOf(month));
			} else { // 下一个月
				dayNumber[i] = j + "";
				j++;
			}
		}
//		String date = "";
//		for (int i = 0; i < dayNumber.length; i++) {
//			date += dayNumber[i] + "_";
//		}
//		System.out.println("date is "+ date);

	}

	public void matchScheduleDate(int year, int month, int day) {

	}

	/**
	 * 点击每一个item时返回item中的日期
	 * 
	 * @param position
	 * @return
	 */
	public String getDateByClickItem(int position) {
		return dayNumber[position];
	}

	/**
	 * 在点击gridView时，得到这个月中第一天的位置
	 * 
	 * @return
	 */
	public int getStartPositon() {
		return dayOfWeek + 7;
	}

	/**
	 * 在点击gridView时，得到这个月中最后一天的位置
	 * 
	 * @return
	 */
	public int getEndPosition() {
		return (dayOfWeek + daysOfMonth + 7) - 1;
	}

	public String getShowYear() {
		return showYear;
	}

	public void setShowYear(String showYear) {
		this.showYear = showYear;
	}

	public String getShowMonth() {
		return showMonth;
	}

	public void setShowMonth(String showMonth) {
		this.showMonth = showMonth;
	}

//	public String getAnimalsYear() {
//		return animalsYear;
//	}
//
//	public void setAnimalsYear(String animalsYear) {
//		this.animalsYear = animalsYear;
//	}

	public String getLeapMonth() {
		return leapMonth;
	}

	public void setLeapMonth(String leapMonth) {
		this.leapMonth = leapMonth;
	}
//
//	public String getCyclical() {
//		return cyclical;
//	}
//
//	public void setCyclical(String cyclical) {
//		this.cyclical = cyclical;
//	}
}
