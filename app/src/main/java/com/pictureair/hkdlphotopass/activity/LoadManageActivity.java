package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.DownLoadFramentAdapter;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.photopass.fragment.DownLoadingFragment;
import com.pictureair.photopass.fragment.LoadSuccessFragment;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * 下载管理页面
 */
public class LoadManageActivity extends BaseFragmentActivity implements ViewPager.OnPageChangeListener,View.OnClickListener{

    private TabPageIndicator indicator;
    private ImageView back;
    private ViewPager viewPager;
    private List<Fragment> fragments = new ArrayList<>();
    private String[] titles;
    private int currentIndex = 0;
    private DownLoadFramentAdapter adapter;
    private DownLoadingFragment downLoadingFragment;
    private LoadSuccessFragment loadSuccessFragment;
    private TextView tv_select;
    private ImageView img_cancle;
    private boolean loadingFragmentChanged;
    private boolean loadSuccessFragmentChanged;
    private PWToast myToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_manage);

        titles = new String[]{getResources().getString(R.string.photo_downloading),getResources().getString(R.string.photo_download_success)};
        tv_select = (TextView) findViewById(R.id.load_manage_select);
        img_cancle = (ImageView) findViewById(R.id.load_manage_cancle);
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
        img_cancle.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(this);
        img_cancle.setOnClickListener(this);
        tv_select.setOnClickListener(this);
        loadingFragmentChanged = false;
        loadSuccessFragmentChanged = false;
        myToast = new PWToast(MyApplication.getInstance());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentIndex = position;
        if (currentIndex == 0){//viewpager滑动后退出编辑状态
            if (loadSuccessFragmentChanged){
                closeLoadSuccessFragmentSelect();
            }
        }else{
            if (loadingFragmentChanged){
                closeLoadingFragmentSelect();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

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
            if(!updateEvent.isDatabaseUpdate()) {
                int count = updateEvent.getDataBasePhotoCount();
                if (updateEvent.getWhichSide() == 0) {
                    titles[0] = getResources().getString(R.string.photo_downloading) + " (" + count + ") ";
                } else if (updateEvent.getWhichSide() == 1) {
                    titles[1] = getResources().getString(R.string.photo_download_success) + " (" + count + ") ";
                }
                if (adapter != null) {
                    adapter.setTitle(titles);
                }
                indicator.updateTabText(updateEvent.getWhichSide());
            }else{
                if (loadSuccessFragment != null)
                    loadSuccessFragment.getDataBackground();
            }
        }
    }

    @Override
    public void onClick(View v) {
        onClick(v.getId());
    }

    public void onClick(int id){
        switch (id){
            case R.id.load_manage_back:
                doBack();
                break;
            case R.id.load_manage_cancle:
                if (currentIndex == 0 && loadingFragmentChanged){
                    closeLoadingFragmentSelect();
                }else if (currentIndex == 1 && loadSuccessFragmentChanged){
                    closeLoadSuccessFragmentSelect();
                }

                break;
            case R.id.load_manage_select:
                if (downLoadingFragment.getIsDownloading()){
                    myToast.setTextAndShow(R.string.photo_download_tips3, Common.TOAST_SHORT_TIME);
                    return;
                }
                if (currentIndex == 0 && !loadingFragmentChanged){
                    openLoadingFragmentSelect();
                }else if (currentIndex == 1 && !loadSuccessFragmentChanged){
                    openLoadSuccessFragmentSelect();
                }
                break;
            default:
                break;
        }
    }

    private void openLoadingFragmentSelect(){
        loadingFragmentChanged = downLoadingFragment.changeToSelectState();
        if (loadingFragmentChanged){
            tv_select.setVisibility(View.INVISIBLE);
            img_cancle.setVisibility(View.VISIBLE);
            back.setVisibility(View.GONE);
        }else{
            myToast.setTextAndShow(R.string.photo_download_tips4);
        }
    }

    private void closeLoadingFragmentSelect(){
        downLoadingFragment.changeToNormalState();
        tv_select.setVisibility(View.VISIBLE);
        img_cancle.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        loadingFragmentChanged = false;
    }

    private void openLoadSuccessFragmentSelect(){
        loadSuccessFragmentChanged = loadSuccessFragment.changeToSelectState();
        if (loadSuccessFragmentChanged){
            tv_select.setVisibility(View.INVISIBLE);
            img_cancle.setVisibility(View.VISIBLE);
            back.setVisibility(View.GONE);
        }else{
            myToast.setTextAndShow(R.string.photo_download_tips4);
        }
    }

    private void closeLoadSuccessFragmentSelect(){
        loadSuccessFragment.changeToNormalState();
        tv_select.setVisibility(View.VISIBLE);
        img_cancle.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        loadSuccessFragmentChanged = false;
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

        if (downLoadingFragment != null) {
            downLoadingFragment.stopDownloadService();
        }
        finish();
    }

    public void successFragmentLoading(){
        if (loadSuccessFragment != null){
            loadSuccessFragment.initView();
        }
    }
}
