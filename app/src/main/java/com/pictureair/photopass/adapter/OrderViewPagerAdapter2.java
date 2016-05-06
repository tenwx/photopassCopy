package com.pictureair.photopass.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.pictureair.photopass.fragment.OrderFragment;

import java.util.List;

/**
 * 订单页面ViewPager的适配器
 *
 * @author bauer_bao
 */
public class OrderViewPagerAdapter2 extends FragmentStatePagerAdapter {
    private static final String TAG = "OrderViewPagerAdapter";
    private List<OrderFragment> mFragments;


    public OrderViewPagerAdapter2(FragmentManager fm,List<OrderFragment> mFragments) {
        super(fm);
        this.mFragments = mFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

//    @Override
//    public int getItemPosition(Object object) {
//        return PagerAdapter.POSITION_NONE;
//    }

}
