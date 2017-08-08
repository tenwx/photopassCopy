package com.pictureair.photopassCopy.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopassCopy.MyApplication;
import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.adapter.ViewPhotoGridViewAdapter;
import com.pictureair.photopassCopy.entity.GoodsInfo;
import com.pictureair.photopassCopy.entity.PhotoInfo;
import com.pictureair.photopassCopy.greendao.PictureAirDbManager;
import com.pictureair.photopassCopy.util.AppUtil;
import com.pictureair.photopassCopy.util.Common;
import com.pictureair.photopassCopy.util.PictureAirLog;
import com.pictureair.photopassCopy.widget.PWToast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 选择照片页面
 * @author bauer_bao
 */

public class SelectPhotoActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "SelectPhotoActivity";
    //申明控件
    private ImageView rtLayout;
    private TextView okButton;
    private GridView gridView;
    private ViewPhotoGridViewAdapter photoPassAdapter;
    private RelativeLayout noPhotoRelativeLayout;
    private TextView noPhotoTextView;
    private ImageView noPhotoImageView;

    private PWToast newToast;
    private MyApplication myApplication;
    private ArrayList<PhotoInfo> photoPassArrayList;
    private String activity = null;

    private ArrayList<PhotoInfo> photoURLlist = new ArrayList<>();//选择的图片的list
    private ArrayList<PhotoInfo> photopassList;
    private int photocount = 1;//需要添加的图片数量，以后要改这个数值
    private int selectedCount = 0;//已经选择了的图片数量
    private GoodsInfo goodsInfo;//存放商品信息

    private Context context;
    //底部view
    private final Handler selectPhotoHandler = new SelectPhotoHandler(this);

    private static final int GET_PHOTOS_DONE = 10101;


    private static class SelectPhotoHandler extends Handler {
        private final WeakReference<SelectPhotoActivity> mActivity;

        public SelectPhotoHandler(SelectPhotoActivity activity) {
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
            case GET_PHOTOS_DONE:
                //判断是否有照片
                if (photoPassArrayList != null && photoPassArrayList.size() > 0) {
                    noPhotoRelativeLayout.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);
                } else {
                    gridView.setVisibility(View.GONE);
                    noPhotoRelativeLayout.setVisibility(View.VISIBLE);
                    if (activity.equals("mypppactivity")) {
                        noPhotoTextView.setText(R.string.no_photo_update);
                        noPhotoImageView.setImageResource(R.drawable.no_photo_upgrade);
                    }
                }
                photoPassAdapter.notifyDataSetChanged();
                dismissPWProgressDialog();
                break;

            case 111:
                okButton.setText(String.format(getString(R.string.hasselectedphoto), msg.arg1, photocount));//更新button
                break;

            default:
                break;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo_1);
        activity = getIntent().getStringExtra("activity");
        photocount = getIntent().getIntExtra("photoCount", 1);
        context = this;
        goodsInfo = (GoodsInfo) getIntent().getSerializableExtra("goodsInfo");
        PictureAirLog.out("goodsinfo---->" + goodsInfo.getGoodsKey());
        if (goodsInfo != null) {
            if (goodsInfo.getName().equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {
                photocount = 10;
            } else {
                photocount = goodsInfo.getEmbedPhotosCount();
            }
        }
        if (photocount == 0) {
            photocount = 1;
        }
        initview();
    }

    //初始化函数
    private void initview() {
        showPWProgressDialog();
        //初始化资源
        newToast = new PWToast(this);
        myApplication = (MyApplication) getApplication();
        //初始化控件
        rtLayout = (ImageView) findViewById(R.id.rlrt);
        gridView = (GridView) findViewById(R.id.gridView_all);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        //空照片介绍页面
        noPhotoRelativeLayout = (RelativeLayout) findViewById(R.id.no_photo_relativelayout);
        noPhotoTextView = (TextView) findViewById(R.id.no_photo_textView);
        noPhotoImageView = (ImageView) findViewById(R.id.no_photo_iv);
        okButton = (TextView) findViewById(R.id.button1);

        //绑定监听
        rtLayout.setOnClickListener(this);
        okButton.setOnClickListener(this);

        //初始化数据列表
        photoPassArrayList = new ArrayList<>();

        PictureAirLog.out("photocount--->" + photocount);
        okButton.setText(String.format(getString(R.string.hasselectedphoto), 0, photocount));
        okButton.setEnabled(false);

        new Thread() {
            @Override
            public void run() {
                super.run();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long cacheTime = System.currentTimeMillis() - PictureAirDbManager.CACHE_DAY * PictureAirDbManager.DAY_TIME;
                photopassList = PictureAirDbManager.getAllPhotoFromPhotoPassInfo(true, sdf.format(new Date(cacheTime)));
                photoPassArrayList.addAll(transferPhotoItemInfoToPhotoInfo());
                PictureAirLog.v(TAG, "pp photo size: " + photoPassArrayList.size());
                selectPhotoHandler.sendEmptyMessage(GET_PHOTOS_DONE);
            }
        }.start();

        //设置默认没有选中
        photoPassAdapter = new ViewPhotoGridViewAdapter(this, photoPassArrayList);
        gridView.setAdapter(photoPassAdapter);
        gridView.setOnItemClickListener(new PhotoSelectedListener());

        //初始化数据
//        if (myApplication.needScanPhoto()) {//如果刚进入这个页面，则onresume不需要重新扫描文件，所以直接将变量设为false，以免造成oncreate和onresume同时进行，造成collection数组越界
//            myApplication.setneedScanPhoto(false);
//        }
    }

    /**
     * 将photoItemInfo的列表转成photoInfo的列表
     */
    private ArrayList<PhotoInfo> transferPhotoItemInfoToPhotoInfo() {
        ArrayList<PhotoInfo> list = new ArrayList<>();
        for (PhotoInfo photoInfo : photopassList) {
            photoInfo.setIsChecked(1);
            photoInfo.setIsSelected(0);
            if (activity.equals("mypppactivity")) {//ppp体验卡选图使用未购买的图片
                if (photoInfo.getIsPaid() == 0) {
                    list.add(photoInfo);
                }
            } else {
                //数码照片--是则获取未购买的图片 礼物--获取全部
                if (goodsInfo == null) {
                    PictureAirLog.v(TAG, "goodsInfo == null");
                    return list;
                }
                //数码照片--是则获取未购买的图片 礼物--获取全部
                if (!goodsInfo.getIsAllowBuy()) {
                    if (photoInfo.getIsPaid() == 0) {
                        list.add(photoInfo);
                    }
                } else {//需要排除纪念照，纪念照不允许制作
                    if (!photoInfo.getLocationId().equals("photoSouvenirs")) {//排除纪念照的照片
                        list.add(photoInfo);
                    }
                }
            }
        }
        return list;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
//        if (myApplication.needScanPhoto()) {//需要刷新
//            PictureAirLog.v(TAG, "need scan photo--------------");
//            myApplication.setneedScanPhoto(false);
//        } else {
//            PictureAirLog.v(TAG, "has new edit photo false");
//        }
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
            Message msg = selectPhotoHandler.obtainMessage();
            //获取已经选择的照片数量
            selectedCount = photoURLlist.size();
            //判断数量
            if (selectedCount >= 0) {
                PictureAirLog.v(TAG, selectedCount + "current:" + position);
                if (info.getIsSelected() == 1) {//取消选中
                    if (photoURLlist.contains(info)) {//存在
                        photoURLlist.remove(info);
                    }
                    info.setIsSelected(0);
                    selectedCount--;
                    PictureAirLog.v(TAG, "点过了，取消选中");
                    int visiblePos = gridView.getFirstVisiblePosition();

                    viewPhotoGridViewAdapter.refreshView(position, gridView.getChildAt(position - visiblePos), 1);
                } else {
                    if (selectedCount < photocount) {
                        info.setIsSelected(1);
                        PictureAirLog.v(TAG, "没点过，选中 url: " + info.getPhotoOriginalURL());
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
                selectPhotoHandler.sendMessage(msg);//通知主UI更改信息
            } else {
                newToast.setTextAndShow(String.format(getString(R.string.limit_photos), photocount), Common.TOAST_SHORT_TIME);
            }
            isEnabled();
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
                if (AppUtil.getNetWorkType(context) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                if (activity.equals("detailproductactivity") || activity.equals("previewproductactivity")) {
                    //从详细商品界面进入
                    PictureAirLog.v(TAG, "选择图片数量" + photoURLlist.size());
                    intent = new Intent(this, PreviewProductActivity.class);
                    intent.putExtra("goodsInfo", goodsInfo);
                    intent.putExtra("photopath", photoURLlist);
                    startActivity(intent);
                } else if (activity.equals("makegiftactivity")) {
                    //从其他界面进来，返回新选择的照片
                    intent = new Intent();
                    intent.putExtra("photopath", photoURLlist);
                    setResult(20, intent);
                    finish();
                } else if (activity.equals("submitorderactivity")) {
                    //从其他界面进来，返回新选择的照片
                    intent = new Intent();
                    intent.putExtra("photopath", photoURLlist);
                    setResult(20, intent);
                    finish();
                } else if (activity.equals("cartactivity")) {
                    PictureAirLog.v(TAG, "提交按钮: " + photoURLlist.get(0).getPhotoOriginalURL());
                    intent = new Intent();
                    intent.putExtra("photopath", photoURLlist);
                    setResult(20, intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void isEnabled() {
        if (photoURLlist.size() == photocount ||
                (goodsInfo.getName().equals(Common.GOOD_NAME_SINGLE_DIGITAL) && photoURLlist.size() > 0)) {//1.选择的数量和需要的数量一致，2.数码商品
            okButton.setEnabled(true);
            okButton.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));
        } else {
            okButton.setEnabled(false);
            okButton.setTextColor(ContextCompat.getColor(this, R.color.gray_light5));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        selectPhotoHandler.removeCallbacksAndMessages(null);
    }
}


