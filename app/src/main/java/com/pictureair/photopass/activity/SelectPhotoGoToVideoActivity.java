package com.pictureair.photopass.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.SelectPhotoGoToVideoAdapter;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DisneyVideoTool;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.MyToast;

import java.util.ArrayList;

/**
 * 2种状态
 * 1:无图介绍页面
 * 2:有图选择图页面
 * （选择图片 发送到服务器）
 *
 * @author bass
 */

public class SelectPhotoGoToVideoActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener {
    private static final String TAG = "SelectPhotoGoToVideoAct";
    private MyApplication myApplication;
    private MyToast myToast;
    private Context context;
    //select photo
    private ImageView rtLayout;
    private TextView okButton;
    private PopupWindow popupWindow;

    private ArrayList<PhotoInfo> photoURLlist = new ArrayList<PhotoInfo>();// 选择的图片的list
    private ArrayList<PhotoInfo> allArrayList;//测试用
    private ArrayList<PhotoInfo> boughtArrayList;

    private SelectPhotoGoToVideoAdapter selectPhotoGoToVideoAdapter;
    private GridView gridView;
    //null photo
    private ImageView rl_back;
    private Button btnGoToSelectPhoto;
    private LinearLayout llNullPhoto;

    private final int photocountMax = 3;// 需要添加的图片数量，以后要改这个数值

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo_goto_video);
        context = this;
        initview();
        initData();
        initEvent();
        if (null != getIntent() && getIntent().getExtras().getBoolean(DisneyVideoTool.IS_BOUGHT)){
            llNullPhoto.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }else {
            gridView.setVisibility(View.GONE);
            llNullPhoto.setVisibility(View.VISIBLE);
        }
    }


    // 初始化函数
    private void initview() {
        myToast = new MyToast(context);
        myApplication = (MyApplication) getApplication();
        llNullPhoto = (LinearLayout)findViewById(R.id.ll_null_photo);
        okButton = (TextView) findViewById(R.id.btn_select_photo);
        rtLayout = (ImageView) findViewById(R.id.rlrt);
        gridView = (GridView) findViewById(R.id.gridView_select_photo);
        btnGoToSelectPhoto = (Button) findViewById(R.id.btn_goto_select);
        rl_back = (ImageView) findViewById(R.id.back1);

        // 初始化数据
        if (myApplication.needScanPhoto()) {// 如果刚进入这个页面，则onresume不需要重新扫描文件，所以直接将变量设为false，以免造成oncreate和onresume同时进行，造成collection数组越界
            myApplication.setneedScanPhoto(false);
        }
        okButton.setVisibility(View.VISIBLE);
        okButton.setText(String.format(getString(R.string.hasselectedphoto), 0, photocountMax));
    }

    private void initData() {
        // 初始化数据列表
        allArrayList = new ArrayList<PhotoInfo>();
        boughtArrayList = new ArrayList<PhotoInfo>();
        transferPhotoItemInfoToPhotoInfo();// 添加数据
//        System.out.println("===============DATA" + boughtArrayList.size());
//		selectPhotoGoToVideoAdapter = new SelectPhotoGoToVideoAdapter(context,
//				boughtArrayList);
        // selectPhotoGoToVideoAdapter.setGridView(gridView);

        //测试的时候选择allArrayList
        selectPhotoGoToVideoAdapter = new SelectPhotoGoToVideoAdapter(context, boughtArrayList);
    }

    private void initEvent() {
        okButton.setOnClickListener(this);
        rtLayout.setOnClickListener(this);

        gridView.setOnItemClickListener(this);
        gridView.setAdapter(selectPhotoGoToVideoAdapter);
        okButton.setEnabled(false);

        btnGoToSelectPhoto.setOnClickListener(this);
        rl_back.setOnClickListener(this);
    }

    /**
     * 将photoItemInfo的列表转成photoInfo的列表
     */
    private void transferPhotoItemInfoToPhotoInfo() {
        for (int i = 0; i < myApplication.allPicList.size(); i++) {
            for (int j = 0; j < myApplication.allPicList.get(i).list.size(); j++) {
                allArrayList.add(myApplication.allPicList.get(i).list.get(j));
                myApplication.allPicList.get(i).list.get(j).isChecked = 1;
                myApplication.allPicList.get(i).list.get(j).isSelected = 0;
            }
        }

        for (int i = 0; i < myApplication.boughtPicList.size(); i++) {
            for (int j = 0; j < myApplication.boughtPicList.get(i).list.size(); j++) {
                boughtArrayList.add(myApplication.boughtPicList.get(i).list
                        .get(j));
                myApplication.boughtPicList.get(i).list.get(j).isChecked = 1;
                myApplication.boughtPicList.get(i).list.get(j).isSelected = 0;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myApplication.needScanPhoto()) {// 需要刷新
            myApplication.setneedScanPhoto(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                popupWindow.dismiss();
                finish();
                break;
            case R.id.rlrt:// 返回按钮
                finish();
                break;

            case R.id.btn_select_photo:// 选择确定按钮
                sendPhoto();
                break;

            case R.id.btn_goto_select:
                //删除所有aty，只剩下mainTab页面，
                //将mainTab切换到shop Tab
                AppManager.getInstance().killOtherActivity(MainTabActivity.class);
                MainTabActivity.changeToShopTab = true;
            case R.id.back1:
                finish();
                break;

            default:
                break;
        }
    }

    private void sendPhoto() {
        int size = photoURLlist.size();
        if (size == 0) {
            myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
        } else {
            if (size < 3) {
                myToast.setTextAndShow(R.string.disney_video_select_photo_less_than3, Common.TOAST_SHORT_TIME);
                return;
            } else {
                if (AppUtil.getNetWorkType(context) == AppUtil.NETWORKTYPE_INVALID) {
                    myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
                    return;
                } else {
                    boolean isSendSuccess = DisneyVideoTool.photoSendServer(photoURLlist);
                    if (isSendSuccess) {
                        // 发送成功，等待服务器推送回来
                        initPopWindow();
                    } else {
                        // 处理失败，数据错误
                        PictureAirLog.e(TAG, "处理失败，数据错误");
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // 获取已经选择的照片数量
        int selectedCount = photoURLlist.size();
        PhotoInfo info = null;
        // 获取对应相册中的SelectPhotoItemInfo对象
        info = (PhotoInfo) parent.getItemAtPosition(position);
        // 判断数量
        if (selectedCount >= 0) {
            System.out.println(selectedCount + "current:" + position);
            if (info.isSelected == 1) {// 取消选中
                if (photoURLlist.contains(info)) {// 存在
                    photoURLlist.remove(info);
                }
                // info.isChecked = 1;
                info.isSelected = 0;
                selectedCount--;
//                System.out.println("点过了，取消选中");
                int visiblePos = gridView.getFirstVisiblePosition();
                selectPhotoGoToVideoAdapter.refreshView(position, gridView.getChildAt(position - visiblePos), 1);
            } else {
                if (selectedCount < photocountMax) {
                    info.isSelected = 1;
//                    System.out.println("没点过，选中");
                    selectedCount++;
                    int visiblePos = gridView.getFirstVisiblePosition();
                    selectPhotoGoToVideoAdapter.refreshView(position, gridView.getChildAt(position - visiblePos), 1);
                    if (photoURLlist.contains(info)) {// 说明已经存在
//                        Log.d(TAG, "之前点过了");
                        myToast.setTextAndShow(R.string.photo_selected, Common.TOAST_SHORT_TIME);
                    } else {
//                        Log.d(TAG, "没有点过，点中");
                        photoURLlist.add(info);
                    }
                } else {
                    myToast.setTextAndShow(String.format(context.getString(R.string.limit_photos), photocountMax), Common.TOAST_SHORT_TIME);
                }
            }
            okButton.setText(String.format(getString(R.string.hasselectedphoto), selectedCount, photocountMax));
//			myToast.setTextAndShow(""+selectedCount, Common.TOAST_SHORT_TIME);
        } else {
        }
        isEnabled();
    }

    private void isEnabled() {
        if (photoURLlist.size() == photocountMax) {
            okButton.setEnabled(true);
            okButton.setTextColor(getResources().getColor(R.color.white));
        } else {
            okButton.setEnabled(false);
            okButton.setTextColor(getResources().getColor(R.color.gray_light5));
        }
    }

    private void initPopWindow(){
        // 利用layoutInflater获得View
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popView = inflater.inflate(R.layout.popupwindow_disney_video_select_photo, null);

        popupWindow = new PopupWindow(popView,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(getResources().getColor(R.color.background_material_light));
        popupWindow.setBackgroundDrawable(dw);
        //设置popwindow出现和消失动画
        popupWindow.setAnimationStyle(R.style.from_bottom_anim);
        popupWindow.showAtLocation(okButton, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(false);
        Button btnSubmit = (Button) popView.findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(this);
//        popupWindow.showAtLocation(gridView, Gravity.BOTTOM, 100, 100);
        popupWindow.update();
    }

}
