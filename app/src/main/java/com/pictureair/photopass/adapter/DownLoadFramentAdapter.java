package com.pictureair.photopass.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by pengwu on 16/7/8.
 */
public class DownLoadFramentAdapter extends FragmentPagerAdapter {

    private List<Fragment> listFragment;
    private String[] title;
    public DownLoadFramentAdapter(FragmentManager manager, List<Fragment> listFragment,String[] title){
        super(manager);
        this.listFragment = listFragment;
        this.title = title;
    }
    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public Fragment getItem(int position) {
        return listFragment.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }

    public void setTitle(String[] title){
        this.title = title;
    }
}
