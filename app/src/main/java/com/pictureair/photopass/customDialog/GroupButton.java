package com.pictureair.photopass.customDialog;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;

/**
 * 自定义对话框的确认按钮和取消按钮
 * @author bauer_bao
 *
 */
public class GroupButton extends FrameLayout {
	
	private TextView btnNegative;
	private TextView btnPositive;
	private View view;
	
	public interface OnClickListener {
		void onNegativeButtonClicked();
		void onPositiveButtonClicked();
	}
	
	private OnClickListener onClickListener;
	
	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}
	
	public GroupButton(Context context) {
		super(context);
	}

	public GroupButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
		View.inflate(getContext(), R.layout.group_button, this);
		btnNegative = (TextView) findViewById(R.id.btn_negative);
		btnNegative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (onClickListener != null) {
					onClickListener.onNegativeButtonClicked();
				}
			}
		});
		
		btnPositive = (TextView) findViewById(R.id.btn_positive);
		btnPositive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (onClickListener != null) {
					onClickListener.onPositiveButtonClicked();
				}
			}
		});
		
		view = (View) findViewById(R.id.middle_line);
	}
	
	/**
	 * 设置按钮的内容，如果内容为null，则不显示按钮
	 * @param positive
	 * @param negative
	 */
	public void setButtonText(String positive, String negative){
		//设置取消按钮
		if (negative == null) {
			btnNegative.setVisibility(View.GONE);
		}else {
			btnNegative.setVisibility(View.VISIBLE);
			btnNegative.setText(negative);
		}
		//设置确定按钮
		if (positive == null) {
			btnPositive.setVisibility(View.GONE);
		}else {
			if (negative == null) {
				view.setVisibility(View.GONE);
			}else {
				view.setVisibility(View.VISIBLE);
			}
			btnPositive.setVisibility(View.VISIBLE);
			btnPositive.setText(positive);
		}
		invalidate();
	}
	

}
