package com.pictureAir.adapter;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.pictureAir.widget.CompositePPCodeView;

public class PPCodeAdapter extends PagerAdapter {
	private List<CompositePPCodeView> list;
	
	public PPCodeAdapter(Context context, List<CompositePPCodeView> list) {
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

	public View instantiateItem(View arg0, final int arg1) {
		((ViewPager) arg0).addView(list.get(arg1));
		return list.get(arg1);
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView((View) arg2);
	}
}
