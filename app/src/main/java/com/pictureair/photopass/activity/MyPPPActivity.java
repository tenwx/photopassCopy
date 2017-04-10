package com.pictureair.photopass.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.ListOfPPPAdapter;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.entity.JsonInfo;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.ScanInfoEvent;
import com.pictureair.photopass.greendao.PictureAirDbManager;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.pullloadlayout.MyListView;
import com.pictureair.photopass.widget.pullloadlayout.OnRefreshListener;
import com.pictureair.photopass.widget.pullloadlayout.ReFreshLayout;
import com.trello.rxlifecycle.android.ActivityEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 显示用户所有的PP+或是对应某个PP而言可使用的PP+
 */
public class MyPPPActivity extends BaseActivity implements OnClickListener, OnRefreshListener, PWDialog.OnPWDialogClickListener {
    private static final String TAG = "MyPPPActivity";
    private boolean isUseHavedPPP = false;
    private boolean isDailyPPP = false;
    private ImageView ppp_guideView, pppImgIv;
    private TextView pppIntroTv;
    private MyListView listPPP;
    private ImageView back;
    private Button button_buy_ppp, button_scan_ppp; // 无PP＋时 底部的两个按钮。
    private LinearLayout ll_button_area, ll_guide_layout;//无PP＋时 底部的两个按钮的区域。
    private ScrollView nopppLayout;

    private PWToast newToast;

    private ListOfPPPAdapter listPPPAdapter;
    private ArrayList<PPPinfo> list1;// 绑定了pp的pp+
    private ArrayList<PPPinfo> listNormal;// 已激活未激活的pp+
    private ArrayList<PPPinfo> listNoUse;// 已过期已用完的pp+

    private boolean hasOtherAvailablePPP = false;//判断是否还有其他可用的ppp

    private String errorMessage = "";
    private NoNetWorkOrNoCountView netWorkOrNoCountView;
    private PPPinfo ppp;

    private List<GoodsInfo> allGoodsList;//全部商品
    private GoodsInfo pppGoodsInfo;
    private String[] photoUrls;

    // 选择PP＋需要的组件
    private TextView ok;
    private String ppsStr;
    private TextView mTitle;
    private PWDialog pictureWorksDialog;
    private RelativeLayout menuLayout;

    private static final int SCAN_PPP_CODE_SUCCESS = 111;
    private static final int SCAN_FAILED_DIALOG = 222;
    private static final int TYPE_NOT_SAME_DIALOG = 333;
    private static final int UPDATE_TIPS_DIALOG = 555;
    private static final int BUY_PPP_AND_UPDATE_TIP_DIALOG = 666;
    private static final int SCAN_PPP_AND_UPDATE_TIP_DIALOG = 777;

    private boolean isOnResume = false;

    private ScanInfoEvent scanInfoEvent;

    private int status = 0;
    private final int normal = 1;
    private final int unUse = 2;
    private final int full = 3;
    private ReFreshLayout refreshLayout;

    private final Handler myPPPHandler = new MyPPPHandler(this);

    ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            PictureAirLog.out("MyPPPActivity onGlobalLayout out");
            if (listPPP.isShown() && listPPPAdapter != null && listPPPAdapter.getArrayList() != null && listPPPAdapter.getArrayList().size() > 0) {
                PictureAirLog.out("MyPPPActivity onGlobalLayout in");
                if (listPPP.getViewTreeObserver().isAlive()) {
                    listPPP.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                showGuideView();
            }
        }
    };

    @Override
    public void onRefresh() {
        Message message = new Message();
        message.what = 3;
        myPPPHandler.sendMessageDelayed(message, 1000);
    }

    private static class MyPPPHandler extends Handler {
        private final WeakReference<MyPPPActivity> mActivity;

        public MyPPPHandler(MyPPPActivity activity) {
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

    /**
     * 处理Message
     *
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case SCAN_PPP_CODE_SUCCESS:
                if (isOnResume) {
                    dealPPPresult();
                } else {
                    myPPPHandler.sendEmptyMessageDelayed(SCAN_PPP_CODE_SUCCESS, 50);
                }
                break;

            case 1:
                dismissPWProgressDialog();
                refreshLayout.setVisibility(View.VISIBLE);
                PictureAirLog.v(TAG, "list=========" + list1.size());
                listPPPAdapter = new ListOfPPPAdapter(list1, isUseHavedPPP, myPPPHandler, MyPPPActivity.this);
                listPPP.setAdapter(listPPPAdapter);
                break;

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                //重新加载购物车数据
                GetPPPList();
                break;

            case 2:
                ok.setText(formaStringPPP(listPPPAdapter.getMap().size(), 1));
                if (listPPPAdapter.getMap().size() >= 1) {
                    ok.setEnabled(true);
                    ok.setTextColor(ContextCompat.getColor(MyPPPActivity.this, R.color.pp_blue));
                } else {
                    ok.setEnabled(false);
                    ok.setTextColor(ContextCompat.getColor(MyPPPActivity.this, R.color.gray_light5));
                }
                break;
            case 3:
                if (status == normal) {
                    if (listNoUse == null || listNoUse.size() < 1) {
                        newToast.setTextAndShow(R.string.ppp_load_all);
                    } else {
                        listPPPAdapter.setArrayList(list1);
                    }
                    status = full;
                    finishLoad();
                    listPPPAdapter.notifyDataSetChanged();
                    listPPP.setSelection(listPPP.getLastVisiblePosition() + 1);
                } else if (status == unUse) {
                    status = full;
                    newToast.setTextAndShow(R.string.ppp_load_all);
                    finishLoad();
                } else if (status == full) {
                    newToast.setTextAndShow(R.string.ppp_load_all);
                    finishLoad();
                }
                break;

            case ListOfPPPAdapter.HEAD_CLICK:
                int position = (int) msg.obj;
                if (position >= list1.size()) {
                    return;
                }
                if (list1.get(position).bindInfo.size() < list1.get(position).capacity && list1.get(position).expired == 0) {
                    if (list1.get(position).expericePPP == 1) {//体验卡
                        Intent intent3 = new Intent(MyPPPActivity.this, SelectPhotoActivity.class);
                        intent3.putExtra("activity", "mypppactivity");
                        intent3.putExtra("pppCode", list1.get(position).PPPCode);
                        intent3.putExtra("photoCount", 1);
                        startActivity(intent3);
                    } else {
                        PictureAirLog.v(TAG, "pppSize :" + list1.get(position).PPPCode);
                        ppp = list1.get(position);
                        getPPsByPPPAndDate(ppp.PPPCode);
                    }
                }

                break;

            default:
                break;
        }
    }


    private void finishLoad() {
        refreshLayout.finishRefreshing();
    }

    /**
     * 显示引导层
     */
    private void showGuideView() {
        boolean isGuide = SPUtils.getBoolean(MyApplication.getInstance(), Common.SHARED_PREFERENCE_APP, isDailyPPP ? Common.DAILY_PPP_GUIDE : Common.PPP_GUIDE, false);
        if (!isGuide) {//引导层没显示过，则保存状态
            SPUtils.put(MyApplication.getInstance(), Common.SHARED_PREFERENCE_APP, isDailyPPP ? Common.DAILY_PPP_GUIDE : Common.PPP_GUIDE, true);
            //调整imageview大小
            View view = listPPP.getChildAt(0);
            if (view != null) {
                if (view.getTag() != null && view.getTag() instanceof ListOfPPPAdapter.ViewHolder) {
                    ListOfPPPAdapter.ViewHolder holder = (ListOfPPPAdapter.ViewHolder) view.getTag();
                    if (holder != null && holder.pp3_img != null && holder.time != null) {

                        holder.pp3_img.measure(0, 0);
                        holder.time.measure(0, 0);
                        //计算列表第三个米奇头的宽高
                        int height = holder.pp3_img.getMeasuredHeight();

                        int oriImgWidth = 583;//图片原宽
                        int oriImgHeight = 324;//图片高
                        //计算引导图中imageView的宽高
                        int guideImgWidth = ScreenUtil.getScreenWidth(MyPPPActivity.this) - 2 * ScreenUtil.dip2px(MyPPPActivity.this, 16);
                        int guideImgHeight = (int) (((guideImgWidth * 1.0f) / oriImgWidth) * oriImgHeight);

                        //计算列表中第三个米奇头的中心
                        int midTop = (int) (ScreenUtil.dip2px(MyPPPActivity.this, 52) + ScreenUtil.dip2px(MyPPPActivity.this, 10) + height / 2f);

                        //计算引导图右上角米奇头的中心
                        //引导图右上角米奇头高52，米奇头距离上边25
                        int guideMikeyMidTop = ScreenUtil.dip2px(MyPPPActivity.this, 52) + (int) (((guideImgWidth * 1.0f) / oriImgWidth) * (26 + 25));

                        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) ppp_guideView.getLayoutParams();
                        //引导图右上角米奇头宽62，米奇头距离右边15
                        params1.rightMargin -= 15 * ((guideImgWidth * 1.0f) / oriImgWidth);
                        params1.leftMargin += 15 * ((guideImgWidth * 1.0f) / oriImgWidth);
                        if (params1.rightMargin < 0) {
                            params1.rightMargin = 0;
                        }

                        if (params1.leftMargin < 0) {
                            params1.leftMargin = 0;
                        }
                        params1.width = guideImgWidth;
                        params1.height = guideImgHeight;
                        params1.topMargin += (midTop - guideMikeyMidTop);
                        ppp_guideView.setLayoutParams(params1);
                        PictureAirLog.out("midTop: " + String.valueOf(midTop));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            View v = findViewById(R.id.ppp_img_guide_status_bar);
                            ViewGroup.LayoutParams params = v.getLayoutParams();
                            params.height = ScreenUtil.getStatusBarHeight(this);
                        }
                        ll_guide_layout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_my_ppp);
        initViewCommon();
        isUseHavedPPP = getIntent().getBooleanExtra("isUseHavedPPP", false);
        isDailyPPP = getIntent().getBooleanExtra("dailyppp", false);
        if (isUseHavedPPP) {
            ppsStr = getIntent().getStringExtra("ppsStr");
            initViewUseHavedPPP();
        } else {
            initView();
        }
    }

    private void initViewCommon() {
        //初始化
        newToast = new PWToast(this);

        pictureWorksDialog = new PWDialog(this)
                .setOnPWDialogClickListener(this)
                .pwDialogCreate();

        mTitle = (TextView) findViewById(R.id.myppp);
        ll_button_area = (LinearLayout) findViewById(R.id.ll_button_area);
        back = (ImageView) findViewById(R.id.back);
        nopppLayout = (ScrollView) findViewById(R.id.nopppinfo);
        listPPP = (MyListView) findViewById(R.id.list_ppp);
        refreshLayout = (ReFreshLayout) findViewById(R.id.ppp_refresh);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
        ll_guide_layout = (LinearLayout) findViewById(R.id.ppp_ll_guide);
        ppp_guideView = (ImageView) findViewById(R.id.ppp_img_guide_view);
        pppImgIv = (ImageView) findViewById(R.id.ppp_img_iv);
        pppIntroTv = (TextView) findViewById(R.id.ppp_intro_tv);
        nopppLayout.setVisibility(View.INVISIBLE);
        ll_button_area.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.GONE);
        ll_guide_layout.setVisibility(View.GONE);
        back.setOnClickListener(this);
    }

    private void initViewUseHavedPPP() {

        mTitle.setText(R.string.select_ppp_title);
        // 显示右上角 ok 按钮。隐藏 ＋ 号
        ok = (TextView) findViewById(R.id.ok);
        ok.setVisibility(View.VISIBLE);
        ok.setText(formaStringPPP(0, 1));
        ok.setOnClickListener(this);
        ok.setEnabled(false);
        ok.setTextColor(ContextCompat.getColor(MyPPPActivity.this, R.color.gray_light5));
        refreshLayout.setVisibility(View.VISIBLE);

        list1 = new ArrayList<>();
        for (int i = 0; i < API2.PPPlist.size(); i++) {
            PPPinfo ppPinfo = API2.PPPlist.get(i);
            if (isDailyPPP) {//一日通
                if (ppPinfo.capacity != 1) {
                    continue;

                }

            } else {//一卡通
                if (ppPinfo.capacity == 1) {
                    continue;
                }

            }
            list1.add(ppPinfo);
        }
        listPPPAdapter = new ListOfPPPAdapter(list1, isUseHavedPPP, myPPPHandler, MyPPPActivity.this);
//        View view = LayoutInflater.from(MyPPPActivity.this).inflate(R.layout.ppp_select_head, null);
//        listPPP.addHeaderView(view);
        listPPP.setAdapter(listPPPAdapter);
    }

    private void initView() {
        //找控件
        button_buy_ppp = (Button) findViewById(R.id.button_buy_ppp);
        button_scan_ppp = (Button) findViewById(R.id.button_scan_ppp);
        menuLayout = (RelativeLayout) findViewById(R.id.ppp_rl);
        menuLayout.setVisibility(View.VISIBLE);
        menuLayout.setOnClickListener(this);
        list1 = new ArrayList<>();
        //设置需要刷新PPPList
        MyApplication.getInstance().setNeedRefreshPPPList(true);
        button_buy_ppp.setOnClickListener(this);
        button_scan_ppp.setOnClickListener(this);
        refreshLayout.setListView(listPPP);
        refreshLayout.setOnRefreshListener(this);
        ll_guide_layout.setOnClickListener(this);
        ViewTreeObserver viewTreeObserver = listPPP.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener);
        String languageType = MyApplication.getInstance().getLanguageType();
        if (languageType == null) {
            ppp_guideView.setImageResource(R.drawable.ppp_guide_zh);
        } else {
            if (languageType.equals(Common.SIMPLE_CHINESE)) {
                ppp_guideView.setImageResource(R.drawable.ppp_guide_zh);
            } else {
                ppp_guideView.setImageResource(R.drawable.ppp_guide_en);
            }
        }
        if (isDailyPPP) {//一日通
            mTitle.setText(R.string.mypage_daily_ppp1);
            pppIntroTv.setText(R.string.instruction_daily_ppp);
            button_buy_ppp.setVisibility(View.VISIBLE);
            button_scan_ppp.setText(R.string.scan_ppp_text);
            button_scan_ppp.setBackgroundResource(R.drawable.button_gray_light);
            button_scan_ppp.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));

        } else {//一卡通
            mTitle.setText(R.string.mypage_ppp);
            pppIntroTv.setText(R.string.instruction);
            button_buy_ppp.setVisibility(View.GONE);
            button_scan_ppp.setText(R.string.story_scan_ppp);
        }
    }

    //获取ppp数据
    private void GetPPPList() {
        showPWProgressDialog();
        list1.clear();
        hasOtherAvailablePPP = false;
        API2.PPPlist.clear();//清空之前的list，从网络中重新获取
        getData();
    }

    /**
     * 获取数据
     */
    private void getData() {
        if (API2.PPPlist.size() == 0) {//没有数据，需要重新获取
            PictureAirLog.v(TAG, "ppp = 0");
            getPPPSByUserId();
        } else {//有数据
            PictureAirLog.v(TAG, "ppp != 0");
            for (int i = 0; i < API2.PPPlist.size(); i++) {
                PictureAirLog.v(TAG, "load==========");
                PPPinfo ppPinfo = API2.PPPlist.get(i);
                if (isDailyPPP) {//一日通
                    if (ppPinfo.capacity != 1) {
                        continue;

                    }

                } else {//一卡通
                    if (ppPinfo.capacity == 1) {
                        continue;
                    }

                }

                String bindddateString = ppPinfo.bindInfo.get(0).bindDate;
                PictureAirLog.v(TAG, bindddateString);
                bindddateString = bindddateString.replace("[", "").replace("]", "").replaceAll("\"", "").trim();
                PictureAirLog.v(TAG, bindddateString);
                String[] timeStrings = bindddateString.split(",");
                if (timeStrings.length > 0) {
                    ppPinfo.ownOn = timeStrings[0];

                } else {
                    ppPinfo.ownOn = "";
                }
                String pplistString = ppPinfo.bindInfo.get(0).customerId;
                PictureAirLog.v(TAG, pplistString);
                String[] ppStrings = pplistString.split(",");

                for (int j = 0; j < ppStrings.length; j++) {
                    if (j == 0) {
                        PictureAirLog.v(TAG, j + ppStrings[0]);
                        if (null != ppStrings[0] && !"".equals(ppStrings[0])) {

                            ppPinfo.pp1 = ppStrings[0];
                        }
                    } else if (j == 1) {
                        if (null != ppStrings[1] && !"".equals(ppStrings[1])) {
                            PictureAirLog.v(TAG, j + ppStrings[1]);
                            ppPinfo.pp2 = ppStrings[1];
                        }
                    } else if (j == 2) {
                        if (null != ppStrings[2] && !"".equals(ppStrings[2])) {

                            PictureAirLog.v(TAG, j + ppStrings[2]);
                            ppPinfo.pp3 = ppStrings[2];
                        }
                    }
                }
                list1.add(ppPinfo);
            }
            Collections.sort(list1);
            myPPPHandler.obtainMessage(1);
            MyApplication.getInstance().setNeedRefreshPPPList(false);
        }
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.back://后退按钮
                doBack();
                break;

            case R.id.ppp_rl://设置按钮   + 按钮

                Intent i = new Intent(MyPPPActivity.this, PreviewPhotoActivity.class);
                i.putExtra("souvenir", true);
                startActivity(i);

                break;

            case R.id.button_buy_ppp:
                //购买PP+，先获取商品 然后进入订单界面
                if (!isNetWorkConnect(MyApplication.getInstance())) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                showPWProgressDialog();
                //获取商品（以后从缓存中取）
                getGoods();
                break;

            case R.id.button_scan_ppp:
                Intent intent;
                if (isDailyPPP) {
                    intent = new Intent(MyPPPActivity.this, MipCaptureActivity.class);
                    intent.putExtra("from", "ppp");

                } else {
                    intent = new Intent(MyPPPActivity.this, AddPPPCodeActivity.class);
                    intent.putExtra("type", "ppp");//只扫描ppp

                }
                startActivity(intent);
                break;

            case R.id.ok: // 确定选择之后
                pictureWorksDialog.setPWDialogId(UPDATE_TIPS_DIALOG)
                        .setPWDialogMessage(isDailyPPP ? R.string.update_daily_ppp_msg : R.string.update_ppp_msg)
                        .setPWDialogNegativeButton(R.string.update_ppp_cancel)
                        .setPWDialogPositiveButton(R.string.update_ppp_ok)
                        .setPWDialogContentCenter(true)
                        .pwDilogShow();
                break;

            case R.id.ppp_ll_guide:
                ll_guide_layout.setVisibility(View.GONE);
                if (!isUseHavedPPP) {//需要显示弹框
                    showDialog();
                }
                break;

            default:
                break;
        }
    }

    /**
     * 初始化数据
     */
    public void getGoods() {
        //从缓层中获取数据
        String goodsByACache = ACache.get(MyPPPActivity.this).getAsString(Common.ALL_GOODS);
        if (goodsByACache != null && !goodsByACache.equals("")) {
            getGoodsSuccess(goodsByACache);
        } else {
            //从网络获取商品,先检查网络
            if (AppUtil.getNetWorkType(MyApplication.getInstance()) != 0) {
                getAllGoods();
            } else {
                newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
            }
        }
    }


    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        if (baseBusEvent instanceof ScanInfoEvent) {
            scanInfoEvent = (ScanInfoEvent) baseBusEvent;
            String result = scanInfoEvent.getResult();
            PictureAirLog.out("myppp----->" + result);
            if (scanInfoEvent.getCodeType().equals("ppp")) {
                myPPPHandler.sendEmptyMessageDelayed(SCAN_PPP_CODE_SUCCESS, 50);
            }
            EventBus.getDefault().removeStickyEvent(scanInfoEvent);
        }
    }

    /**
     * 处理扫描得到的ppp
     */
    private void dealPPPresult() {
        String pppResultStr = scanInfoEvent.getResult();
        if (pppResultStr.equals("pppOK")) {//ppp绑定成功，需要重新获取ppp信息
            GetPPPList();
        } else if (pppResultStr.equals("failed")) {//扫描失败
            int id = scanInfoEvent.getErrorType();
            PictureAirLog.v(TAG, "------>" + id);
            switch (id) {
                case R.string.http_error_code_6055:
                    errorMessage = getString(R.string.not_ppp_card);
                    break;

                default:
                    errorMessage = getString(id);
                    break;
            }

            pictureWorksDialog.setPWDialogId(SCAN_FAILED_DIALOG)
                    .setPWDialogMessage(errorMessage)
                    .setPWDialogNegativeButton(null)
                    .setPWDialogPositiveButton(R.string.dialog_ok1)
                    .setPWDialogContentCenter(true)
                    .pwDilogShow();

        } else if (pppResultStr.equals("notSame")) {//卡片类型不一致
            //初始化dialog
            pictureWorksDialog.setPWDialogId(TYPE_NOT_SAME_DIALOG)
                    .setPWDialogMessage(R.string.not_ppp_card)
                    .setPWDialogNegativeButton(null)
                    .setPWDialogPositiveButton(R.string.dialog_ok1)
                    .setPWDialogContentCenter(true)
                    .pwDilogShow();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        PictureAirLog.out(MyPPPActivity.class.getSimpleName() + "------>onreusme");
        isOnResume = true;
        if (isUseHavedPPP) {

        } else {
            if (MyApplication.getInstance().getNeedRefreshPPPList()) { //解决问题，从PPP页面扫描PP+成功之后不刷新的问题。
                PictureAirLog.out("start get ppplist");
                GetPPPList();
            }
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        PictureAirLog.out(MyPPPActivity.class.getSimpleName() + "------>ondestroy");
        if (isUseHavedPPP) {
//            API1.PPPlist.clear();
        } else {
            //防止在出现引导页的时候，按手机返回键，导致数据没有清空造成下次依旧出现弹框的问题。
            MyApplication.getInstance().clearIsBuyingPhotoList();
            MyApplication.getInstance().setBuyPPPStatus("");

            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
            myPPPHandler.removeCallbacksAndMessages(null);
            if (listPPP.getViewTreeObserver().isAlive()) {
                listPPP.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
            }
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        isOnResume = false;
    }

    private String formaStringPPP(int count1, int count2) {
        return String.format(getString(R.string.pp_ok), count1, count2);
    }


    private void showDialog() {
        boolean isGuide = SPUtils.getBoolean(MyApplication.getInstance(), Common.SHARED_PREFERENCE_APP, isDailyPPP ? Common.DAILY_PPP_GUIDE : Common.PPP_GUIDE, false);
        if (isGuide) {
            if (getIntent().getBooleanExtra("upgradePP", false) && hasUnUpgradedPP()) {//需要选择pp进行升级
                pictureWorksDialog.setPWDialogId(SCAN_PPP_AND_UPDATE_TIP_DIALOG)
                        .setPWDialogMessage(getString(R.string.scan_ppp_upgrade))
                        .setPWDialogNegativeButton(null)
                        .setPWDialogPositiveButton(R.string.use_ppp_upgrade_ok)
                        .setPWDialogContentCenter(false)
                        .pwDilogShow();

            } else if (!TextUtils.isEmpty(MyApplication.getInstance().getBuyPPPStatus())) {
                String photoCode = MyApplication.getInstance().getIsBuyingPhotoPassCode();
                String[] codes = photoCode.split(",");
                String shootTime = MyApplication.getInstance().getIsBuyingPhotoShootTime();

                String photoPassInfo = String.format(getString(R.string.use_ppp_upgrade_pp_code), shootTime, codes[0]);
                for (int i = 1; i < codes.length; i++) {
                    photoPassInfo += String.format(getString(R.string.use_ppp_upgrade_pp_code), shootTime, codes[i]);
                }
                boolean isVideo = MyApplication.getInstance().getBuyPPPStatus().equals(Common.FROM_AD_ACTIVITY_PAYED);

                String message = String.format(getString(isVideo ? R.string.use_ppp_upgrade_read_video : R.string.use_ppp_upgrade_read_photo), photoPassInfo);

                pictureWorksDialog.setPWDialogId(BUY_PPP_AND_UPDATE_TIP_DIALOG)
                        .setPWDialogMessage(message)
                        .setPWDialogNegativeButton(null)
                        .setPWDialogPositiveButton(R.string.use_ppp_upgrade_ok)
                        .setPWDialogContentCenter(false)
                        .pwDilogShow();

                MyApplication.getInstance().clearIsBuyingPhotoList();
                MyApplication.getInstance().setBuyPPPStatus("");
            }
        }
    }

    /**
     * 是否有未升级的pp卡
     *
     * @return
     */
    private boolean hasUnUpgradedPP() {
        boolean hasUnUpgrade = false;
        ArrayList<JsonInfo> jsonInfos = PictureAirDbManager.getJsonInfos(JsonInfo.JSON_LOCATION_PHOTO_TYPE);
        for (int i = 0; i < jsonInfos.size(); i++) {
            if (jsonInfos.get(i).getJsonString().contains("ifActive: 0")) {
                hasUnUpgrade = true;
                break;
            }
        }
        return hasUnUpgrade;
    }

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            switch (dialogId) {
                case UPDATE_TIPS_DIALOG://升级提示
                    if (listPPPAdapter.getMap().size() == 1) {
                        showPWProgressDialog();
                        bindPPsDateToPPP(JSONArray.parseArray(ppsStr), API2.PPPlist.get(listPPPAdapter.getOnclickPosition()).PPPCode);
                    } else {
                        newToast.setTextAndShow(R.string.select_your_ppp, Common.TOAST_SHORT_TIME);
                    }
                    break;

                case SCAN_PPP_AND_UPDATE_TIP_DIALOG://扫描完ppp，选择pp升级
                case BUY_PPP_AND_UPDATE_TIP_DIALOG://购买完ppp之后，去选择pp升级
                    if (list1.size() == 0) {
                        return;
                    }

                    int position = 0;
                    //按照顺序，找到第一个全新的ppp
                    for (int i = 0; i < list1.size(); i++) {
                        if (list1.get(i).bindInfo.size() == 0 && list1.get(position).expired == 0) {
                            position = i;
                            break;
                        }
                    }
                    if (list1.get(position).bindInfo.size() < list1.get(position).capacity && list1.get(position).expired == 0) {
                        if (list1.get(position).expericePPP == 1) {//体验卡
                        } else {
                            PictureAirLog.v(TAG, "pppSize :" + list1.get(position).PPPCode);
                            ppp = list1.get(position);
                            getPPsByPPPAndDate(ppp.PPPCode);
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private void getPPsByPPPAndDate(String pppCode) {
        API2.getPPsByPPPAndDate(pppCode)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        API2.PPlist = JsonUtil.getPPSByPPP(jsonObject);
                        Intent intent = new Intent(MyPPPActivity.this, SelectPPActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("ppp", ppp);
                        intent.putExtra("dailyppp", isDailyPPP);
                        intent.putExtra("bundle", bundle);
                        startActivity(intent);
                    }

                    @Override
                    public void _onError(int status) {
                        newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void getPPPSByUserId() {
        API2.getPPPSByUserId(MyApplication.getTokenId())
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.out("ppp--->" + jsonObject.toString());
                        API2.PPPlist = JsonUtil.getPPPSByUserId(jsonObject);
                        getPPPListSuccess();
                    }

                    @Override
                    public void _onError(int status) {
                        PictureAirLog.d(Common.GET_PPPS_BY_USERID + "failed---->" + status);
                        /**改rxjava之前有，觉得没意义，需确认*/
//                        if (msg.obj != null && msg.obj.toString().equals("PPHasUpgraded")) {
//                            PictureAirLog.v(TAG, "PP has upgraded");
//                            newToast.setTextAndShow(R.string.select_pp_hasUpgraded, Common.TOAST_SHORT_TIME);
//                        }
                        dismissPWProgressDialog();
                        netWorkOrNoCountView.setVisibility(View.VISIBLE);
                        netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, myPPPHandler, true);
                        refreshLayout.setVisibility(View.INVISIBLE);
                        nopppLayout.setVisibility(View.GONE);
                        ll_button_area.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void getPPPListSuccess() {
        SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.PPP_COUNT, API2.PPPlist.size());
        list1.clear();
        for (int i = 0; i < API2.PPPlist.size(); i++) {
            PPPinfo ppPinfo = API2.PPPlist.get(i);
            if (isDailyPPP) {//一日通
                if (ppPinfo.capacity != 1) {
                    continue;

                }

            } else {//一卡通
                if (ppPinfo.capacity == 1) {
                    continue;
                }

            }
            //判断是否有可用的ppp
            if (!hasOtherAvailablePPP) {
                if (ppPinfo.bindInfo.size() < ppPinfo.capacity) {// 有空位的ppp
                    hasOtherAvailablePPP = true;
                }
            }
            // 需要对ppp进行排序
            list1.add(ppPinfo);
            PictureAirLog.v(TAG, "the ppp code ====>"
                    + ppPinfo.PPPCode + "expericePPP===>" + ppPinfo.expericePPP + "capacity" + ppPinfo.capacity);
        }
        PictureAirLog.v(TAG, "list-=--=" + list1.size());

        if (list1.size() == 0) {
            refreshLayout.setVisibility(View.GONE);
            nopppLayout.setVisibility(View.VISIBLE);
            ll_button_area.setVisibility(View.VISIBLE);
            if (isDailyPPP) {
                pppImgIv.setImageResource(R.drawable.daily_ppp_introduce1);
            } else {
                pppImgIv.setImageResource(R.drawable.ppp_introduce1);

            }

        } else {
            Collections.sort(list1);
            if (listNormal == null) listNormal = new ArrayList<>();
            if (listNoUse == null) listNoUse = new ArrayList<>();
            listNormal.clear();
            listNoUse.clear();

            for (int i = 0; i < list1.size(); i++) {
                PPPinfo info = list1.get(i);
                if (info.bindInfo.size() < info.capacity && info.expired == 0) {
                    listNormal.add(info);
                } else {
                    listNoUse.add(info);
                }
            }
            refreshLayout.setVisibility(View.VISIBLE);
            nopppLayout.setVisibility(View.GONE);
            ll_button_area.setVisibility(View.GONE);
            if (listNormal != null && listNormal.size() > 0) {
                status = normal;
                listPPPAdapter = new ListOfPPPAdapter(listNormal, isUseHavedPPP, myPPPHandler, MyPPPActivity.this);
            } else if (listNoUse != null && listNoUse.size() > 0) {
                status = unUse;
                listPPPAdapter = new ListOfPPPAdapter(listNoUse, isUseHavedPPP, myPPPHandler, MyPPPActivity.this);
            }
            listPPP.setAdapter(listPPPAdapter);
        }
        netWorkOrNoCountView.setVisibility(View.GONE);
        MyApplication.getInstance().setNeedRefreshPPPList(false);
        dismissPWProgressDialog();
        if (!isUseHavedPPP) {//需要显示弹框
            showDialog();
        }
    }

    private void getGoodsSuccess(String goodStr) {
        GoodsInfoJson goodsInfoJson = JsonTools.parseObject(goodStr, GoodsInfoJson.class);//GoodsInfoJson.getString()
        if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
            allGoodsList = goodsInfoJson.getGoods();
            PictureAirLog.v(TAG, "goods size: " + allGoodsList.size());
        }
        //获取PP+
        for (GoodsInfo goodsInfo : allGoodsList) {
            if (goodsInfo.getName().equals(Common.GOOD_NAME_PPP)) {
                pppGoodsInfo = goodsInfo;
                //封装购物车宣传图
                photoUrls = new String[goodsInfo.getPictures().size()];
                for (int i = 0; i < goodsInfo.getPictures().size(); i++) {
                    photoUrls[i] = goodsInfo.getPictures().get(i).getUrl();
                }
                break;
            }
        }
        buyPPP();
    }

    private void buyPPP() {
        //调用addToCart API1
        API2.addToCart(pppGoodsInfo.getGoodsKey(), 1, true, null)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        int currentCartCount = SPUtils.getInt(MyPPPActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
                        SPUtils.put(MyPPPActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + 1);

                        String cartId = jsonObject.getString("cartId");
                        dismissPWProgressDialog();
                        //生成订单
                        Intent intent = new Intent(MyPPPActivity.this, SubmitOrderActivity.class);
                        ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();
                        CartItemInfo cartItemInfo = new CartItemInfo();
                        cartItemInfo.setCartId(cartId);
                        cartItemInfo.setProductName(pppGoodsInfo.getName());
                        cartItemInfo.setProductNameAlias(pppGoodsInfo.getNameAlias());
                        cartItemInfo.setUnitPrice(pppGoodsInfo.getPrice());
                        cartItemInfo.setEmbedPhotos(new ArrayList<CartPhotosInfo>());
                        cartItemInfo.setDescription(pppGoodsInfo.getDescription());
                        cartItemInfo.setQty(1);
                        cartItemInfo.setStoreId(pppGoodsInfo.getStoreId());
                        cartItemInfo.setPictures(photoUrls);
                        cartItemInfo.setPrice(pppGoodsInfo.getPrice());
                        cartItemInfo.setCartProductType(3);

                        orderinfoArrayList.add(cartItemInfo);
                        intent.putExtra("orderinfo", orderinfoArrayList);
                        startActivity(intent);
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void dissmissDialogAndShowToast() {
        dismissPWProgressDialog();
        newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
    }

    private void getAllGoods() {
        API2.getGoods()
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, jsonObject.toString(), ACache.TIME_DAY);
                        getGoodsSuccess(jsonObject.toString());
                    }

                    @Override
                    public void _onError(int status) {
                        dissmissDialogAndShowToast();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void bindPPsDateToPPP(JSONArray jsonArray, String pppCode) {
        API2.bindPPsDateToPPP(jsonArray, pppCode)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {

                        dismissPWProgressDialog();
                        if (API2.PPPlist.size() != 0) {
                            API2.PPPlist.clear(); // 绑定成功 之后 清空API中的数据。
                        }
                        if (AppManager.getInstance().checkActivity(SelectPPActivity.class)) { //SelectPPActivity，就把这个类杀掉。
                            AppManager.getInstance().killActivity(SelectPPActivity.class);
                        }

                        //设置需要刷新 （其实可以不需要，不过保证数据同步，加上更保险）
                        SPUtils.put(MyPPPActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.NEED_FRESH, true);
                        PictureAirDbManager.insertRefreshPPFlag(JSONArray.parseArray(ppsStr), JsonInfo.DAILY_PP_REFRESH_ALL_TYPE);

                        if (AppManager.getInstance().checkActivity(PreviewPhotoActivity.class)) { //如果存在MyPPActivity，就把这个类杀掉。
                            AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
                        }

                        if (AppManager.getInstance().checkActivity(ADVideoDetailProductActivity.class)) {//存在，是视频通过已有的ppp升级流程
                            AppManager.getInstance().killActivity(ADVideoDetailProductActivity.class);

                        } else {//照片通过ppp升级的流程
                            if (AppManager.getInstance().checkActivity(EditStoryAlbumActivity.class)) {
                                AppManager.getInstance().killActivity(EditStoryAlbumActivity.class);
                            }
                        }
                        finish();

                    }

                    @Override
                    public void _onError(int status) {
                        dissmissDialogAndShowToast();
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
    }
}
