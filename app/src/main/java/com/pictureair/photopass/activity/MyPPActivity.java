package com.pictureair.photopass.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.ListOfPPAdapter;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.ScanInfoEvent;
import com.pictureair.photopass.eventbus.SocketEvent;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PPPPop;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;


/*
 * 显示用户所有的PP或某张PP+可绑定的PP
 * @author talon & milo
 */
public class MyPPActivity extends BaseActivity implements OnClickListener, PWDialog.OnPWDialogClickListener {
    private final String TAG = "MyPPActivity";
    private final int GET_SELECT_PP_SUCCESS = 2222;
    private final int REMOVE_PP_FROM_DB_FINISH = 3333;
    private static final int BIND_TIP_DIALOG = 5555;
    private static final int WRONG_DATE_DIALOG = 6666;
    private static final int DELETE_API_DIALOG = 7777;
    private static final int DEAL_PP_RESULT_DIALOG = 8888;

    private ImageView back;
    private ListView listPP;
    private ImageView delete;
    private RelativeLayout menuLayout;
    private ListOfPPAdapter listPPAdapter;
    private ArrayList<PPinfo> showPPCodeList;// 需要显示的List

    private PictureAirDbManager pictureAirDbManager;

    private static final int UPDATE_UI = 10000;
    private boolean needNotifyStoryRefresh = false;
    private JSONArray pps;
    private MyApplication myApplication;
    private int selectedCurrent = -1;
    private int selectedTag = -1;
    private String selectedPhotoId = null;//记录已经购买了的照片的photoId
    private int deletePosition;

    private NoNetWorkOrNoCountView netWorkOrNoCountView;
    private RelativeLayout noPhotoPassView;

    private TextView tvTitle;

    //selectPP 中需要的。
    private TextView ok;
    private PPPinfo dppp;
    private ArrayList<PhotoInfo> tempPhotoLists; //保存选中的 pp。 （准备升级PP＋的pp）
    private PWToast myToast;

    private PWDialog pictureWorksDialog;

    private PPPPop pppPop;

    private final Handler myPPHandler = new MyPPHandler(this);

    private ScanInfoEvent scanInfoEvent;

    private static final int SCAN_PP_CODE_SUCCESS = 111;

    private static final int SAVE_JSON_DONE = 222;

    private boolean isOnResume = false;

    private static class MyPPHandler extends Handler{
        private final WeakReference<MyPPActivity> mActivity;

        public MyPPHandler(MyPPActivity activity){
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
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case API1.GET_ALL_PHOTOS_BY_CONDITIONS_SUCCESS://获取图片信息成功
                /**
                 * 1.根据code，先从网络获取图片信息，存入数据库
                 * 2.获取最新的pp列表
                 */
                PictureAirLog.out("get photos---->" + msg.obj.toString());
                saveJsonToSQLite((JSONObject)msg.obj);
                break;

            case SAVE_JSON_DONE:
                PictureAirLog.out("save json done----> start ");
                API1.getPPSByUserId(myPPHandler);
                break;

            case UPDATE_UI:
                PictureAirLog.out("update ui----->");
                showPPCodeList = pictureAirDbManager.getPPCodeInfo1ByPPCodeList(this, showPPCodeList, 1);// 根据条码从数据库获取图片
                PictureAirLog.out("pp code size --->" + showPPCodeList.size());
                // 更新界面  查看pp页面
                if (showPPCodeList != null && showPPCodeList.size() > 0) {
                    PictureAirLog.out("has ppcode ");
                    listPP.setVisibility(View.VISIBLE);
                    noPhotoPassView.setVisibility(View.GONE);
                    listPPAdapter.refresh(showPPCodeList, true);
                } else {
                    PictureAirLog.out("has not pp code");
                    showPPCodeList.clear();
                    listPPAdapter.refresh(showPPCodeList, true);
                    delete.setVisibility(View.GONE);
                    listPP.setVisibility(View.INVISIBLE);
                    noPhotoPassView.setVisibility(View.VISIBLE);
                }
                dismissPWProgressDialog();
                break;

            case API1.REMOVE_PP_SUCCESS:
                //请求删除API成功 更新界面
                if (showPPCodeList != null && showPPCodeList.size() > 0) {
                    needNotifyStoryRefresh = true;
                    final int deletePosition = (int) msg.obj;
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            pictureAirDbManager.removePhotosFromUserByPPCode(deletePosition, showPPCodeList);
                            showPPCodeList.remove(deletePosition);
                            myPPHandler.sendEmptyMessage(REMOVE_PP_FROM_DB_FINISH);
                        }
                    }.start();
                }
                break;

            case API1.GET_ALL_PHOTOS_BY_CONDITIONS_FAILED://获取图片失败
            case API1.REMOVE_PP_FAILED:
                dismissPWProgressDialog();
                // 请求删除API失败
                myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case REMOVE_PP_FROM_DB_FINISH://数据库更新完毕之后
                listPPAdapter.refresh(showPPCodeList, true);
                dismissPWProgressDialog();
                break;

            case API1.GET_PPS_SUCCESS:// 获取pp列表成功。 如果status等于 200 的时候
                JSONObject ppsJsonObject = (JSONObject) msg.obj;
                PictureAirLog.e("","ppsJsonObject:"+ppsJsonObject.toString());
                if (ppsJsonObject.containsKey("PPList")) {
                    showPPCodeList.clear();
                    try {
                        JSONArray pplists = ppsJsonObject
                                .getJSONArray("PPList");
                        PPinfo ppCodeInfo = null;
                        String ppcode = null;
                        int isupgrade = 0;
                        boolean createnew = false;
                        // 遍历所有pplist，如果有重复的pp，isUpgrade属性取升级过的值，图片选最新的图片(如果图片数量为零，就不查找)
                        for (int i = 0; i < pplists.size(); i++) {
                            JSONObject pplist = pplists.getJSONObject(i);
                            ppcode = pplist.getString("customerId");
                            isupgrade = pplist.getIntValue("isUpgrade");
                            createnew = false;
                            // 查看是否有重复的ppcode，需要更新isupgrade和图片属性
                            for (int j = 0; j < showPPCodeList.size(); j++) {
                                if (ppcode.equals(showPPCodeList.get(j).getPpCode())) {
                                    createnew = true;
                                    ppCodeInfo = showPPCodeList.get(j);
                                    ppCodeInfo.setShootDate(pplist.getString("shootDate")); //new add 取最新的时间，解决PP排序问题。
                                    if (ppCodeInfo.getIsUpgrade() == 1) {

                                    } else {
                                        if (isupgrade == 1) {
                                            ppCodeInfo.setIsUpgrade(1);
                                            ppCodeInfo.setPhotoCount(ppCodeInfo.getPhotoCount() + pplist.getIntValue("photoCount"));
                                            PictureAirLog.out("changing------------");
                                        }
                                    }
                                    break;
                                }
                            }
                            if (!createnew) {
                                ppCodeInfo = new PPinfo();
                                ppCodeInfo.setPpCode(pplist
                                        .getString("customerId"));
                                ppCodeInfo.setPhotoCount(pplist.getIntValue("photoCount"));
                                ppCodeInfo.setIsUpgrade(pplist.getIntValue("isUpgrade"));
                                ppCodeInfo.setShootDate(pplist
                                        .getString("shootDate"));
                                ppCodeInfo.setIsHidden(pplist.getIntValue("isHidden"));
                                showPPCodeList.add(ppCodeInfo);
                            }
                        }
//                        Collections.sort(showPPCodeList, new PPinfoSortUtil());  //取消排序，由服务器返回的结果为准。
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                updateUI(UPDATE_UI);
                netWorkOrNoCountView.setVisibility(View.GONE);
                break;

            case API1.GET_PPS_FAILED:// 获取pp列表失败
//                myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                dismissPWProgressDialog();
                netWorkOrNoCountView.setVisibility(View.VISIBLE);
                netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, myPPHandler, true);
                noPhotoPassView.setVisibility(View.GONE);
                listPP.setVisibility(View.INVISIBLE);


                break;
            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                showPWProgressDialog();
                API1.getPPSByUserId(myPPHandler);
                break;

            // seletePP的页面
            case GET_SELECT_PP_SUCCESS:
                listPPAdapter = new ListOfPPAdapter(showPPCodeList, MyPPActivity.this, null, true, true, myPPHandler, dppp);
                listPP.setAdapter(listPPAdapter);

                if (showPPCodeList.size() == 0) {
                    ok.setEnabled(false);
                    ok.setTextColor(getResources().getColor(R.color.gray_light5));
                    PictureAirLog.out("has not pp code");
                    listPP.setVisibility(View.INVISIBLE);
                    noPhotoPassView.setVisibility(View.VISIBLE);
                } else {
                    noPhotoPassView.setVisibility(View.GONE);
                }
                dismissPWProgressDialog();
                break;
            case 2:
                ok.setText(formaStringPPP(msg.arg1, dppp.capacity));
                if (msg.arg1 - dppp.bindInfo.size() == 0) {
                    ok.setEnabled(false);
                    ok.setTextColor(getResources().getColor(R.color.gray_light5));
                } else {
                    ok.setEnabled(true);
                    ok.setTextColor(getResources().getColor(R.color.white));
                }
                break;

            case API1.BIND_PP_FAILURE://网络获取失败
                if (msg.obj.toString().equals("PPHasUpgraded")) {//提示已经绑定
                    myToast.setTextAndShow(R.string.select_pp_hasUpgraded, Common.TOAST_SHORT_TIME);
                } else {//获取失败
                    myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    listPP.setVisibility(View.GONE);
                }
                dismissPWProgressDialog();
                break;

            case API1.BIND_PPS_DATE_TO_PP_SUCESS://绑定成功
                SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.NEED_FRESH, true);
                ((MyApplication) getApplication()).setNeedRefreshPPPList(true);
                break;

            case API1.BIND_PPS_DATE_TO_PP_FAILED: //绑定失败。
                dismissPWProgressDialog();
                myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case PPPPop.POP_SCAN://扫描
                Intent intent = new Intent(MyPPActivity.this, MipCaptureActivity.class);
                intent.putExtra("type", "pp");//只扫描pp
                startActivity(intent);
                if (pppPop.isShowing()) {
                    pppPop.dismiss();
                }
                break;

            case PPPPop.POP_INPUT://手动输入
                Intent intent1 = new Intent(MyPPActivity.this, InputCodeActivity.class);
                intent1.putExtra("type", "pp");//只扫描pp
                startActivity(intent1);
                if (pppPop.isShowing()) {
                    pppPop.dismiss();
                }
                break;

            case SCAN_PP_CODE_SUCCESS:
                if (isOnResume) {
                    dealPPresult();
                } else {
                    myPPHandler.sendEmptyMessageDelayed(SCAN_PP_CODE_SUCCESS, 50);
                }
                break;

            default:
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pp);
        initView();
        dppp = getIntent().getParcelableExtra("ppp");
        if (dppp != null) {
            initView_selectPP();
            tvTitle.setText(R.string.selectionpp);  //选择PP界面
        } else {
            initView_notSelectPP();
            tvTitle.setText(R.string.mypage_pp); // PP 界面。
        }

    }

    private void initView() {
        pictureAirDbManager = new PictureAirDbManager(this);
        myToast = new PWToast(this);
        listPP = (ListView) findViewById(R.id.list_pp);
        tvTitle = (TextView) findViewById(R.id.mypp);
        back = (ImageView) findViewById(R.id.back);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
        noPhotoPassView = (RelativeLayout) findViewById(R.id.no_photo_relativelayout);
        back.setOnClickListener(this);
        SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.IS_DELETED_PHOTO_FROM_PP, false);
        pictureWorksDialog = new PWDialog(this)
                .setOnPWDialogClickListener(this)
                .pwDialogCreate();
    }

    private void initView_selectPP() {
        ok = (TextView) findViewById(R.id.ok);
        ok.setVisibility(View.VISIBLE);
        ok.setOnClickListener(this);
        ok.setText(formaStringPPP(dppp.bindInfo.size(), dppp.capacity));
        ok.setEnabled(false);
        ok.setTextColor(getResources().getColor(R.color.gray_light5));
        showPWProgressDialog();
        View view = LayoutInflater.from(this).inflate(R.layout.pp_header, null);
        listPP.addHeaderView(view);
        getPhotoUrlFromDatabase();
    }

    private void initView_notSelectPP() {
        PictureAirLog.out("not select pp");
        delete = (ImageView) findViewById(R.id.cancel);
        pppPop = new PPPPop(this, myPPHandler, PPPPop.MENU_TYPE_PP);
        menuLayout = (RelativeLayout) findViewById(R.id.pp_rl);
        menuLayout.setVisibility(View.VISIBLE);
        myApplication = (MyApplication) getApplication();
        menuLayout.setOnClickListener(this);
        // 获取PP信息
        showPWProgressDialog();
        API1.getPPSByUserId(myPPHandler);
        // pPCodeList = getIntent().getParcelableArrayListExtra("pPCodeList");
        showPPCodeList = new ArrayList<PPinfo>();
        listPPAdapter = new ListOfPPAdapter(showPPCodeList, MyPPActivity.this,
                new ListOfPPAdapter.doDeletePhotoListener() {

                    @Override
                    public void deletePhoto(int position) {
                        // TODO Auto-generated method stub
                        deleteAPI();// 提交删除PP
                        deletePosition = position;
                    }
                }, false, true, null, null);

        listPP.setAdapter(listPPAdapter);
        if (showPPCodeList == null || showPPCodeList.size() <= 0) {
            PictureAirLog.out(" no select pp ---> return");
            return;
        }
        PictureAirLog.out(" no select pp ---> not return");

        updateUI(UPDATE_UI);
    }

    // 更新界面
    public void updateUI(int what) {
        Message message = myPPHandler.obtainMessage(what);
        myPPHandler.sendMessage(message);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:// 返回按钮
                finish();
                break;

            case R.id.pp_rl:
                pppPop.showAsDropDown(delete, 0, ScreenUtil.dip2px(MyPPActivity.this, 15) - 10);
                break;

            //  seletePP 界面的 点击事件
            case R.id.ok://确认绑定按钮
                HashMap<Integer, Boolean> map = listPPAdapter.getMap();
                if (map.size() == 0) {
                    myToast.setTextAndShow(R.string.select_your_pp, Common.TOAST_SHORT_TIME);
                    return;
                }
                for (int i = 0; i < map.size(); i++) {
                    PictureAirLog.out("->" + map.get(i));
                }
                pps = new JSONArray();
                tempPhotoLists = new ArrayList<>();
                String selectedString = "";
                for (int j = 0; j < showPPCodeList.size(); j++) {
                    JSONObject jsonObject = new JSONObject();
                    if (null != map.get(j) && map.get(j)) {
                        try {
                            PhotoInfo photoInfo = new PhotoInfo();
                            jsonObject.put("code", showPPCodeList.get(j).getPpCode());
                            jsonObject.put("bindDate", showPPCodeList.get(j).getShootDate());
                            photoInfo.photoId = showPPCodeList.get(j).getPpCode();
                            photoInfo.shootTime = showPPCodeList.get(j).getShootDate();
                            tempPhotoLists.add(photoInfo);
                            selectedString += String.format(getString(R.string.select_pp), showPPCodeList.get(j).getPpCode(), showPPCodeList.get(j).getShootDate());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        pps.add(jsonObject);
                    }
                }

                pictureWorksDialog.setPWDialogId(BIND_TIP_DIALOG)
                        .setPWDialogMessage(String.format(getString(R.string.select_pp_right_date), selectedString))
                        .setPWDialogNegativeButton(R.string.button_cancel)
                        .setPWDialogPositiveButton(R.string.button_ok)
                        .setPWDialogContentCenter(false)
                        .pwDilogShow();

                break;
            default:
                break;
        }
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        isOnResume = true;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (dppp == null) {
            PictureAirLog.out("MyPPActivity----->" + myApplication.getRefreshViewAfterBuyBlurPhoto());
            if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASSPAYED)) {
                PictureAirLog.out("deal data after bought photo");
                myApplication.setRefreshViewAfterBuyBlurPhoto("");
                //找到之前选择的图片的索引值，并且更新购买信息
                showPPCodeList.get(selectedCurrent).getSelectPhotoItemInfos().get(selectedTag).isPayed = 1;
                selectedCurrent = -1;
                selectedTag = -1;
                listPPAdapter.notifyDataSetChanged();
                //根据photoId，更新数据库中的字段
                pictureAirDbManager.updatePhotoBought(selectedPhotoId, false);
                selectedPhotoId = null;
            }
        }

        if (SPUtils.getBoolean(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.IS_DELETED_PHOTO_FROM_PP, false)) {
            needNotifyStoryRefresh = true;
            SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.IS_DELETED_PHOTO_FROM_PP, false);
            showPWProgressDialog();
            updateUI(UPDATE_UI);
        }
    }

    //处理解析结果，并且从数据库中获取照片信息，新开线程，防止阻塞主线程
    private void getPhotoUrlFromDatabase() {
        new Thread() {
            public void run() {
//                PictureAirLog.e("","API1.PPlist:"+API1.PPlist.size());
                showPPCodeList = pictureAirDbManager.getPPCodeInfo1ByPPCodeList(MyPPActivity.this, API1.PPlist, 2);
//                PictureAirLog.e("","showPPCodeList.size():"+showPPCodeList.size());
                myPPHandler.sendEmptyMessage(GET_SELECT_PP_SUCCESS);
            }
        }.start();
    }

    // 请求删除API
    public boolean deleteAPI() {
        if (showPPCodeList == null || showPPCodeList.size() <= 0) {
            return false;
        }

        pictureWorksDialog.setPWDialogId(DELETE_API_DIALOG)
                .setPWDialogMessage(R.string.delete_pp)
                .setPWDialogNegativeButton(R.string.delete_pp_cancel)
                .setPWDialogPositiveButton(R.string.delete_pp_ok)
                .setPWDialogContentCenter(true)
                .pwDilogShow();

        return false;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (needNotifyStoryRefresh) {
            needNotifyStoryRefresh = false;
            PictureAirLog.out("need notify story to refresh");
            EventBus.getDefault().post(new SocketEvent(false, -1, null, null, null));
        }

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        myPPHandler.removeCallbacksAndMessages(null);
    }

    private String formaStringPPP(int count1, int count2) {
        return String.format(getString(R.string.pp_ok), count1, count2);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        isOnResume = false;
    }

    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        if (baseBusEvent instanceof ScanInfoEvent) {
            scanInfoEvent = (ScanInfoEvent) baseBusEvent;
            String result = scanInfoEvent.getResult();
            PictureAirLog.out("mypp----->" + result);
            if (scanInfoEvent.getCodeType().equals("pp")) {
                myPPHandler.sendEmptyMessageDelayed(SCAN_PP_CODE_SUCCESS, 50);
            }
            EventBus.getDefault().removeStickyEvent(scanInfoEvent);
        }
    }

    /**
     * 处理扫描得到的pp
     */
    private void dealPPresult() {
        String pppResultStr = scanInfoEvent.getResult();
        String errorMessage = null;
        if (pppResultStr.contains("ppOK")) {//ppp绑定成功，需要重新获取ppp信息
            getPPList(pppResultStr);
            return;

        } else if (pppResultStr.equals("failed")) {//扫描失败
            int id = scanInfoEvent.getErrorType();
            PictureAirLog.v(TAG, "------>" + id);
            switch (id) {
                case R.string.http_error_code_6055:
                    errorMessage = getString(R.string.not_pp_card);
                    break;

                default:
                    errorMessage = getString(id);
                    break;
            }

        } else if (pppResultStr.equals("notSame")) {//卡片类型不一致
            errorMessage = getString(R.string.not_pp_card);

        }

        pictureWorksDialog.setPWDialogId(DEAL_PP_RESULT_DIALOG)
                .setPWDialogMessage(errorMessage)
                .setPWDialogNegativeButton(null)
                .setPWDialogPositiveButton(R.string.dialog_ok1)
                .setPWDialogContentCenter(true)
                .pwDilogShow();
    }

    /**
     * 获取pp数据
     * 1.根据code，先从网络获取图片信息，存入数据库
     * 2.获取最新的pp列表
     */
    private void getPPList(String code) {
        showPWProgressDialog();

        API1.getPhotosByConditions(MyApplication.getTokenId(), myPPHandler, null, code.replace("ppOK", ""));
    }

    /**
     * 解析服务器返回的数据
     *
     * @param jsonObject json对象
     */
    private void saveJsonToSQLite(final JSONObject jsonObject) {
        PictureAirLog.out("start save json");
        new Thread() {
            public void run() {
                PictureAirLog.out("start save json in thread");
                final JSONArray responseArray = jsonObject.getJSONArray("photos");
                pictureAirDbManager.insertPhotoInfoIntoPhotoPassInfo(responseArray, false);
                //通知已经处理完毕
                myPPHandler.sendEmptyMessage(SAVE_JSON_DONE);
            }
        }.start();
    }

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            switch (dialogId) {
                case BIND_TIP_DIALOG:
                    if (AppUtil.getGapCount(pps.getJSONObject(0).getString("bindDate"),
                            pps.getJSONObject(pps.size() - 1).getString("bindDate")) > 3){
                        pictureWorksDialog.setPWDialogId(WRONG_DATE_DIALOG)
                                .setPWDialogMessage(R.string.select_pp_wrong_date)
                                .setPWDialogNegativeButton(null)
                                .setPWDialogPositiveButton(R.string.button_ok)
                                .setPWDialogContentCenter(true)
                                .pwDilogShow();
                    } else {
                        showPWProgressDialog();
                        API1.bindPPsDateToPPP(pps, dppp.PPPCode, myPPHandler);
                    }
                    break;

                case DELETE_API_DIALOG:
                    showPWProgressDialog();
                    UmengUtil.onEvent(MyPPActivity.this,Common.EVENT_ONCLICK_DEL_PP); //友盟统计
                    API1.removePPFromUser(showPPCodeList.get(deletePosition).getPpCode(), deletePosition, myPPHandler);
                    break;

                default:
                    break;
            }
        }
    }
}
