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
	
	public interface OnClickListener {
		void onNegativeButtonClicked();
		void onPositiveButtonClicked();
	}
	
	private OnClickListener onClickListener;
	
	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}
	
//	Paint p = new Paint(); 
//	{
//		p.setStrokeWidth(1);
//		p.setColor(Color.GRAY);
//	}
	
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
	}
	
	//设置按钮的内容，如果内容为null，则不显示按钮
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
				btnPositive.setBackgroundResource(R.drawable.dia_ok1);
			}else {
				btnPositive.setBackgroundResource(R.drawable.dia_ok);
			}
			btnPositive.setVisibility(View.VISIBLE);
			btnPositive.setText(positive);
		}
		invalidate();
	}
	
//	public void setPositivtText(String positive, int image){
//		if (positive == null) {
//			btnPositive.setVisibility(View.GONE);
//			invalidate();
//		}else {
//			btnPositive.setVisibility(View.VISIBLE);
//			invalidate();
//			btnPositive.setText(positive);
//		}
//	}
	
	
	
//	@Override
//	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
//		if (btnNegative.isShown() && btnPositive.isShown()) {
//			canvas.drawLine(0, 0, getWidth(), 0, p);
//			canvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), p);
//		}
//	}

}
