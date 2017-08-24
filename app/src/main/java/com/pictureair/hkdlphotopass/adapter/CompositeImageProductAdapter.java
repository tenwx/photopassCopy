package com.pictureair.hkdlphotopass.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.pictureair.hkdlphotopass.widget.CompositeImageProductView;

import java.util.List;

/**
 * 商品合成预览adapter
 */
public class CompositeImageProductAdapter extends PagerAdapter {
	private List<CompositeImageProductView> list;
	
	public CompositeImageProductAdapter(Context context, List<CompositeImageProductView> list) {
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

	public View instantiateItem(ViewGroup arg0, int arg1) {
		arg0.addView(list.get(arg1));
		return list.get(arg1);
	}

	@Override
	public void destroyItem(ViewGroup arg0, int arg1, Object arg2) {
		arg0.removeView((View) arg2);
	}
}
