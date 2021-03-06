package com.pictureworks.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * 用来解决点击listView的item，按钮的selector也会执行的bug
 * @author bauer_bao
 *
 */
public class ListViewImageView extends ImageView{

	public ListViewImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ListViewImageView(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
	}

	@Override
	public void setPressed(boolean pressed) {
		// TODO Auto-generated method stub
		if (pressed && getParent() instanceof View
				&& ((View) getParent()).isPressed()) {
			return;
		}
		super.setPressed(pressed);
	}

}
