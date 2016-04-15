package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.MainTabSwitchEvent;
import com.pictureair.photopass.eventbus.RedPointControlEvent;
import com.pictureair.photopass.eventbus.StoryRefreshOnClickEvent;
import com.pictureair.photopass.fragment.FragmentPageCamera;
import com.pictureair.photopass.fragment.FragmentPageDiscover;
import com.pictureair.photopass.fragment.FragmentPageMe;
import com.pictureair.photopass.fragment.FragmentPageShop;
import com.pictureair.photopass.fragment.FragmentPageStory;
import com.pictureair.photopass.service.NotificationService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.CheckUpdateManager;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.dropview.CoverManager;
import com.pictureair.photopass.widget.dropview.DropCover.OnDragCompeteListener;
import com.pictureair.photopass.widget.dropview.WaterDrop;

import cn.smssdk.gui.AppManager;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;


/**
 * 包含三个页面，photo显示、相机拍照、商城，默认进入第一个photo显示页面
 * 通过扫描或者登录之后会来到此页面
 */
public class MainTabActivity extends BaseFragmentActivity implements OnDragCompeteListener, Handler.Callback {
    private RelativeLayout parentLayout;
    private WaterDrop waterDropView;
    private ImageView explored;
    private Handler handler;

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

    private MyToast newToast;

    //上次的tab页面，用来判断点击视频之后回到那个tab
    private int last_tab = 0;

    private MyApplication application;
    private SharedPreferences sharedPreferences;
    private CheckUpdateManager checkUpdateManager;
    private String currentLanguage;

    private static final String TAG = "MainTabActivity";

    /**
     * 消失动画的更新
     */
    private static final int UPDATE_EXPLORED = 101;

    /**
     * 更新间隔
     */
    private static final int UPDATE_EXPLORED_DURING_TIME = 50;

    /**
     * 动画播放索引值
     */
    private int expolredAnimFrameIndex = 0;

    private static final String REFLECTION_RESOURCE = "explored";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PictureAirLog.out(TAG + "==== onCreate");
        setContentView(R.layout.activity_main);
        application = (MyApplication) getApplication();
        handler = new Handler(this);
        initView();
    }

    //清除acahe框架的缓存数据
    private void clearCache() {
        ACache.get(this).remove(Common.TOP_GOODS);
        ACache.get(this).remove(Common.ALL_GOODS);
        ACache.get(this).remove(Common.BANNER_GOODS);
        ACache.get(this).remove(Common.PPP_GOOD);
        ACache.get(this).remove(Common.LOCATION_INFO);
        ACache.get(this).remove(Common.ACACHE_ADDRESS);
    }

    /**
     * 初始化组件
     */
    private void initView() {
        // 实例化布局对象
        layoutInflater = LayoutInflater.from(this);
        parentLayout = (RelativeLayout) findViewById(R.id.parent);
        // 实例化TabHost对象，得到TabHost
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        newToast = new MyToast(this);
        // 自动检查更新
        sharedPreferences = getSharedPreferences(Common.APP, MODE_PRIVATE);
        currentLanguage = sharedPreferences.getString(Common.LANGUAGE_TYPE, "");
        checkUpdateManager = new CheckUpdateManager(this, currentLanguage,
                parentLayout);
        checkUpdateManager.startCheck();
        // 得到fragment的个数
        int count = fragmentArray.length;

        parentLayout = (RelativeLayout) findViewById(R.id.parent);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        explored = new ImageView(this);
        explored.setLayoutParams(params);
        explored.setImageResource(R.drawable.explored1);
        explored.setVisibility(View.INVISIBLE);
        explored.setAdjustViewBounds(true);
        parentLayout.addView(explored);

        loadFragment(count);//加载tab
        application.setIsStoryTab(true);

        CoverManager.getInstance().init(this);
    }

    /**
     * 动态添加TabHot
     *
     * @param count
     */
    public void loadFragment(int count) {
        for (int i = 0; i < count; i++) {
            PictureAirLog.out("count --------->" + i);
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

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        PictureAirLog.out("maintab ==== resume");
        Intent intent1 = new Intent(this, com.pictureair.photopass.service.NotificationService.class);
        this.startService(intent1);
        if (application.getMainTabIndex() == 3) {
            PictureAirLog.out("skip to shop tab");
            mTabHost.setCurrentTab(3);
            application.setMainTabIndex(-1);
            application.setIsStoryTab(false);
            last_tab = 3;
        } else if (application.getMainTabIndex() == 0) {
            PictureAirLog.out("skip to story tab");
            mTabHost.setCurrentTab(0);
            application.setMainTabIndex(-1);
            application.setIsStoryTab(true);
            last_tab = 0;
        } else {
            PictureAirLog.out("skip to last tab");
            //设置成为上次的tab页面
            mTabHost.setCurrentTab(last_tab);
            if (last_tab == 0) {
                application.setIsStoryTab(true);
            }
        }
        PictureAirLog.out("currenagLanguage--->" + currentLanguage + "___" + MyApplication.getInstance().getLanguageType());
        if (currentLanguage != null && !currentLanguage.equals(MyApplication.getInstance().getLanguageType())) {
            PictureAirLog.out("maintab ==== currentLanguage");
            mTabHost.clearAllTabs();
            mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
            loadFragment(fragmentArray.length);
            mTabHost.setCurrentTab(4);
            currentLanguage = MyApplication.getInstance().getLanguageType();
        }
        PictureAirLog.out("pushcount-->" + application.getPushPhotoCount());
        if (application.getPushPhotoCount() > 0) {//显示红点
            waterDropView.setVisibility(View.VISIBLE);
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
        checkUpdateManager.onDestroy();
        CoverManager.getInstance().destroy();
        clearCache();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
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
                    PictureAirLog.out("photo tab on click");
                    if (mTabHost.getCurrentTab() == 0) {//获取最新数据
                        PictureAirLog.d(TAG, "need refresh");
                        EventBus.getDefault().post(new StoryRefreshOnClickEvent(true));
                    } else {
                        PictureAirLog.d(TAG, "need not refresh");
                    }
                    mTabHost.setCurrentTab(0);
                    last_tab = 0;
                    application.setIsStoryTab(true);
                    break;

                case 2:
                    PictureAirLog.out("camera tab on click");
                    Common.TAB_HEIGHT = mTabHost.getHeight();
                    mTabHost.setCurrentTab(2);
                    application.setIsStoryTab(false);
                    break;

                case 1:
                case 3:
                case 4:
                    PictureAirLog.out(currentTab + " tab on click");
                    mTabHost.setCurrentTab(currentTab);
                    last_tab = currentTab;
                    application.setIsStoryTab(false);
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
            layoutParams.width = ScreenUtil.dip2px(this, 40);
        } else {
            layoutParams.width = ScreenUtil.dip2px(this, 60);

        }
        layoutParams.height = layoutParams.width;
        imageView.setLayoutParams(layoutParams);
        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(mTextviewArray[index]);
        if (index == 2) {
            textView.setVisibility(View.GONE);

            /**
             * 隐藏视频模块
             */
            view.setVisibility(View.GONE);
        }
        if (index == 0) {//添加badgeview
            waterDropView = (WaterDrop) view.findViewById(R.id.waterdrop);
            waterDropView.setOnDragCompeteListener(this);
            AppUtil.expandViewTouchDelegate(waterDropView, 40, 40, 40, 40);
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

    @Override
    public void onDrag(float endX, float endY) {
        //小红点的拖拽消失，消失之后的消息回调，暂时此处不需要做任何的操作
        explored.setVisibility(View.VISIBLE);
        explored.setX(endX - explored.getWidth() / 2);
        explored.setY(endY - explored.getHeight() / 2);
        handler.sendEmptyMessage(UPDATE_EXPLORED);
    }

    @Override
    public void onVisible(boolean visible) {
        waterDropView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public boolean handleMessage(Message msg) {
        // TODO Auto-generated method stub
        switch (msg.what) {
            /**
             * 使用handler只是为了监听动画结束，从而将imageview消失，一般的帧动画是没有监听动画结束的监听
             */
            case UPDATE_EXPLORED:
                expolredAnimFrameIndex++;
                PictureAirLog.out("index--->" + expolredAnimFrameIndex);
                if (expolredAnimFrameIndex < 6) {//更新动画
                    PictureAirLog.out("update");
                    explored.setImageResource(ReflectionUtil.getDrawableId(this, REFLECTION_RESOURCE + expolredAnimFrameIndex));
                    handler.sendEmptyMessageDelayed(UPDATE_EXPLORED, UPDATE_EXPLORED_DURING_TIME);
                } else {//动画结束，隐藏控件
                    PictureAirLog.out("dismiss");
                    expolredAnimFrameIndex = 0;
                    explored.setVisibility(View.GONE);
                }
                break;

            default:
                break;
        }
        return false;
    }


    /**
     * 检测dropView显示消失事件
     *
     * @param baseBusEvent
     */
    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        if (baseBusEvent instanceof RedPointControlEvent) {//红点的刷新
            PictureAirLog.out("control the badgeView----->");
            RedPointControlEvent redPointControlEvent = (RedPointControlEvent) baseBusEvent;
            if (redPointControlEvent.isShowRedPoint()) {//显示红点
                waterDropView.setVisibility(View.VISIBLE);
            } else {//消失红点
                if (waterDropView.isShown()) {
                    waterDropView.setVisibility(View.GONE);
                }
            }
            //刷新列表
            EventBus.getDefault().removeStickyEvent(redPointControlEvent);
        } else if (baseBusEvent instanceof MainTabSwitchEvent) {//切换到对应的tab
            MainTabSwitchEvent mainTabSwitchEvent = (MainTabSwitchEvent) baseBusEvent;
            mTabHost.setCurrentTab(mainTabSwitchEvent.getMainTabSwitchIndex());
            application.setIsStoryTab(mainTabSwitchEvent.getMainTabSwitchIndex() == 0 ? true : false);
            last_tab = mainTabSwitchEvent.getMainTabSwitchIndex();
            EventBus.getDefault().removeStickyEvent(mainTabSwitchEvent);
        }
    }
}
