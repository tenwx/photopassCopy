package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.FragmentAdapter;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.MainTabOnClickEvent;
import com.pictureair.photopass.eventbus.MainTabSwitchEvent;
import com.pictureair.photopass.eventbus.RedPointControlEvent;
import com.pictureair.photopass.eventbus.StoryLoadCompletedEvent;
import com.pictureair.photopass.fragment.FragmentPageDiscover;
import com.pictureair.photopass.fragment.FragmentPageMe;
import com.pictureair.photopass.fragment.FragmentPageShop;
import com.pictureair.photopass.fragment.FragmentPageStory;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.service.NotificationService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.widget.CheckUpdateListener;
import com.pictureair.photopass.widget.CheckUpdateManager;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.dropview.CoverManager;
import com.pictureair.photopass.widget.dropview.DropCover.OnDragCompeteListener;
import com.pictureair.photopass.widget.dropview.WaterDrop;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;


/**
 * 包含三个页面，photo显示、相机拍照、商城，默认进入第一个photo显示页面
 * 通过扫描或者登录之后会来到此页面
 */
public class MainTabActivity extends BaseFragmentActivity implements OnDragCompeteListener, Handler.Callback,
        PWDialog.OnCustomerViewCallBack, OnClickListener, CheckUpdateListener {
    private FragmentPageStory fragmentPageStory;
    private FragmentPageDiscover fragmentPageDiscover;
    private FragmentPageShop fragmentPageShop;
    private FragmentPageMe fragmentPageMe;

    private ViewPager mainViewPager;
    private FragmentAdapter fragmentAdapter;
    private LinearLayout storyTabTrue, discoverTabTrue, shopTabTrue, meTabTrue;
    private ImageView storyTrueIV, discoverTrueIV, shopTrueIV, meTrueIV;
    private TextView storyTrueTV, discoverTrueTV, shopTrueTV, meTrueTV;
    private TextView storyFalseTV, discoverFalseTV, shopFalseTV, meFalseTV;
    private List<Fragment> mainActivityFragmentList = new ArrayList<>();
    private List<ImageView> mainActivityIVList = new ArrayList<>();
    private List<TextView> mainActivityTVList = new ArrayList<>();
    private String[] titleStrings = new String[]{"story", "discover", "shop", "me"};

    private RelativeLayout parentLayout;
    private WaterDrop waterDropView;
    private ImageView explored;
    private Handler handler;

    //story的引导层
    private RelativeLayout leadViewRL;
    private ImageView leadViewIV;
    private boolean showLeadView = true;

    //对话框
    private PWDialog pwDialog;
    private TextView specialDealBuyTV;
    private ImageView specialDealCloseIV;

    //记录退出的时候的两次点击的间隔时间
    private long exitTime = 0;

    private PWToast newToast;

    //上次的tab页面，用来判断点击视频之后回到那个tab
    private int last_tab = 0;

    private boolean hasCreated = false;

    private MyApplication application;
    private CheckUpdateManager checkUpdateManager;
    private boolean needUpdate = true;
    private String currentLanguage;

    private static final String TAG = MainTabActivity.class.getSimpleName();

    private static final int SPECIAL_DIALOG = 99;
    private static final int START_CHECK_UPDATE = 100;

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
        hasCreated = true;
        initView();
        setTabSelection(0);
        changAlpha(0);
    }

    //清除acahe框架的缓存数据
    private void clearCache() {
        ACache.get(this).remove(Common.ALL_GOODS);
        ACache.get(this).remove(Common.ACACHE_ADDRESS);
    }

    /**
     * 初始化组件
     */
    private void initView() {
        mainViewPager = (ViewPager) findViewById(R.id.main_viewpager);

        storyTabTrue = (LinearLayout) findViewById(R.id.main_photo_tab_ll_true);
        discoverTabTrue = (LinearLayout) findViewById(R.id.main_discover_tab_ll_true);
        shopTabTrue = (LinearLayout) findViewById(R.id.main_shop_tab_ll_true);
        meTabTrue = (LinearLayout) findViewById(R.id.main_me_tab_ll_true);

        storyTrueIV = (ImageView) findViewById(R.id.main_photo_iv_true);
        discoverTrueIV = (ImageView) findViewById(R.id.main_discover_iv_true);
        shopTrueIV = (ImageView) findViewById(R.id.main_shop_iv_true);
        meTrueIV = (ImageView) findViewById(R.id.main_me_iv_true);

        storyTrueTV = (TextView) findViewById(R.id.main_photo_tv_true);
        discoverTrueTV = (TextView) findViewById(R.id.main_discover_tv_true);
        shopTrueTV = (TextView) findViewById(R.id.main_shop_tv_true);
        meTrueTV = (TextView) findViewById(R.id.main_me_tv_true);

        storyFalseTV = (TextView) findViewById(R.id.main_photo_tv_false);
        discoverFalseTV = (TextView) findViewById(R.id.main_discover_tv_false);
        shopFalseTV = (TextView) findViewById(R.id.main_shop_tv_false);
        meFalseTV = (TextView) findViewById(R.id.main_me_tv_false);

        initFragment();

        initViewPager();

        storyTabTrue.setOnClickListener(new TabOnClick(0));
        discoverTabTrue.setOnClickListener(new TabOnClick(1));
        shopTabTrue.setOnClickListener(new TabOnClick(2));
        meTabTrue.setOnClickListener(new TabOnClick(3));

        waterDropView = (WaterDrop) findViewById(R.id.waterdrop);
        waterDropView.setOnDragCompeteListener(this);
        AppUtil.expandViewTouchDelegate(waterDropView, 40, 40, 40, 40);

        parentLayout = (RelativeLayout) findViewById(R.id.parent);
        newToast = new PWToast(this);

        // 自动检查更新
        currentLanguage = SPUtils.getString(this, Common.SHARED_PREFERENCE_APP, Common.LANGUAGE_TYPE, Common.ENGLISH);
        checkUpdateManager = new CheckUpdateManager(this, currentLanguage);
        checkUpdateManager.setOnCheckUpdateListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                checkUpdateManager.init();
                handler.sendEmptyMessage(START_CHECK_UPDATE);
            }
        }).start();

        parentLayout = (RelativeLayout) findViewById(R.id.parent);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        explored = new ImageView(this);
        explored.setLayoutParams(params);
        explored.setImageResource(R.drawable.explored1);
        explored.setVisibility(View.INVISIBLE);
        explored.setAdjustViewBounds(true);
        parentLayout.addView(explored);

        application.setIsStoryTab(true);

        CoverManager.getInstance().init(this);
    }

    private void initFragment() {
        fragmentPageStory = new FragmentPageStory();
        fragmentPageDiscover = new FragmentPageDiscover();
        fragmentPageShop = new FragmentPageShop();
        fragmentPageMe = new FragmentPageMe();
        mainActivityFragmentList.clear();
        mainActivityFragmentList.add(fragmentPageStory);
        mainActivityFragmentList.add(fragmentPageDiscover);
        mainActivityFragmentList.add(fragmentPageShop);
        mainActivityFragmentList.add(fragmentPageMe);

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), mainActivityFragmentList, titleStrings);
        mainViewPager.setAdapter(fragmentAdapter);
    }

    private void initViewPager() {
        mainActivityTVList.add(storyTrueTV);
        mainActivityTVList.add(discoverTrueTV);
        mainActivityTVList.add(shopTrueTV);
        mainActivityTVList.add(meTrueTV);

        mainActivityIVList.add(storyTrueIV);
        mainActivityIVList.add(discoverTrueIV);
        mainActivityIVList.add(shopTrueIV);
        mainActivityIVList.add(meTrueIV);

        mainViewPager.setOffscreenPageLimit(3);
        mainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                changAlpha(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                last_tab = position;
                changAlpha(position);
                clickTab(position, false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initLeadView() {
        leadViewRL = (RelativeLayout) findViewById(R.id.story_lead_view_rl);
        leadViewIV = (ImageView) findViewById(R.id.story_lead_iv);
        leadViewRL.setVisibility(View.VISIBLE);
        leadViewRL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtils.put(MainTabActivity.this, Common.SHARED_PREFERENCE_APP, Common.STORY_LEAD_VIEW, Common.STORY_LEAD_VIEW);
                leadViewRL.setVisibility(View.GONE);
                showLeadView = false;
                getSpecialDealGoods();
            }
        });

        if (application.getLanguageType().equals(Common.ENGLISH)) {
            leadViewIV.setImageResource(R.drawable.story_lead_en);
        } else if (application.getLanguageType().equals(Common.SIMPLE_CHINESE)) {
            leadViewIV.setImageResource(R.drawable.story_lead_zh);
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
        if (hasCreated) {
            hasCreated = false;
            return;
        }
        if (application.getMainTabIndex() == 2) {
            PictureAirLog.out("skip to shop tab");
            setTabSelection(2);
            application.setMainTabIndex(-1);
            application.setIsStoryTab(false);
            last_tab = 2;
        } else if (application.getMainTabIndex() == 0) {
            PictureAirLog.out("skip to story tab");
            setTabSelection(0);
            application.setMainTabIndex(-1);
            application.setIsStoryTab(true);
            last_tab = 0;
        } else {
            PictureAirLog.out("skip to last tab");
            //设置成为上次的tab页面
            setTabSelection(last_tab);
            if (last_tab == 0) {
                application.setIsStoryTab(true);
            }
        }
        PictureAirLog.out("currenagLanguage--->" + currentLanguage + "___" + MyApplication.getInstance().getLanguageType());
        if (currentLanguage != null && !currentLanguage.equals(MyApplication.getInstance().getLanguageType())) {
            PictureAirLog.out("maintab ==== currentLanguage");
            //修改底部tab语言
            storyTrueTV.setText(R.string.tab_story);
            discoverTrueTV.setText(R.string.tab_discover);
            shopTrueTV.setText(R.string.tab_shops);
            meTrueTV.setText(R.string.tab_me);
            storyFalseTV.setText(R.string.tab_story);
            discoverFalseTV.setText(R.string.tab_discover);
            shopFalseTV.setText(R.string.tab_shops);
            meFalseTV.setText(R.string.tab_me);
            initFragment();
            setTabSelection(3);
            currentLanguage = MyApplication.getInstance().getLanguageType();
        }
        PictureAirLog.out("pushcount-->" + application.getPushPhotoCount());
        if (application.getPushPhotoCount() + application.getPushViedoCount() > 0) {//显示红点
            waterDropView.setVisibility(View.VISIBLE);
        }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.special_dialog_buy_tv:
                EventBus.getDefault().post(new MainTabOnClickEvent(false, true));
                pwDialog.pwDialogDismiss();
                break;

            case R.id.special_dialog_deal_close_iv:
                EventBus.getDefault().post(new MainTabOnClickEvent(false, true));
                pwDialog.pwDialogDismiss();
                break;

            default:
                break;
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
            clickTab(currentTab, true);
        }
    }

    private void clickTab(int currentTab, boolean isClick) {
        switch (currentTab) {
            case 0:
                PictureAirLog.out("photo tab on click");
                if (last_tab == 0 && isClick) {//获取最新数据
                    PictureAirLog.d(TAG, "need refresh");
                    EventBus.getDefault().post(new MainTabOnClickEvent(true, false));
                } else {
                    PictureAirLog.d(TAG, "need not refresh");
                }
                setTabSelection(currentTab);
                last_tab = currentTab;
                application.setIsStoryTab(true);
                break;

            case 1:
            case 2:
            case 3:
                PictureAirLog.out(currentTab + " tab on click");
                last_tab = currentTab;
                setTabSelection(currentTab);
                application.setIsStoryTab(false);
                break;

            default:
                break;
        }

    }

    private void setTabSelection(int index) {
        if (checkCurrentSelection(index)) {//如果正在显示，不需要做任何处理
            PictureAirLog.out("current showing tab is ---> " + index);
            return;
        } else {
            PictureAirLog.out("current tab not showing--->" + index);
        }

        mainViewPager.setCurrentItem(index);
        PictureAirLog.out("maintab---->commit");
    }

    /**
     * 检查当前是显示的是哪个fragment
     * @param index
     * @return
     */
    private boolean checkCurrentSelection(int index) {
        return index == mainViewPager.getCurrentItem();
    }

    /**
     * 一开始运行、滑动和点击tab结束后设置tab的透明度，fragment的透明度和大小
     */
    private void changAlpha(int postion) {
        for (int i = 0; i < mainActivityTVList.size(); i++) {
            if (i == postion) {
                mainActivityTVList.get(i).setAlpha(1.0f);
                mainActivityIVList.get(i).setAlpha(1.0f);
            } else {
                mainActivityTVList.get(i).setAlpha(0.0f);
                mainActivityIVList.get(i).setAlpha(0.0f);
            }
        }
    }

    /**
     * 根据滑动设置透明度
     */
    private void changAlpha(int pos, float posOffset) {
        int nextIndex = pos + 1;
        if (posOffset > 0) {
            //设置tab的颜色渐变效果
            mainActivityTVList.get(nextIndex).setAlpha(posOffset);
            mainActivityIVList.get(nextIndex).setAlpha(posOffset);
            mainActivityTVList.get(pos).setAlpha(1 - posOffset);
            mainActivityIVList.get(pos).setAlpha(1 - posOffset);
        }
    }

    //双击退出app
    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 1000) {
            newToast.setTextAndShow(R.string.exit, Common.TOAST_SHORT_TIME);
            exitTime = System.currentTimeMillis();
        } else {
            newToast.cancelShow();
            finish();
            clearCache();
            //取消通知
            Intent intent = new Intent(MainTabActivity.this, NotificationService.class);
            intent.putExtra("status", "disconnect");
            startService(intent);
            //关闭下载
            Intent intent1 = new Intent(MyApplication.getInstance(), DownloadService.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean("logout",true);
            intent1.putExtras(bundle);
            startService(intent1);
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

    private void showSpecialDealDialog() {
        if (pwDialog == null) {
            pwDialog = new PWDialog(this, SPECIAL_DIALOG)
                    .setPWDialogNegativeButton(null)
                    .setPWDialogPositiveButton(null)
                    .setPWDialogBackgroundColor(R.color.transparent)
                    .setPWDialogContentView(R.layout.dialog_special_deal, this)
                    .pwDialogCreate();
        }
        pwDialog.pwDilogShow();
    }

    private void getSpecialDealGoods() {
        if (!needUpdate && !showLeadView) {//不需要更新apk，并且不需要显示引导层，才需要显示抢购
            //如果活动进行中，则不需要重新获取数据
            API1.getDealingGoods(MyApplication.getTokenId(), MyApplication.getInstance().getLanguageType(), handler);
            showSpecialDealDialog();//抢购活动，需要在更新提示框之后出现
        }
    }

    @Override
    public void initCustomerView(View view, int dialogId) {
        if (dialogId == SPECIAL_DIALOG) {
            specialDealBuyTV = (TextView) view.findViewById(R.id.special_dialog_buy_tv);
            specialDealCloseIV = (ImageView) view.findViewById(R.id.special_dialog_deal_close_iv);
            specialDealBuyTV.setOnClickListener(this);
            specialDealCloseIV.setOnClickListener(this);
        }
    }

    @Override
    public void checkUpdateCompleted(int result) {//抢购的弹框需要在更新框之后
        needUpdate = result == CheckUpdateManager.APK_NEED_UPDATE;
        getSpecialDealGoods();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

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

            case START_CHECK_UPDATE:
                checkUpdateManager.startCheck();
                break;

            case API1.GET_DEALING_GOODS_SUCCESS:
                break;

            case API1.GET_DEALING_GOODS_FAILED:
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
            setTabSelection(mainTabSwitchEvent.getMainTabSwitchIndex());
            application.setIsStoryTab(mainTabSwitchEvent.getMainTabSwitchIndex() == 0 ? true : false);
            last_tab = mainTabSwitchEvent.getMainTabSwitchIndex();
            EventBus.getDefault().removeStickyEvent(mainTabSwitchEvent);

        } else if (baseBusEvent instanceof StoryLoadCompletedEvent) {//显示引导层
            StoryLoadCompletedEvent storyLoadCompletedEvent = (StoryLoadCompletedEvent) baseBusEvent;
            showLeadView = storyLoadCompletedEvent.isShowLeadView();
            if (showLeadView) {
                initLeadView();
            } else {
                getSpecialDealGoods();
            }
            EventBus.getDefault().removeStickyEvent(storyLoadCompletedEvent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> listFragments = getSupportFragmentManager().getFragments();
        if (listFragments != null && listFragments.size() > 0) {
            for (Fragment fragment : listFragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
