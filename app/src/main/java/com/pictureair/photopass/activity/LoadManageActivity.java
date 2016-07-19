package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.DownLoadFramentAdapter;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.photopass.fragment.DownLoadingFragment;
import com.pictureair.photopass.fragment.LoadSuccessFragment;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.viewpagerindicator.TabPageIndicator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class LoadManageActivity extends BaseFragmentActivity implements ViewPager.OnPageChangeListener,View.OnClickListener{

    private TabPageIndicator indicator;
    private ImageView back;
    private ViewPager viewPager;
    private List<Fragment> fragments = new ArrayList<>();
    private String[] titles;
    private int currentIndex = 0;
    DownLoadFramentAdapter adapter;
    DownloadService downloadService;
    DownLoadingFragment downLoadingFragment;
    LoadSuccessFragment loadSuccessFragment;
    public final Handler manageHandler = new LoadManageHandler(this);
    public static final int UPDATE_LOAD_SUCCESS_FRAGMENT = 6666;

    public static class LoadManageHandler extends Handler {
        private final WeakReference<LoadManageActivity> mActivity;

        public LoadManageHandler(LoadManageActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    private void dealHandler(Message msg) {
        switch (msg.what) {
            case UPDATE_LOAD_SUCCESS_FRAGMENT:
                loadSuccessFragment.getDataBackground();
                break;

            default:

                break;


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_manage);

        titles = new String[]{getResources().getString(R.string.photo_downloading),getResources().getString(R.string.photo_download_success)};
        back = (ImageView) findViewById(R.id.load_manage_back);
        indicator = (TabPageIndicator) findViewById(R.id.load_manage_indicator);
        indicator.setmSelectedTabIndex(0);
        viewPager = (ViewPager) findViewById(R.id.load_manage_viewPager);
        indicator.setOnPageChangeListener(this);
        fragments.clear();
        downLoadingFragment = new DownLoadingFragment();
        loadSuccessFragment = new LoadSuccessFragment();
        fragments.add(downLoadingFragment);
        fragments.add(loadSuccessFragment);
        adapter = new DownLoadFramentAdapter(getSupportFragmentManager(),fragments,titles);
        viewPager.setAdapter(adapter);
        indicator.setViewPager(viewPager);
        indicator.setCurrentItem(currentIndex);
        back.setOnClickListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 1) {
            loadSuccessFragment.updateList();
        }
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
        super.onDestroy();
    }

    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        PictureAirLog.v("LoadManageActivity onUserEvent","onUserEvent");
        if (baseBusEvent instanceof TabIndicatorUpdateEvent) {
            TabIndicatorUpdateEvent updateEvent = (TabIndicatorUpdateEvent)baseBusEvent;
            int count = updateEvent.getDataBasePhotoCount();
            if (updateEvent.getWhichSide() == 0) {
                titles[0] = getResources().getString(R.string.photo_downloading)+" ("+count+") ";
            }else if (updateEvent.getWhichSide() == 1){
                titles[1] = getResources().getString(R.string.photo_download_success)+" ("+count+") ";
            }
            if (adapter != null) {
                adapter.setTitle(titles);
            }
            indicator.updateTabText(updateEvent.getWhichSide());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.load_manage_back:
                doBack();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //退出app进行的判断，判断是否是栈中的唯一一个app，如果是，启动主页
    private void doBack() {
        // TODO Auto-generated method stub
        if (AppManager.getInstance().getActivityCount() == 1) {//一个activity的时候
            Intent intent = new Intent(this, MainTabActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
