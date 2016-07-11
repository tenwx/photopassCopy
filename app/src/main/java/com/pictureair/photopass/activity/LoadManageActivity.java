package com.pictureair.photopass.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.DownLoadFramentAdapter;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.photopass.fragment.DownLoadingFragment;
import com.pictureair.photopass.fragment.LoadSuccessFragment;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class LoadManageActivity extends BaseFragmentActivity implements ViewPager.OnPageChangeListener{

    private TabPageIndicator indicator;
    private ImageView back;
    private ViewPager viewPager;
    private List<Fragment> fragments = new ArrayList<>();
    private String[] titles;
    private int currentIndex = 0;
    DownLoadFramentAdapter adapter;
    DownloadService downloadService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_manage);

        titles = new String[]{getResources().getString(R.string.photo_download_success)
                ,getResources().getString(R.string.photo_downloading)};
        back = (ImageView) findViewById(R.id.load_manage_back);
        indicator = (TabPageIndicator) findViewById(R.id.load_manage_indicator);
        indicator.setmSelectedTabIndex(0);
        viewPager = (ViewPager) findViewById(R.id.load_manage_viewPager);
        indicator.setOnPageChangeListener(this);
        fragments.clear();
        fragments.add(new LoadSuccessFragment());
        fragments.add(new DownLoadingFragment());
        adapter = new DownLoadFramentAdapter(getSupportFragmentManager(),fragments,titles);
        viewPager.setAdapter(adapter);
        indicator.setViewPager(viewPager);
        indicator.setCurrentItem(currentIndex);
        bindService();
    }

    private void bindService(){
        Intent intent = new Intent(LoadManageActivity.this,DownloadService.class);
        bindService(intent,conn, Context.BIND_AUTO_CREATE);
    }

    private void unBind(){
        unbindService(conn);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.PhotoBind photoBind = (DownloadService.PhotoBind)service;
            downloadService = photoBind.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onResume() {
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        unBind();
        super.onDestroy();
    }

    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        PictureAirLog.v("LoadManageActivity onUserEvent","onUserEvent");
        if (baseBusEvent instanceof TabIndicatorUpdateEvent) {
            TabIndicatorUpdateEvent updateEvent = (TabIndicatorUpdateEvent)baseBusEvent;
            if (updateEvent.getWhichSide() == 0) {
                int count = updateEvent.getDataBasePhotoCount();
                titles[0] = getResources().getString(R.string.photo_download_success)+" ("+count+") ";
            }else if (updateEvent.getWhichSide() == 1){
                int count = updateEvent.getDataBasePhotoCount();
                titles[1] = getResources().getString(R.string.photo_downloading)+" ("+count+") ";
            }
            if (adapter != null) {
                adapter.setTitle(titles);
            }
            indicator.updateTabText(updateEvent.getWhichSide());
        }
    }

}
