package com.pictureair.hkdlphotopass.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.R;


public class NoNetWorkOrNoCountView extends RelativeLayout implements OnClickListener{

	private Context context;
	private LayoutInflater layoutInflater;
	private TextView resultTextView;//显示结果的textView
	private TextView resultTipTextView;//显示结果下方的小提示的textView
	private Button resultButton;//重新加载或者逛逛的按钮
	private ImageView statusImageView;//显示当前状态的imageView
	private Handler handler;
	private boolean reload;//重新加载
	public static final int BUTTON_CLICK_WITH_RELOAD = 11;
	public static final int BUTTON_CLICK_WITH_NO_RELOAD = 12;
	
	public NoNetWorkOrNoCountView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
		initView();
	}
	
	public NoNetWorkOrNoCountView(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
		this.context = context;
		initView();
	}

	
	private void initView() {
		// TODO Auto-generated method stub
		layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.no_network_or_count_view, this);
		resultTextView = (TextView)findViewById(R.id.resultTextView);
		resultTipTextView = (TextView)findViewById(R.id.resultTipTextView);
		resultButton = (Button)findViewById(R.id.reloadOrOtherButton);
		statusImageView = (ImageView)findViewById(R.id.statusImageView);
		resultButton.setOnClickListener(this);
	}
	
	/**
	 * 设置空或者网络连接失败的view
	 * @param resultString 网络请求结果
	 * @param resultTipsString 对应结果的提示
	 * @param buttonString 按钮上的文字
	 * @param imageResource 图片drawable资源
	 * @param handler 消息传递
	 * @param reload 重新加载
	 */
	public void setResult(int resultString, int resultTipsString, int buttonString, int imageResource, Handler handler, boolean reload) {
		// TODO Auto-generated method stub
		if (this.handler == null) {
			this.handler = handler;
		}
		this.reload = reload;
		if (resultString == 0) {
			resultTextView.setVisibility(View.INVISIBLE);
		}else {
			resultTextView.setVisibility(View.VISIBLE);
			resultTextView.setText(resultString);
		}

		if (resultTipsString == 0) {
			resultTipTextView.setVisibility(View.INVISIBLE);
		} else {
			resultTipTextView.setVisibility(View.VISIBLE);
			resultTipTextView.setText(resultTipsString);
		}

		if (buttonString == 0) {
			resultButton.setVisibility(View.INVISIBLE);
		}else {
			resultButton.setVisibility(View.VISIBLE);
			resultButton.setText(buttonString);
		}
		
		statusImageView.setImageResource(imageResource);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.reloadOrOtherButton) {
			if (reload) {
				handler.sendEmptyMessage(BUTTON_CLICK_WITH_RELOAD);
			}else {
				handler.sendEmptyMessage(BUTTON_CLICK_WITH_NO_RELOAD);
			}
		}
	}
}
