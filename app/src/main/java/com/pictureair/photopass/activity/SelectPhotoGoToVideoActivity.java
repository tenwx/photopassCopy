package com.pictureair.photopass.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.SelectPhotoGoToVideoAdapter;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DisneyVideoTool;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.MyToast;

import java.util.ArrayList;
/**
 * 选择图片 发送到服务器
 *
 * @author bass
 */

public class SelectPhotoGoToVideoActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener {
    private static final String TAG = "SelectPhotoGoToVideoAct";
    private MyApplication myApplication;
    private MyToast myToast;
    private Context context;

    private ImageView rtLayout;
    private Button okButton;

    private ArrayList<PhotoInfo> photoURLlist = new ArrayList<PhotoInfo>();// 选择的图片的list
    private ArrayList<PhotoInfo> allArrayList;//测试用
    private ArrayList<PhotoInfo> boughtArrayList;

    private SelectPhotoGoToVideoAdapter selectPhotoGoToVideoAdapter;
    private GridView gridView;

    private final int photocountMax = 9;// 需要添加的图片数量，以后要改这个数值

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo_goto_video);
        context = this;
        initview();
        initData();
        initEvent();
    }

    // 初始化函数
    private void initview() {
        myToast = new MyToast(context);
        myApplication = (MyApplication) getApplication();
        okButton = (Button) findViewById(R.id.btn_select_photo);
        rtLayout = (ImageView) findViewById(R.id.rlrt);
        gridView = (GridView) findViewById(R.id.gridView_select_photo);

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
        System.out.println("===============DATA" + boughtArrayList.size());
        Log.d(TAG, allArrayList.size() + "====DATA===" + boughtArrayList.size());
//		selectPhotoGoToVideoAdapter = new SelectPhotoGoToVideoAdapter(context,
//				boughtArrayList);
        // selectPhotoGoToVideoAdapter.setGridView(gridView);

        selectPhotoGoToVideoAdapter = new SelectPhotoGoToVideoAdapter(context,
                allArrayList);

    }

    private void initEvent() {
        okButton.setOnClickListener(this);
        rtLayout.setOnClickListener(this);

        gridView.setOnItemClickListener(this);
        gridView.setAdapter(selectPhotoGoToVideoAdapter);
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
            case R.id.rlrt:// 返回按钮
                finish();
                break;

            case R.id.btn_select_photo:// 选择确定按钮
                int size = photoURLlist.size();
                myToast.setTextAndShow("submit:" + size, Common.TOAST_SHORT_TIME);
                if (size == 0) {
                    myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
                } else {
                    if (size < 3) {
                        myToast.setTextAndShow(R.string.disney_video_select_photo_less_than3, Common.TOAST_SHORT_TIME);
                        return;
                    } else {
                        boolean isSendSuccess = DisneyVideoTool .photoSendServer(photoURLlist);
                        if (isSendSuccess) {
                            // 发送成功，等待服务器推送回来
                            finish();
                        } else {
                            // 处理失败，数据错误
                            PictureAirLog.e(TAG, "处理失败，数据错误");
                            return;
                        }
                    }
                }
                break;

            default:
                break;
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
                System.out.println("点过了，取消选中");
                int visiblePos = gridView.getFirstVisiblePosition();
                selectPhotoGoToVideoAdapter.refreshView(position, gridView.getChildAt(position - visiblePos), 1);
            } else {
                if (selectedCount < photocountMax) {
                    info.isSelected = 1;
                    System.out.println("没点过，选中");
                    selectedCount++;
                    int visiblePos = gridView.getFirstVisiblePosition();
                    selectPhotoGoToVideoAdapter.refreshView(position, gridView.getChildAt(position - visiblePos), 1);
                    if (photoURLlist.contains(info)) {// 说明已经存在
                        Log.d(TAG, "之前点过了");
                        myToast.setTextAndShow(R.string.photo_selected,
                                Common.TOAST_SHORT_TIME);
                    } else {
                        Log.d(TAG, "没有点过，点中");
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

    }

}
