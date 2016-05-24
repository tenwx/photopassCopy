package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.BindPPInfo;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PictureWorksDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;


/*
 * 显示用户所有的PP或某张PP+可绑定的PP
 * @author talon & milo
 */
public class MyPPActivity extends BaseActivity implements OnClickListener {
    private final String TAG = "MyPPActivity";
    private boolean isUseHavedPPP = false;
    private final int GET_SELECT_PP_SUCCESS = 2222;
    private final int REMOVE_PP_FROM_DB_FINISH = 3333;
    private ImageView back;
    private ListView listPP;
    private ImageView delete;
    private ListOfPPAdapter listPPAdapter;
    private ArrayList<PPinfo> showPPCodeList;// 需要显示的List

    private SharedPreferences sharedPreferences;
    private PictureAirDbManager pictureAirDbManager;

    private static final int UPDATE_UI = 10000;
    private static final int DELETE_PHOTO = 10001;
    private boolean isDeletePhoto = false;//是否是编辑状态
    private MyApplication myApplication;
    private int selectedCurrent = -1;
    private int selectedTag = -1;
    private String selectedPhotoId = null;//记录已经购买了的照片的photoId

    private NoNetWorkOrNoCountView netWorkOrNoCountView;
    private RelativeLayout noPhotoPassView;
    private CustomProgressDialog customProgressDialog;

    private TextView tvTitle;

    //selectPP 中需要的。
    private TextView ok;
    private PPPinfo dppp;
    private ArrayList<PhotoInfo> tempPhotoLists; //保存选中的 pp。 （准备升级PP＋的pp）
    private CustomProgressDialog dialog;
    private MyToast myToast;
    private SettingUtil settingUtil;

    private PictureWorksDialog pictureWorksDialog;

    private final Handler myPPHandler = new MyPPHandler(this);


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
            case UPDATE_UI:
                PictureAirLog.out("update ui----->");
                showPPCodeList = pictureAirDbManager.getPPCodeInfo1ByPPCodeList(showPPCodeList, 1);// 根据条码从数据库获取图片
                PictureAirLog.out("pp code size --->" + showPPCodeList.size());
                // 更新界面  查看pp页面
                if (showPPCodeList != null && showPPCodeList.size() > 0) {
                    PictureAirLog.out("has ppcode ");
                    if (!isDeletePhoto) {
                        delete.setVisibility(View.VISIBLE);
                    }
                    listPP.setVisibility(View.VISIBLE);
                    noPhotoPassView.setVisibility(View.GONE);
                    listPPAdapter.refresh(showPPCodeList, isDeletePhoto);
                } else {
                    PictureAirLog.out("has not pp code");
                    showPPCodeList.clear();
                    listPPAdapter.refresh(showPPCodeList, isDeletePhoto);
                    delete.setVisibility(View.GONE);
                    listPP.setVisibility(View.INVISIBLE);
                    noPhotoPassView.setVisibility(View.VISIBLE);
                }
                if (customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
                break;

            case API1.REMOVE_PP_SUCCESS:
                //请求删除API成功 更新界面
                if (showPPCodeList != null && showPPCodeList.size() > 0) {
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

            case API1.REMOVE_PP_FAILED:
                if (customProgressDialog.isShowing())
                    customProgressDialog.dismiss();
                // 请求删除API失败
                myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case REMOVE_PP_FROM_DB_FINISH://数据库更新完毕之后
                listPPAdapter.refresh(showPPCodeList, isDeletePhoto);
                if (customProgressDialog.isShowing())
                    customProgressDialog.dismiss();
                break;

            case DELETE_PHOTO:
                //更新界面
                if (showPPCodeList != null && showPPCodeList.size() >= 0) {
                    listPPAdapter.refresh(showPPCodeList, isDeletePhoto);
                }
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
                customProgressDialog.dismiss();
                netWorkOrNoCountView.setVisibility(View.VISIBLE);
                netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, myPPHandler, true);
                noPhotoPassView.setVisibility(View.GONE);
                listPP.setVisibility(View.INVISIBLE);


                break;
            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                customProgressDialog = CustomProgressDialog.show(MyPPActivity.this, getString(R.string.is_loading), false, null);
                API1.getPPSByUserId(myPPHandler);
                break;

            // seletePP的页面
            case GET_SELECT_PP_SUCCESS:
                listPPAdapter = new ListOfPPAdapter(showPPCodeList, MyPPActivity.this, null, true, isDeletePhoto, myPPHandler, dppp);
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
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                break;

//                case API1.FAILURE://连接失败
//                    if (msg.obj.toString().equals("PPHasUpgraded")) {//提示已经绑定
//                        myToast.setTextAndShow(R.string.select_pp_hasUpgraded, Common.TOAST_SHORT_TIME);
//                    } else {//获取失败
//                        myToast.setTextAndShow(R.string.select_bind_pp_faile, Common.TOAST_SHORT_TIME);
//                    }
//                    break;

            case API1.BIND_PPS_DATE_TO_PP_SUCESS://绑定成功

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Common.NEED_FRESH, true);
                editor.commit();
                ((MyApplication) getApplication()).setNeedRefreshPPPList(true);
//                    goIntent(); //备注。
//                     tips。如果绑定成功。
                if (settingUtil.isFirstTipsSyns(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
                    //如果没有设置过。
                    if (settingUtil.isAutoUpdate(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
                        if (AppUtil.getNetWorkType(MyPPActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
                            downloadPhotoList(); // 下载。
                        }
                        goIntent();
                    } else {
                        new CustomDialog(MyPPActivity.this, R.string.first_tips_syns_msg2, R.string.first_tips_syns_no_msg2, R.string.first_tips_syns_yes_msg2, new CustomDialog.MyDialogInterface() {

                            @Override
                            public void yes() {
                                // TODO Auto-generated method stub 同步更新：下载照片。
                                settingUtil.insertSettingAutoUpdateStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                                if (AppUtil.getNetWorkType(MyPPActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
                                    downloadPhotoList();
                                }
                                goIntent();
                            }

                            @Override
                            public void no() {
                                // TODO Auto-generated method stub 取消：不操作
                                settingUtil.deleteSettingAutoUpdateStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                                goIntent();
                            }
                        });
                    }
                    settingUtil.insertSettingFirstTipsSynsStatus(sharedPreferences.getString(Common.USERINFO_ID, ""));
                } else {
                    if (settingUtil.isAutoUpdate(sharedPreferences.getString(Common.USERINFO_ID, ""))) {
                        // 下载。
                        if (AppUtil.getNetWorkType(MyPPActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
                            downloadPhotoList();
                        }
                    }
                    goIntent();
                }
                break;

            case API1.BIND_PPS_DATE_TO_PP_FAILED: //绑定失败。
                dialog.dismiss();
                myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
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
        isUseHavedPPP =  getIntent().getBooleanExtra("isUseHavedPPP",false);
        if (isUseHavedPPP){
            initView_selectPP1();
            tvTitle.setText(R.string.selectionpp);  //选择PP界面
        }else{
            dppp = getIntent().getParcelableExtra("ppp");
            if (dppp != null) {
                initView_selectPP();
                tvTitle.setText(R.string.selectionpp);  //选择PP界面
            } else {
                initView_notSelectPP();
                tvTitle.setText(R.string.mypage_pp); // PP 界面。
            }
        }

    }

    private void initView() {
        pictureAirDbManager = new PictureAirDbManager(this);
        settingUtil = new SettingUtil(pictureAirDbManager);
        myToast = new MyToast(this);
        sharedPreferences = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);
        listPP = (ListView) findViewById(R.id.list_pp);
        tvTitle = (TextView) findViewById(R.id.mypp);
        back = (ImageView) findViewById(R.id.back);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
        noPhotoPassView = (RelativeLayout) findViewById(R.id.no_photo_relativelayout);
        back.setOnClickListener(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Common.IS_DELETED_PHOTO_FROM_PP, false);
        editor.commit();
    }


    /**
     * 使用已有的迪斯尼乐拍通一卡通  情况下
     */
    private void initView_selectPP1(){
        String photoPassCode = this.getIntent().getStringExtra("photoPassCode");
        final String shootTime = this.getIntent().getStringExtra("shootTime");
        String[] photoCode = null;
        if (photoPassCode.contains(",")){
            photoCode = photoPassCode.split(",");
        }
        ok = (TextView) findViewById(R.id.ok);
        ok.setVisibility(View.VISIBLE);
        ok.setOnClickListener(this);
        ok.setText(formaStringPPP(0, 1));
        ok.setEnabled(false);
        ok.setTextColor(getResources().getColor(R.color.gray_light5));
        final String[] finalPhotoCode = photoCode;
        new Thread() {
            public void run() {
                ArrayList<PPinfo> PPlist = new ArrayList<PPinfo>(); //创造一个List。
                for (int i = 0; i< finalPhotoCode.length; i++){
                    PPinfo pPinfo = new PPinfo();
                    pPinfo.setPpCode(finalPhotoCode[i]);
                    pPinfo.setShootDate(shootTime);
                    PPlist.add(pPinfo);
                }
                showPPCodeList = pictureAirDbManager.getPPCodeInfo1ByPPCodeList(PPlist, 2);

                dppp = new PPPinfo();
                dppp.capacity = 1;
                dppp.bindInfo = new ArrayList<BindPPInfo>();

                myPPHandler.sendEmptyMessage(GET_SELECT_PP_SUCCESS);
            }
        }.start();
    }

    private void initView_selectPP() {
        ok = (TextView) findViewById(R.id.ok);
        ok.setVisibility(View.VISIBLE);
        ok.setOnClickListener(this);
        ok.setText(formaStringPPP(dppp.bindInfo.size(), dppp.capacity));
        ok.setEnabled(false);
        ok.setTextColor(getResources().getColor(R.color.gray_light5));
        getPhotoUrlFromDatabase();
    }

    private void initView_notSelectPP() {
        PictureAirLog.out("not select pp");
        delete = (ImageView) findViewById(R.id.cancel);
        myApplication = (MyApplication) getApplication();
        delete.setOnClickListener(this);
        // 获取PP信息
        customProgressDialog = CustomProgressDialog.show(MyPPActivity.this, getString(R.string.is_loading), false, null);
        API1.getPPSByUserId(myPPHandler);
        // pPCodeList = getIntent().getParcelableArrayListExtra("pPCodeList");
        showPPCodeList = new ArrayList<PPinfo>();
        listPPAdapter = new ListOfPPAdapter(showPPCodeList, MyPPActivity.this,
                new ListOfPPAdapter.doDeletePhotoListener() {

                    @Override
                    public void deletePhoto(int position) {
                        // TODO Auto-generated method stub
                        deleteAPI(position);// 提交删除PP
                    }
                }, false, isDeletePhoto, null, null);

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
                if (isDeletePhoto) {
                    isDeletePhoto = false;
                    // 在删除状态下返回，不提交数据
                    back.setImageResource(R.drawable.back_white);
                    delete.setVisibility(View.VISIBLE);
                    PictureAirLog.d("===========", "取消删除......");
                    listPPAdapter.refresh(showPPCodeList, isDeletePhoto);
                } else {
                    finish();
                }

                break;

            case R.id.cancel:// 删除按钮
                PictureAirLog.d("==============",
                        "点击删除按钮 showPPCodeList" + showPPCodeList.size());
                if (isDeletePhoto) {
                    isDeletePhoto = false;
                    back.setImageResource(R.drawable.back_white);
                    delete.setVisibility(View.VISIBLE);
                    updateUI(DELETE_PHOTO);
                } else {
                    if (showPPCodeList.size() == 0) {
                        return;
                    }
                    isDeletePhoto = true;
                    back.setImageResource(R.drawable.cancel_my_pp);
                    delete.setVisibility(View.GONE);
                    updateUI(DELETE_PHOTO);
                }
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
                JSONArray pps = new JSONArray();
                tempPhotoLists = new ArrayList<>();
                //			String binddate = null;
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
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        pps.add(jsonObject);
                    }
                }
                if (AppUtil.getGapCount(pps.getJSONObject(0).getString("bindDate"),
                        pps.getJSONObject(pps.size() - 1).getString("bindDate")) > 3){
                    pictureWorksDialog = new PictureWorksDialog(MyPPActivity.this, null,
                            getString(R.string.select_pp_wrong_date), null, getString(R.string.button_ok), true, myPPHandler);
                    pictureWorksDialog.show();
                } else {
                    if(isUseHavedPPP){
                        Intent intent = new Intent(MyPPActivity.this, MyPPPActivity.class);
                        intent.putExtra("ppsStr",pps.toString());
                        intent.putExtra("isUseHavedPPP", true);
                        startActivity(intent);
                        this.finish();
                    }else{
                        dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), true, null);
                        API1.bindPPsDateToPPP(pps, dppp.PPPCode, myPPHandler);
                    }
                }

                break;
            default:
                break;
        }
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (!isUseHavedPPP && dppp == null) {
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

        if (sharedPreferences.getBoolean(Common.IS_DELETED_PHOTO_FROM_PP, false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Common.IS_DELETED_PHOTO_FROM_PP, false);
            editor.commit();
            if (!customProgressDialog.isShowing()) {
                customProgressDialog.show();
            }
            updateUI(UPDATE_UI);
        }
    }

    //处理解析结果，并且从数据库中获取照片信息，新开线程，防止阻塞主线程
    private void getPhotoUrlFromDatabase() {
        new Thread() {
            public void run() {
//                PictureAirLog.e("","API1.PPlist:"+API1.PPlist.size());
                showPPCodeList = pictureAirDbManager.getPPCodeInfo1ByPPCodeList(API1.PPlist, 2);
//                PictureAirLog.e("","showPPCodeList.size():"+showPPCodeList.size());
                myPPHandler.sendEmptyMessage(GET_SELECT_PP_SUCCESS);
            }
        }.start();
    }

    // 请求删除API
    public boolean deleteAPI(final int position) {
        if (showPPCodeList == null || showPPCodeList.size() <= 0) {
            return false;
        }
        new CustomDialog(MyPPActivity.this, R.string.delete_pp, R.string.delete_pp_cancel, R.string.delete_pp_ok, new CustomDialog.MyDialogInterface() {

            @Override
            public void yes() {
                if (!customProgressDialog.isShowing())
                    customProgressDialog.show();
                API1.removePPFromUser(showPPCodeList.get(position).getPpCode(), position, myPPHandler);
            }

            @Override
            public void no() {

            }
        });
        return false;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        myPPHandler.removeCallbacksAndMessages(null);
    }


    private String formaStringPPP(int count1, int count2) {
        return String.format(getString(R.string.pp_ok), count1, count2);
    }

    //下载照片
    private void downloadPhotoList() {
        if (tempPhotoLists.size() > 0) {
            for (int i = 0; i < tempPhotoLists.size(); i++) {
                download(pictureAirDbManager.getPhotoUrlByPhotoIDAndShootOn(tempPhotoLists.get(i).photoId, tempPhotoLists.get(i).shootTime));
            }
        }
        goIntent();
    }

    private void download(ArrayList<PhotoInfo> arrayList) {
        if (arrayList.size() > 0) {
            Intent intent = new Intent(MyPPActivity.this,
                    DownloadService.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("photos", arrayList);
            intent.putExtras(bundle);
            startService(intent);
        }
    }

    private void goIntent() {
        Intent intent = new Intent(MyPPActivity.this, MyPPPActivity.class);
        API1.PPPlist.clear();
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }


}
