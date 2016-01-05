package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.StoryRefreshOnClickEvent;
import com.pictureair.photopass.fragment.FragmentPageCamera;
import com.pictureair.photopass.fragment.FragmentPageDiscover;
import com.pictureair.photopass.fragment.FragmentPageMe;
import com.pictureair.photopass.fragment.FragmentPageShop;
import com.pictureair.photopass.fragment.FragmentPageStory;
import com.pictureair.photopass.service.NotificationService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.BadgeView;
import com.pictureair.photopass.widget.CheckUpdateManager;
import com.pictureair.photopass.widget.MyToast;

import de.greenrobot.event.EventBus;


/**
 * 包含三个页面，photo显示、相机拍照、商城，默认进入第一个photo显示页面
 * 通过扫描或者登录之后会来到此页面
 */
public class MainTabActivity extends BaseFragmentActivity {
    public static MainTabActivity instances;
    private LinearLayout linearLayout;
    // 定义FragmentTabHost对象
    private FragmentTabHost mTabHost;
    // 定义一个布局
    private LayoutInflater layoutInflater;
    // 定义数组来存放Fragment界面
    private Class<?> fragmentArray[] = {FragmentPageStory.class, FragmentPageDiscover.class, FragmentPageCamera.class, FragmentPageShop.class, FragmentPageMe.class};
    // 定义数组来存放按钮图片
    private int mImageViewArray[] = {R.drawable.tab_photo_btn, R.drawable.tab_discover_btn, R.drawable.tab_camera_btn, R.drawable.tab_shop_btn, R.drawable.tab_me_btn};
    // Tab选项卡的文字
    private int mTextviewArray[] = {R.string.tab_story, R.string.tab_discover, R.string.tab_camera, R.string.tab_shops, R.string.tab_me};
    //记录退出的时候的两次点击的间隔时间
    private long exitTime = 0;

    public static BadgeView maintabbadgeView;
    //上次的tab页面，用来判断点击camera之后回到那个tab
    private int last_tab = 0;
    private MyToast newToast;

    public static boolean changeToShopTab = false;

    private MyApplication application;
    private SharedPreferences sharedPreferences;
    private CheckUpdateManager checkUpdateManager;
    private String currentLanguage;

    private static final String TAG = "MainTabActivity";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        application = (MyApplication) getApplication();
        instances = this;
        initView();
    }

    //清除acahe框架的缓存数据
    private void clearCache() {
        ACache.get(this).remove(Common.TOP_GOODS);
        ACache.get(this).remove(Common.ALL_GOODS);
        ACache.get(this).remove(Common.BANNER_GOODS);
        ACache.get(this).remove(Common.PPP_GOOD);
    }

    /**
     * 初始化组件
     */
    private void initView() {
        // 实例化布局对象
        layoutInflater = LayoutInflater.from(this);
        linearLayout = (LinearLayout) findViewById(R.id.parent_view);
        // 实例化TabHost对象，得到TabHost
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        newToast = new MyToast(this);
        // 自动检查更新
        sharedPreferences = getSharedPreferences(Common.APP, MODE_PRIVATE);
        currentLanguage = sharedPreferences.getString(Common.LANGUAGE_TYPE, "");
        checkUpdateManager = new CheckUpdateManager(this, currentLanguage,
                linearLayout);
        checkUpdateManager.startCheck();
        // 得到fragment的个数
        int count = fragmentArray.length;
        loadFragment(count);//加载tab

    }

    /**
     * 动态添加TabHot
     *
     * @param count
     */
    public void loadFragment(int count) {
        for (int i = 0; i < count; i++) {
            System.out.println("count --------->" + i);
            // 为每一个Tab按钮设置图标、文字和内容
            TabSpec tabSpec = mTabHost.newTabSpec(getString(mTextviewArray[i])).setIndicator(getTabItemView(i));
            // 将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            mTabHost.getTabWidget().getChildTabViewAt(i).setOnClickListener(new TabOnClick(i));
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        PictureAirLog.out("maintab ==== resume");
        Intent intent1 = new Intent(this, com.pictureair.photopass.service.NotificationService.class);
        this.startService(intent1);
        if (changeToShopTab) {
            PictureAirLog.out("skip to shop tab");
            mTabHost.setCurrentTab(3);
            changeToShopTab = false;
        } else {
            PictureAirLog.out("skip to last tab");
            //设置成为上次的tab页面
            mTabHost.setCurrentTab(last_tab);
        }
        if (currentLanguage != null && !currentLanguage.equals(MyApplication.getInstance().getLanguageType())) {
            mTabHost.clearAllTabs();
            mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
            loadFragment(fragmentArray.length);
            mTabHost.setCurrentTab(4);
            currentLanguage = MyApplication.getInstance().getLanguageType();
        }
        PictureAirLog.out("pushcount-->" + application.getPushPhotoCount());
        if (application.getPushPhotoCount() > 0) {//显示红点
            if (MainTabActivity.maintabbadgeView != null && !MainTabActivity.maintabbadgeView.isShown()) {
                MainTabActivity.maintabbadgeView.show();
            }
            application.setPushPhotoCount(0);
        }

        // 接收消息回复
        UmengUtil.syncFeedback(this);
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        clearCache();
    }

    //tab按钮的点击监听
    private class TabOnClick implements OnClickListener {
        private int currentTab;

        public TabOnClick(int currentTab) {
            this.currentTab = currentTab;
        }

        @Override
        public void onClick(View v) {
            switch (currentTab) {
                case 0:
                    System.out.println("photo tab on click");
                    if (mTabHost.getCurrentTab() == 0) {//获取最新数据
                        PictureAirLog.d(TAG, "need refresh");
                        EventBus.getDefault().postSticky(new StoryRefreshOnClickEvent(true));
                    } else {
                        PictureAirLog.d(TAG, "need not refresh");
                    }
                    mTabHost.setCurrentTab(0);
                    last_tab = 0;
                    break;

                case 2:
                    System.out.println("camera tab on click");
                    Common.TAB_HEIGHT = mTabHost.getHeight();
                    mTabHost.setCurrentTab(2);
                    break;

                case 1:
                case 3:
                case 4:
                    System.out.println(currentTab + " tab on click");
                    mTabHost.setCurrentTab(currentTab);
                    last_tab = currentTab;
                    break;

                default:
                    break;
            }
        }

    }

    /**
     * 给Tab按钮设置图标和文字
     */
    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.tab_item_view, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        imageView.setImageResource(mImageViewArray[index]);
        LayoutParams layoutParams = imageView.getLayoutParams();
        if (index != 2) {
            layoutParams.width = ScreenUtil.dip2px(this, 25);
        } else {
            layoutParams.width = ScreenUtil.dip2px(this, 40);

        }
        layoutParams.height = layoutParams.width;
        imageView.setLayoutParams(layoutParams);
        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(mTextviewArray[index]);
        if (index == 2) {
            textView.setVisibility(View.GONE);
        }
        if (index == 0) {//添加badgeview
            maintabbadgeView = new BadgeView(getApplicationContext(), imageView);
            maintabbadgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            maintabbadgeView.setTextSize(1);
            maintabbadgeView.setBackgroundResource(R.drawable.notificaitonpoint);
        }
        return view;
    }

    //双击退出app
    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 1000) {
            newToast.setTextAndShow(R.string.exit, Common.TOAST_SHORT_TIME);
            exitTime = System.currentTimeMillis();
        } else {
            newToast.cancel();
            mTabHost.removeAllViews();
            finish();
            clearCache();
            //取消通知
            Intent intent = new Intent(MainTabActivity.this, NotificationService.class);
            intent.putExtra("status", "disconnect");
            startService(intent);
            AppManager.getInstance().killAllActivity();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitApp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
