package cn.smssdk.gui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.smssdk.R;

public class MyToast extends Toast{

	private Toast toast;
	private TextView textView;
	private Typeface typeface;
	public MyToast(Context context) {
		super(context);
		toast = new Toast(context);
		// TODO Auto-generated constructor stub
		//获取LayoutInflater对象  
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//由layout文件创建一个View对象  
		View layout = inflater.inflate(R.layout.newtoast, null);  
		//实例化ImageView和TextView对象  
		textView = (TextView) layout.findViewById(R.id.toast_textview);
		if (null == typeface) {
			typeface = Typeface.createFromAsset(context.getAssets(), CustomFontManager.CUSOTM_FONT_NAME);
		}
		textView.setTypeface(typeface);
		toast.setView(layout);  
	}

	/**
	 * 设置内容，时间
	 * @param text
	 */
	public void setTextAndShow(String text, int time) { 
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);//设置toast位置
		toast.setDuration(time);
		textView.setText(text);
		toast.show();
	}
	/**
	 * 设置内容，时间
	 * @param text
	 */
	public void setTextAndShow(int text, int time) { 
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);//设置toast位置
		toast.setDuration(time);
		textView.setText(text);
		toast.show();
	}
	
	
}
