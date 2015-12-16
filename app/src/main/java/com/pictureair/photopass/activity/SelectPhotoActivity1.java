package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.ViewPhotoGridViewAdapter;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.MyToast;

import java.util.ArrayList;

/**
 * 预览照片，总共有4大类，1，全部的照片，2，pictureair网络获取的图片，3，本软件拍的照片，4，已经购买的照片
 * 只能选择照片或者拍摄新的照片，不能做其他操作
 *
 * @author bauer_bao
 */

public class SelectPhotoActivity1 extends BaseActivity implements OnClickListener {
    private static final String TAG = "SelectPhotoActivity1";
    //申明控件
    private ImageView rtLayout;
    private Button okButton;
    private GridView gridView;
    private ViewPhotoGridViewAdapter photoPassAdapter;

    private MyToast newToast;
    private MyApplication myApplication;
    //预览的四个相册
    private ArrayList<PhotoInfo> photoPassArrayList;
    private String activity = null;

    private ArrayList<PhotoInfo> photoURLlist = new ArrayList<>();//选择的图片的list
    private int photocount = 1;//需要添加的图片数量，以后要改这个数值
    private int selectedCount = 0;//已经选择了的图片数量

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo_1);
        initview();
    }

    //初始化函数
    private void initview() {
        //初始化资源
        newToast = new MyToast(this);
        myApplication = (MyApplication) getApplication();
        activity = getIntent().getStringExtra("activity");

        //初始化控件
        okButton = (Button) findViewById(R.id.button1);
        rtLayout = (ImageView) findViewById(R.id.rlrt);
        gridView = (GridView) findViewById(R.id.gridView_all);

        //绑定监听
        rtLayout.setOnClickListener(this);
        okButton.setOnClickListener(this);

        //初始化数据列表
        photoPassArrayList = new ArrayList<>();
        photoPassArrayList.addAll(myApplication.photoPassPicList);
        transferPhotoItemInfoToPhotoInfo();

        photoPassAdapter = new ViewPhotoGridViewAdapter(this, photoPassArrayList);
        gridView.setAdapter(photoPassAdapter);
        gridView.setOnItemClickListener(new PhotoSelectedListener());

        //初始化数据
        if (myApplication.needScanPhoto()) {//如果刚进入这个页面，则onresume不需要重新扫描文件，所以直接将变量设为false，以免造成oncreate和onresume同时进行，造成collection数组越界
            myApplication.setneedScanPhoto(false);
        }
        //获取可选图片总数
        photocount = 1;

        okButton.setVisibility(View.VISIBLE);
        okButton.setText(String.format(getString(R.string.hasselectedphoto), 0, photocount));
    }


    //处理viewpager传递过来的数据
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 111:
                    okButton.setText(String.format(getString(R.string.hasselectedphoto), msg.arg1, photocount));//更新button
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 将photoItemInfo的列表转成photoInfo的列表
     */
    private void transferPhotoItemInfoToPhotoInfo() {
        for (int i = 0; i < myApplication.photoPassPicList.size(); i++) {
            myApplication.photoPassPicList.get(i).isChecked = 1;
            myApplication.photoPassPicList.get(i).isSelected = 0;
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (myApplication.needScanPhoto()) {//需要刷新
            PictureAirLog.v(TAG, "need scan photo--------------");
//            checkNewPhotos(Common.PHOTO_SAVE_PATH, magicArrayList, Common.ALBUM_MAGIC);
            myApplication.setneedScanPhoto(false);
        } else {
            PictureAirLog.v(TAG, "has new edit photo false");
        }
    }


    //gridview点击监听
    private class PhotoSelectedListener implements AdapterView.OnItemClickListener {
        private PhotoInfo info;
        private ViewPhotoGridViewAdapter viewPhotoGridViewAdapter;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //获取对应相册中的SelectPhotoItemInfo对象
            info = photoPassArrayList.get(position);
            viewPhotoGridViewAdapter = photoPassAdapter;
            Message msg = mHandler.obtainMessage();
            //获取已经选择的照片数量
            selectedCount = photoURLlist.size();
            //判断数量
            if (selectedCount >= 0) {
                PictureAirLog.v(TAG, selectedCount + "current:" + position);
                if (info.isSelected == 1) {//取消选中
                    if (photoURLlist.contains(info)) {//存在
                        photoURLlist.remove(info);
                    }
                    info.isSelected = 0;
                    selectedCount--;
                    PictureAirLog.v(TAG, "点过了，取消选中");
                    int visiblePos = gridView.getFirstVisiblePosition();
                    viewPhotoGridViewAdapter.refreshView(position, gridView.getChildAt(position - visiblePos), 1);
                } else {
                    if (selectedCount < photocount) {
                        info.isSelected = 1;
                        PictureAirLog.v(TAG, "没点过，选中");
                        selectedCount++;
                        int visiblePos = gridView.getFirstVisiblePosition();
                        viewPhotoGridViewAdapter.refreshView(position, gridView.getChildAt(position - visiblePos), 1);
                        if (photoURLlist.contains(info)) {//说明已经存在
                            newToast.setTextAndShow(R.string.photo_selected, Common.TOAST_SHORT_TIME);
                        } else {
                            photoURLlist.add(info);
                        }
                    } else {
                        newToast.setTextAndShow(String.format(getString(R.string.limit_photos), photocount), Common.TOAST_SHORT_TIME);
                    }
                }
                msg.what = 111;
                msg.arg1 = selectedCount;
                mHandler.sendMessage(msg);//通知主UI更改信息
            } else {
                newToast.setTextAndShow(String.format(getString(R.string.limit_photos), photocount), Common.TOAST_SHORT_TIME);
            }
        }

    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.rlrt://返回按钮
                finish();
                break;

            case R.id.button1://选择确定按钮
                if (photoURLlist.size() == 0) {
                    newToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
                } else if (photoURLlist.size() < photocount) {
                    newToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
                } else {
                    if (activity.equals("detailproductactivity") || activity.equals("previewproductactivity")) {//从详细商品界面进入
                        intent = new Intent(this, PreviewProductActivity.class);
                        intent.putExtra("name", getIntent().getStringExtra("name"));
                        intent.putExtra("price", getIntent().getStringExtra("price"));
                        intent.putExtra("introduce", getIntent().getStringExtra("introduce"));
                        intent.putExtra("productImage", getIntent().getStringExtra("productImage"));
                        intent.putExtra("photopath", photoURLlist);
                        intent.putExtra("storeid", getIntent().getStringExtra("storeid"));
                        intent.putExtra("productid", getIntent().getStringExtra("productid"));
                        startActivity(intent);
                    } else if (activity.equals("makegiftactivity")) {//从其他界面进来，返回新选择的照片
                        intent = new Intent();
                        intent.putExtra("photopath", photoURLlist);
                        setResult(20, intent);
                        finish();
                    } else if (activity.equals("submitorderactivity")) {//从其他界面进来，返回新选择的照片
                        intent = new Intent();
                        intent.putExtra("photopath", photoURLlist);
                        setResult(20, intent);
                        finish();
                    } else if (activity.equals("cartactivity")) {
                        intent = new Intent();
                        intent.putExtra("photopath", photoURLlist);
                        setResult(20, intent);
                        finish();
                    }
                }
                break;
            default:
                break;
        }
    }

}
